// Fabian Strauch, 238709
// Ahmed Hassan, 237534


package com.example;

import akka.actor.typed.ActorRef;

public record Packet(String Name, String Sender, ActorRef<Customer.Message> Receiver) { }
