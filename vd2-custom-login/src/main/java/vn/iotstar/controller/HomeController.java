package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller public class HomeController {
    @GetMapping({"/","/dashboard"}) public String home(){return "home";}
    @GetMapping("/login") public String login(){return "auth/login";}
    @GetMapping("/access-denied") public String denied(){return "access-denied";}
    @GetMapping("/admin") public String admin(){return "admin";}
}
