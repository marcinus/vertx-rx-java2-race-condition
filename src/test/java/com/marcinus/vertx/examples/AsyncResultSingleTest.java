package com.marcinus.vertx.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.reactivex.Single;
import io.vertx.reactivex.impl.AsyncResultSingle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncResultSingleTest {

  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

  @Test
  @DisplayName("Expect AsyncResult's result always delivered to subscriber")
  void checkDeliveryTryingToForceRaceCondition() throws InterruptedException {
    int RUNS = 1000;
    AtomicInteger delivered = new AtomicInteger(0);

    for (int i = 0; i < RUNS; i++) {
      ExternalAsyncResult asyncResult = new ExternalAsyncResult();
      Single<Boolean> single = AsyncResultSingle.toSingle(handler -> {
        scheduler.schedule(asyncResult::finish, 1, TimeUnit.MILLISECONDS);
        try {
          TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException e) {
          // Not handled for now
        }
        handler.handle(asyncResult);
      });

      single.subscribe(result -> delivered.getAndIncrement());
    }

    TimeUnit.MILLISECONDS.sleep(2000);
    assertEquals(RUNS, delivered.get());
  }
}
