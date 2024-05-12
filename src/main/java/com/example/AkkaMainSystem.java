package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.ArrayList;

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
        var cust1 = this.getContext().spawn(Customer.create("Cust1"), "cust1");
        var cust2 = this.getContext().spawn(Customer.create("Cust2"), "cust2");
        var cust3 = this.getContext().spawn(Customer.create("Cust3"), "cust3");
        var cust4 = this.getContext().spawn(Customer.create("Cust4"), "cust4");

        // Customer dem Adressbuch übergeben
        ActorRef<Customer.Message>[] custArr = new ActorRef[]{cust1, cust2, cust3, cust4};
        addressbook.tell(new AddressBook.CustomerArray(custArr));

        var distributionCenter = this.getContext().spawn(DistributionCenter.create(custArr),"distributionCenter");
        //#create-actors

        /* TEST */
        //addressbook.tell(new AddressBook.GetRandomCustomer(cust1));
        //addressbook.tell(new AddressBook.GetRandomCustomer(cust2));
        //addressbook.tell(new AddressBook.GetRandomCustomer(cust3));
        /* TEST */
        return this;
    }
}
