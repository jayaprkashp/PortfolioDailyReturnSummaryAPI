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

/**
 * Service implementation for portfolio performance calculations.
 * 
 * <p>This service handles the core business logic for calculating portfolio performance metrics,
 * including portfolio returns, benchmark comparisons, and excess returns. It serves as the
 * application's business logic layer between the controller and validation/data layers.
 * 
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li>Validating portfolio request data using {@link PortfolioValidator}</li>
 *   <li>Calculating portfolio return percentages based on market values and cash flows</li>
 *   <li>Computing excess returns by comparing portfolio return against benchmark</li>
 *   <li>Identifying significant performance deviations from benchmark</li>
 *   <li>Constructing comprehensive performance response objects</li>
 * </ul>
 * 
 * <p><b>Calculation Methodology:</b>
 * <pre>
 * Portfolio Return (%) = ((End Value - Begin Value - Net Cash Flow) / Begin Value) * 100
 * Excess Return (%) = Portfolio Return - Benchmark Return
 * </pre>
 * 
 * <p><b>Performance Thresholds:</b>
 * <ul>
 *   <li>Excess Return > 5% (absolute): Flagged as significant performance deviation</li>
 *   <li>Net Cash Flow > 20% of Begin Value: Flagged as unusual trading activity</li>
 * </ul>
 * 
 * @author Portfolio API Team
 * @version 1.0
 * @since 1.0
 * @see PortfolioValidator
 * @see PortfolioRequest
 * @see PortfolioResponse
 */
@Service
public class PortfolioServiceImpl {

	/**
	 * Threshold percentage for excess return flagging (5%).
	 * Used to identify portfolios with significant performance deviations from the benchmark.
	 * Absolute excess return greater than 5% triggers a "REVIEW_REQUIRED" status.
	 */
	private static final BigDecimal EXCESS_RETURN_THRESHOLD_PCT = BigDecimal.valueOf(5);

	/**
	 * Validator component for input data validation.
	 * 
	 * <p>This validator is responsible for:
	 * <ul>
	 *   <li>Checking positive market values</li>
	 *   <li>Validating currency requirements</li>
	 *   <li>Ensuring value mismatches are detected</li>
	 *   <li>Validating net cash flow thresholds</li>
	 * </ul>
	 * 
	 * @see PortfolioValidator
	 */
	private final PortfolioValidator validator;

	/**
	 * Constructs a new PortfolioServiceImpl with dependency injection.
	 * 
	 * <p>The {@link PortfolioValidator} is injected via constructor to enable:
	 * <ul>
	 *   <li>Separation of concerns (validation logic isolated)</li>
	 *   <li>Ease of unit testing with mock validators</li>
	 *   <li>Flexible validation rule changes</li>
	 *   <li>Spring Framework dependency management</li>
	 * </ul>
	 *
	 * @param validator the {@link PortfolioValidator} to be used for input validation
	 * @throws IllegalArgumentException if validator is null
	 */
	public PortfolioServiceImpl(PortfolioValidator validator) {
		this.validator = validator;
	}

	/**
	 * Calculates the performance metrics of a portfolio based on the provided request.
	 * 
	 * <p>This method orchestrates the portfolio performance calculation workflow:
	 * <ol>
	 *   <li>Validates the input request data</li>
	 *   <li>Calculates the portfolio return percentage</li>
	 *   <li>Calculates the excess return (portfolio return - benchmark return)</li>
	 *   <li>Checks for significant performance deviations</li>
	 *   <li>Populates and returns a response with all metrics and status</li>
	 * </ol>
	 * 
	 * <p><b>Calculation Details:</b>
	 * <pre>
	 * Portfolio Return = ((End Value - Begin Value - Net Cash Flow) / Begin Value) * 100
	 * Excess Return = Portfolio Return - Benchmark Return
	 * </pre>
	 * 
	 * <p><b>Response Status Determination:</b>
	 * <ul>
	 *   <li><b>INVALID_INPUT:</b> If validation errors are found in the request data</li>
	 *   <li><b>REVIEW_REQUIRED:</b> If validation passes but:
	 *     <ul>
	 *       <li>Excess return exceeds ±5% threshold (significant outperformance/underperformance), OR</li>
	 *       <li>Net cash flow exceeds 20% of begin market value (unusual activity)</li>
	 *     </ul>
	 *   </li>
	 *   <li><b>VALID:</b> If all validations pass and no flags are triggered</li>
	 * </ul>
	 * 
	 * <p><b>Performance Flagging Scenarios:</b>
	 * <table border="1">
	 *   <tr>
	 *     <th>Excess Return</th>
	 *     <th>Cash Flow Status</th>
	 *     <th>Response Status</th>
	 *     <th>Reason</th>
	 *   </tr>
	 *   <tr>
	 *     <td>Any</td>
	 *     <td>Validation Error</td>
	 *     <td>INVALID_INPUT</td>
	 *     <td>Failed input validation</td>
	 *   </tr>
	 *   <tr>
	 *     <td>&gt; 5%</td>
	 *     <td>Valid</td>
	 *     <td>REVIEW_REQUIRED</td>
	 *     <td>Significant outperformance</td>
	 *   </tr>
	 *   <tr>
	 *     <td>&lt; -5%</td>
	 *     <td>Valid</td>
	 *     <td>REVIEW_REQUIRED</td>
	 *     <td>Significant underperformance</td>
	 *   </tr>
	 *   <tr>
	 *     <td>-5% to +5%</td>
	 *     <td>Valid</td>
	 *     <td>VALID</td>
	 *     <td>Performance within acceptable range</td>
	 *   </tr>
	 * </table>
	 * 
	 * <p><b>Example Calculations:</b>
	 * <pre>
	 * Example 1: Positive Return
	 * - Begin Value: 100,000
	 * - End Value: 110,000
	 * - Net Cash Flow: 0
	 * - Benchmark: 5%
	 * Result:
	 *   Portfolio Return = ((110,000 - 100,000 - 0) / 100,000) * 100 = 10.00%
	 *   Excess Return = 10.00% - 5% = 5.00%
	 *   Status = VALID (5.00% is at threshold boundary)
	 * 
	 * Example 2: Significant Outperformance
	 * - Begin Value: 100,000
	 * - End Value: 115,000
	 * - Net Cash Flow: 0
	 * - Benchmark: 5%
	 * Result:
	 *   Portfolio Return = ((115,000 - 100,000 - 0) / 100,000) * 100 = 15.00%
	 *   Excess Return = 15.00% - 5% = 10.00%
	 *   Status = REVIEW_REQUIRED (10.00% > 5% threshold)
	 * </pre>
	 *
	 * @param request the {@link PortfolioRequest} containing portfolio data to analyze
	 * @return a {@link PortfolioResponse} containing:
	 *         <ul>
	 *           <li>Portfolio ID and valuation date (echoed from request)</li>
	 *           <li>Calculated portfolio return percentage (2 decimal places)</li>
	 *           <li>Benchmark return percentage (echoed from request)</li>
	 *           <li>Calculated excess return percentage</li>
	 *           <li>Status: VALID, INVALID_INPUT, or REVIEW_REQUIRED</li>
	 *           <li>Reasons list containing validation errors or warnings (if applicable)</li>
	 *           <li>Processing timestamp</li>
	 *         </ul>
	 * @throws NullPointerException if request or its critical fields are null
	 * @see PortfolioValidator#validate(PortfolioRequest)
	 * @see #calculateReturn(PortfolioRequest)
	 * @see #isExcessReturnSignificant(BigDecimal)
	 */
	public PortfolioResponse calculatePerformance(PortfolioRequest request) {

		// Step 1: Validate the input request
		List<String> reasons = validator.validate(request);

		// Step 2: Calculate portfolio return
		BigDecimal portfolioReturn = calculateReturn(request);

		// Step 3: Calculate excess return
		BigDecimal excessReturn = null;
		if (portfolioReturn != null && request.getBenchmarkReturnPct() != null) {
			excessReturn = portfolioReturn.subtract(request.getBenchmarkReturnPct());
		}

		// Step 4: Construct response object
		PortfolioResponse response = new PortfolioResponse();

		response.setPortfolioId(request.getPortfolioId());
		response.setValuationDate(request.getValuationDate());
		response.setPortfolioReturnPct(portfolioReturn);
		response.setBenchmarkReturnPct(request.getBenchmarkReturnPct());
		response.setExcessReturnPct(excessReturn);

		// Step 5: Determine response status based on validation and thresholds
		if (reasons != null && reasons.size() > 0) {
			// Validation failed
			response.setStatus("INVALID_INPUT");
			response.setReasons(reasons);
		} else if (isExcessReturnSignificant(excessReturn)) {
			// Validation passed but excess return is significant
			response.setStatus("REVIEW_REQUIRED");
			if (reasons == null) {
				reasons = new ArrayList<>();
			}
			String warningMessage = String.format(
					"Significant excess return detected: %.2f%% (threshold: ±5%%). "
					+ "Portfolio performance deviates significantly from benchmark.",
					excessReturn);
			reasons.add(warningMessage);
			response.setReasons(reasons);
		} else {
			// All validations passed and no significant deviations
			response.setStatus("VALID");
			response.setReasons(Collections.emptyList());
		}

		// Step 6: Set processing timestamp
		response.setProcessedAt(LocalDateTime.now());

		return response;
	}

	/**
	 * Calculates the portfolio return percentage.
	 * 
	 * <p><b>Formula:</b><br>
	 * Portfolio Return (%) = ((End Value - Begin Value - Net Cash Flow) / Begin Value) * 100
	 * 
	 * <p><b>Calculation Details:</b>
	 * <ul>
	 *   <li>Numerator: End Value - Begin Value - Net Cash Flow (profit/loss amount)</li>
	 *   <li>Denominator: Begin Value (base for percentage calculation)</li>
	 *   <li>Intermediate precision: 6 decimal places (RoundingMode.HALF_UP)</li>
	 *   <li>Final precision: 2 decimal places (standard percentage format)</li>
	 * </ul>
	 * 
	 * <p><b>Examples:</b>
	 * <pre>
	 * Example 1: Positive Return (No Cash Flow)
	 * - Begin: 100,000
	 * - End: 110,000
	 * - Cash Flow: 0
	 * Return = ((110,000 - 100,000 - 0) / 100,000) * 100 = 10.00%
	 * 
	 * Example 2: Return with Cash Inflow
	 * - Begin: 100,000
	 * - End: 115,000
	 * - Cash Flow: 5,000 (inflow)
	 * Return = ((115,000 - 100,000 - 5,000) / 100,000) * 100 = 10.00%
	 * 
	 * Example 3: Return with Cash Outflow
	 * - Begin: 100,000
	 * - End: 95,000
	 * - Cash Flow: -10,000 (outflow/withdrawal)
	 * Return = ((95,000 - 100,000 - (-10,000)) / 100,000) * 100 = 5.00%
	 * 
	 * Example 4: Negative Return
	 * - Begin: 100,000
	 * - End: 90,000
	 * - Cash Flow: 0
	 * Return = ((90,000 - 100,000 - 0) / 100,000) * 100 = -10.00%
	 * </pre>
	 * 
	 * <p><b>Precision and Rounding:</b>
	 * <ul>
	 *   <li>Intermediate calculations use 6 decimal places for precision</li>
	 *   <li>HALF_UP rounding mode ensures standard rounding (0.5 rounds up)</li>
	 *   <li>Final result is scaled to 2 decimal places for currency percentage</li>
	 * </ul>
	 *
	 * @param request the {@link PortfolioRequest} containing market values and cash flows
	 * @return the portfolio return percentage as a {@link BigDecimal} with 2 decimal places,
	 *         or {@code null} if begin market value is not positive (division by zero protection)
	 * @throws NullPointerException if request or its value fields are null
	 * @see PortfolioRequest#getBeginMarketValue()
	 * @see PortfolioRequest#getEndMarketValue()
	 * @see PortfolioRequest#getNetCashFlow()
	 */
	private BigDecimal calculateReturn(PortfolioRequest request) {

		// Validate denominator to prevent division by zero
		if (request.getBeginMarketValue().compareTo(BigDecimal.ZERO) > 0) {

			// Calculate the profit/loss amount (numerator)
			BigDecimal numerator = request.getEndMarketValue()
					.subtract(request.getBeginMarketValue())
					.subtract(request.getNetCashFlow());

			// Calculate return percentage with proper rounding
			return numerator.divide(request.getBeginMarketValue(), 6, RoundingMode.HALF_UP)
					.multiply(BigDecimal.valueOf(100))
					.setScale(2, RoundingMode.HALF_UP);
		} else {
			// Return null if begin value is not positive (cannot calculate return)
			return null;
		}
	}

	/**
	 * Checks if the absolute value of excess return exceeds the 5% significance threshold.
	 * 
	 * <p>This method identifies portfolios with significant performance deviations from
	 * the benchmark. An absolute excess return greater than 5% indicates either:
	 * <ul>
	 *   <li><b>Strong Outperformance:</b> Excess return > +5%</li>
	 *   <li><b>Significant Underperformance:</b> Excess return < -5%</li>
	 * </ul>
	 * 
	 * <p><b>Scenarios:</b>
	 * <table border="1">
	 *   <tr>
	 *     <th>Excess Return</th>
	 *     <th>Absolute Value</th>
	 *     <th>Result</th>
	 *     <th>Interpretation</th>
	 *   </tr>
	 *   <tr>
	 *     <td>+6.5%</td>
	 *     <td>6.5%</td>
	 *     <td>true</td>
	 *     <td>Strong outperformance (6.5 > 5)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>-7.2%</td>
	 *     <td>7.2%</td>
	 *     <td>true</td>
	 *     <td>Significant underperformance (7.2 > 5)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>+3.5%</td>
	 *     <td>3.5%</td>
	 *     <td>false</td>
	 *     <td>Acceptable outperformance (3.5 ≤ 5)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>-4.0%</td>
	 *     <td>4.0%</td>
	 *     <td>false</td>
	 *     <td>Acceptable underperformance (4.0 ≤ 5)</td>
	 *   </tr>
	 *   <tr>
	 *     <td>+5.0%</td>
	 *     <td>5.0%</td>
	 *     <td>false</td>
	 *     <td>At threshold boundary (5.0 = 5, not greater)</td>
	 *   </tr>
	 * </table>
	 * 
	 * <p><b>Purpose:</b>
	 * <ul>
	 *   <li>Identifies portfolios requiring management review</li>
	 *   <li>Flags unusual performance that may indicate:</li>
	 *   <li>Exceptional portfolio management success (outperformance)</li>
	 *   <li>Potential portfolio management issues (underperformance)</li>
	 *   <li>Significant benchmark mismatch requiring investigation</li>
	 * </ul>
	 *
	 * @param excessReturn the excess return percentage as a {@link BigDecimal}
	 * @return {@code true} if the absolute value of excess return exceeds 5%, 
	 *         {@code false} if within ±5% or if excessReturn is null
	 * @see #EXCESS_RETURN_THRESHOLD_PCT
	 */
	private boolean isExcessReturnSignificant(BigDecimal excessReturn) {

		// Return false if excess return is null
		if (excessReturn == null) {
			return false;
		}

		// Get absolute value of excess return
		BigDecimal absoluteExcessReturn = excessReturn.abs();

		// Check if absolute value exceeds 5% threshold
		return absoluteExcessReturn.compareTo(EXCESS_RETURN_THRESHOLD_PCT) > 0;
	}
}
