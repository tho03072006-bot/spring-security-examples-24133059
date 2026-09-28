package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.*;
import vn.iotstar.service.AuthService;
@Controller @RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;
    @GetMapping("/login") public String login(){return "auth/login";}
    @GetMapping("/register") public String register(Model model){model.addAttribute("registerDTO",new RegisterDTO());return "auth/register";}
    @PostMapping("/register") public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,BindingResult result,RedirectAttributes redirect){
        if(result.hasErrors())return "auth/register";
        try{auth.register(dto);redirect.addAttribute("email",dto.getEmail());redirect.addFlashAttribute("success","Đã gửi OTP xác nhận đăng ký.");return "redirect:/verify-otp";}
        catch(RuntimeException e){result.reject("register",message(e));return "auth/register";}
    }
    @GetMapping("/verify-otp") public String verify(@RequestParam(defaultValue="") String email,Model model){var dto=new VerifyOtpDTO();dto.setEmail(email);model.addAttribute("verifyOtpDTO",dto);return "auth/verify-otp";}
    @PostMapping("/verify-otp") public String verify(@Valid @ModelAttribute("verifyOtpDTO") VerifyOtpDTO dto,BindingResult result,RedirectAttributes redirect){
        if(result.hasErrors())return "auth/verify-otp";
        if(!auth.verifyRegister(dto.getEmail(),dto.getOtp())){result.reject("otp","OTP không hợp lệ, hết hạn hoặc đã quá 5 lần thử.");return "auth/verify-otp";}
        redirect.addFlashAttribute("success","Xác nhận thành công. Hãy đăng nhập.");return "redirect:/login";
    }
    @PostMapping("/resend-register-otp") public String resend(@RequestParam String email,RedirectAttributes redirect){
        redirect.addAttribute("email",email);
        try{auth.resendRegisterOtp(email);redirect.addFlashAttribute("success","Đã gửi lại OTP.");}
        catch(RuntimeException e){redirect.addFlashAttribute("error",message(e));}
        return "redirect:/verify-otp";
    }
    @GetMapping("/forgot-password") public String forgot(Model model){model.addAttribute("forgotPasswordDTO",new ForgotPasswordDTO());return "auth/forgot-password";}
    @PostMapping("/forgot-password") public String forgot(@Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto,BindingResult result,RedirectAttributes redirect){
        if(result.hasErrors())return "auth/forgot-password";
        try{auth.forgotPassword(dto.getEmail());redirect.addAttribute("email",dto.getEmail());redirect.addFlashAttribute("success","Đã gửi OTP đặt lại mật khẩu.");return "redirect:/reset-password";}
        catch(RuntimeException e){result.reject("forgot",message(e));return "auth/forgot-password";}
    }
    @GetMapping("/reset-password") public String reset(@RequestParam(defaultValue="") String email,Model model){var dto=new ResetPasswordDTO();dto.setEmail(email);model.addAttribute("resetPasswordDTO",dto);return "auth/reset-password";}
    @PostMapping("/reset-password") public String reset(@Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO dto,BindingResult result,RedirectAttributes redirect){
        if(result.hasErrors())return "auth/reset-password";
        try{if(!auth.resetPassword(dto)){result.reject("otp","OTP không hợp lệ, hết hạn hoặc đã quá 5 lần thử.");return "auth/reset-password";}}
        catch(IllegalArgumentException e){result.reject("reset",e.getMessage());return "auth/reset-password";}
        redirect.addFlashAttribute("success","Đổi mật khẩu thành công. Hãy đăng nhập lại.");return "redirect:/login";
    }
    private String message(RuntimeException e){return e instanceof IllegalArgumentException?e.getMessage():"Không thể gửi OTP. Kiểm tra cấu hình email hoặc thử lại sau.";}
}
