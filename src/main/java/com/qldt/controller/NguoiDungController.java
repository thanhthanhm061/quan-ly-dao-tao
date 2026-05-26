package com.qldt.controller;


import com.qldt.model.NguoiDung;
import com.qldt.model.enums.VaiTro;
import com.qldt.service.DoiMatKhauDTO;
import com.qldt.service.NguoiDungService;
import com.qldt.service.TaoNguoiDungDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/nguoi-dung")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NguoiDungController {

    private final NguoiDungService nguoiDungService;

    private void addFormData(Model model) {
        model.addAttribute("dsVaiTro", VaiTro.values());
    }

    // ── Danh sách ──────────────────────────────────────────────
    @GetMapping
    public String danhSach(@RequestParam(required = false) String search,
                           @RequestParam(required = false) VaiTro vaiTro,
                           Model model) {
        var ds = search != null    ? nguoiDungService.timKiem(search)
                : vaiTro != null    ? nguoiDungService.findByVaiTro(vaiTro)
                : nguoiDungService.findAll();

        model.addAttribute("dsNguoiDung", ds);
        model.addAttribute("timKiem", search);
        model.addAttribute("vaiTroLoc", vaiTro);
        model.addAttribute("dsVaiTro", VaiTro.values());
        model.addAttribute("thongKe", nguoiDungService.thongKe());
        return "nguoi-dung/danh-sach";
    }

    // ── Thêm mới ───────────────────────────────────────────────
    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("dto", new TaoNguoiDungDTO());
        model.addAttribute("tieuDe", "Thêm Người Dùng Mới");
        addFormData(model);
        return "nguoi-dung/them-sua";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute("dto") TaoNguoiDungDTO dto,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("tieuDe", "Thêm Người Dùng Mới");
            addFormData(model);
            return "nguoi-dung/them-sua";
        }
        try {
            nguoiDungService.taoTuForm(dto);
            ra.addFlashAttribute("success",
                    "Tạo tài khoản '" + dto.getUsername() + "' thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/nguoi-dung";
    }

    // ── Sửa thông tin ──────────────────────────────────────────
    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        NguoiDung nd = nguoiDungService.findById(id).orElseThrow();
        model.addAttribute("nguoiDung", nd);
        model.addAttribute("tieuDe", "Chỉnh Sửa: " + nd.getUsername());
        addFormData(model);
        return "nguoi-dung/them-sua";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id,
                      @Valid @ModelAttribute("nguoiDung") NguoiDung form,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("tieuDe", "Chỉnh Sửa Người Dùng");
            addFormData(model);
            return "nguoi-dung/them-sua";
        }
        try {
            nguoiDungService.capNhat(id, form);
            ra.addFlashAttribute("success", "Cập nhật tài khoản thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/nguoi-dung";
    }

    // ── Chi tiết ───────────────────────────────────────────────
    @GetMapping("/chi-tiet/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        NguoiDung nd = nguoiDungService.findById(id).orElseThrow();
        model.addAttribute("nguoiDung", nd);
        model.addAttribute("doiMatKhauDTO", new DoiMatKhauDTO());
        return "nguoi-dung/chi-tiet";
    }

    // ── Đặt lại mật khẩu (Admin) ───────────────────────────────
    @PostMapping("/dat-lai-mat-khau/{id}")
    public String datLaiMatKhau(@PathVariable Long id,
                                @RequestParam String matKhauMoi,
                                RedirectAttributes ra) {
        try {
            if (matKhauMoi == null || matKhauMoi.length() < 6)
                throw new IllegalArgumentException("Mật khẩu phải ít nhất 6 ký tự");
            nguoiDungService.datLaiMatKhau(id, matKhauMoi);
            ra.addFlashAttribute("success", "Đã đặt lại mật khẩu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/nguoi-dung/chi-tiet/" + id;
    }

    // ── Đổi trạng thái (kích hoạt / tắt) ──────────────────────
    @PostMapping("/doi-trang-thai/{id}")
    public String doiTrangThai(@PathVariable Long id, RedirectAttributes ra) {
        try {
            nguoiDungService.doiTrangThai(id);
            ra.addFlashAttribute("success", "Đã cập nhật trạng thái tài khoản!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/nguoi-dung";
    }

    // ── Khóa tài khoản có thời hạn ─────────────────────────────
    @PostMapping("/khoa/{id}")
    public String khoaTaiKhoan(@PathVariable Long id,
                               @RequestParam(defaultValue = "24") int soGio,
                               RedirectAttributes ra) {
        try {
            nguoiDungService.khoaTaiKhoan(id, soGio);
            ra.addFlashAttribute("success",
                    soGio <= 0 ? "Đã khóa tài khoản vĩnh viễn!"
                            : "Đã khóa tài khoản trong " + soGio + " giờ!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/nguoi-dung";
    }

    // ── Mở khóa ────────────────────────────────────────────────
    @PostMapping("/mo-khoa/{id}")
    public String moKhoaTaiKhoan(@PathVariable Long id, RedirectAttributes ra) {
        try {
            nguoiDungService.moKhoaTaiKhoan(id);
            ra.addFlashAttribute("success", "Đã mở khóa tài khoản!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/nguoi-dung";
    }

    // ── Xóa ────────────────────────────────────────────────────
    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            nguoiDungService.xoa(id);
            ra.addFlashAttribute("success", "Đã xóa tài khoản thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/nguoi-dung";
    }
}
