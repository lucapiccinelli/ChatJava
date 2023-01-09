package com.example.chatjava;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestsController {

    @GetMapping("/public/test")
    public String sayHi(){
        return "Hello";
    }

    @GetMapping("/basic/test")
    public String sayNope(){
        return "Nope";
    }

    @GetMapping("/api/test")
    public String withJwt(){
        return "Requires jwt";
    }

}
