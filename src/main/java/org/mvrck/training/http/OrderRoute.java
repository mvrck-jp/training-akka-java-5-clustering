package org.mvrck.training.http;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import akka.cluster.sharding.typed.javadsl.*;
import akka.http.javadsl.marshallers.jackson.*;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.*;
import org.mvrck.training.actor.*;
import org.mvrck.training.dto.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class OrderRoute extends AllDirectives {
  ActorSystem<Void> system;
  ClusterSharding sharding;

  public OrderRoute(ActorSystem<Void> system, ClusterSharding sharding){
    this.system = system;
    this.sharding = sharding;
  }

  public Route route(){
    return pathPrefix("orders", () ->
      pathEndOrSingleSlash(() ->
        entity(Jackson.unmarshaller(OrderPutRequest.class), req -> {
          var orderId = UUID.randomUUID();
          var entityRef = sharding.entityRefFor(TicketStockActor.ENTITY_TYPE_KEY, Integer.toString(req.getTicketId()));

          CompletionStage<OrderActor.Response> completionStage = AskPattern.ask(
            entityRef,
            replyTo -> new TicketStockActor.ProcessOrder(orderId.toString(), req.getTicketId(),req.getUserId(), req.getQuantity(), replyTo),
            Duration.ofSeconds(3),
            system.scheduler()
          );

          return onSuccess(completionStage, response -> {
            var putResponse = OrderPutResponse.convert(response);
            if(putResponse.isSuccess()) {
              return complete(StatusCodes.OK, putResponse, Jackson.marshaller());
            } else {
              return complete(StatusCodes.INTERNAL_SERVER_ERROR, "internal server error");
            }
          });
        })
      )
    );
  }
}
