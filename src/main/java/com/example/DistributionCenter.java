package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DistributionCenter extends AbstractBehavior<DistributionCenter.Message>{
    private ActorRef[] custArr;
    private ArrayList<Packet> stockRoom = new ArrayList<>();

    public interface Message {};

    private DistributionCenter(ActorContext<Message> context, ActorRef[] custArr) {
        super(context);
        //context.spawn(DeliveryCar.create(createRoute()), "car1");
        //context.spawn(DeliveryCar.create(createRoute()), "car2");
        //context.spawn(DeliveryCar.create(createRoute()), "car3");
        //context.spawn(DeliveryCar.create(createRoute()), "car4");
        this.custArr = custArr;
        createRoute();
        createRoute();
        createRoute();
    }

    public static Behavior<DistributionCenter.Message> create(ActorRef[] custArr) {
        return Behaviors.setup(context -> new DistributionCenter(context, custArr));
    }

    private ArrayList<ActorRef> createRoute(){
        ArrayList<ActorRef> route = new ArrayList<>(4);

        route.addAll(Arrays.asList(custArr));
        Collections.shuffle(route);

        getContext().getLog().info(route.get(0).toString());
        getContext().getLog().info(route.get(3).toString());

        return route;
    }

    @Override
    public Receive<DistributionCenter.Message> createReceive() {
        return newReceiveBuilder()
                //.onMessage()
                .build();
    }
}
