package com.qianyi.moduleswagger2.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("swagger2")
@ApiIgnore
public class Test {

    @GetMapping("test")
    public String test() {
        return "swagger2 ok";
    }
}
