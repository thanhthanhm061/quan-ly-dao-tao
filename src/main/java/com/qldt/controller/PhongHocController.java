package com.qldt.controller;

import com.qldt.model.PhongHoc;
import com.qldt.model.enums.LoaiPhong;
import com.qldt.service.PhongHocService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/phong-hoc")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PhongHocController {

    private final PhongHocService phongHocService;

    @GetMapping
    public String danhSach(Model model) {

        List<PhongHoc> ds = phongHocService.findAll();

        model.addAttribute("danhSachPhong", ds);

        model.addAttribute("tongPhong", ds.size());

        long dangHoatDong = ds.stream()
                .filter(PhongHoc::isHoatDong)
                .count();

        model.addAttribute("dangHoatDong", dangHoatDong);

        int tongSucChua = ds.stream()
                .mapToInt(PhongHoc::getSucChua)
                .sum();

        model.addAttribute("tongSucChua", tongSucChua);

        long voHieu = ds.stream()
                .filter(p -> !p.isHoatDong())
                .count();

        model.addAttribute("voHieu", voHieu);

        return "phonghoc/danh-sach";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("phongHoc", new PhongHoc());
        model.addAttribute("danhSachLoai", LoaiPhong.values()); // ← sửa
        return "phonghoc/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute PhongHoc phongHoc, RedirectAttributes ra) {
        try {
            phongHocService.save(phongHoc);
            ra.addFlashAttribute("success", "Đã thêm phòng " + phongHoc.getMaPhong());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/phong-hoc";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        PhongHoc p = phongHocService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng"));
        model.addAttribute("phongHoc", p);
        model.addAttribute("danhSachLoai", LoaiPhong.values()); // ← sửa
        return "phonghoc/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id,
                      @ModelAttribute PhongHoc phongHoc,
                      RedirectAttributes ra) {
        try {
            phongHoc.setId(id);
            phongHocService.save(phongHoc);
            ra.addFlashAttribute("success", "Đã cập nhật phòng " + phongHoc.getMaPhong());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/phong-hoc";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            phongHocService.delete(id);
            ra.addFlashAttribute("success", "Đã vô hiệu hóa phòng học");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/phong-hoc";
    }
}