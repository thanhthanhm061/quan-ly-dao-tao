package com.qldt.controller;

import com.qldt.model.NhanVien;
import com.qldt.model.NhanVienChucVu;
import com.qldt.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/nhan-vien/{nhanVienId}/chuc-vu")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NhanVienChucVuController {

    private final NhanVienService nhanVienService;
    private final NhanVienChucVuService nvcvService;
    private final ChucVuService chucVuService;

    @GetMapping("/them")
    public String themForm(@PathVariable Long nhanVienId, Model model) {
        NhanVien nv = nhanVienService.findById(nhanVienId).orElseThrow();
        NhanVienChucVu nvcv = new NhanVienChucVu();
        nvcv.setNhanVien(nv);
        model.addAttribute("nvcv", nvcv);
        model.addAttribute("nhanVien", nv);
        model.addAttribute("dsChucVu", chucVuService.findAllHoatDong());
        model.addAttribute("tieuDe", "Thêm Lịch Sử Chức Vụ");
        return "nhan-vien-chuc-vu/them-sua";
    }

    @PostMapping("/them")
    public String them(@PathVariable Long nhanVienId,
                       @Valid @ModelAttribute("nvcv") NhanVienChucVu nvcv,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("nhanVien", nhanVienService.findById(nhanVienId).orElseThrow());
            model.addAttribute("dsChucVu", chucVuService.findAllHoatDong());
            model.addAttribute("tieuDe", "Thêm Lịch Sử Chức Vụ");
            return "nhan-vien-chuc-vu/them-sua";
        }
        try {
            NhanVien nv = nhanVienService.findById(nhanVienId).orElseThrow();
            nvcv.setNhanVien(nv);
            nvcvService.save(nvcv);
            ra.addFlashAttribute("thanhCong", "Đã thêm chức vụ thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/nhan-vien/chi-tiet/" + nhanVienId;
    }

    @PostMapping("/{id}/ket-thuc")
    public String ketThuc(@PathVariable Long nhanVienId,
                          @PathVariable Long id, RedirectAttributes ra) {
        try {
            nvcvService.ketThucChucVu(id);
            ra.addFlashAttribute("thanhCong", "Đã kết thúc chức vụ!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/nhan-vien/chi-tiet/" + nhanVienId;
    }

    @PostMapping("/{id}/xoa")
    public String xoa(@PathVariable Long nhanVienId,
                      @PathVariable Long id, RedirectAttributes ra) {
        try {
            nvcvService.delete(id);
            ra.addFlashAttribute("thanhCong", "Đã xóa bản ghi chức vụ!");
        } catch (Exception e) {
            ra.addFlashAttribute("loiLam", e.getMessage());
        }
        return "redirect:/admin/nhan-vien/chi-tiet/" + nhanVienId;
    }
}
