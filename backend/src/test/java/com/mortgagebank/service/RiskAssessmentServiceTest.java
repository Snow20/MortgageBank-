package com.mortgagebank.service;

import com.mortgagebank.model.MortgageApplication;
import com.mortgagebank.repository.MortgageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RiskAssessmentServiceTest {

    @Mock
    private MortgageRepository mortgageRepository;

    @InjectMocks
    private RiskAssessmentService riskAssessmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void evaluate_ShouldApprove_WhenLtvIsLessThan85Percent() {
        MortgageApplication app = new MortgageApplication();
        app.setId(1L);
        app.setPropertyValue(200000.0);
        app.setRequestedAmount(150000.0); // 75% LTV

        when(mortgageRepository.findById(1L)).thenReturn(Optional.of(app));

        riskAssessmentService.evaluate(1L);

        ArgumentCaptor<MortgageApplication> captor = ArgumentCaptor.forClass(MortgageApplication.class);
        verify(mortgageRepository, times(1)).save(captor.capture());

        assertEquals("APPROVED", captor.getValue().getStatus());
    }

    @Test
    void evaluate_ShouldReject_WhenLtvIsGreaterThan85Percent() {
        MortgageApplication app = new MortgageApplication();
        app.setId(2L);
        app.setPropertyValue(200000.0);
        app.setRequestedAmount(180000.0); // 90% LTV

        when(mortgageRepository.findById(2L)).thenReturn(Optional.of(app));

        riskAssessmentService.evaluate(2L);

        ArgumentCaptor<MortgageApplication> captor = ArgumentCaptor.forClass(MortgageApplication.class);
        verify(mortgageRepository, times(1)).save(captor.capture());

        assertEquals("REJECTED", captor.getValue().getStatus());
    }
}
