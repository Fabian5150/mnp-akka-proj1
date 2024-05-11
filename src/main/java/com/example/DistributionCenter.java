package com.example;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class DistributionCenter extends AbstractBehavior<DistributionCenter.Message>{
    public interface Message {};
}
