package com.qldt.controller;

import com.qldt.model.Khoa;
import com.qldt.service.KhoaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/khoa")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class KhoaController {

    private final KhoaService khoaService;

    @GetMapping
    public String danhSach(@RequestParam(required = false) String search, Model model) {
        var dsKhoa = khoaService.findAll();
        long soHoatDong = dsKhoa.stream().filter(k -> Boolean.TRUE.equals(k.getTrangThai())).count();
        long tongNhanVien = dsKhoa.stream().mapToLong(k -> k.getSoNhanVien()).sum();

        model.addAttribute("dsKhoa", dsKhoa);
        model.addAttribute("tongSo", khoaService.count());
        model.addAttribute("soHoatDong", soHoatDong);
        model.addAttribute("soTamDung", dsKhoa.size() - soHoatDong);
        model.addAttribute("tongNhanVien", tongNhanVien);
        return "khoa/danh-sach";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("khoa", new Khoa());
        model.addAttribute("tieuDe", "Thêm Khoa Mới");
        return "khoa/them-sua";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute Khoa khoa, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("tieuDe", "Thêm Khoa Mới");
            return "khoa/them-sua";
        }
        try {
            khoaService.save(khoa);
            ra.addFlashAttribute("thanhCong", "Thêm khoa '" + khoa.getTenKhoa() + "' thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/khoa";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("khoa", khoaService.findById(id).orElseThrow());
        model.addAttribute("tieuDe", "Chỉnh Sửa Khoa");
        return "khoa/them-sua";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @Valid @ModelAttribute Khoa khoa,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("tieuDe", "Chỉnh Sửa Khoa");
            return "khoa/them-sua";
        }
        try {
            khoa.setId(id);
            khoaService.save(khoa);
            ra.addFlashAttribute("thanhCong", "Cập nhật khoa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/khoa";
    }

    @GetMapping("/chi-tiet/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        model.addAttribute("khoa", khoaService.findById(id).orElseThrow());
        return "khoa/chi-tiet";
    }

    @PostMapping("/doi-trang-thai/{id}")
    public String doiTrangThai(@PathVariable Long id, RedirectAttributes ra) {
        try {
            khoaService.doiTrangThai(id);
            ra.addFlashAttribute("thanhCong", "Đã cập nhật trạng thái khoa!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/khoa";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            khoaService.delete(id);
            ra.addFlashAttribute("thanhCong", "Đã xóa khoa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/khoa";
    }
}
