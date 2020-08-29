# Vert.x Rx Java 2 Race Condition

This is a sample project to present a concurrency issue with [`io.vertx.reactivex.impl.AsyncResultSingle<T>`](https://github.com/vert-x3/vertx-rx/blob/3.9.2/rx-java2-gen/src/main/java/io/vertx/reactivex/impl/AsyncResultSingle.java) class (version 3.9.2, but also present in [`master`](https://github.com/vert-x3/vertx-rx/blob/master/rx-java2-gen/src/main/java/io/vertx/reactivex/impl/AsyncResultSingle.java) branch at the time of writing).

## How to execute test

Just launch `./gradlew test` in the repository root.

The test should fail.

The report will be present under `build/reports/tests/test/index.html`

## Origin of vulnerability

The issue is that [`subscribeActual`](https://github.com/vert-x3/vertx-rx/blob/3.9.2/rx-java2-gen/src/main/java/io/vertx/reactivex/impl/AsyncResultSingle.java#L47) does not actively wait for [`AsyncResult<T>`](https://github.com/eclipse-vertx/vert.x/blob/3.9.2/src/main/java/io/vertx/core/AsyncResult.java) to complete*, so there is a race condition between the worker completing the [`AsyncResult<T>`](https://github.com/eclipse-vertx/vert.x/blob/3.9.2/src/main/java/io/vertx/core/AsyncResult.java), and the subscriber to the created `Single<T>`.

The result of this race condition is a possibility that **some results will never be delivered to the subscriber** of the created `Single<T>`.

The issue was originally spotted when working on [Knot.x project](https://knotx.io/) - see at [GitHub](https://github.com/Knotx).

> \* The actual issue is that [`AsyncResult<T>`](https://github.com/eclipse-vertx/vert.x/blob/3.9.2/src/main/java/io/vertx/core/AsyncResult.java) does not expose an interface for neither blocking wait nor asynchronous callback on completion.
> Therefore some deeper refactoring might be required (possibly using [`Future<T>`](https://github.com/eclipse-vertx/vert.x/blob/3.9.2/src/main/java/io/vertx/core/Future.java) instead of [`AsyncResult<T>`](https://github.com/eclipse-vertx/vert.x/blob/3.9.2/src/main/java/io/vertx/core/AsyncResult.java)?)

## Reproduction

[Reproducting test](https://github.com/marcinus/vertx-rx-java2-race-condition/blob/master/src/test/java/com/marcinus/vertx/examples/AsyncResultSingleTest.java#L41) measures how many results have been successfully delivered to the subscriber within 2s window after all subscriptions are finished, using worker thread pool of 20 and 1ms wait after scheduling to promote thread interchange.

This race condition was successfully observed on author's machine (Windows 10.0.0.18363, Java 1.8.0_202):
* In 194 out of 1000 runs the worker was faster and the result was delivered
* In 806 out of 1000 runs the subscriber was faster than the worker and no result was delivered until the timeout hit.

## Impact

The [`AsyncResultSingle<T>`](https://github.com/vert-x3/vertx-rx/blob/3.9.2/rx-java2-gen/src/main/java/io/vertx/reactivex/impl/AsyncResultSingle.java) is also used as part of [code generation](https://github.com/vert-x3/vertx-rx/blob/3.9.2/rx-java2-gen/src/main/java/io/vertx/lang/reactivex/RxJava2Generator.java#L192) with `@RxGen` annotation. Therefore each user's class having method with `Handler<AsyncResult<T>>` as last argument and the `@RxGen` annotation present is impacted.

Also, [`AsyncResultMaybe<T>`](https://github.com/vert-x3/vertx-rx/blob/master/rx-java2-gen/src/main/java/io/vertx/reactivex/impl/AsyncResultMaybe.java) seems to have the same logic implemented, so it will be impacted as well. [`AsyncResultCompletable<T>`](https://github.com/vert-x3/vertx-rx/blob/master/rx-java2-gen/src/main/java/io/vertx/reactivex/impl/AsyncResultCompletable.java) has just `else` block which means that the logic is different - but the race condition still may occur.
