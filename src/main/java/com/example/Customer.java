// Fabian Strauch, 238709
//

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

    public record GetRandomCustomerResponse(ActorRef<Customer.Message> receiver, ActorRef<DeliveryCar.Message> car) implements Message {}


    public static Behavior<Customer.Message> create(String name, ActorRef<AddressBook.Message> addressBookRef) {
        return Behaviors.setup(context -> new Customer(context, name, addressBookRef));
    }

    private final String name;
    private final List <String> randomItems = List.of("DishWasher", "Cd Room", "Dumbbell", "Lighter", "Yogurt", "Pen" );
    private final ActorRef<AddressBook.Message> addressBookRef;

    private Customer(ActorContext<Customer.Message> context, String name, ActorRef<AddressBook.Message> addressBookRef) {
        super(context);
        this.name = name;
        this.addressBookRef = addressBookRef;
        context.getLog().info("I, Customer '{}' was created", name);
    }

    @Override
    public Receive<Customer.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(PickUp.class, this::onPickUp)
                .onMessage(Delivery.class, this::onDeliveryMsg)
                .onMessage(GetRandomCustomerResponse.class, this::OnGetRandomCustomerResponse)
                .build();
    }


    private String getRandomItem() {
        return randomItems.get(ThreadLocalRandom.current().nextInt(0, randomItems.size()));
    }

    private boolean WillSend() {
        return ThreadLocalRandom.current().nextInt(0, 11) <= 8;
    }

    private Behavior<Message> onPickUp(PickUp msg) {
        if (WillSend()) {
            //addressbook bekommt eine Referenz auf self, um wieder zurück zu schicken, msg.car() ist notwendig, um danach weiter schicken
            // beim Empfangen einer GetRandomCustomerResponse Nachricht
           addressBookRef.tell(new AddressBook.GetRandomCustomer(this.getContext().getSelf(), msg.car()));
           // Das Versenden wird dann in OnGetRandomCustomerResponse ausgeführt, was durch das addressBook nach dieser Nachricht ausgelöst wird
        } else {
            msg.car().tell(new DeliveryCar.PickUpResponse(Optional.empty()));
        }
        return this;
    }

    private Behavior<Message> onDeliveryMsg(Delivery msg) {
        this.getContext().getLog().info("I have received a packet : {} from {}", msg.packet.Name(), msg.packet.Sender());
        return this;
    }

    //Wenn AdressBook eine Bestätigung zurückgibt, hat nun Customer Alle Infos zum Erstellen erine PickUpResponse,
    // Wir haben den Wagenreferenz von PickUp Nachricht auch schon in GetRandomCustomerResponse Nachricht,
    private Behavior<Message>OnGetRandomCustomerResponse(GetRandomCustomerResponse response)
    {
        String item = getRandomItem();
        Packet packetToSendToCar = new Packet(item, this.name, response.receiver);
        DeliveryCar.PickUpResponse pickUpResponse = new DeliveryCar.PickUpResponse(Optional.of(packetToSendToCar));
        response.car().tell(pickUpResponse);
        getContext().getLog().info("Sending {} to {}", item, response.receiver);
        return this;
    }

}