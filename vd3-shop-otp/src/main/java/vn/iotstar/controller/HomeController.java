package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.*;
@Controller @RequiredArgsConstructor
public class HomeController {
    private final UserService users;
    private final ProductService products;
    @GetMapping({"/","/dashboard"}) public String home(Authentication auth,Model model){
        model.addAttribute("userCount",users.countUsers());model.addAttribute("productCount",products.countProducts());
        long own=auth!=null && auth.getPrincipal() instanceof CustomUserDetails u?products.countByUser(u.getId()):0;
        model.addAttribute("ownProductCount",own);return "home";
    }
    @GetMapping("/access-denied") public String denied(){return "access-denied";}
}
