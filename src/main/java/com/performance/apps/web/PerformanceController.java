package com.performance.apps.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.task.TaskRejectedException;
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

        try {
            service.execute(measureFlag);
        } catch (TaskRejectedException e) {
            log.error("非同期処理実行中", e);
        }

        String message = null;
        if(MEASURE_FLAG_ON.equals(measureFlag)) {
            message = "非同期にて処理を実行しています。処理時間はログかスプレッドシートを確認してください。";
        }

        model.addAttribute("message", message);

        return "result";
    }
}
