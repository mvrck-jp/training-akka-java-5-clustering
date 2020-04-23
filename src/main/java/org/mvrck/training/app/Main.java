package org.mvrck.training.app;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.cluster.sharding.typed.javadsl.*;
import akka.http.javadsl.*;
import akka.stream.*;
import org.mvrck.training.actor.*;
import org.mvrck.training.http.*;

public class Main {
  public static void main(String[] args) throws Exception {

    /********************************************************************************
     *  Initialize System, Cluster, and ShardRegion
     *******************************************************************************/
    var system = ActorSystem.create(Behaviors.<Void>empty(), "MeverickTraining");
    var sharding = ClusterSharding.get(system);
    var materializer = Materializer.createMaterializer(system);

    sharding.init(Entity.of(OrderActor.ENTITY_TYPE_KEY, ctx -> OrderActor.create(ctx.getEntityId())));
    sharding.init(Entity.of(TicketStockActor.ENTITY_TYPE_KEY, ctx -> TicketStockActor.create(sharding, ctx.getEntityId())));

    /********************************************************************************
     *  Initialize TicketStock actors
     *******************************************************************************/
    var ref1 = sharding.entityRefFor(TicketStockActor.ENTITY_TYPE_KEY,"1");
    ref1.tell(new TicketStockActor.CreateTicketStock(1, 5000));
    var ref2 = sharding.entityRefFor(TicketStockActor.ENTITY_TYPE_KEY,"2");
    ref2.tell(new TicketStockActor.CreateTicketStock(2, 2000));

    /********************************************************************************
     *  Http setup
     *******************************************************************************/
    var http = Http.get(system.classicSystem());
    var allRoute = new AllRoute(system, sharding);
    var routeFlow = allRoute.route().flow(system.classicSystem(), materializer);
    var binding = http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);

    System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
    System.in.read(); // let it run until user presses return

    binding
      .thenCompose(ServerBinding::unbind)
      .thenAccept(unbound -> system.terminate());
  }
}