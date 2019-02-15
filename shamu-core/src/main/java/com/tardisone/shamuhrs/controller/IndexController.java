package com.tardisone.shamuhrs.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping(value = {"/index", "/"})
    public String index() {
        return "hello world";
    }
}
