package vn.iotstar.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
@ControllerAdvice public class ExceptionAdvice {
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalid(IllegalArgumentException ex,Model model){model.addAttribute("message",ex.getMessage());return "error";}
    @ExceptionHandler(MaxUploadSizeExceededException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String upload(Model model){model.addAttribute("message","Ảnh tối đa 10 MB, tổng dữ liệu tối đa 20 MB.");return "error";}
}
