package lk.serene.marketing;

import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class PublicContentController {
  private final SiteContentRepository repo;

  public PublicContentController(SiteContentRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/api/public/content")
  public List<SiteContent> content() {
    return repo.findAll().stream().filter(c -> c.active).toList();
  }
}
