package com.example;


import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;
import java.util.Optional;

public class DeliveryCar extends AbstractBehavior<DeliveryCar.Message>
{
    public interface  Message{}
    public  record Load(List<Packet> packets) implements  Message {}
    public record PickUpResponse (Optional<Packet> packet) implements Message {}
    public static Behavior<DeliveryCar.Message> create() {
        return Behaviors.setup(context -> new DeliveryCar(context));
    }
    public DeliveryCar(ActorContext<DeliveryCar.Message> context)
    {
        super(context);
    }
    @Override
    public Receive<DeliveryCar.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Load.class, this::onLoad).onMessage(PickUpResponse.class, this::onPickUpResponse)
                .build();
    }
    private Behavior<DeliveryCar.Message> onLoad(Load l)
    {
        return Behaviors.stopped();
    }
    private Behavior<DeliveryCar.Message> onPickUpResponse (PickUpResponse pickUpResponse)
    {
        return Behaviors.stopped();
    }


}
