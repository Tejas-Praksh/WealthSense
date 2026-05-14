package com.wealthsense.ai.controller;

import com.wealthsense.ai.domain.Conversation;
import com.wealthsense.ai.dto.ChatRequest;
import com.wealthsense.ai.dto.ChatResponse;
import com.wealthsense.ai.dto.ConversationDto;
import com.wealthsense.ai.dto.FinancialInsightDto;
import com.wealthsense.ai.service.AiAdvisorService;
import com.wealthsense.ai.service.ConversationService;
import com.wealthsense.common.dto.ApiResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Advisor", description = "Chat, conversations, and insights")
@SecurityRequirement(name = "Bearer Authentication")
public class AiAdvisorController {

    private final AiAdvisorService aiAdvisorService;
    private final ConversationService conversationService;

    @Operation(summary = "AI chat", description = "Send a message and receive an AI response")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Chat processed",
                    content = @Content(schema = @Schema(implementation = ChatResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @RequestHeader("X-User-ID") UUID userId,
            @Valid @RequestBody ChatRequest request) {

        ChatResponse response = aiAdvisorService.chat(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Chat processed successfully"));
    }

    @Operation(summary = "List conversations", description = "Return saved conversations for the user")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversations retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationDto>>> getConversations(
            @RequestHeader("X-User-ID") UUID userId) {

        List<Conversation> conversations = conversationService.getUserConversations(userId);
        List<ConversationDto> dtos = conversations.stream()
                .map(c -> ConversationDto.builder()
                        .conversationId(c.getConversationId())
                        .messages(c.getMessages())
                        .createdAt(c.getCreatedAt())
                        .updatedAt(c.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(dtos, "Conversations retrieved successfully"));
    }

    @Operation(summary = "Get insights", description = "Return latest spending insights snapshot")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Insights retrieved",
                    content = @Content(schema = @Schema(implementation = FinancialInsightDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<FinancialInsightDto>> getInsights(
            @RequestHeader("X-User-ID") UUID userId) {

        FinancialInsightDto dto = FinancialInsightDto.builder()
                .totalSpendingCurrentMonth(BigDecimal.valueOf(15000))
                .aiRecommendation("Consider cooking at home to save money.")
                .build();

        return ResponseEntity.ok(ApiResponse.success(dto, "Insights retrieved successfully"));
    }

    @Operation(summary = "Generate insights", description = "Trigger a fresh insight generation for the user")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Insights generated",
                    content = @Content(schema = @Schema(implementation = FinancialInsightDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/insights/generate")
    public ResponseEntity<ApiResponse<FinancialInsightDto>> generateInsights(
            @RequestHeader("X-User-ID") UUID userId) {

        FinancialInsightDto dto = FinancialInsightDto.builder()
                .totalSpendingCurrentMonth(BigDecimal.valueOf(15000))
                .aiRecommendation("Freshly generated: Consider cooking at home to save money.")
                .build();

        return ResponseEntity.ok(ApiResponse.success(dto, "Insights generated successfully"));
    }
}
