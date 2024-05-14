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
public class DeliveryCar extends AbstractBehavior<DeliveryCar.Message>
{
    public interface  Message{}
    public  record Load(ArrayList<Packet> packets) implements  Message {}
    public record PickUpResponse (Optional<Packet> packet) implements Message {}
    private record LoadHandler() implements Message{}
    private record UnkownHandle() implements Message{}
    private final TimerScheduler<DeliveryCar.Message> timer;
    private Queue<ActorRef<Customer.Message>> customersRoute; //queue of customer or Queue of ActorRef?
   // private Queue<ActorRef> customers2;

    private ArrayList<Packet> cargoArea;
    private ActorRef<DistributionCenter.Message> distributionCenterActorRef;


    public static Behavior<DeliveryCar.Message> create(Queue<ActorRef<Customer.Message>> route,
                                                       ActorRef<DistributionCenter.Message> initializingDistributionCenter) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new DeliveryCar(context, timers,route, initializingDistributionCenter)));
    }


    public DeliveryCar(ActorContext<DeliveryCar.Message> context, TimerScheduler<DeliveryCar.Message> timers,
                       Queue<ActorRef<Customer.Message>> route, ActorRef<DistributionCenter.Message> initialzingDistributionCenter)
    {
        super(context);
        this.timer=timers;
        this.customersRoute= route;
        this.distributionCenterActorRef= initialzingDistributionCenter;




    }
    @Override
    public Receive<DeliveryCar.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Load.class, this::onLoad).onMessage(PickUpResponse.class, this::onPickUpResponse)
                .onMessage(LoadHandler.class, this::onLoadHandler)
                .build();
    }
    private Behavior<DeliveryCar.Message> onLoad(Load l)
    {
        cargoArea.addAll(l.packets);
        timer.startSingleTimer(new LoadHandler(), Duration.ofSeconds(3));
        return this;
    }
    private Behavior<DeliveryCar.Message> onPickUpResponse (PickUpResponse pickUpResponse)
    {
        pickUpResponse.packet.ifPresent(cargoArea::add);

        return this;
    }
    private List<Packet> GetPacketsForCustomer(akka.actor.typed.ActorRef<Customer.Message> customer )
    {
      /*  ArrayList<Packet>res= new ArrayList<>();
        for (Packet packet:
             cargoArea) {

            if(packet.Receiver().equals( customer))
                res.add(packet);
        }*/
        return this.cargoArea.stream()
                .filter(packet -> packet.Receiver().equals(customer)).
                collect(Collectors.toList());
    }

    private Behavior<DeliveryCar.Message> onLoadHandler(LoadHandler f)
    {
        if(customersRoute.isEmpty())
        {
            ArrayList<Packet> remainingPackets = new ArrayList<>(this.cargoArea);
            cargoArea.clear();
            this.distributionCenterActorRef.tell(new DistributionCenter.Arrive(this.getContext().getSelf(), remainingPackets));
            return this;
        }

        List<Packet> firstCustomerPackets= GetPacketsForCustomer(customersRoute.peek());
        for (Packet packet :
                firstCustomerPackets) {

            packet.Receiver().tell(new Customer.Delivery(packet));
            cargoArea.remove(packet);


        }

        return this;
    }


}
