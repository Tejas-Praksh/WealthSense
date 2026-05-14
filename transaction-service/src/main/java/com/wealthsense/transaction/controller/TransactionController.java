package com.wealthsense.transaction.controller;

import com.wealthsense.common.dto.ApiResponse;
import com.wealthsense.common.dto.PagedResponse;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.common.exception.ValidationException;
import com.wealthsense.transaction.domain.Account;
import com.wealthsense.transaction.dto.*;
import com.wealthsense.transaction.mapper.TransactionMapper;
import com.wealthsense.transaction.repository.AccountRepository;
import com.wealthsense.transaction.service.TransactionQueryService;
import com.wealthsense.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Transactions & Accounts", description = "Transactions and account management")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionQueryService queryService;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Value("${transaction.cache.page-ttl-minutes:5}")
    private long pageCacheTtl;

    @Operation(
            summary = "Create Transaction",
            description = "Process a new financial transaction. Requires idempotency key.")
    @Parameter(
            name = "X-Idempotency-Key",
            description = "Unique key to prevent duplicate transactions",
            required = true,
            in = ParameterIn.HEADER)
    @Parameter(name = "X-User-ID", description = "Authenticated user id", required = true, in = ParameterIn.HEADER)
    @Parameter(name = "X-Correlation-ID", description = "Optional correlation id for tracing", in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Transaction created",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or missing idempotency key"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @RequestHeader("X-User-ID") String userIdHeader,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
            @Valid @RequestBody CreateTransactionRequest request) {

        if (!StringUtils.hasText(idempotencyKey)) {
            throw new ValidationException("X-Idempotency-Key header is required");
        }

        UUID userId = UUID.fromString(userIdHeader);

        TransactionResponse response = transactionService.createTransaction(
                userId, idempotencyKey, correlationId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Transaction created successfully"));
    }

    @Operation(summary = "List transactions", description = "Paged transactions with optional filters")
    @Parameter(name = "X-User-ID", description = "Authenticated user id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transactions retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getTransactions(
            @RequestHeader("X-User-ID") String userIdHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate) {

        UUID userId = UUID.fromString(userIdHeader);

        PagedResponse<TransactionResponse> response = queryService.getTransactions(
                userId, page, size, type, category, startDate, endDate, pageCacheTtl);

        return ResponseEntity.ok(ApiResponse.success(response, "Transactions retrieved"));
    }

    @Operation(summary = "Get transaction", description = "Fetch a single transaction by id for the user")
    @Parameter(name = "X-User-ID", description = "Authenticated user id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction found",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Transaction not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @RequestHeader("X-User-ID") String userIdHeader,
            @PathVariable UUID id) {

        UUID userId = UUID.fromString(userIdHeader);
        TransactionResponse response = transactionService.getTransaction(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Transaction retrieved"));
    }

    @Operation(summary = "List accounts", description = "All accounts for the authenticated user")
    @Parameter(name = "X-User-ID", description = "Authenticated user id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Accounts retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountDto>>> getAccounts(
            @RequestHeader("X-User-ID") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        List<AccountDto> accounts = accountRepository.findByUserId(userId)
                .stream().map(transactionMapper::toAccountDto).toList();
        return ResponseEntity.ok(ApiResponse.success(accounts, "Accounts retrieved"));
    }

    @Operation(summary = "Create account", description = "Open a new account for the user")
    @Parameter(name = "X-User-ID", description = "Authenticated user id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Account created",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountDto>> createAccount(
            @RequestHeader("X-User-ID") String userIdHeader,
            @RequestParam(defaultValue = "SAVINGS") String accountType) {
        UUID userId = UUID.fromString(userIdHeader);

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .userId(userId)
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .accountType(accountType)
                .active(true)
                .build();

        Account saved = accountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(transactionMapper.toAccountDto(saved),
                        "Account created successfully"));
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "WS" + String.format("%018d",
                    (long) (Math.random() * 1_000_000_000_000_000_000L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
