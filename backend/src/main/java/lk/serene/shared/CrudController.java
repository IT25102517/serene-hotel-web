package lk.serene.shared;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

public abstract class CrudController<T extends OwnedRecord> {
  protected final JpaRepository<T, Long> repo;
  protected final AuditRepository audit;
  protected final String role;
  protected final boolean customer;

  protected CrudController(
      JpaRepository<T, Long> repo, AuditRepository audit, String role, boolean customer) {
    this.repo = repo;
    this.audit = audit;
    this.role = role;
    this.customer = customer;
  }

  protected void allowed(Authentication a) {
    Access.require(
        Access.role(a, role) || (customer && Access.role(a, "CUSTOMER")),
        "This workspace belongs to another role.");
  }

  protected T record(Long id, Authentication a) {
    allowed(a);
    T r =
        repo.findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
    Access.require(
        Access.role(a, role) || r.owner.equals(a.getName()),
        "This record belongs to another customer.");
    return r;
  }

  @GetMapping
  public List<T> list(Authentication a) {
    allowed(a);
    return repo.findAll().stream()
        .filter(r -> Access.role(a, role) || r.owner.equals(a.getName()))
        .toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public T create(@Valid @RequestBody T r, Authentication a) {
    allowed(a);
    Access.valid(
        r.id == null && r.version == null, "New records must not contain an ID or version");
    r.owner = a.getName();
    r.createdAt = Instant.now();
    r.updatedAt = r.createdAt;
    validate(r, null, a);
    T saved = repo.saveAndFlush(r);
    audit.save(new AuditEntry(a.getName(), role, "CREATE", saved.id));
    return saved;
  }

  @PutMapping("/{id}")
  @Transactional
  public T update(@PathVariable Long id, @Valid @RequestBody T r, Authentication a) {
    T old = record(id, a);
    if (!Objects.equals(r.version, old.version))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Record changed. Refresh and try again.");
    r.id = id;
    r.owner = old.owner;
    r.createdAt = old.createdAt;
    r.updatedAt = Instant.now();
    validate(r, old, a);
    T saved = repo.saveAndFlush(r);
    audit.save(new AuditEntry(a.getName(), role, "UPDATE", id));
    return saved;
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Transactional
  public void delete(@PathVariable Long id, Authentication a) {
    T old = record(id, a);
    Access.require(Access.role(a, role), "Only the responsible staff role can remove records.");
    beforeDelete(old);
    repo.delete(old);
    audit.save(new AuditEntry(a.getName(), role, "DELETE", id));
  }

  @GetMapping("/activity")
  public List<AuditEntry> activity(Authentication a) {
    Access.require(Access.role(a, role), "Staff only");
    return audit.findAll().stream()
        .filter(e -> e.module.equals(role))
        .sorted(Comparator.comparing((AuditEntry e) -> e.occurredAt).reversed())
        .limit(30)
        .toList();
  }

  protected abstract void validate(T next, T old, Authentication a);

  protected void beforeDelete(T old) {}
}
