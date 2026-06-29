package com.portfolio.dailyreturn.summaryapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.portfolio.dailyreturn.summaryapi.model.PortfolioRequest;
import com.portfolio.dailyreturn.summaryapi.model.PortfolioResponse;
import com.portfolio.dailyreturn.summaryapi.validation.PortfolioValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PortfolioServiceImpl class.
 * 
 * Tests the core business logic for portfolio performance calculations,
 * including portfolio returns, excess returns, and status determination.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioServiceImpl Test Suite")
class PortfolioServiceImplTest {

    @Mock
    private PortfolioValidator validator;

    private PortfolioServiceImpl portfolioService;

    @BeforeEach
    void setUp() {
        portfolioService = new PortfolioServiceImpl(validator);
    }

    // ============= Valid Request Tests =============

    @Test
    @DisplayName("Should calculate performance with VALID status for valid request")
    void testCalculatePerformanceValidRequest() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORTFOLIO001",
                BigDecimal.valueOf(100000),   // beginMarketValue
                BigDecimal.valueOf(110000),   // endMarketValue
                BigDecimal.valueOf(0),        // netCashFlow
                BigDecimal.valueOf(5)         // benchmarkReturnPct
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertNotNull(response);
        assertEquals("PORTFOLIO001", response.getPortfolioId());
        assertEquals("VALID", response.getStatus());
        assertTrue(response.getReasons().isEmpty());
        assertNotNull(response.getProcessedAt());
        assertNotNull(response.getPortfolioReturnPct());
        // Expected return: (110000 - 100000 - 0) / 100000 * 100 = 10%
        assertEquals(BigDecimal.valueOf(10.00), response.getPortfolioReturnPct());
    }

    @Test
    @DisplayName("Should calculate portfolio return correctly")
    void testPortfolioReturnCalculation() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT002",
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(55000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(3)
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        // Expected: (55000 - 50000 - 1000) / 50000 * 100 = 8%
        assertEquals(BigDecimal.valueOf(8.00), response.getPortfolioReturnPct());
    }

    @Test
    @DisplayName("Should handle negative portfolio return (loss)")
    void testNegativePortfolioReturn() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT003",
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(95000),
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(2)
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        // Expected: (95000 - 100000 - 2000) / 100000 * 100 = -7%
        assertEquals(BigDecimal.valueOf(-7.00), response.getPortfolioReturnPct());
    }

    @Test
    @DisplayName("Should calculate excess return correctly")
    void testExcessReturnCalculation() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT004",
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(112000),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(5)
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        // Portfolio return: 12%, Benchmark: 5%, Excess: 7%
        assertNotNull(response.getExcessReturnPct());
    }

    // ============= REVIEW_REQUIRED Status Tests =============

    @Test
    @DisplayName("Should return REVIEW_REQUIRED status when excess return exceeds 5%")
    void testReviewRequiredStatusForHighExcessReturn() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT005",
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(120000),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(3)
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        // Portfolio return: 20%, Benchmark: 3%, Excess: 17% > 5% threshold
        assertEquals("REVIEW_REQUIRED", response.getStatus());
    }

    @Test
    @DisplayName("Should return REVIEW_REQUIRED status when cash flow threshold exceeded")
    void testReviewRequiredStatusForCashFlowThreshold() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT006",
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(110000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(5)
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request))
                .thenReturn("Net Cash Flow exceeds 20%");

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertEquals("REVIEW_REQUIRED", response.getStatus());
    }

    // ============= INVALID_INPUT Status Tests =============

    @Test
    @DisplayName("Should return INVALID_INPUT status when validation fails")
    void testInvalidInputStatus() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT007",
                BigDecimal.valueOf(-100000),  // Invalid: negative begin value
                BigDecimal.valueOf(110000),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(5)
        );

        List<String> validationErrors = new ArrayList<>();
        validationErrors.add("Begin market value should be greater than zero");

        when(validator.validate(request)).thenReturn(validationErrors);
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertEquals("INVALID_INPUT", response.getStatus());
        assertEquals(1, response.getReasons().size());
        assertTrue(response.getReasons().contains("Begin market value should be greater than zero"));
    }

    @Test
    @DisplayName("Should include all validation errors in response")
    void testMultipleValidationErrors() {
        // Arrange
        PortfolioRequest request = new PortfolioRequest();
        request.setPortfolioId("PORT008");
        request.setValuationDate(LocalDate.now());

        List<String> validationErrors = new ArrayList<>();
        validationErrors.add("Begin market value should be greater than zero");
        validationErrors.add("End market value should be greater than zero");
        validationErrors.add("Currency is Mandatory");

        when(validator.validate(request)).thenReturn(validationErrors);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertEquals("INVALID_INPUT", response.getStatus());
        assertEquals(3, response.getReasons().size());
        verify(validator, times(1)).validate(request);
    }

    // ============= Edge Cases =============

    @Test
    @DisplayName("Should handle zero portfolio return")
    void testZeroPortfolioReturn() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT009",
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0)
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertEquals(BigDecimal.valueOf(0.00), response.getPortfolioReturnPct());
        assertEquals("VALID", response.getStatus());
    }

    @Test
    @DisplayName("Should handle high precision decimal calculations")
    void testHighPrecisionCalculations() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT010",
                BigDecimal.valueOf(123456.78),
                BigDecimal.valueOf(125789.45),
                BigDecimal.valueOf(345.67),
                BigDecimal.valueOf(1.5)
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertNotNull(response.getPortfolioReturnPct());
        // Verify scale is 2 decimal places
        assertEquals(2, response.getPortfolioReturnPct().scale());
    }

    @Test
    @DisplayName("Should handle large market values")
    void testLargeMarketValues() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT011",
                new BigDecimal("1000000000"),
                new BigDecimal("1100000000"),
                new BigDecimal("5000000"),
                BigDecimal.valueOf(3)
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertNotNull(response.getPortfolioReturnPct());
        assertTrue(response.getPortfolioReturnPct().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Should handle very small market values")
    void testVerySmallMarketValues() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT012",
                BigDecimal.valueOf(0.01),
                BigDecimal.valueOf(0.015),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0)
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertNotNull(response.getPortfolioReturnPct());
        assertEquals(2, response.getPortfolioReturnPct().scale());
    }

    @Test
    @DisplayName("Should handle negative benchmark return")
    void testNegativeBenchmarkReturn() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT013",
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(105000),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(-2)  // Negative benchmark
        );

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertNotNull(response);
        assertEquals("VALID", response.getStatus());
    }

    @Test
    @DisplayName("Should set response metadata correctly")
    void testResponseMetadata() {
        // Arrange
        PortfolioRequest request = createValidPortfolioRequest(
                "PORT014",
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(110000),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(5)
        );
        LocalDate testDate = LocalDate.of(2024, 6, 15);
        request.setValuationDate(testDate);

        when(validator.validate(request)).thenReturn(new ArrayList<>());
        when(validator.validateNetCashFlowThreshold(request)).thenReturn(null);

        // Act
        PortfolioResponse response = portfolioService.calculatePerformance(request);

        // Assert
        assertEquals("PORT014", response.getPortfolioId());
        assertEquals(testDate, response.getValuationDate());
        assertNotNull(response.getProcessedAt());
        assertEquals(BigDecimal.valueOf(5), response.getBenchmarkReturnPct());
    }

    // ============= Helper Methods =============

    private PortfolioRequest createValidPortfolioRequest(
            String portfolioId,
            BigDecimal beginValue,
            BigDecimal endValue,
            BigDecimal netCashFlow,
            BigDecimal benchmarkReturn) {

        PortfolioRequest request = new PortfolioRequest();
        request.setPortfolioId(portfolioId);
        request.setValuationDate(LocalDate.now());
        request.setBeginMarketValue(beginValue);
        request.setEndMarketValue(endValue);
        request.setNetCashFlow(netCashFlow);
        request.setBenchmarkReturnPct(benchmarkReturn);
        request.setCurrency("USD");
        request.setRequestedBy("TEST_USER");

        return request;
    }
}
