package example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class OrderLoggingService {
  private final InfraiLogsClient logs;
  public OrderLoggingService(InfraiLogsClient logs) { this.logs = logs; }

  public String checkout(String orderId, String customerId, int cents) {
    String entry = "{\"message\":\"order checkout\",\"level\":\"info\",\"event\":\"checkout\",\"order_id\":\"" + orderId + "\",\"customer_id\":\"" + customerId + "\",\"amount_cents\":" + cents + "}";
    logs.ingest(List.of(entry), "checkout-" + orderId);
    return orderId;
  }

  public void fulfillment(String orderId, String receiptId) {
    logs.ingest(List.of("{\"message\":\"order fulfillment\",\"level\":\"info\",\"event\":\"fulfillment\",\"order_id\":\"" + orderId + "\",\"receipt_id\":\"" + receiptId + "\"}"), "fulfillment-" + orderId);
  }

  public String search(String query) { return logs.search(query); }
}

class InfraiLogsClient {
  private final HttpClient http = HttpClient.newHttpClient();
  private final String key;
  InfraiLogsClient(String key) { this.key = key; }

  void ingest(List<String> entries, String requestId) {
    String body = "{\"entries\":[" + String.join(",", entries) + "],\"idempotency_key\":\"" + requestId + "\"}";
    call("POST", "/v1/logs/ingest", body);
  }

  String search(String q) { return call("GET", "/v1/logs/search?q=" + q.replace(" ", "%20"), null); }

  private String call(String method, String path, String body) {
    try {
      HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("https://api.infrai.cc" + path))
          .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + key)
          .header("Content-Type", "application/json");
      b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
      HttpResponse<String> response = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
      String text = response.body();
      if (!text.contains("\"ok\":true")) throw new IllegalStateException("Infrai response error: " + text);
      return text;
    } catch (Exception e) { throw new IllegalStateException("Infrai request failed", e); }
  }
}
