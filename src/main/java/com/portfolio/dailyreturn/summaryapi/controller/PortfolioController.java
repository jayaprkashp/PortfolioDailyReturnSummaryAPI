package com.portfolio.dailyreturn.summaryapi.controller;

import com.portfolio.dailyreturn.summaryapi.model.PortfolioRequest;
import com.portfolio.dailyreturn.summaryapi.model.PortfolioResponse;
import com.portfolio.dailyreturn.summaryapi.service.PortfolioServiceImpl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for portfolio performance management endpoints.
 * 
 * <p>This controller exposes API endpoints for calculating and retrieving portfolio
 * performance metrics. It handles HTTP requests related to portfolio daily return summary
 * calculations and performance analysis.
 * 
 * <p>Base URL: {@code /api/v1/portfolio}
 * 
 * <p>This controller is responsible for:
 * <ul>
 *   <li>Receiving portfolio performance calculation requests</li>
 *   <li>Delegating business logic to the service layer</li>
 *   <li>Returning calculated performance metrics to the client</li>
 *   <li>Handling HTTP request/response lifecycle</li>
 * </ul>
 * 
 * @author Portfolio API Team
 * @version 1.0
 * @since 1.0
 * @see PortfolioServiceImpl
 * @see PortfolioRequest
 * @see PortfolioResponse
 */
@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

	/**
	 * Service implementation for portfolio-related business logic.
	 * 
	 * <p>This service instance is responsible for:
	 * <ul>
	 *   <li>Validating portfolio request data</li>
	 *   <li>Calculating portfolio return percentages</li>
	 *   <li>Computing excess returns vs benchmark</li>
	 *   <li>Identifying significant performance deviations</li>
	 * </ul>
	 * 
	 * @see PortfolioServiceImpl
	 */
	private final PortfolioServiceImpl portfolioService;

    /**
     * Constructs a new PortfolioController with dependency injection.
     * 
     * <p>The {@code PortfolioServiceImpl} is injected via constructor to enable:
     * <ul>
     *   <li>Loose coupling between controller and service</li>
     *   <li>Easy unit testing with mock services</li>
     *   <li>Spring Framework dependency management</li>
     * </ul>
     *
     * @param portfolioService the {@link PortfolioServiceImpl} to be used
     *                         for portfolio performance calculations
     * @throws IllegalArgumentException if portfolioService is null
     */
    public PortfolioController(PortfolioServiceImpl portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * Calculates the performance of a portfolio based on the provided request.
     * 
     * <p>This endpoint accepts a {@link PortfolioRequest} containing portfolio details
     * and calculates various performance metrics. It validates the input data, computes
     * returns, and returns a comprehensive {@link PortfolioResponse} with results.
     * 
     * <p><b>Request Details:</b>
     * <ul>
     *   <li>HTTP Method: <b>POST</b></li>
     *   <li>Endpoint: <code>/api/v1/portfolio/performance</code></li>
     *   <li>Content-Type: <code>application/json</code></li>
     * </ul>
     * 
     * <p><b>Input Parameters (PortfolioRequest):</b>
     * <ul>
     *   <li>{@code portfolioId} - Unique identifier for the portfolio</li>
     *   <li>{@code valuationDate} - Date of portfolio valuation</li>
     *   <li>{@code beginMarketValue} - Starting market value (must be > 0)</li>
     *   <li>{@code endMarketValue} - Ending market value (must be > 0)</li>
     *   <li>{@code netCashFlow} - Net cash flows (must not exceed 20% of begin value)</li>
     *   <li>{@code benchmarkReturnPct} - Benchmark return percentage for comparison</li>
     *   <li>{@code currency} - Currency code (required, non-blank)</li>
     *   <li>{@code requestedBy} - User requesting the calculation</li>
     * </ul>
     * 
     * <p><b>Output (PortfolioResponse):</b>
     * <ul>
     *   <li>{@code portfolioId} - Echo of input portfolio ID</li>
     *   <li>{@code valuationDate} - Echo of input valuation date</li>
     *   <li>{@code portfolioReturnPct} - Calculated portfolio return percentage</li>
     *   <li>{@code benchmarkReturnPct} - Echo of input benchmark return</li>
     *   <li>{@code excessReturnPct} - Excess return (portfolio - benchmark)</li>
     *   <li>{@code status} - Validation status: "VALID" or "INVALID_INPUT"</li>
     *   <li>{@code reasons} - List of validation error messages (if invalid)</li>
     *   <li>{@code processedAt} - Timestamp of calculation</li>
     * </ul>
     * 
     * <p><b>HTTP Status Codes:</b>
     * <ul>
     *   <li>200 (OK) - Request processed successfully. Response includes calculated metrics
     *       and validation status (VALID or INVALID_INPUT)</li>
     *   <li>400 (Bad Request) - Request format is invalid or missing required fields</li>
     *   <li>500 (Internal Server Error) - Unexpected server error occurred</li>
     * </ul>
     * 
     * <p><b>Validation Rules:</b>
     * <ul>
     *   <li>Beginning Market Value must be greater than 0</li>
     *   <li>Ending Market Value must be greater than 0</li>
     *   <li>Currency must be provided and non-blank</li>
     *   <li>Net Cash Flow must not exceed 20% of Begin Market Value</li>
     *   <li>Begin Value and End Value must not have mismatches</li>
     * </ul>
     * 
     * <p><b>Performance Flagging:</b>
     * <ul>
     *   <li>If absolute excess return > 5%, a warning is added to response reasons</li>
     *   <li>This indicates significant over/underperformance vs benchmark</li>
     * </ul>
     *
     * @param request a {@link PortfolioRequest} object containing portfolio data
     * @return a {@link ResponseEntity} wrapping a {@link PortfolioResponse} with:
     *         <ul>
     *           <li>HTTP 200 (OK) status</li>
     *           <li>Response body containing calculated performance metrics,
     *               validation status, and any error/warning messages</li>
     *         </ul>
     * 
     * @see PortfolioRequest
     * @see PortfolioResponse
     * @see PortfolioServiceImpl#calculatePerformance(PortfolioRequest)
     * 
     * @example
     * <b>Request Example:</b>
     * <pre>
     * POST /api/v1/portfolio/performance HTTP/1.1
     * Content-Type: application/json
     * 
     * {
     *   "portfolioId": "PORT-2026-001",
     *   "valuationDate": "2026-06-29",
     *   "beginMarketValue": 100000.00,
     *   "endMarketValue": 110000.00,
     *   "netCashFlow": 0.00,
     *   "benchmarkReturnPct": 5.00,
     *   "currency": "USD",
     *   "requestedBy": "jayaprkashp"
     * }
     * </pre>
     * 
     * <b>Response Example (Valid):</b>
     * <pre>
     * HTTP/1.1 200 OK
     * Content-Type: application/json
     * 
     * {
     *   "portfolioId": "PORT-2026-001",
     *   "valuationDate": "2026-06-29",
     *   "portfolioReturnPct": 10.00,
     *   "benchmarkReturnPct": 5.00,
     *   "excessReturnPct": 5.00,
     *   "status": "VALID",
     *   "reasons": [],
     *   "processedAt": "2026-06-29T10:30:45.123456"
     * }
     * </pre>
     */
    @PostMapping("/performance")
    public ResponseEntity<PortfolioResponse> calculatePerformance(
            @RequestBody PortfolioRequest request) {

        return ResponseEntity.ok(
                portfolioService.calculatePerformance(request));
    }
}
