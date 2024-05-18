package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.*;

public class DistributionCenter extends AbstractBehavior<DistributionCenter.Message>{
    private final ActorRef<Customer.Message>[] custArr;
    private ArrayList<Packet> stockRoom = new ArrayList<>();

    public interface Message {};

    public record Arrive(ActorRef<DeliveryCar.Message> car, ArrayList<Packet> packets) implements Message {}

    private DistributionCenter(ActorContext<Message> context, ActorRef[] custArr) {
        super(context);
        this.custArr = custArr;
        context.getLog().info("The DistributionCenter was created.");
        // Erstelle die Paketwagen //

        context.spawn(DeliveryCar.create(createRoute(), this.getContext().getSelf(), "car1"), "car1");
        context.spawn(DeliveryCar.create(createRoute(), this.getContext().getSelf(), "car2"), "car2");
        context.spawn(DeliveryCar.create(createRoute(), this.getContext().getSelf(), "car3"), "car3");
        context.spawn(DeliveryCar.create(createRoute(), this.getContext().getSelf(), "car4"), "car4");
    }

    private ArrayList<ActorRef<Customer.Message>> createRoute(){
        ArrayList<ActorRef<Customer.Message>> route = new ArrayList<>(4);

        route.addAll(Arrays.asList(custArr));
        Collections.shuffle(route);

        return route;
    }

    public static Behavior<DistributionCenter.Message> create(ActorRef[] custArr) {
        return Behaviors.setup(context -> new DistributionCenter(context, custArr));
    }

    @Override
    public Receive<DistributionCenter.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Arrive.class, this::onArrive)
                .build();
    }

    private Behavior<Message> onArrive(Arrive arrive) {
        // Füge alle Pakete dem Lagerraum hinzu
        stockRoom.addAll(arrive.packets);
        // Sende dem Paketwagen 3 zufällige Pakete
        ArrayList<Packet> cargo = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if(stockRoom.isEmpty()) break;
            cargo.add(stockRoom.remove(new Random().nextInt(stockRoom.size())));
        }

        arrive.car.tell(new DeliveryCar.Load(cargo));
        getContext().getLog().info("There are currently {} packets in the DeliveryCenter.", cargo.size());

        return this;
    }
}
