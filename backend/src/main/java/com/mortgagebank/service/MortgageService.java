package com.mortgagebank.service;

import com.mortgagebank.config.RabbitMQConfig;
import com.mortgagebank.model.MortgageApplication;
import com.mortgagebank.repository.MortgageRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MortgageService {

    private final MortgageRepository mortgageRepository;
    private final RabbitTemplate rabbitTemplate;

    public MortgageService(MortgageRepository mortgageRepository, RabbitTemplate rabbitTemplate) {
        this.mortgageRepository = mortgageRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public MortgageApplication saveApplication(MortgageApplication application) {
        if (application.getStatus() == null) {
            application.setStatus("PENDING");
        }
        
        MortgageApplication savedApp = mortgageRepository.save(application);

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME, 
            RabbitMQConfig.ROUTING_KEY, 
            savedApp
        );

        return savedApp;
    }

    @Transactional
    public MortgageApplication createApplication(MortgageApplication application) {
        return saveApplication(application);
    }

    public List<MortgageApplication> getAllApplications() {
        return mortgageRepository.findAll();
    }
}
