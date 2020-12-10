package io.vertx.codeone.conduit;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;

public class HttpVerticle extends AbstractVerticle{
  @Override
  public void start(Future<Void> startFuture) {
    Router router = Router.router(vertx);
    router.route().handler(CorsHandler.create(".*."));
    router.post("/regVals").handler(this::regVals);
    router.get("/getVals").handler(this::getVals);

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(9000, result -> {
        if (result.succeeded()) {
          startFuture.complete();
        } else {
          startFuture.fail(result.cause());
        }
      });
  }
  private void getVals(RoutingContext routingContext) {
    JsonObject message = new JsonObject().put("action","get-vals");
    vertx.eventBus().send("persistence-address",message,ar ->{
      if(ar.succeeded())
      {
        System.out.println(ar.result().body());
        routingContext.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(ar.result().body()));
      }
      else
      {
        routingContext.response()
                .setStatusCode(500)
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(ar.cause().getMessage()));
      }
    });
  }
  private void regVals(RoutingContext routingContext) {
    JsonObject message = new JsonObject()
      .put("action", "register");
    vertx.eventBus().send("persistence-address", message, ar -> {
      if (ar.succeeded()) {
        routingContext.response()
          .setStatusCode(201)
          .putHeader("Content-Type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(ar.result().body()));
      }else{
        routingContext.response()
          .setStatusCode(500)
          .putHeader("Content-Type", "application/json; charset=utf-8")
          //.putHeader("Content-Length", String.valueOf(userResult.toString().length()))
          .end(Json.encodePrettily(ar.cause().getMessage()));
      }
    });
  }
}
