package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.service.UserService;
@Controller @RequestMapping("/users") @RequiredArgsConstructor
public class UserController {
    private final UserService users;
    @GetMapping public String list(@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size,Model model){
        var data=users.findAll(keyword,page,size);model.addAttribute("users",data);model.addAttribute("keyword",keyword);model.addAttribute("size",data.getSize());return "users/list";
    }
    @GetMapping("/create") public String create(Model model){var dto=new UserDTO();dto.setEnabled(true);dto.setRoleName("ROLE_USER");model.addAttribute("userDTO",dto);model.addAttribute("mode","create");return "users/form";}
    @GetMapping("/edit/{id}") public String edit(@PathVariable Long id,Model model){model.addAttribute("userDTO",users.findById(id));model.addAttribute("mode","edit");return "users/form";}
    @PostMapping("/create") public String create(@Valid @ModelAttribute("userDTO") UserDTO dto,BindingResult result,Model model,RedirectAttributes redirect){return save(null,dto,result,model,redirect);}
    @PostMapping("/edit/{id}") public String edit(@PathVariable Long id,@Valid @ModelAttribute("userDTO") UserDTO dto,BindingResult result,Model model,RedirectAttributes redirect){dto.setId(id);return save(id,dto,result,model,redirect);}
    private String save(Long id,UserDTO dto,BindingResult result,Model model,RedirectAttributes redirect){
        model.addAttribute("mode",id==null?"create":"edit");
        if(result.hasErrors())return "users/form";
        try{if(id==null)users.create(dto);else users.update(id,dto);redirect.addFlashAttribute("success",id==null?"Tạo user thành công. Mật khẩu mặc định: 123456":"Cập nhật user thành công.");return "redirect:/users";}
        catch(IllegalArgumentException e){result.reject("user",e.getMessage());return "users/form";}
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable Long id,RedirectAttributes redirect){
        try{users.delete(id);redirect.addFlashAttribute("success","Xóa user và sản phẩm của user thành công.");}
        catch(RuntimeException e){redirect.addFlashAttribute("error",e instanceof IllegalArgumentException?e.getMessage():"Không thể xóa user. Kiểm tra kết nối lưu trữ ảnh.");}
        return "redirect:/users";
    }
}
