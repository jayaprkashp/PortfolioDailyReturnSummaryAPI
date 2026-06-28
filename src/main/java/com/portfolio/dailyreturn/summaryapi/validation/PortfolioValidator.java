package com.portfolio.dailyreturn.summaryapi.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.portfolio.dailyreturn.summaryapi.exception.ValidationException;
import com.portfolio.dailyreturn.summaryapi.model.PortfolioRequest;

@Component
public class PortfolioValidator {

	List<String> reasons = new ArrayList<String>();

	public List<String> validate(PortfolioRequest request) {

		validatePositive(request.getBeginMarketValue(), "Beginning Market Value");

		validatePositive(request.getEndMarketValue(), "Ending Market Value");
		
		if(request.getCurrency() == null || request.getCurrency().isBlank()) {
			
			reasons.add("Currency is Mandatory");
			throw new ValidationException("Currency is Mandatory");
		}

		if(request.getBeginMarketValue().compareTo(BigDecimal.ZERO) == 0 && request.getEndMarketValue().compareTo(BigDecimal.ZERO) != 0) {
			reasons.add("Begin Value and End Value Mismatch");
			throw new ValidationException("Begin Value and End Value Mismatch");
		}

		return reasons;
	}

	private void validatePositive(BigDecimal value, String fieldName) {

		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {

			reasons.add(fieldName + " should be greater than zero");
			throw new ValidationException(fieldName + " should be greater than zero");
		}
	}

}
