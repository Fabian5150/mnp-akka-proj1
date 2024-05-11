package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.Random;

public class AddressBook extends AbstractBehavior<AddressBook.Message> {
    private ActorRef<Customer.Message>[] customers;

    public interface Message {}

    public static record GetRandomCustomer(ActorRef<Customer.Message> customer, ActorRef<DeliveryCar.Message> car) implements Message { }
    public static record CustomerArray(ActorRef<Customer.Message>[] customers) implements Message { }

    private AddressBook(ActorContext<Message> context){
        super(context);
    }

    public static Behavior<AddressBook.Message> create() {
        return Behaviors.setup(context -> new AddressBook(context));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetRandomCustomer.class, this::onGetRandomCustomer)
                .onMessage(CustomerArray.class, this::onCustomerArray)
                .build();
    }

    /*
    * Erhält vom Customer auch das DeliveryCar, an das das Paket übergeben werden soll und
    * übergibt dessen Referenz wieder an den Customer über GetRandomCustomerResponse, damit
    * der Customer diese Referenz auch in onGetRandomCustomerResponse zur Verfügung hat
    * */
    private Behavior<Message> onGetRandomCustomer(GetRandomCustomer request) {
        if(customers == null){
            getContext().getLog().info("I don't have a customer list yet. :/");
            return this;
        }

        ActorRef<Customer.Message> randomCust = customers[new Random().nextInt(customers.length)];
        //getContext().getLog().info("GetRandomCustomer: {} for {}", randomCust, request.customer);
        request.customer.tell(new Customer.GetRandomCustomerResponse(randomCust, request.car));
        return this;
    }

    /*
    * Übernimmt das Customer Array nur, wenn das Interne noch nicht initialisiert ist
    * */
    private Behavior<Message> onCustomerArray(CustomerArray arr) {
        if(customers == null){
            customers = arr.customers;
            getContext().getLog().info("Received {} customers", customers.length);
        }
        return this;
    }

    private void myMethod (ActorRef<Customer.Message> customer){

    }
}
