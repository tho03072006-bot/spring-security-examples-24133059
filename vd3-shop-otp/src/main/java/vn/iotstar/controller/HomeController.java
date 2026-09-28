package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final ProductService productService;

    // Dashboard: đếm tổng user, tổng sản phẩm và sản phẩm của user đang đăng nhập
    @GetMapping({"/", "/dashboard"})
    public String home(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("productCount", productService.countProducts());
        model.addAttribute("ownProductCount",
                currentUser == null ? 0 : productService.countByUser(currentUser.getId()));
        return "home";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
