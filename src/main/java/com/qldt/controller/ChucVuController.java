package com.qldt.controller;

import com.qldt.model.ChucVu;
import com.qldt.service.ChucVuService;
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
@RequestMapping("/admin/chuc-vu")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class ChucVuController {

    private final ChucVuService chucVuService;
    private final KhoaService khoaService;

    private void addFormData(Model model) {
        model.addAttribute("dsKhoa", khoaService.findAll());
        model.addAttribute("dsCapBac", new String[]{
                "Cấp trường", "Cấp khoa", "Cấp bộ môn", "Nhân viên"
        });
    }

    @GetMapping
    public String danhSach(@RequestParam(required = false) String search, Model model) {
        var ds = search != null ? chucVuService.timKiem(search) : chucVuService.findAll();
        model.addAttribute("dsChucVu", ds);
        model.addAttribute("timKiem", search);
        model.addAttribute("tongSo", chucVuService.count());
        return "chuc-vu/danh-sach";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("chucVu", new ChucVu());
        model.addAttribute("tieuDe", "Thêm Chức Vụ Mới");
        addFormData(model);
        return "chuc-vu/them-sua";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute ChucVu chucVu, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("tieuDe", "Thêm Chức Vụ Mới");
            addFormData(model);
            return "chuc-vu/them-sua";
        }
        try {
            chucVuService.save(chucVu);
            ra.addFlashAttribute("thanhCong", "Thêm chức vụ '" + chucVu.getTenChucVu() + "' thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/chuc-vu";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("chucVu", chucVuService.findById(id).orElseThrow());
        model.addAttribute("tieuDe", "Chỉnh Sửa Chức Vụ");
        addFormData(model);
        return "chuc-vu/them-sua";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @Valid @ModelAttribute ChucVu chucVu,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("tieuDe", "Chỉnh Sửa Chức Vụ");
            addFormData(model);
            return "chuc-vu/them-sua";
        }
        try {
            chucVu.setId(id);
            chucVuService.save(chucVu);
            ra.addFlashAttribute("thanhCong", "Cập nhật chức vụ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/chuc-vu";
    }

    @GetMapping("/chi-tiet/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        model.addAttribute("chucVu", chucVuService.findById(id).orElseThrow());
        return "chuc-vu/chi-tiet";
    }

    @PostMapping("/doi-trang-thai/{id}")
    public String doiTrangThai(@PathVariable Long id, RedirectAttributes ra) {
        chucVuService.doiTrangThai(id);
        ra.addFlashAttribute("thanhCong", "Đã cập nhật trạng thái chức vụ!");
        return "redirect:/admin/chuc-vu";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            chucVuService.delete(id);
            ra.addFlashAttribute("thanhCong", "Đã xóa chức vụ!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/chuc-vu";
    }
}