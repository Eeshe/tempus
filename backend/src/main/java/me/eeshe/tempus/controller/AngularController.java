package me.eeshe.tempus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AngularController {

    @RequestMapping(value = "/**/{path:[^\\.]*}")
    public String forwardToIndex() {
        return "forward:/";
    }
}
