package lk.serene.shared;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

public final class Access {
  public static boolean role(Authentication a, String role) {
    return a != null
        && a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_" + role));
  }

  public static void require(boolean ok, String message) {
    if (!ok) throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
  }

  public static void valid(boolean ok, String message) {
    if (!ok) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
  }
}
