package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.service.UserService;

// Quản lý user, chỉ ADMIN (chặn ở SecurityConfig và @PreAuthorize trong service)
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Danh sách có tìm kiếm theo username/email/họ tên và phân trang
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        var users = userService.findAll(keyword, page, size);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", users.getSize());
        return "users/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        var dto = new UserDTO();
        dto.setEnabled(true);
        dto.setRoleName("ROLE_USER");
        model.addAttribute("userDTO", dto);
        model.addAttribute("mode", "create");
        return "users/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("userDTO") UserDTO dto, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        return save(null, dto, result, model, redirect);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("userDTO", userService.findById(id));
        model.addAttribute("mode", "edit");
        return "users/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("userDTO") UserDTO dto,
                       BindingResult result, Model model, RedirectAttributes redirect) {
        dto.setId(id);
        return save(id, dto, result, model, redirect);
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            userService.delete(id);
            redirect.addFlashAttribute("success", "Xóa user và sản phẩm của user thành công.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", "Không thể xóa user. Kiểm tra kết nối lưu trữ ảnh.");
        }
        return "redirect:/users";
    }

    // Dùng chung cho thêm (id == null) và sửa
    private String save(Long id, UserDTO dto, BindingResult result, Model model, RedirectAttributes redirect) {
        boolean creating = id == null;
        model.addAttribute("mode", creating ? "create" : "edit");
        if (result.hasErrors()) {
            return "users/form";
        }
        try {
            if (creating) {
                userService.create(dto);
            } else {
                userService.update(id, dto);
            }
        } catch (IllegalArgumentException e) {
            result.reject("user", e.getMessage());
            return "users/form";
        }
        redirect.addFlashAttribute("success",
                creating ? "Tạo user thành công. Mật khẩu mặc định: 123456" : "Cập nhật user thành công.");
        return "redirect:/users";
    }
}
