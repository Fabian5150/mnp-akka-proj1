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

    public record Init(ActorRef<AddressBook.Message> addressBox) implements Message {
    }

    public record GetRandomCustomerResponse(ActorRef<Customer.Message> receiver, ActorRef<DeliveryCar.Message> car) implements Message {}


    public static Behavior<Customer.Message> create(String name) {
        return Behaviors.setup(context -> new Customer(context, name));
    }

    private final String name;
    private final List <String> randomItems = List.of("DishWasher", "Cd Room", "Dumbbell", "Lighter", "Yogurt", "Pen" );
    private ActorRef<AddressBook.Message> addressBook;


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


    private String getRandomItem() {
        return randomItems.get(ThreadLocalRandom.current().nextInt(0, randomItems.size()));
    }

    private boolean WillSend() {
        return ThreadLocalRandom.current().nextInt(0, 11) >= 8;

    }

    private Behavior<Message> onPickUp(PickUp msg) {
        if (WillSend()) {
            //addressbook bekommt eine Referenz auf self, um wieder zurück zu schicken, msg.car() ist notwendig, um danach weiter schicken
            // beim Empfangen einer GetRandomCustomerResponse Nachricht
           addressBook.tell(new AddressBook.GetRandomCustomer(this.getContext().getSelf(), msg.car()));
        } else {
            msg.car().tell(new DeliveryCar.PickUpResponse(Optional.empty()));
        }
        return this;
    }

    private Behavior<Message> onDeliveryMsg(Delivery msg) {
        this.getContext().getLog().info("I have received a Message : {} from {}", msg.packet.Name(), msg.packet.Sender());
        return this;
    }
    private  Behavior<Message> OnInit( Init msg)
    {
        this.addressBook= msg.addressBox;
        return this;
    }
    //Wenn AdressBook eine Bestätigung zurückgibt, hat nun Customer Alle Infos zum Erstellen erine PickUpResponse,
    // Wir haben den Wagenreferenz von PickUp Nachricht auch schon in GetRandomCustomerResponse Nachricht,
    private Behavior<Message>OnGetRandomCustomerResponse(GetRandomCustomerResponse randomCustomerResponse)
    {
        String Item= getRandomItem();
        Packet packetToSendToCar= new Packet(Item, this.name, randomCustomerResponse.receiver);
        DeliveryCar.PickUpResponse pickUpResponse= new DeliveryCar.PickUpResponse(Optional.of(packetToSendToCar));
        randomCustomerResponse.car().tell(pickUpResponse);
        return this;
    }

}