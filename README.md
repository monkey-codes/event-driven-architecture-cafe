# Event Driven Architecture Cafe
Sample project that demonstrates using and event driven architecture backed by atom feeds. The system consists
of 4 microservices: *waiter, kitchen, stockroom and cashier.* Each service is built
on top of the [Axon Framework](https://axoniq.io/) and uses [Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
for persistence.

![](https://res.cloudinary.com/monkey-codes/image/upload/v1617752028/event-driven-architecture-cafe/cafe.gif)

## Architecture 

Integration between the services are achieved via [Atom Feeds](https://en.wikipedia.org/wiki/Atom_(Web_standard)). Each
service hosts its own Atom Feed of public events and periodically pulls the feed
of other services it wants to integrate with.

![](https://res.cloudinary.com/monkey-codes/image/upload/v1617767992/event-driven-architecture-cafe/event-driven-cafe-architecture.png)

## Build & Run

[Batect System Requirements](https://batect.dev/docs/getting-started/requirements) include:
* Docker
* Java 8 or newer
* Bash
* curl

```
$ ./batect go
```

The UI is available at [http://localhost:4200](http://localhost:4200).