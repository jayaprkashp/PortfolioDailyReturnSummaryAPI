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
 * performance metrics. It handles HTTP requests related to portfolio daily return summary.
 * 
 * <p>Base URL: {@code /api/v1/portfolio}
 * 
 * @author Portfolio API Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

	/**
	 * Service implementation for portfolio-related business logic.
	 * This service handles the calculation of portfolio performance metrics.
	 */
	private final PortfolioServiceImpl portfolioService;

    /**
     * Constructs a new PortfolioController with dependency injection.
     * 
     * <p>The {@code PortfolioServiceImpl} is injected via constructor to handle
     * portfolio service operations and calculations.
     *
     * @param portfolioService the portfolio service implementation to be used
     *                         for portfolio performance calculations
     */
    public PortfolioController(PortfolioServiceImpl portfolioService) {
        this.portfolioService = portfolioService;
    }

    /**
     * Calculates the performance of a portfolio based on the provided request.
     * 
     * <p>This endpoint accepts a {@link PortfolioRequest} containing portfolio details
     * such as holdings, purchase dates, and purchase prices. It calculates the daily
     * return summary and returns a {@link PortfolioResponse} with the computed metrics.
     * 
     * <p>HTTP Method: POST<br>
     * Endpoint: {@code /api/v1/portfolio/performance}<br>
     * Content-Type: application/json
     * 
     * @param request a {@link PortfolioRequest} object containing:
     *                <ul>
     *                  <li>Portfolio holdings information</li>
     *                  <li>Purchase prices and dates</li>
     *                  <li>Current market values</li>
     *                </ul>
     * 
     * @return a {@link ResponseEntity} containing:
     *         <ul>
     *           <li>HTTP 200 (OK) with {@link PortfolioResponse} containing calculated
     *               performance metrics including daily returns, gains/losses, and summaries</li>
     *           <li>HTTP 400 (Bad Request) if request validation fails</li>
     *           <li>HTTP 500 (Internal Server Error) if unexpected errors occur</li>
     *         </ul>
     * @throws ValidationException 
     * 
     * 
     * @see PortfolioRequest
     * @see PortfolioResponse
     * @see PortfolioServiceImpl#calculatePerformance(PortfolioRequest)
     */
    @PostMapping("/performance")
    public ResponseEntity<PortfolioResponse> calculatePerformance(
            @RequestBody PortfolioRequest request) {

        return ResponseEntity.ok(
                portfolioService.calculatePerformance(request));
    }
}
