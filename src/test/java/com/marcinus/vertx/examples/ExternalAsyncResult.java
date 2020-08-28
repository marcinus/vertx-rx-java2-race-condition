package com.marcinus.vertx.examples;

import io.vertx.core.AsyncResult;

import java.util.concurrent.atomic.AtomicBoolean;

public class ExternalAsyncResult implements AsyncResult<Boolean> {

  private final AtomicBoolean finished = new AtomicBoolean(false);

  @Override
  public Boolean result() {
    if(finished.get()) {
      return true;
    }
    return null;
  }

  @Override
  public Throwable cause() {
    return null;
  }

  @Override
  public boolean succeeded() {
    return finished.get();
  }

  @Override
  public boolean failed() {
    return false;
  }

  public void finish() {
    finished.set(true);
  }
}
