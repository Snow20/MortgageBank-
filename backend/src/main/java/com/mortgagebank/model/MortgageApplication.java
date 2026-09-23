package com.mortgagebank.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mortgage_applications")
public class MortgageApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String customerEmail;
    private Double propertyValue;
    private Double requestedAmount;
    private Integer termYears;
    private String status = "PENDING";

    public MortgageApplication() {}

    public MortgageApplication(Long id, String customerName, String customerEmail, Double propertyValue, Double requestedAmount, Integer termYears, String status) {
        this.id = id;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.propertyValue = propertyValue;
        this.requestedAmount = requestedAmount;
        this.termYears = termYears;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public Double getPropertyValue() { return propertyValue; }
    public void setPropertyValue(Double propertyValue) { this.propertyValue = propertyValue; }

    public Double getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(Double requestedAmount) { this.requestedAmount = requestedAmount; }

    public Integer getTermYears() { return termYears; }
    public void setTermYears(Integer termYears) { this.termYears = termYears; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
