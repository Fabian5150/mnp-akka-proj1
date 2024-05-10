package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Customer extends AbstractBehavior<Customer.Message>{

public interface Message {}
public  record PickUp(ActorRef<DeliveryCar.Message> car) implements  Message {}
public  record Delivery(Packet packet) implements  Message {}



    public static Behavior<Customer.Message> create(String name) {
        return Behaviors.setup(context -> new Customer(context, name));
    }

    private final String name;
    private AdressBok addressBox;

    private Customer(ActorContext<Customer.Message> context, String name) {
        super(context);
        this.name = name;
    }

    @Override
    public Receive<Customer.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(PickUp.class, this::onPickUp).onMessage(Delivery.class, this::onDeliveryMsg)
                .build();
    }
    private Behavior<Message> onPickUp(PickUp msg)
    {
        return Behaviors.stopped();
    }
    private Behavior<Message> onDeliveryMsg(Delivery msg)
    {
        return Behaviors.stopped();
    }

}