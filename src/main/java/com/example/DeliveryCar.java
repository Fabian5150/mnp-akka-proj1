package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.ArrayList;

import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.List;

public class DeliveryCar extends AbstractBehavior<DeliveryCar.Message> {
    public interface Message {
    }

    public record Load(ArrayList<Packet> packets) implements Message {
    }

    public record PickUpResponse(Optional<Packet> packet) implements Message {
    }

    private record LoadHandler() implements Message {
    }

    private final TimerScheduler<DeliveryCar.Message> timer;
    private Queue<ActorRef<Customer.Message>> customerRoute;

    private ArrayList<Packet> cargoArea = new ArrayList<>();
    private ActorRef<DistributionCenter.Message> distributionCenterActorRef;
    private String name;


    public static Behavior<DeliveryCar.Message> create(
            Queue<ActorRef<Customer.Message>> route,
            ActorRef<DistributionCenter.Message> initializingDistributionCenter, String name)
    {
        return Behaviors.setup(context -> Behaviors.withTimers(timers ->
                new DeliveryCar(context, timers, route, initializingDistributionCenter, name)));
    }


    public DeliveryCar(ActorContext<DeliveryCar.Message> context, TimerScheduler<DeliveryCar.Message> timers,
                       Queue<ActorRef<Customer.Message>> route, ActorRef<DistributionCenter.Message> initialzingDistributionCenter,
                       String name) {
        super(context);
        this.timer = timers;
        this.customerRoute = route;
        this.distributionCenterActorRef = initialzingDistributionCenter;
        this.name = name;
        context.getLog().info("I, Delivery car '{}' was created", name);
    }

    @Override
    public Receive<DeliveryCar.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Load.class, this::onLoad)
                .onMessage(PickUpResponse.class, this::onPickUpResponse)
                .onMessage(LoadHandler.class, this::onLoadHandler)
                .build();
    }

    private Behavior<DeliveryCar.Message> onLoad(Load l) {
        cargoArea.addAll(l.packets);
        timer.startSingleTimer(new LoadHandler(), Duration.ofSeconds(3));
        return this;
    }

    private Behavior<DeliveryCar.Message> onPickUpResponse(PickUpResponse pickUpResponse) {
        pickUpResponse.packet.ifPresent(thePacket ->
                {
                    cargoArea.add(thePacket);
                    getContext().getLog().info("I, ({}) have now {} packets when receiving pickUpResponse", this.name, this.cargoArea.size());
                    this.timer.startSingleTimer(new LoadHandler(), Duration.ofSeconds(1));
                }
        );

        return this;
    }

    private boolean IsThereARoom() {
        return this.cargoArea.size() < 3;
    }

    private List<Packet> GetPacketsForCustomer(ActorRef<Customer.Message> customer) {

        return this.cargoArea.stream()
                .filter(packet -> packet.Receiver().equals(customer)).
                collect(Collectors.toList());
    }

    private void DeliverCustomerPacketsAndRemoveThem(ActorRef<Customer.Message> customerToDeliver) {
        List<Packet> customerPackets = GetPacketsForCustomer(customerToDeliver);
        customerPackets.forEach(packet -> customerToDeliver.tell(new Customer.Delivery(packet)));
        cargoArea.removeAll(customerPackets);
    }

    private Behavior<DeliveryCar.Message> onLoadHandler(LoadHandler f) {
        ActorRef<Customer.Message> nextCustomer = customerRoute.poll();
        getContext().getLog().info("I {} am currently at {}'s house", name, nextCustomer);

        if (customerRoute.isEmpty()) { // TBD: (Wo) wird die Route wieder "aufgefüllt"?
            ArrayList<Packet> remainingPackets = new ArrayList<>(this.cargoArea);
            cargoArea.clear();
            this.distributionCenterActorRef.tell(new DistributionCenter.Arrive(this.getContext().getSelf(), remainingPackets));

            return this;
        }

        // We still have customers to serve

        DeliverCustomerPacketsAndRemoveThem(nextCustomer);
        //As said in the assignment, the Checking if there is a place to pickUp at should not depend on the customer or his packets.
        if (IsThereARoom())
            nextCustomer.tell(new Customer.PickUp(this.getContext().getSelf()));
        else
            this.timer.startSingleTimer(new LoadHandler(), Duration.ofSeconds(1));

        return this;
    }
}
