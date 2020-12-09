package io.vertx.codeone.conduit.models;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.ArrayList;
import java.util.Random;

public class PersistenceVerticle extends AbstractVerticle{
  // for DB access
  private MongoClient mongoClient;

  @Override
  public void start(Future<Void> startFuture) {
    // Configure the MongoClient inline.  This should be externalized into a config file
    mongoClient = MongoClient.createShared(vertx, new JsonObject().put("test", config().getString("test", "data")).put("connection_string", config().getString("connection_string", "mongodb://localhost:27017/test")));
    EventBus eventBus = vertx.eventBus();
    MessageConsumer<JsonObject> consumer = eventBus.consumer("persistence-address");

    consumer.handler(message -> {

      String action = message.body().getString("action");

      switch (action) {
        case "register-user":
          regVals(message);
          break;
        case "get-user":
          getVals(message);
          break;
        default:
          message.fail(1, "Unkown action: " + message.body());
      }
    });

    startFuture.complete();

  }

  private void getVals(Message<JsonObject> message) {
    JsonObject result = new JsonObject();
    mongoClient.find("data", result, res->{
      if(res.succeeded())
      {
        /*
          for(JsonObject json: res.result())
          {
            result.put("X",json.getJsonArray("X")).put("Y",json.getJsonArray("Y"));
          }*/
          result.put("X",res.result().get(0).getJsonArray("X")).put("Y",res.result().get(0).getJsonArray("Y"));
        message.reply(result);
        }
      else
      {
        res.cause().printStackTrace();
      }
    });
  }
  private void regVals(Message<JsonObject> message) {
    Random rand = new Random();
    ArrayList xVal = new ArrayList<Integer>();
    ArrayList yVal = new ArrayList<Integer>();
    for(int i = 0; i < 100; i++)
    {
      xVal.add(i,rand.nextInt(100)+1);
    }
    for(int i = 0; i < 100; i++)
    {
      yVal.add(i,rand.nextInt(100)+1);
    }
    JsonObject vals = new JsonObject().put("X",xVal).put("Y",yVal);
    mongoClient.save("data",vals,res ->{
      if(res.succeeded())
      {
        System.out.println("Saved successfully");
        message.reply(res.succeeded());
      }
      else
      {
        res.cause().printStackTrace();
        message.reply(res.failed());
      }
    });
  }
}
