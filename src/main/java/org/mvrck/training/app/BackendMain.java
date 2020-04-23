package org.mvrck.training.app;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.cluster.sharding.typed.javadsl.*;
import org.mvrck.training.actor.*;

public class BackendMain {
  public static void main(String[] args) throws Exception {
    /********************************************************************************
     *  Initialize System, Cluster, and ShardRegion
     *******************************************************************************/
    var system = ActorSystem.create(Behaviors.<Void>empty(), "MeverickTraining");
    var sharding = ClusterSharding.get(system);

    sharding.init(Entity.of(OrderActor.ENTITY_TYPE_KEY, ctx -> OrderActor.create(ctx.getEntityId())));
    sharding.init(Entity.of(TicketStockActor.ENTITY_TYPE_KEY, ctx -> TicketStockActor.create(sharding, ctx.getEntityId())));

    /********************************************************************************
     *  Initialize TicketStock actors
     *******************************************************************************/
    var ref1 = sharding.entityRefFor(TicketStockActor.ENTITY_TYPE_KEY,"1");
    ref1.tell(new TicketStockActor.CreateTicketStock(1, 5000));
    var ref2 = sharding.entityRefFor(TicketStockActor.ENTITY_TYPE_KEY,"2");
    ref2.tell(new TicketStockActor.CreateTicketStock(2, 2000));
  }
}