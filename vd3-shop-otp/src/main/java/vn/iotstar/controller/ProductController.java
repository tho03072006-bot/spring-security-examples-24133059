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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Danh sách có tìm kiếm theo tên/mô tả và phân trang
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        var products = productService.findAll(keyword, page, size);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", products.getSize());
        return "products/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("mode", "create");
        return "products/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("productDTO") ProductDTO dto, BindingResult result,
                         @RequestParam(required = false) MultipartFile image,
                         Model model, RedirectAttributes redirect) {
        return save(null, dto, result, image, model, redirect);
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("productDTO", productService.findById(id));
        model.addAttribute("mode", "edit");
        return "products/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("productDTO") ProductDTO dto, BindingResult result,
                       @RequestParam(required = false) MultipartFile image,
                       Model model, RedirectAttributes redirect) {
        dto.setId(id);
        return save(id, dto, result, image, model, redirect);
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            productService.delete(id);
            redirect.addFlashAttribute("success", "Xóa sản phẩm thành công.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }

    // Dùng chung cho thêm (id == null) và sửa
    private String save(Long id, ProductDTO dto, BindingResult result, MultipartFile image,
                        Model model, RedirectAttributes redirect) {
        boolean creating = id == null;
        model.addAttribute("mode", creating ? "create" : "edit");
        if (!creating) {
            // Giữ ảnh hiện tại để form hiển thị lại khi có lỗi
            dto.setImageUrl(productService.findById(id).getImageUrl());
        }
        if (result.hasErrors()) {
            return "products/form";
        }
        try {
            if (creating) {
                productService.create(dto, image);
            } else {
                productService.update(id, dto, image);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            result.reject("product", e.getMessage());
            return "products/form";
        }
        redirect.addFlashAttribute("success", creating ? "Tạo sản phẩm thành công." : "Cập nhật sản phẩm thành công.");
        return "redirect:/products";
    }
}
