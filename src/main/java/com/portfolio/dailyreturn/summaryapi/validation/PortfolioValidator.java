package com.portfolio.dailyreturn.summaryapi.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.portfolio.dailyreturn.summaryapi.model.PortfolioRequest;

/**
 * This is validator class which contains all business validations
 */
@Component
public class PortfolioValidator {

	
	private static final BigDecimal CASH_FLOW_THRESHOLD_PCT = BigDecimal.valueOf(20);

	/*
	 * This method validates the Input for INVALID_INPUT status
	 */
	public List<String> validate(PortfolioRequest request) {
		
		List<String> reasons = new ArrayList<String>();
		

		if (request.getBeginMarketValue() == null || request.getBeginMarketValue().compareTo(BigDecimal.ZERO) <= 0) {
			reasons.add("Begin market value should be greater than zero");
		}

		if (request.getEndMarketValue() == null || request.getEndMarketValue().compareTo(BigDecimal.ZERO) <= 0) {
			reasons.add("End market value should be greater than zero");
		}
		
		if(request.getCurrency() == null || request.getCurrency().isBlank()) {
			reasons.add("Currency is Mandatory");
		}

		if(request.getBeginMarketValue().compareTo(BigDecimal.ZERO) == 0 && request.getEndMarketValue().compareTo(BigDecimal.ZERO) != 0) {
			reasons.add("Begin Value and End Value Mismatch");
		}

		return reasons;
	}

	/*
	 * This method calculates the threshold percentage for REVIEW_REQUIRED status
	 */
	public String validateNetCashFlowThreshold(PortfolioRequest request) {

		BigDecimal beginMarketValue = request.getBeginMarketValue();
		BigDecimal netCashFlow = request.getNetCashFlow();
		
		String returnMessage = null;

		// If net cash flow is null, skip this validation
		if (netCashFlow == null) {
			return returnMessage;
		}

		// Calculate 20% of begin market value
		BigDecimal threshold = beginMarketValue.multiply(CASH_FLOW_THRESHOLD_PCT)
				.divide(BigDecimal.valueOf(100));

		// Get absolute value of net cash flow
		BigDecimal absoluteNetCashFlow = netCashFlow.abs();

		// Check if net cash flow exceeds the threshold
		if (absoluteNetCashFlow.compareTo(threshold) > 0) {
			String errorMessage = String.format(
					"Net Cash Flow (%.2f) exceeds 20%% of Begin Market Value (%.2f). "
					+ "Threshold: %.2f. Please review the input data.",
					netCashFlow, beginMarketValue, threshold);
			returnMessage = errorMessage;
		}
		
		return returnMessage;
	}
}
