package com.portfolio.dailyreturn.summaryapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.portfolio.dailyreturn.summaryapi.model.PortfolioRequest;
import com.portfolio.dailyreturn.summaryapi.model.PortfolioResponse;
import com.portfolio.dailyreturn.summaryapi.validation.PortfolioValidator;

@Service
public class PortfolioServiceImpl {

	private final PortfolioValidator validator;

	public PortfolioServiceImpl(PortfolioValidator validator) {
		this.validator = validator;
	}

	public PortfolioResponse calculatePerformance(PortfolioRequest request) {

		List reasons = validator.validate(request);

		BigDecimal portfolioReturn = calculateReturn(request);

		BigDecimal excessReturn = portfolioReturn.subtract(request.getBenchmarkReturnPct());

		PortfolioResponse response = new PortfolioResponse();

		response.setPortfolioId(request.getPortfolioId());
		response.setValuationDate(request.getValuationDate());
		response.setPortfolioReturnPct(portfolioReturn);
		response.setBenchmarkReturnPct(request.getBenchmarkReturnPct());
		response.setExcessReturnPct(excessReturn);
		if (reasons != null && reasons.size() != 0) {
			response.setStatus("INVALID_INPUT");
			response.setReasons(reasons);
		} else {
			response.setStatus("VALID");
			response.setReasons(Collections.emptyList());
		}
		response.setProcessedAt(LocalDateTime.now());

		return response;
	}

	private BigDecimal calculateReturn(PortfolioRequest request) {

		BigDecimal numerator = request.getEndMarketValue().subtract(request.getBeginMarketValue())
				.subtract(request.getNetCashFlow());

		return numerator.divide(request.getBeginMarketValue(), 6, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
	}

}
