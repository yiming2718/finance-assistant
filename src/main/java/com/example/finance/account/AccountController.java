package com.example.finance.account;

import com.example.finance.account.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private static final String USER_HEADER = "X-User-Id";   // Day 5 換成 JWT

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @RequestHeader(USER_HEADER) Long userId,
            @Valid @RequestBody CreateAccountRequest req) {
        AccountResponse created = service.create(userId, req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);   // 201 + Location
    }

    @GetMapping
    public List<AccountResponse> list(@RequestHeader(USER_HEADER) Long userId) {
        return service.list(userId);
    }

    @GetMapping("/{id}")
    public AccountResponse get(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long id) {
        return service.get(userId, id);
    }

    @PutMapping("/{id}")
    public AccountResponse update(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest req) {
        return service.update(userId, id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();   // 204
    }
}