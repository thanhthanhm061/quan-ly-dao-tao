package com.qldt.controller;


import com.qldt.model.NguoiDung;
import com.qldt.service.DoiMatKhauDTO;
import com.qldt.service.NguoiDungService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tai-khoan")
@RequiredArgsConstructor
public class TaiKhoanController {

    private final NguoiDungService nguoiDungService;

    @GetMapping
    public String trangCaNhan(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        NguoiDung nd = nguoiDungService.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("nguoiDung", nd);
        model.addAttribute("doiMatKhauDTO", new DoiMatKhauDTO());
        return "tai-khoan/ca-nhan";
    }

    @PostMapping("/doi-mat-khau")
    public String doiMatKhau(@AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute DoiMatKhauDTO dto,
                             BindingResult result, Model model, RedirectAttributes ra) {
        NguoiDung nd = nguoiDungService.findByUsername(userDetails.getUsername()).orElseThrow();

        if (result.hasErrors()) {
            model.addAttribute("nguoiDung", nd);
            return "tai-khoan/ca-nhan";
        }
        try {
            nguoiDungService.doiMatKhau(nd.getId(), dto);
            ra.addFlashAttribute("thanhCong", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/tai-khoan";
    }
}