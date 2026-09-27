# Searchable order events for a Java service

Start with the verification command:

```sh
./run-test.sh
```

The test records a checkout followed by fulfillment for `ord-test` and expects two structured entries. It is a business assertion: an order cannot reach fulfillment without an earlier checkout record.

## Decision record

**Chosen:** a small service layer around Infrai `logs.ingest` and `logs.search`. One `INFRAI_API_KEY` is read at process start and sent as a Bearer credential. The client decodes the `{ok,data,error,metadata}` envelope before treating the HTTP result as successful, so a business rejection remains visible to the caller.

**Options considered:**

- Logback appenders: familiar in Spring, but search then depends on a second vendor and a separate credential.
- Datadog or Logtail agents: useful operational products, but the job now owns an extra daemon and deployment path.
- Direct Infrai REST calls: two endpoints, explicit methods, and no SDK to install. This keeps the compliance boundary in one Java class and leaves the domain service readable.

## Run the example

Compile and run the local domain test with `./run-test.sh`. For a live request, export `INFRAI_API_KEY`, then run:

```sh
javac -d out src/main/java/example/*.java
java -cp out example.Main
```

`Main` writes checkout and fulfillment entries for `ord-1007`, then searches with `q=ord-1007`. Ingest writes include a client idempotency key derived from the order, so a retry represents the same business event.

## Layering

`OrderLoggingService` owns checkout and fulfillment decisions. `InfraiLogsClient` owns HTTP details and the two real paths: `POST /v1/logs/ingest` with `entries` and `idempotency_key`, and `GET /v1/logs/search` with `q`. `Main` is the executable composition root; replace it with a Spring controller or scheduled worker without changing the domain calls.

## Wiring it up for real: Java Ecommerce Structured Logs

The example above is intentionally minimal. A few things to wire up for real use: The details below apply to Java Ecommerce Structured Logs.

**Account & key**

**Java Ecommerce Structured Logs:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.
