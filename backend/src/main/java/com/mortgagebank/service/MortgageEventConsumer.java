package com.mortgagebank.service;

import com.mortgagebank.config.RabbitMQConfig;
import com.mortgagebank.model.MortgageApplication;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MortgageEventConsumer {

    private final RiskAssessmentService riskAssessmentService;

    public MortgageEventConsumer(RiskAssessmentService riskAssessmentService) {
        this.riskAssessmentService = riskAssessmentService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleMortgageEvent(MortgageApplication application) {
        if (application != null && application.getId() != null) {
            riskAssessmentService.evaluate(application.getId());
        }
    }
}
