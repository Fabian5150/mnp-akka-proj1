package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Create> {

    public static class Create {
    }

    public static Behavior<Create> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<Create> context) {
        super(context);
    }

    @Override
    public Receive<Create> createReceive() {
        return newReceiveBuilder().onMessage(Create.class, this::onCreate).build();
    }

    private Behavior<Create> onCreate(Create command) {
        //#create-actors
        /* Wir erstellen zuerst das Adressbuch, damit wir den Empfängern direkt dessen Referenz übergeben können.
        Das Adressbuch erhält dann als erste Nachricht des Systems die Referenzen zu den nach ihm erstellten Empfängern */
        var addressbook = this.getContext().spawn(AddressBook.create(), "addressbook");
        var cust1 = this.getContext().spawn(Customer.create("Eddie", addressbook), "eddie");
        var cust2 = this.getContext().spawn(Customer.create("Alex", addressbook), "alex");
        var cust3 = this.getContext().spawn(Customer.create("Michael", addressbook), "michael");
        var cust4 = this.getContext().spawn(Customer.create("Sammy", addressbook), "sammy");

        // Customer dem Adressbuch übergeben
        ActorRef<Customer.Message>[] custArr = new ActorRef[]{cust1, cust2, cust3, cust4};
        addressbook.tell(new AddressBook.CustomerArray(custArr));

        var distributionCenter = this.getContext().spawn(DistributionCenter.create(custArr),"distributionCenter");
        //#create-actors

        return this;
    }
}
