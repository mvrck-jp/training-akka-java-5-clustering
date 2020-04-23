package org.mvrck.training.http;

import akka.actor.typed.*;
import akka.cluster.sharding.typed.javadsl.*;
import akka.http.javadsl.server.*;

public class AllRoute extends AllDirectives {
  OrderRoute orderRoute;

  public AllRoute(
    ActorSystem<Void> system,
    ClusterSharding sharding
  ){
    this.orderRoute = new OrderRoute(system, sharding);
  }

  public Route route(){
    return concat(
      orderRoute.route()
    );
  }
}
