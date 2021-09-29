package com.performance.apps.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.performance.domain.service.GoogleApiService;
import com.performance.domain.service.PerformanceService;

@Controller
public class PerformanceController {
    
    final static Logger log = LogManager.getLogger(PerformanceController.class);
    
    private final String MEASURE_FLAG_ON  = "1";
    
    PerformanceService service;
    GoogleApiService googleService;
    
    public PerformanceController(PerformanceService service, GoogleApiService googleService) {
        this.service = service;
        this.googleService = googleService;
    }

    @GetMapping(value = "/index")
    public String index() {
        return "index";
    }

    @PostMapping(value = "/execute")
    public String confirm(@RequestParam("measureFlag")String measureFlag, Model model) {

        service.truncateTable();
        
        Long start = System.currentTimeMillis();
        
        service.execute();
        
        Long end = System.currentTimeMillis();
        Long executeTime = end - start;
        String errorMessage = "";
        if(MEASURE_FLAG_ON.equals(measureFlag)) {
            try {
                googleService.execute(executeTime);
            } catch (Exception e) {
                log.error("スプレッドシートの更新でエラーが発生しました。", e);
                errorMessage = "スプレッドシートの更新でエラーが発生したので実行結果は手動で更新して下さい。";
            }
        }
        model.addAttribute("executeTime", executeTime);
        model.addAttribute("errorMessage", errorMessage);
        return "result";
    }
}
