package example;

import java.util.ArrayList;
import java.util.List;

public final class OrderLoggingServiceTest {
  public static void main(String[] args) {
    FakeLogs fake = new FakeLogs();
    OrderLoggingService service = new OrderLoggingService(fake);
    service.checkout("ord-test", "cust-test", 500);
    service.fulfillment("ord-test", "rcpt-test");
    if (fake.entries.size() != 2 || !fake.entries.get(0).contains("checkout") || !fake.entries.get(1).contains("fulfillment")) throw new AssertionError("order lifecycle was not logged");
    System.out.println("order lifecycle test passed");
  }
  static final class FakeLogs extends InfraiLogsClient {
    final List<String> entries = new ArrayList<>();
    FakeLogs() { super("test"); }
    @Override void ingest(List<String> values, String requestId) { entries.addAll(values); }
  }
}
