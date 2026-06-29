// ============================================
// PORTFOLIO CALCULATOR - JAVASCRIPT
// ============================================

// DOM Elements
const form = document.getElementById('portfolioForm');
const submitBtn = document.getElementById('submitBtn');
const spinner = document.getElementById('spinner');
const responseSection = document.getElementById('responseSection');
const errorSection = document.getElementById('errorSection');
const valuationDateInput = document.getElementById('valuationDate');

// API Configuration
const API_BASE_URL = '/api/v1/portfolio';
const CALCULATE_ENDPOINT = `${API_BASE_URL}/performance`;

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', function() {
    // Set today's date as default valuation date
    const today = new Date().toISOString().split('T')[0];
    valuationDateInput.value = today;

    // Add event listeners
    form.addEventListener('submit', handleFormSubmit);
    form.addEventListener('reset', handleFormReset);
});

// ============================================
// FORM HANDLING
// ============================================

/**
 * Handle form submission
 */
async function handleFormSubmit(event) {
    event.preventDefault();

    // Clear previous errors and results
    clearErrors();
    hideAllSections();

    // Validate form
    if (!form.checkValidity()) {
        displayClientValidationErrors();
        return;
    }

    // Get form data
    const formData = new FormData(form);
    const portfolioRequest = buildPortfolioRequest(formData);

    // Show loading state
    setSubmitButtonLoading(true);

    try {
        // Call API
        const response = await fetch(CALCULATE_ENDPOINT, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(portfolioRequest)
        });

        if (!response.ok) {
            throw new Error(`API Error: ${response.status} ${response.statusText}`);
        }

        const portfolioResponse = await response.json();
        displayResponse(portfolioResponse);

    } catch (error) {
        console.error('Error:', error);
        displayError(error.message);
    } finally {
        setSubmitButtonLoading(false);
    }
}

/**
 * Build PortfolioRequest object from form data
 */
function buildPortfolioRequest(formData) {
    return {
        portfolioId: formData.get('portfolioId'),
        valuationDate: formData.get('valuationDate'),
        beginMarketValue: parseFloat(formData.get('beginMarketValue')),
        endMarketValue: parseFloat(formData.get('endMarketValue')),
        netCashFlow: parseFloat(formData.get('netCashFlow')) || 0,
        benchmarkReturnPct: parseFloat(formData.get('benchmarkReturnPct')),
        currency: formData.get('currency'),
        requestedBy: formData.get('requestedBy')
    };
}

/**
 * Handle form reset
 */
function handleFormReset() {
    hideAllSections();
    clearErrors();
    const today = new Date().toISOString().split('T')[0];
    valuationDateInput.value = today;
}

// ============================================
// VALIDATION
// ============================================

/**
 * Display client-side validation errors
 */
function displayClientValidationErrors() {
    const formElements = form.elements;

    for (let element of formElements) {
        if (element.name && !element.validity.valid) {
            const errorElement = document.getElementById(`${element.name}Error`);
            if (errorElement) {
                errorElement.textContent = getValidationErrorMessage(element);
                errorElement.classList.add('show');
            }
        }
    }
}

/**
 * Get custom validation error message
 */
function getValidationErrorMessage(element) {
    if (element.validity.valueMissing) {
        return 'This field is required';
    }
    if (element.validity.typeMismatch) {
        return `Please enter a valid ${element.type}`;
    }
    if (element.validity.rangeUnderflow) {
        return 'Value must be greater than zero';
    }
    if (element.validity.customError) {
        return element.validationMessage;
    }
    return 'Please enter a valid value';
}

/**
 * Clear all validation errors
 */
function clearErrors() {
    const errorElements = document.querySelectorAll('.error-text');
    errorElements.forEach(element => {
        element.textContent = '';
        element.classList.remove('show');
    });
}

// ============================================
// RESPONSE DISPLAY
// ============================================

/**
 * Display successful response
 */
function displayResponse(portfolioResponse) {
    responseSection.style.display = 'block';

    // Set status badge
    const statusBadge = document.getElementById('statusBadge');
    statusBadge.textContent = formatStatus(portfolioResponse.status);
    statusBadge.className = `status-badge ${portfolioResponse.status.toLowerCase().replace('_', '-')}`;

    // Populate response fields
    document.getElementById('responsePortfolioId').textContent = 
        portfolioResponse.portfolioId || '-';
    
    document.getElementById('responseValuationDate').textContent = 
        formatDate(portfolioResponse.valuationDate) || '-';
    
    document.getElementById('responsePortfolioReturn').textContent = 
        formatPercentage(portfolioResponse.portfolioReturnPct);
    
    document.getElementById('responseBenchmarkReturn').textContent = 
        formatPercentage(portfolioResponse.benchmarkReturnPct);
    
    document.getElementById('responseExcessReturn').textContent = 
        formatPercentage(portfolioResponse.excessReturnPct);
    
    document.getElementById('responseProcessedAt').textContent = 
        formatDateTime(portfolioResponse.processedAt) || '-';

    // Display reasons if any
    if (portfolioResponse.reasons && portfolioResponse.reasons.length > 0) {
        displayReasons(portfolioResponse.reasons);
    }

    // Store response for download
    window.lastResponse = {
        request: buildPortfolioRequest(new FormData(form)),
        response: portfolioResponse
    };

    // Scroll to response
    responseSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

/**
 * Display validation reasons/notes
 */
function displayReasons(reasons) {
    const reasonsContainer = document.getElementById('reasonsContainer');
    const reasonsList = document.getElementById('reasonsList');

    reasonsList.innerHTML = '';
    reasons.forEach(reason => {
        const li = document.createElement('li');
        li.textContent = reason;
        reasonsList.appendChild(li);
    });

    reasonsContainer.style.display = 'block';
}

/**
 * Display error message
 */
function displayError(message) {
    errorSection.style.display = 'block';
    document.getElementById('errorMessage').textContent = 
        message || 'An unexpected error occurred. Please try again.';
    errorSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

/**
 * Format status text
 */
function formatStatus(status) {
    const statusMap = {
        'VALID': '✓ Valid',
        'REVIEW_REQUIRED': '⚠ Review Required',
        'INVALID_INPUT': '✗ Invalid Input'
    };
    return statusMap[status] || status;
}

/**
 * Format percentage value
 */
function formatPercentage(value) {
    if (value === null || value === undefined) {
        return '-';
    }
    const numValue = parseFloat(value);
    const formatted = numValue.toFixed(2);
    return `${formatted}%`;
}

/**
 * Format date string
 */
function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

/**
 * Format datetime string
 */
function formatDateTime(datetimeString) {
    if (!datetimeString) return '-';
    const date = new Date(datetimeString);
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

/**
 * Set submit button loading state
 */
function setSubmitButtonLoading(isLoading) {
    submitBtn.disabled = isLoading;
    const btnText = document.querySelector('.btn-text');
    
    if (isLoading) {
        spinner.style.display = 'inline-block';
        btnText.textContent = 'Calculating...';
    } else {
        spinner.style.display = 'none';
        btnText.textContent = 'Calculate Performance';
    }
}

/**
 * Hide all result sections
 */
function hideAllSections() {
    responseSection.style.display = 'none';
    errorSection.style.display = 'none';
}

// ============================================
// EXPORT/DOWNLOAD FUNCTIONS
// ============================================

/**
 * Download results as JSON
 */
function downloadResult() {
    if (!window.lastResponse) {
        alert('No results to download');
        return;
    }

    const dataStr = JSON.stringify(window.lastResponse, null, 2);
    const dataBlob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    
    link.href = url;
    link.download = `portfolio-result-${new Date().getTime()}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}

/**
 * Reset form and clear results
 */
function resetForm() {
    form.reset();
    handleFormReset();
    form.focus();
}

// ============================================
// REAL-TIME VALIDATION
// ============================================

/**
 * Setup real-time validation listeners
 */
const formInputs = form.querySelectorAll('input, select');
formInputs.forEach(input => {
    input.addEventListener('blur', function() {
        validateSingleField(this);
    });

    input.addEventListener('input', function() {
        // Clear error on input
        const errorElement = document.getElementById(`${this.name}Error`);
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.classList.remove('show');
        }
    });
});

/**
 * Validate a single field
 */
function validateSingleField(element) {
    if (!element.validity.valid) {
        const errorElement = document.getElementById(`${element.name}Error`);
        if (errorElement) {
            errorElement.textContent = getValidationErrorMessage(element);
            errorElement.classList.add('show');
        }
    }
}

// ============================================
// KEYBOARD SHORTCUTS
// ============================================

document.addEventListener('keydown', function(event) {
    // Ctrl/Cmd + Enter to submit form
    if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
        if (form.checkValidity()) {
            form.dispatchEvent(new Event('submit'));
        }
    }

    // Escape to hide results
    if (event.key === 'Escape') {
        hideAllSections();
    }
});

// ============================================
// LOGGING & DEBUGGING
// ============================================

/**
 * Log API request/response for debugging
 */
function logAPICall(request, response, status) {
    console.log('📊 Portfolio API Call', {
        timestamp: new Date().toISOString(),
        request: request,
        response: response,
        status: status
    });
}
