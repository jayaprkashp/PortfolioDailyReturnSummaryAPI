package com.portfolio.dailyreturn.summaryapi.controller;

import com.portfolio.dailyreturn.summaryapi.model.PortfolioRequest;
import com.portfolio.dailyreturn.summaryapi.model.PortfolioResponse;
import com.portfolio.dailyreturn.summaryapi.service.PortfolioServiceImpl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

	
	private final PortfolioServiceImpl portfolioService;

    
    public PortfolioController(PortfolioServiceImpl portfolioService) {
        this.portfolioService = portfolioService;
    }

   
    @PostMapping("/performance")
    public ResponseEntity<PortfolioResponse> calculatePerformance(
            @RequestBody PortfolioRequest request) {

        return ResponseEntity.ok(
                portfolioService.calculatePerformance(request));
    }
}
