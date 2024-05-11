package com.example;


import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class Customer extends AbstractBehavior<Customer.Message> {

    public interface Message {
    }

    public record PickUp(ActorRef<DeliveryCar.Message> car) implements Message {
    }

    public record Delivery(Packet packet) implements Message {
    }

    public record Init(AdressBok addressBox) implements Message {
    }

    public record RandomCustomer(ActorRef<Customer.Message> receiver) implements Message {}


    public static Behavior<Customer.Message> create(String name) {
        return Behaviors.setup(context -> new Customer(context, name));
    }

    private final String name;
    private final List <String> randomItems = List.of("DishWasher", "Cd Room", "Dumbbell", "Lighter", "Yogurt", "Pen" );
    private AddressBook addressBook;


    private Customer(ActorContext<Customer.Message> context, String name) {
        super(context);
        this.name = name;
    }

    @Override
    public Receive<Customer.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(PickUp.class, this::onPickUp).
                onMessage(Delivery.class, this::onDeliveryMsg)
                .onMessage(Init.class, this::OnInit)
                .build();
    }

    public String getName() {
        return name;
    }

    private String getRandomItem() {
        return randomItems.get(ThreadLocalRandom.current().nextInt(0, randomItems.size()));
    }

    private boolean WillSend() {
        return ThreadLocalRandom.current().nextInt(0, 11) >= 8;

    }

    private Behavior<Message> onPickUp(PickUp msg) {
        if (WillSend()) {
            String Item = getRandomItem();
            //Should be replaced with AdressBook.GetRandomCustomer()
            ActorRef<Customer.Message> customer= getContext().spawn(Customer.create("Temp"), "Temp");

            msg.car.tell(new DeliveryCar.PickUpResponse(Optional.of(new Packet(Item, this.getName(), customer))));
        } else {
            msg.car.tell(new DeliveryCar.PickUpResponse(Optional.empty()));
        }
        return Behaviors.stopped();
    }

    private Behavior<Message> onDeliveryMsg(Delivery msg) {
        this.getContext().getLog().info("I have received a Message : {} from {}", msg.packet.Name(), msg.packet.Sender());
        return Behaviors.stopped();
    }
    private  Behavior<Message> OnInit( Init msg)
    {
        this.addressBox= msg.addressBox;
        return Behaviors.stopped();
    }

}