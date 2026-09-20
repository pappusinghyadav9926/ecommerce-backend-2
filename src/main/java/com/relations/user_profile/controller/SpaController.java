package com.relations.user_profile.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/products",
        "/products/**",
        "/cart",
        "/checkout",
        "/login",
        "/register",
        "/orders",
        "/profile",
        "/vendor/**",
        "/admin/**"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
