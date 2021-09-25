package com.performance.apps.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.performance.domain.service.GoogleApiService;
import com.performance.domain.service.PerformanceService;

@Controller
public class PerformanceController {
    
    PerformanceService service;
    GoogleApiService googleService;
    
    public PerformanceController(PerformanceService service, GoogleApiService googleService) {
        this.service = service;
        this.googleService = googleService;
    }

    @GetMapping(value = "/index")
    public String index(PerformanceForm form, Model model) {
        return "index";
    }

    @PostMapping(value = "/execute")
    public String confirm(@Validated @ModelAttribute PerformanceForm form, BindingResult result, Model model) {

        service.truncateTable();
        
        Long start = System.currentTimeMillis();
        
        service.execute();
        
        Long end = System.currentTimeMillis();
        try {
            googleService.execute();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        model.addAttribute("executeTime", end - start);
        return "result";
    }
}
