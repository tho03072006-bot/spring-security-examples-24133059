package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.service.ProductService;
@Controller @RequestMapping("/products") @RequiredArgsConstructor
public class ProductController {
    private final ProductService products;
    @GetMapping public String list(@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size,Model model){
        var data=products.findAll(keyword,page,size);model.addAttribute("products",data);model.addAttribute("keyword",keyword);model.addAttribute("size",data.getSize());return "products/list";
    }
    @GetMapping("/create") public String create(Model model){model.addAttribute("productDTO",new ProductDTO());model.addAttribute("mode","create");return "products/form";}
    @GetMapping("/edit/{id}") public String edit(@PathVariable Long id,Model model){model.addAttribute("productDTO",products.findById(id));model.addAttribute("mode","edit");return "products/form";}
    @PostMapping("/create") public String create(@Valid @ModelAttribute("productDTO") ProductDTO dto,BindingResult result,@RequestParam(required=false) MultipartFile image,Model model,RedirectAttributes redirect){return save(null,dto,result,image,model,redirect);}
    @PostMapping("/edit/{id}") public String edit(@PathVariable Long id,@Valid @ModelAttribute("productDTO") ProductDTO dto,BindingResult result,@RequestParam(required=false) MultipartFile image,Model model,RedirectAttributes redirect){dto.setId(id);return save(id,dto,result,image,model,redirect);}
    private String save(Long id,ProductDTO dto,BindingResult result,MultipartFile image,Model model,RedirectAttributes redirect){
        model.addAttribute("mode",id==null?"create":"edit");
        if(id!=null)dto.setImageUrl(products.findById(id).getImageUrl());
        if(result.hasErrors())return "products/form";
        try{if(id==null)products.create(dto,image);else products.update(id,dto,image);redirect.addFlashAttribute("success",id==null?"Tạo sản phẩm thành công.":"Cập nhật sản phẩm thành công.");return "redirect:/products";}
        catch(IllegalArgumentException|IllegalStateException e){result.reject("product",e.getMessage());return "products/form";}
    }
    @PostMapping("/delete/{id}") public String delete(@PathVariable Long id,RedirectAttributes redirect){
        try{products.delete(id);redirect.addFlashAttribute("success","Xóa sản phẩm thành công.");}
        catch(IllegalArgumentException|IllegalStateException e){redirect.addFlashAttribute("error",e.getMessage());}
        return "redirect:/products";
    }
}
