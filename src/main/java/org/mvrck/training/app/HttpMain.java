package org.mvrck.training.app;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.cluster.sharding.typed.javadsl.*;
import akka.http.javadsl.*;
import akka.stream.*;
import com.typesafe.config.*;
import org.mvrck.training.actor.*;
import org.mvrck.training.http.*;

public class HttpMain {
  public static void main(String[] args) throws Exception {

    /********************************************************************************
     *  Initialize System, Cluster, and ShardRegion
     *******************************************************************************/
    var config = ConfigFactory.load("http-main.conf");
    var system = ActorSystem.create(Behaviors.<Void>empty(), "MaverickTraining", config);
    var sharding = ClusterSharding.get(system);
    var materializer = Materializer.createMaterializer(system);

    // ShardRegions start
    sharding.init(Entity.of(OrderActor.ENTITY_TYPE_KEY, ctx -> OrderActor.create(ctx.getEntityId())));
    sharding.init(Entity.of(TicketStockActor.ENTITY_TYPE_KEY, ctx -> TicketStockActor.create(sharding, ctx.getEntityId())));

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