package com.example;

import akka.actor.typed.ActorRef;

public record Packet(String Name, String Sender, ActorRef<Customer.Message> reciever) { }
