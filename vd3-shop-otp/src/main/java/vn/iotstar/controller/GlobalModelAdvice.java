package vn.iotstar.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import vn.iotstar.security.CustomUserDetails;
@ControllerAdvice
public class GlobalModelAdvice {
    @ModelAttribute public void currentUser(Authentication authentication,Model model){
        boolean signed=authentication!=null && authentication.getPrincipal() instanceof CustomUserDetails;
        model.addAttribute("signedIn",signed); model.addAttribute("shop",true);
        model.addAttribute("admin",signed && authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN")));
        if(signed)model.addAttribute("currentUser",authentication.getPrincipal());
    }
}
