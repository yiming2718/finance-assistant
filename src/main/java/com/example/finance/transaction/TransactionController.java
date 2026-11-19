package com.example.finance.transaction;

import com.example.finance.common.PageResponse;
import com.example.finance.transaction.dto.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private static final String USER_HEADER = "X-User-Id";   // Day 5 換成 JWT

    private final TransactionService service;

    public TransactionController(TransactionService service) { this.service = service; }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> create(
            @RequestHeader(USER_HEADER) Long userId,
            @Valid @RequestBody CreateTransactionRequest req) {
        TransactionResponse created = service.create(userId, req);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri()).body(created);
    }

    @PostMapping("/transfers")
    public ResponseEntity<List<TransactionResponse>> transfer(
            @RequestHeader(USER_HEADER) Long userId,
            @Valid @RequestBody CreateTransferRequest req) {
        return ResponseEntity.status(201).body(service.transfer(userId, req));
    }

    @GetMapping("/transactions")
    public PageResponse<TransactionResponse> search(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(userId, accountId, categoryId, from, to, page, size);
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> delete(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse balance(@RequestHeader(USER_HEADER) Long userId, @PathVariable Long accountId) {
        return service.balance(userId, accountId);
    }
}