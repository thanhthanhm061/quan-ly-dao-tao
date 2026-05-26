package com.qldt.controller;

import com.qldt.model.*;
import com.qldt.model.enums.*;
import com.qldt.repository.NguoiDungRepository;
import com.qldt.repository.NhanVienRepository;
import com.qldt.repository.SinhVienRepository;
import com.qldt.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.util.*;

import java.util.Arrays;
import java.util.List;

// =====================================================================
// AUTH CONTROLLER
// =====================================================================
@Controller
@RequiredArgsConstructor
class AuthController {
        private final NguoiDungRepository nguoiDungRepo;
        private final NhanVienService nhanVienService;

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/access-denied")
    public String accessDenied() { return "auth/access-denied"; }

    @GetMapping("/")
    public String home(Authentication auth) {
        if (auth == null) return "redirect:/login";
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN"     -> "redirect:/admin/dashboard";
            case "ROLE_NHAN_VIEN" -> "redirect:/nhanvien/redirect";
            case "ROLE_SINH_VIEN" -> "redirect:/sinhvien/dashboard";
            default               -> "redirect:/login";
        };
    }

    /**
     * Redirect nhân viên vào đúng portal dựa trên chức vụ:
     * - Giảng viên  → /giangvien/dashboard
     * - Nhân viên hành chính → /nhanvien/dashboard
     */
    @GetMapping("/nhanvien/redirect")
    public String redirectNhanVien(Authentication auth) {
        NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        NhanVien nv  = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);

        if (nv != null && nv.isGiangVien()) {
            return "redirect:/giangvien/dashboard";
        }
        return "redirect:/nhanvien/dashboard";
    }
}

// =====================================================================
// ADMIN DASHBOARD CONTROLLER
// =====================================================================
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class AdminController {
    private final SinhVienService svService;
    private final NhanVienService nhanVienService;   // thay GiangVienService
    private final MonHocService monService;
    private final LopService lopService;
    private final LopHocPhanService lhpService;
    private final NguoiDungService nguoiDungService;
    private final NguoiDungRepository nguoiDungRepo;
    private final SinhVienRepository sinhVienRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("tongSV",  svService.count());
        model.addAttribute("tongGV",  nhanVienService.countGiangVien()); // đổi
        model.addAttribute("tongMon", monService.count());
        model.addAttribute("tongLop", lopService.count());
        model.addAttribute("tongLHP", lhpService.count());
        return "admin/dashboard";
    }

    /**
     * Tạo / liên kết tài khoản cho GIẢNG VIÊN (NhanVien có isGiangVien() == true).
     * Thay thế /admin/fix-accounts cũ.
     */
    @GetMapping("/fix-accounts")
    public String fixAccounts(RedirectAttributes ra) {
        List<NhanVien> gvList = nhanVienService.findAllGiangVien();
        int taoMoi = 0, lienKet = 0;

        for (NhanVien nv : gvList) {
            if (nv.getNguoiDung() == null) {
                String username = nv.getMaNhanVien().toLowerCase();
                Optional<NguoiDung> existing = nguoiDungRepo.findByUsername(username);

                if (existing.isPresent()) {
                    nv.setNguoiDung(existing.get());
                    lienKet++;
                } else {
                    NguoiDung nd = nguoiDungService.taoTaiKhoan(
                            username,
                            nv.getHoTen(),
                            nv.getEmail(),
                            VaiTro.NHAN_VIEN   // không còn GIANG_VIEN
                    );
                    nv.setNguoiDung(nd);
                    taoMoi++;
                }
                nhanVienService.save(nv);
            }
        }

        ra.addFlashAttribute("success",
                "GV - Tạo mới: " + taoMoi
                        + " | Liên kết lại: " + lienKet
                        + " tài khoản. Mật khẩu: Admin@123");
        return "redirect:/admin/dashboard";
    }

    /**
     * Tạo / liên kết tài khoản cho SINH VIÊN — giữ nguyên logic cũ.
     */
    @GetMapping("/fix-sv-accounts")
    public String fixSvAccounts(RedirectAttributes ra) {
        List<SinhVien> svList = svService.findAll();
        int taoMoi = 0, lienKet = 0;

        for (SinhVien sv : svList) {
            if (sv.getNguoiDung() == null) {
                String username = sv.getMaSv().toLowerCase();
                Optional<NguoiDung> existing = nguoiDungRepo.findByUsername(username);

                if (existing.isPresent()) {
                    sv.setNguoiDung(existing.get());
                    lienKet++;
                } else {
                    NguoiDung nd = nguoiDungService.taoTaiKhoan(
                            username,
                            sv.getHoTen(),
                            sv.getEmail(),
                            VaiTro.SINH_VIEN
                    );
                    sv.setNguoiDung(nd);
                    taoMoi++;
                }
                sinhVienRepo.save(sv);
            }
        }

        ra.addFlashAttribute("success",
                "SV - Tạo mới: " + taoMoi
                        + " | Liên kết lại: " + lienKet
                        + " tài khoản. Mật khẩu: Admin@123");
        return "redirect:/admin/dashboard";
    }
}

/// =====================================================================
// SINH VIEN CONTROLLER (ADMIN manages)
// =====================================================================
@Controller
@RequestMapping("/admin/sinh-vien")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class SinhVienController {

    private final SinhVienService svService;
    private final LopService lopService;

    private void addFormData(Model model) {
        List<Lop> lops = lopService.findAll();
        model.addAttribute("lops", lops != null ? lops : Collections.emptyList());
        model.addAttribute("gioiTinhs", GioiTinh.values());
        model.addAttribute("trangThais", TrangThaiSV.values());
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("sinhViens",
                search != null ? svService.search(search) : svService.findAll());
        model.addAttribute("search", search);
        model.addAttribute("tongSo", svService.count());
        return "sinhvien/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        SinhVien sv = new SinhVien();
        sv.setLop(new Lop());
        model.addAttribute("sinhVien", sv);
        addFormData(model);
        return "sinhvien/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute SinhVien sv,
                       BindingResult result,
                       Model model,
                       RedirectAttributes ra) {

        if (sv.getLop() == null) sv.setLop(new Lop());

        if (result.hasErrors()) {
            addFormData(model);
            model.addAttribute("sinhVien", sv);
            return "sinhvien/form";
        }

        try {
            svService.save(sv);
            ra.addFlashAttribute("success",
                    "Thêm sinh viên thành công! Tài khoản: "
                            + sv.getMaSv().toLowerCase() + " / Sv@" + sv.getMaSv());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/sinh-vien";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        SinhVien sv = svService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));
        if (sv.getLop() == null) sv.setLop(new Lop());
        model.addAttribute("sinhVien", sv);
        addFormData(model);
        return "sinhvien/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id,
                      @Valid @ModelAttribute SinhVien sv,
                      BindingResult result,
                      Model model,
                      RedirectAttributes ra) {

        if (sv.getLop() == null) sv.setLop(new Lop());

        if (result.hasErrors()) {
            addFormData(model);
            model.addAttribute("sinhVien", sv);
            return "sinhvien/form";
        }

        try {
            sv.setId(id);
            svService.save(sv);
            ra.addFlashAttribute("success", "Cập nhật sinh viên thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/sinh-vien";
    }
    @GetMapping("/chi-tiet/{id}")
    public String chiTietSinhVien(@PathVariable Long id, Model model) {
        SinhVien sv = svService.findByIdWithLopAndCvht(id)  // đổi ở đây
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));
        model.addAttribute("sv", sv);
        return "sinhvien/chi-tiet";
    }
    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            svService.delete(id);
            ra.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sinh-vien";
    }
}
// =====================================================================
// GIANG VIEN CONTROLLER
// =====================================================================
@Controller
@RequestMapping("/admin/giang-vien")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class AdminGiangVienController {

    private final NhanVienService nhanVienService;
    private final KhoaService khoaService;

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        List<NhanVien> ds = (search != null && !search.isBlank())
                ? nhanVienService.searchGiangVien(search)
                : nhanVienService.findAllGiangVien();
        model.addAttribute("giangViens", ds);
        model.addAttribute("search",     search);
        model.addAttribute("tongSo",     nhanVienService.countGiangVien());
        return "giangvien/list";
    }

    @GetMapping("/chi-tiet/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("gv", nhanVienService.getChiTietGiangVien(id));
        return "giangvien/chi-tiet";
    }

    /**
     * Thêm / sửa giảng viên → chuyển hướng sang trang nhân viên
     * vì giảng viên chỉ là nhân viên với chức vụ phù hợp.
     */
    @GetMapping("/them")
    public String them() {
        return "redirect:/admin/nhan-vien/them";
    }

    @GetMapping("/sua/{id}")
    public String sua(@PathVariable Long id) {
        return "redirect:/admin/nhan-vien/sua/" + id;
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            nhanVienService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa giảng viên!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/giang-vien";
    }
}
