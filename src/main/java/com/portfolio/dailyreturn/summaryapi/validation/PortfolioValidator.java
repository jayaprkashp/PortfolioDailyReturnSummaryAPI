package com.portfolio.dailyreturn.summaryapi.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.portfolio.dailyreturn.summaryapi.model.PortfolioRequest;

/**
 * Validator component for portfolio request data.
 * 
 * <p>This class provides validation methods to ensure that portfolio request data
 * meets all business requirements and constraints before processing.
 * 
 * <p>Validation errors are collected in a list and returned to the caller rather than
 * throwing exceptions, allowing for comprehensive error reporting.
 * 
 * @author Portfolio API Team
 * @version 1.0
 * @since 1.0
 */
@Component
public class PortfolioValidator {

	/**
	 * List of validation error reasons collected during validation.
	 * This list accumulates all validation failures encountered.
	 */
	List<String> reasons = new ArrayList<String>();

	/**
	 * Threshold percentage for net cash flow validation (20%).
	 * If net cash flow exceeds this percentage of begin market value,
	 * it may indicate unusual trading activity.
	 */
	private static final BigDecimal CASH_FLOW_THRESHOLD_PCT = BigDecimal.valueOf(20);

	/**
	 * Validates the entire portfolio request for data integrity and business rules.
	 * 
	 * <p>Performs the following validations:
	 * <ul>
	 *   <li>Begin Market Value must be positive</li>
	 *   <li>End Market Value must be positive</li>
	 *   <li>Currency must be provided and not blank</li>
	 *   <li>Begin Value and End Value cannot have mismatches</li>
	 *   <li>Net Cash Flow must not exceed 20% of Begin Market Value</li>
	 * </ul>
	 *
	 * @param request the {@link PortfolioRequest} to validate
	 * @return a {@link List} of validation error messages; empty if all validations pass
	 * @see PortfolioRequest
	 */
	public List<String> validate(PortfolioRequest request) {

		validatePositive(request.getBeginMarketValue(), "Beginning Market Value");

		validatePositive(request.getEndMarketValue(), "Ending Market Value");
		
		if(request.getCurrency() == null || request.getCurrency().isBlank()) {
			reasons.add("Currency is Mandatory");
		}

		if(request.getBeginMarketValue().compareTo(BigDecimal.ZERO) == 0 && request.getEndMarketValue().compareTo(BigDecimal.ZERO) != 0) {
			reasons.add("Begin Value and End Value Mismatch");
		}

		// Validate net cash flow against begin market value
		validateNetCashFlowThreshold(request);

		return reasons;
	}

	/**
	 * Validates that the provided value is greater than zero.
	 * 
	 * <p>This method is used for mandatory positive value fields such as market values.
	 * If validation fails, an error message is added to the reasons list.
	 *
	 * @param value the {@link BigDecimal} value to validate
	 * @param fieldName the name of the field being validated (used in error messages)
	 */
	private void validatePositive(BigDecimal value, String fieldName) {

		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			reasons.add(fieldName + " should be greater than zero");
		}
	}

	/**
	 * Validates that the net cash flow does not exceed 20% of the begin market value.
	 * 
	 * <p>This validation ensures that cash flows are within acceptable bounds relative to
	 * the portfolio size. Net cash flows greater than 20% of the beginning market value
	 * may indicate unusual trading activity or potential data entry errors.
	 * 
	 * <p>Calculation logic:
	 * <ol>
	 *   <li>Calculate 20% of begin market value: {@code beginMarketValue * 0.20}</li>
	 *   <li>Get the absolute value of net cash flow: {@code abs(netCashFlow)}</li>
	 *   <li>Compare if absolute net cash flow exceeds the threshold</li>
	 * </ol>
	 * 
	 * <p>Example:
	 * <pre>
	 * Begin Market Value: 100,000
	 * 20% Threshold: 20,000
	 * Net Cash Flow: 25,000 (absolute)
	 * Result: INVALID - exceeds threshold
	 * 
	 * Begin Market Value: 100,000
	 * 20% Threshold: 20,000
	 * Net Cash Flow: 15,000 (absolute)
	 * Result: VALID - within threshold
	 * </pre>
	 *
	 * @param request the {@link PortfolioRequest} containing portfolio data
	 * @see #CASH_FLOW_THRESHOLD_PCT
	 */
	private void validateNetCashFlowThreshold(PortfolioRequest request) {

		BigDecimal beginMarketValue = request.getBeginMarketValue();
		BigDecimal netCashFlow = request.getNetCashFlow();

		// If net cash flow is null, skip this validation
		if (netCashFlow == null) {
			return;
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
			reasons.add(errorMessage);
		}
	}
}
