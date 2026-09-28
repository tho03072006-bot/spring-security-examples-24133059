package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.VerifyOtpDTO;
import vn.iotstar.service.AuthService;

// Đăng ký + xác nhận OTP, quên mật khẩu + đặt lại bằng OTP
@Controller
@RequiredArgsConstructor
public class AuthController {

    private static final String INVALID_OTP = "OTP không hợp lệ, hết hạn hoặc đã quá 5 lần thử.";

    private final AuthService authService;

    // POST /login do Spring Security xử lý
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO dto, BindingResult result,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        try {
            authService.register(dto);
        } catch (RuntimeException e) {
            result.reject("register", message(e));
            return "auth/register";
        }
        redirect.addAttribute("email", dto.getEmail());
        redirect.addFlashAttribute("success", "Đã gửi OTP xác nhận đăng ký.");
        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtp(@RequestParam(defaultValue = "") String email, Model model) {
        var dto = new VerifyOtpDTO();
        dto.setEmail(email);
        model.addAttribute("verifyOtpDTO", dto);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@Valid @ModelAttribute("verifyOtpDTO") VerifyOtpDTO dto, BindingResult result,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/verify-otp";
        }
        if (!authService.verifyRegister(dto.getEmail(), dto.getOtp())) {
            result.reject("otp", INVALID_OTP);
            return "auth/verify-otp";
        }
        redirect.addFlashAttribute("success", "Xác nhận thành công. Hãy đăng nhập.");
        return "redirect:/login";
    }

    @PostMapping("/resend-register-otp")
    public String resendOtp(@RequestParam String email, RedirectAttributes redirect) {
        redirect.addAttribute("email", email);
        try {
            authService.resendRegisterOtp(email);
            redirect.addFlashAttribute("success", "Đã gửi lại OTP.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", message(e));
        }
        return "redirect:/verify-otp";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto,
                                 BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/forgot-password";
        }
        try {
            authService.forgotPassword(dto.getEmail());
        } catch (RuntimeException e) {
            result.reject("forgot", message(e));
            return "auth/forgot-password";
        }
        redirect.addAttribute("email", dto.getEmail());
        redirect.addFlashAttribute("success", "Đã gửi OTP đặt lại mật khẩu.");
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(defaultValue = "") String email, Model model) {
        var dto = new ResetPasswordDTO();
        dto.setEmail(email);
        model.addAttribute("resetPasswordDTO", dto);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO dto,
                                BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "auth/reset-password";
        }
        try {
            if (!authService.resetPassword(dto)) {
                result.reject("otp", INVALID_OTP);
                return "auth/reset-password";
            }
        } catch (IllegalArgumentException e) {
            result.reject("reset", e.getMessage());
            return "auth/reset-password";
        }
        redirect.addFlashAttribute("success", "Đổi mật khẩu thành công. Hãy đăng nhập lại.");
        return "redirect:/login";
    }

    // Lỗi nghiệp vụ hiện nguyên văn; lỗi gửi mail thì hiện thông báo chung
    private String message(RuntimeException e) {
        return e instanceof IllegalArgumentException
                ? e.getMessage()
                : "Không thể gửi OTP. Kiểm tra cấu hình email hoặc thử lại sau.";
    }
}
