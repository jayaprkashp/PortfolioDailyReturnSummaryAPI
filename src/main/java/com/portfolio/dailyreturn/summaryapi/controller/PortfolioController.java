package com.portfolio.dailyreturn.summaryapi.controller;

import com.portfolio.dailyreturn.summaryapi.model.PortfolioRequest;
import com.portfolio.dailyreturn.summaryapi.model.PortfolioResponse;
import com.portfolio.dailyreturn.summaryapi.service.PortfolioServiceImpl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * This is controller class to receive inputs from the front end
 */

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

	
	private final PortfolioServiceImpl portfolioService;

    
    public PortfolioController(PortfolioServiceImpl portfolioService) {
        this.portfolioService = portfolioService;
    }

   /*
    * Method to initial the portfolio performance calculation
    */
    @PostMapping("/performance")
    public ResponseEntity<PortfolioResponse> calculatePerformance(
            @RequestBody PortfolioRequest request) {

        return ResponseEntity.ok(
                portfolioService.calculatePerformance(request));
    }
}
