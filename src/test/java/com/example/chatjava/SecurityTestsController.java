package com.example.chatjava;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityTestsController {

    @GetMapping("/unprotected/test")
    public String sayHi(){
        return "Hello";
    }

    @GetMapping("/api/test")
    public String sayNope(){
        return "Nope";
    }

}
