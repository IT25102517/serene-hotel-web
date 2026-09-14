package lk.serene.shared;

import java.util.*;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SystemController {
  private final Environment env;

  public SystemController(Environment env) {
    this.env = env;
  }

  @GetMapping("/health")
  public Map<String, Object> health() {
    return Map.of(
        "status",
        "UP",
        "profile",
        String.join(
            ",",
            env.getActiveProfiles().length > 0
                ? env.getActiveProfiles()
                : env.getDefaultProfiles()));
  }

  @GetMapping("/me")
  public Map<String, Object> me(Authentication a) {
    return Map.of(
        "username",
        a.getName(),
        "role",
        a.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
  }
}
