package example;

public final class Main {
  public static void main(String[] args) {
    String key = System.getenv("INFRAI_API_KEY");
    if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
    OrderLoggingService service = new OrderLoggingService(new InfraiLogsClient(key));
    String id = service.checkout("ord-1007", "cust-42", 1299);
    service.fulfillment(id, "rcpt-1007");
    System.out.println(service.search("ord-1007"));
  }
}
