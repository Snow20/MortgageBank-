package com.mortgagebank.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class MortgageApplicationDto {

    @NotNull
    private Long customerId;

    @NotNull
    @DecimalMin("1.00")
    private BigDecimal propertyValue;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal downPayment;

    @NotNull
    @Min(1)
    @Max(40)
    private Integer termYears;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal interestRate;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(BigDecimal propertyValue) {
        this.propertyValue = propertyValue;
    }

    public BigDecimal getDownPayment() {
        return downPayment;
    }

    public void setDownPayment(BigDecimal downPayment) {
        this.downPayment = downPayment;
    }

    public Integer getTermYears() {
        return termYears;
    }

    public void setTermYears(Integer termYears) {
        this.termYears = termYears;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }
}