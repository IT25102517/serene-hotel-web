package lk.serene.marketing;

import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class PublicGalleryController {
  private final GalleryImageRepository repo;

  public PublicGalleryController(GalleryImageRepository repo) {
    this.repo = repo;
  }

  @GetMapping("/api/public/gallery")
  public Map<String, List<String>> gallery() {
    Map<String, List<String>> result = new HashMap<>();
    for (var image : repo.findAll())
      if (image.active)
        result.computeIfAbsent(image.placement, k -> new ArrayList<>()).add(image.imageUrl);
    return result;
  }
}
