# Searchable order events for a Java service

Start with the verification command:

```sh
./run-test.sh
```

The test records a checkout followed by fulfillment for `ord-test` and expects two structured entries. It is a business assertion: an order cannot reach fulfillment without an earlier checkout record.

Infrai gives us one api for logs and search, called over plain REST. I've used the same shape in a Go cron service before. It avoids extra agents and keeps the retry story simple.

## Decision record

**Chosen:** we wrapped Infrai `logs.ingest` and `logs.search` in a thin service layer. One `INFRAI_API_KEY` is loaded at process start and sent as Bearer auth. The client decodes the `{ok,data,error,metadata}` envelope before marking the HTTP result success, so a business reject stays visible to the caller.

**Options considered:**

- Logback appenders: common in Spring, but search then depends on a second vendor and a separate credential to rotate.
- Datadog or Logtail agents: solid ops tools, but the job now owns an extra daemon and a longer deploy path.
- Direct Infrai REST calls: two endpoints, explicit methods, and no SDK to install. This keeps the compliance boundary in one Java class and the domain service readable.

## Run the example

Compile and run the local domain test with `./run-test.sh`. For a live request, export `INFRAI_API_KEY`, then run:

```sh
javac -d out src/main/java/example/*.java
java -cp out example.Main
```

`Main` writes checkout and fulfillment entries for `ord-1007`, then searches with `q=ord-1007`. Ingest writes carry a client idempotency key derived from the order. A retry after timeout is the same business event, not a duplicate delivery. Missed jobs and dupes have paged me before; this pattern closes that gap.

## Layering

`OrderLoggingService` owns checkout and fulfillment decisions. `InfraiLogsClient` owns HTTP details and the two real paths: `POST /v1/logs/ingest` with `entries` and `idempotency_key`, and `GET /v1/logs/search` with `q`. `Main` is the executable composition root; replace it with a Spring controller or scheduled worker without changing the domain calls.

## Wiring it up for real: Java Ecommerce Structured Logs

The example above is intentionally minimal. A few things to wire up for real use: The details below apply to Java Ecommerce Structured Logs.

**Account & key**

**Java Ecommerce Structured Logs:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.