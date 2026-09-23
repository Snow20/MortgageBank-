package com.mortgagebank.controller;

import org.springframework.web.bind.annotation.*;
import com.mortgagebank.service.MortgageService;
import com.mortgagebank.model.MortgageApplication;
import java.util.List;

@RestController
@RequestMapping("/api/mortgages")
public class MortgageController {
    private final MortgageService service;

    public MortgageController(MortgageService service) {
        this.service = service;
    }

    @GetMapping
    public List<MortgageApplication> getAll() {
        return service.getAllApplications();
    }

    @PostMapping
    public MortgageApplication create(@RequestBody MortgageApplication app) {
        return service.saveApplication(app);
    }
}
