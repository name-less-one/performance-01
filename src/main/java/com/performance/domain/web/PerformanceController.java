package com.performance.domain.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.performance.domain.service.PerformanceService;

@Controller
public class PerformanceController {

    final static Logger log = LogManager.getLogger(PerformanceController.class);
    
    PerformanceService service;
    
    public PerformanceController(PerformanceService service) {
        this.service = service;
    }

    @GetMapping(value = "/index")
    public String index(PerformanceForm form, Model model) {
        return "index";
    }

    @PostMapping(value = "/execute")
    public String confirm(@Validated @ModelAttribute PerformanceForm form, BindingResult result, Model model) {

        Long start = System.currentTimeMillis();
        
        service.execute();
        
        Long end = System.currentTimeMillis();
        model.addAttribute("executeTime", end - start);
        return "result";
    }
}
