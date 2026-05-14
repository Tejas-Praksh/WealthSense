package com.wealthsense.investment.controller;

import com.wealthsense.investment.dto.*;
import com.wealthsense.investment.service.GoalService;
import com.wealthsense.investment.service.InvestmentService;
import com.wealthsense.investment.service.SipCalculatorService;
import com.wealthsense.investment.service.TaxSavingService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
@RequiredArgsConstructor
@Tag(name = "Investments", description = "SIP, goals, tax, portfolio")
@SecurityRequirement(name = "Bearer Authentication")
public class InvestmentController {

    private final SipCalculatorService sipCalculatorService;
    private final GoalService goalService;
    private final TaxSavingService taxSavingService;
    private final InvestmentService investmentService;

    @Operation(summary = "Calculate SIP", description = "Project SIP maturity from inputs")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SIP calculated",
                    content = @Content(schema = @Schema(implementation = SipCalculationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/sip/calculate")
    public ResponseEntity<ApiResponse<SipCalculationResponse>> calculateSip(
            @Valid @RequestBody SipCalculationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sipCalculatorService.calculate(request), "SIP Calculated Successfully"));
    }

    @Operation(summary = "Recommendations", description = "Personalized investment ideas")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Recommendations returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<String>>> getRecommendations(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(required = false) BigDecimal monthlyIncomeRupees) {
        return ResponseEntity.ok(ApiResponse.success(investmentService.getRecommendations(userId, monthlyIncomeRupees), "Recommendations Fetched"));
    }

    @Operation(summary = "Create goal", description = "Create a new investment goal")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Goal created",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            @RequestHeader("X-User-ID") UUID userId,
            @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(ApiResponse.success(goalService.createGoal(userId, request), "Goal Created Successfully"));
    }

    @Operation(summary = "List goals", description = "Goals for the user")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Goals returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/goals")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoals(
            @RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(goalService.getGoals(userId), "Goals Fetched Successfully"));
    }

    @Operation(summary = "Update goal", description = "Update an existing goal")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Goal updated",
                    content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Goal not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/goals/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @RequestHeader("X-User-ID") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(ApiResponse.success(goalService.updateGoal(userId, id, request), "Goal Updated Successfully"));
    }

    @Operation(summary = "Tax saving", description = "Tax-saving summary for the user")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tax projection returned",
                    content = @Content(schema = @Schema(implementation = TaxSavingDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/tax-saving")
    public ResponseEntity<ApiResponse<TaxSavingDto>> getTaxSavings(
            @RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(taxSavingService.calculateTaxSavings(userId), "Tax Savings Calculated"));
    }

    @Operation(summary = "Portfolio summary", description = "Aggregate portfolio for the user")
    @Parameter(name = "X-User-ID", description = "User id", required = true, in = ParameterIn.HEADER)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Portfolio summary",
                    content = @Content(schema = @Schema(implementation = PortfolioSummaryDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/portfolio")
    public ResponseEntity<ApiResponse<PortfolioSummaryDto>> getPortfolio(
            @RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(investmentService.getPortfolioSummary(userId), "Portfolio Summary Fetched"));
    }
}
