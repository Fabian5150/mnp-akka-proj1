package com.example;


import akka.actor.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Queue;

public class DeliveryCar extends AbstractBehavior<DeliveryCar.Message>
{
    public interface  Message{}
    public  record Load(ArrayList<Packet> packets) implements  Message {}
    public record PickUpResponse (Optional<Packet> packet) implements Message {}
    private record HandleFirstCustomer() implements Message{}
    private record UnkownHandle() implements Message{}
    private  TimerScheduler<DeliveryCar.Message> timer;
    private Queue<AbstractBehavior<Customer.Message>> customers; //queue of customer or Queue of ActorRef?
   // private Queue<ActorRef> customers2;

    private ArrayList<Packet> cargoArea;


    public static Behavior<DeliveryCar.Message> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new DeliveryCar(context, timers)));
    }
    public DeliveryCar(ActorContext<DeliveryCar.Message> context)
    {
        super(context);
    }

    public DeliveryCar(ActorContext<DeliveryCar.Message> context, TimerScheduler<DeliveryCar.Message> timers)
    {
        super(context);
        this.timer=timers;



    }
    @Override
    public Receive<DeliveryCar.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Load.class, this::onLoad).onMessage(PickUpResponse.class, this::onPickUpResponse)
                .onMessage(HandleFirstCustomer.class, this::onHandleFirstCustomer)
                .build();
    }
    private Behavior<DeliveryCar.Message> onLoad(Load l)
    {
        cargoArea.addAll(l.packets);
        timer.startSingleTimer(new HandleFirstCustomer(), Duration.ofSeconds(3));
        return Behaviors.stopped();
    }
    private Behavior<DeliveryCar.Message> onPickUpResponse (PickUpResponse pickUpResponse)
    {
        return Behaviors.stopped();
    }
    private ArrayList<Packet> GetPacketsForCustomer(AbstractBehavior<Customer.Message> customer )
    {
        ArrayList<Packet>res= new ArrayList<>();
        for (Packet packet:
             cargoArea) {
            if(packet.Receiver().getClass().getName().equals( customer.getClass().getName()))
                res.add(packet);
        }
        return res;
    }
    private Behavior<DeliveryCar.Message> onHandleFirstCustomer(HandleFirstCustomer f)
    {
        ArrayList<Packet> firstCustomerPackets= GetPacketsForCustomer(customers.peek());
        for (Packet packet :
                firstCustomerPackets) {
            packet.Receiver().tell(new Customer.Delivery(packet));
            cargoArea.remove(packet);


        }

        return Behaviors.stopped();
    }


}
