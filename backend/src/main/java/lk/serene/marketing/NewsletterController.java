package lk.serene.marketing;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
import lk.serene.shared.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
public class NewsletterController {
  @org.springframework.beans.factory.annotation.Autowired
  private lk.serene.shared.integrations.NotificationService notifications;

  @Value("${MAILERLITE_COUPON_FIELD:serene_discount_code}")
  private String couponField;

  @Value("${serene.backup-mode:false}")
  private boolean backupMode;

  private final SubscriberRepository repo;
  private final String token, group;
  private final RestClient client;

  public NewsletterController(
      SubscriberRepository repo,
      @Value("${MAILERLITE_API_KEY:}") String token,
      @Value("${MAILERLITE_GROUP_ID:}") String group) {
    this.repo = repo;
    this.token = token;
    this.group = group;
    var f =
        new JdkClientHttpRequestFactory(
            java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build());
    f.setReadTimeout(Duration.ofSeconds(12));
    client =
        RestClient.builder()
            .requestFactory(f)
            .baseUrl("https://connect.mailerlite.com/api")
            .build();
  }

  public record Signup(
      @NotBlank @Email @Size(max = 160) String email, @AssertTrue boolean consent) {}

  @PostMapping("/api/public/newsletter")
  public synchronized Map<String, String> signup(@Valid @RequestBody Signup req) {
    if (backupMode)
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
          "Newsletter signup is unavailable in offline backup mode.");
    String email = req.email.trim().toLowerCase(Locale.ROOT);
    var s =
        repo.findById(email)
            .orElseGet(
                () -> {
                  var n = new Subscriber();
                  n.email = email;
                  n.coupon = "SERENE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                  n.consentAt = Instant.now();
                  n.syncStatus = "PENDING";
                  return n;
                });
    if (token.isBlank() || group.isBlank())
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
          "Wedding offers signup is temporarily unavailable. Please contact our team.");
    if (!"SYNCED".equals(s.syncStatus)) {
      try {
        Map<?, ?> response =
            client
                .post()
                .uri("/subscribers")
                .header("Authorization", "Bearer " + token)
                .body(
                    Map.of(
                        "email",
                        email,
                        "groups",
                        List.of(group),
                        "fields",
                        Map.of(couponField, s.coupon)))
                .retrieve()
                .body(Map.class);
        if (response == null
            || !(response.get("data") instanceof Map<?, ?> data)
            || data.get("id") == null) throw new IllegalStateException("Invalid provider response");
        s.syncStatus = "SYNCED";
      } catch (Exception ex) {
        s.syncStatus = "FAILED";
        repo.save(s);
        throw new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.BAD_GATEWAY,
            "Signup saved, but MailerLite sync failed. Please retry later.");
      }
    }
    repo.save(s);
    notifications.queue(
        new lk.serene.shared.integrations.CustomerNotification(
            "newsletter-" + s.coupon,
            email,
            "Your Serene wedding offer code",
            "Thank you for subscribing. Your 10% package offer code is "
                + s.coupon
                + ". Present it to the reservations team when requesting your quote. Offer"
                + " eligibility is confirmed by the hotel."));
    return Map.of(
        "message",
        "Subscribed successfully. Save your 10% wedding package discount code for the front"
            + " office.",
        "coupon",
        s.coupon,
        "status",
        s.syncStatus);
  }

  @GetMapping("/api/marketing/subscribers")
  public List<Subscriber> subscribers(Authentication a) {
    Access.require(Access.role(a, "MARKETING"), "Marketing staff only");
    return repo.findAll();
  }
}
