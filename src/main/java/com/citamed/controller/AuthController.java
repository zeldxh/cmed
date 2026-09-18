package com.citamed.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class AuthController {

    // Muestra la pantalla de inicio de sesion. La autenticacion la resuelve Spring Security.
    @GetMapping
    public String login() {
        return "/auth/login";
    }
}
