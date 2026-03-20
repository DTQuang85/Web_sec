package com.electronicshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupportController {

    @GetMapping("/guide")
    public String guide() {
        return "guide";
    }

    @GetMapping("/payment")
    public String payment() {
        return "payment";
    }

    @GetMapping("/shipping")
    public String shipping() {
        return "shipping";
    }

    @GetMapping("/returns")
    public String returns() {
        return "returns";
    }
}
