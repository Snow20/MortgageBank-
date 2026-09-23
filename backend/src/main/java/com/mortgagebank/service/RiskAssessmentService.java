package com.mortgagebank.service;

import com.mortgagebank.model.MortgageApplication;
import com.mortgagebank.repository.MortgageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskAssessmentService {

    private final MortgageRepository mortgageRepository;

    public RiskAssessmentService(MortgageRepository mortgageRepository) {
        this.mortgageRepository = mortgageRepository;
    }

    @Transactional
    public void evaluate(Long mortgageId) {
        MortgageApplication app = mortgageRepository.findById(mortgageId)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + mortgageId));

        // Regla de riesgo simple: Si el importe supera el 85% del valor de la propiedad, se rechaza.
        double ltv = (app.getRequestedAmount() / app.getPropertyValue()) * 100;
        
        if (ltv <= 85.0) {
            app.setStatus("APPROVED");
        } else {
            app.setStatus("REJECTED");
        }

        mortgageRepository.save(app);
    }
}
