# Akka Package Delivery Simulation

A concurrent package delivery system simulation built with Akka Actor Typed, developed as part of the "Modellierung Nebenläufiger Prozesse" (Modeling Concurrent Processes) course at TU Dortmund University.

## Overview

This project simulates a package delivery service with multiple delivery vans, customers, a distribution center, and an address book. The system models real-world package logistics using the Actor model for concurrent programming.

## System Architecture

### Actors

The simulation consists of the following actor types:

- **AddressBook**: Maintains references to all customers and provides random customer addresses upon request
- **DistributionCenter**: Central hub that receives packages from delivery vans, stores them, and redistributes them to vans
- **DeliveryCar** (4 instances): Delivery vans that follow routes, deliver packages, and pick up new ones from customers
- **Customer** (4 instances): Package recipients who can also send packages to other customers

### Data Model

**Package** class contains:
- Content (String): The item being shipped
- Sender (String): Name of the sender
- Recipient (ActorRef): Reference to the receiving customer actor

## System Behavior

### AddressBook
- Stores references to all customer actors
- Returns a random customer reference when queried

### Customer
- **Receiving packages** (`Delivery` message):
  - Outputs the package content and sender information
  
- **Pickup request** (`Pickup` message):
  - 80% probability of sending a package
  - If sending: queries AddressBook for random recipient, creates package with random item from inventory
  - Sends `PickupResponse` to the delivery van (with or without package)
  - Inventory items: "Spülmaschine", "Altes CD-Regal", "Goldbarren", "20kg Hanteln", "Holzkohlegrill", "Blumenerde"

### DeliveryCar
- **Initialization**: Created with a route (permutation of all customers)
- **Capacity**: Holds up to 3 packages

- **Loading** (`Load` message):
  - Stores packages in cargo
  - After 3 seconds, visits first stop on route
  - Delivers packages addressed to current customer
  - Sends `Pickup` request if cargo has space (< 3 packages)
  - Proceeds to next stop after 1 second

- **Pickup response** (`PickupResponse` message):
  - Adds package to cargo if one was provided
  - Outputs current cargo count
  - Continues to next stop after 1 second

- **End of route**:
  - Sends all remaining packages to DistributionCenter via `Arrive` message

### DistributionCenter
- Maintains a storage area for packages
- **Van arrival** (`Arrive` message):
  - Adds received packages to storage
  - Randomly selects up to 3 packages from storage
  - Sends selected packages to van via `Load` message
  - Outputs current storage count

## Technical Requirements

### Dependencies
- **Akka Actor Typed** version 2.9 or higher
- Java or Scala standard library only

### Important Implementation Notes

1. **No busy waiting**: Use `TimerScheduler` instead of `Thread.sleep()` for delays
2. **Actor initialization order**: Consider dependencies between actors (AddressBook ↔ Customers)
3. **Optional package in PickupResponse**: Design elegant solution for optional package handling
4. **Continuous operation**: System runs indefinitely - all actors should continue generating output

## Getting Started

### Prerequisites
- Java JDK 11 or higher
-  Maven/Gradle

### Running the Simulation

```bash
# Clone the repository
git clone https://github.com/Fabian5150/mnp-akka-proj1.git
cd mnp-akka-proj1

# Run with sbt
sbt run

# Or with your build tool of choice
```

### Expected Output

The simulation will continuously output:
- Package deliveries (customer receives package)
- Package pickups (customer sends package)
- Cargo status (current package count in vans)
- Distribution center inventory (package count after van arrives)

## Project Structure

```
mnp-akka-proj1/
├── src/
│   └── main/
│       └── [java/
│           ├── Main.java
│           ├── AddressBook.java
│           ├── Customer.java
│           ├── DeliveryCar.java
│           ├── DistributionCenter.java
│           └── Package.java
├── pom.xml
└── README.md
```

## Design Decisions

### Message Protocol
The system uses the following message types:
- `Delivery(package)`: Deliver package to customer
- `Pickup(vanRef)`: Request package from customer
- `PickupResponse(optionalPackage)`: Customer's response to pickup
- `Load(packages)`: Load packages into van
- `Arrive(packages)`: Van arrives at distribution center

### Timing
- 3-second delay before van starts route
- 1-second delay between route stops
- Immediate processing of most messages

### Randomization
- Customer addresses selected randomly by AddressBook
- Van routes are random permutations of all customers
- Package items selected randomly from inventory
- 80% probability of customer sending package on pickup
- Distribution center randomly selects 3 packages from storage

## Acknowledgments

- Template project: [AkkaBlanko](https://git.cs.tu-dortmund.de/mnp24/AkkaBlanko)
- Akka documentation and examples
