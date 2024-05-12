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
        // Erstelle DeliveryCars //
        //context.spawn(DeliveryCar.create(createRoute()), "car1");
        //context.spawn(DeliveryCar.create(createRoute()), "car2");
        //context.spawn(DeliveryCar.create(createRoute()), "car3");
        //context.spawn(DeliveryCar.create(createRoute()), "car4");
        //
    }

    private Queue<ActorRef<Customer.Message>> createRoute(){
        Queue<ActorRef<Customer.Message>> route = new ArrayDeque<>(4);

        List<ActorRef<Customer.Message>> tempList = new ArrayList<>(Arrays.asList(custArr));
        Collections.shuffle(tempList);

        route.addAll(tempList);

        return route;
    }

    public static Behavior<DistributionCenter.Message> create(ActorRef[] custArr) {
        return Behaviors.setup(context -> new DistributionCenter(context, custArr));
    }

    @Override
    public Receive<DistributionCenter.Message> createReceive() {
        return newReceiveBuilder()
                //.onMessage()
                .build();
    }

    private Behavior<Message> onArrive(Arrive arrive) {
        // Füge alle Pakete dem Lagerraum hinzu
        stockRoom.addAll(arrive.packets);
        // Sende dem Paketwagen 3 zufällige Pakete
        ArrayList<Packet> cargo = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if(stockRoom.isEmpty()) break;
            cargo.add(arrive.packets.get(new Random().nextInt(stockRoom.size())));
        }

        arrive.car.tell(new DeliveryCar.Load(cargo));

        return this;
    }
}
