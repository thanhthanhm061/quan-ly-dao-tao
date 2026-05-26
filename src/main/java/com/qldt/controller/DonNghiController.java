package com.qldt.controller;

import com.qldt.model.DonNghi;
import com.qldt.model.NhanVien;
import com.qldt.model.enums.TrangThaiDonNghi;
import com.qldt.repository.NhanVienRepository;
import com.qldt.service.DonNghiService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/don-nghi")
@RequiredArgsConstructor
public class DonNghiController {

    private final DonNghiService donNghiService;
    private final NhanVienRepository nhanVienRepo;

    /* =========================================================
       HELPER — lấy NhanVien từ session
    ========================================================= */
    private NhanVien getNhanVienHienTai(Authentication auth) {
        String username = auth.getName();
        return nhanVienRepo.findByNguoiDungUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy hồ sơ nhân viên cho tài khoản: " + username));
    }

    /* =========================================================
       DANH SÁCH — nhân viên xem đơn của mình
    ========================================================= */
    @GetMapping
    public String danhSach(Model model, Authentication auth) {

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            model.addAttribute("danhSach", donNghiService.findAll());
            model.addAttribute("nhanVien", null);
        } else {
            NhanVien nv = getNhanVienHienTai(auth);
            model.addAttribute("danhSach", donNghiService.findByNguoiNop(nv.getId()));
            model.addAttribute("nhanVien", nv);
        }

        model.addAttribute("trangThai", TrangThaiDonNghi.values());
        model.addAttribute("activePage", "don-nghi");
        return "don-nghi/danh-sach";
    }

    /* =========================================================
       DANH SÁCH DUYỆT — TK/PTK/TBM/Admin xem đơn chờ duyệt
    ========================================================= */
    @GetMapping("/duyet")
    public String danhSachDuyet(
            @RequestParam(required = false) TrangThaiDonNghi trangThai,
            Model model, Authentication auth) {

        List<DonNghi> danhSach;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            danhSach = (trangThai != null)
                    ? donNghiService.findByTrangThai(trangThai)
                    : donNghiService.findAll();
        } else {
            NhanVien nv = getNhanVienHienTai(auth); // chỉ gọi khi không phải admin
            Long khoaId = nv.getKhoa() != null ? nv.getKhoa().getId() : null;
            if (khoaId == null) {
                model.addAttribute("error", "Bạn chưa được gán vào khoa nào.");
                model.addAttribute("danhSach", List.of());
                return "don-nghi/duyet";
            }
            danhSach = (trangThai != null && trangThai != TrangThaiDonNghi.CHO_DUYET)
                    ? donNghiService.findByTrangThai(trangThai)
                    : donNghiService.findChoDuyetTheoKhoa(khoaId);
        }

        model.addAttribute("danhSach", danhSach);
        model.addAttribute("trangThaiList", TrangThaiDonNghi.values());
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("activePage", "don-nghi-duyet");
        return "don-nghi/duyet";
    }

    /* =========================================================
       FORM TẠO ĐƠN
    ========================================================= */
    @GetMapping("/tao")
    public String formTao(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBatDau,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKetThuc,
            Model model, Authentication auth, RedirectAttributes ra) {

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            ra.addFlashAttribute("error", "Tài khoản admin không thể tạo đơn xin nghỉ.");
            return "redirect:/don-nghi";
        }

        NhanVien nv = getNhanVienHienTai(auth);
        model.addAttribute("nhanVien", nv);
        model.addAttribute("loaiNghiList", List.of(
                "Nghỉ phép", "Nghỉ bệnh", "Nghỉ không lương",
                "Nghỉ việc riêng", "Nghỉ thai sản"));

        // Pre-fill ngày nếu được truyền từ popup TKB
        model.addAttribute("ngayBatDauDefault",
                ngayBatDau != null ? ngayBatDau.toString() : "");
        model.addAttribute("ngayKetThucDefault",
                ngayKetThuc != null ? ngayKetThuc.toString() : "");

        model.addAttribute("activePage", "don-nghi");
        return "don-nghi/tao-don";
    }

    @PostMapping("/tao")
    public String taoMoi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBatDau,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKetThuc,
            @RequestParam String lyDo,
            @RequestParam(required = false) String loaiNghi,
            @RequestParam(required = false) String fileDinhKem,
            Authentication auth,
            RedirectAttributes ra) {

        try {
            NhanVien nv = getNhanVienHienTai(auth);
            donNghiService.taoMoi(nv.getId(), ngayBatDau, ngayKetThuc,
                    lyDo, loaiNghi, fileDinhKem);
            ra.addFlashAttribute("success", "Tạo đơn xin nghỉ thành công! Chờ phê duyệt.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/don-nghi/tao";
        }
        return "redirect:/don-nghi";
    }

    /* =========================================================
       CHI TIẾT
    ========================================================= */
    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Long id, Model model, Authentication auth) {

        DonNghi don = donNghiService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ"));

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Admin xem thẳng, không cần hồ sơ nhân viên
        if (isAdmin) {
            model.addAttribute("don", don);
            model.addAttribute("laNguoiNop", false);
            model.addAttribute("isAdmin", true);
            model.addAttribute("activePage", "don-nghi");
            return "don-nghi/chi-tiet";
        }

        NhanVien nv = getNhanVienHienTai(auth);
        boolean laNguoiNop = don.getNguoiNop().getId().equals(nv.getId());

        if (!laNguoiNop) {
            String maChucVu = nv.getChucVu() != null ? nv.getChucVu().getMaChucVu() : "";
            boolean cungKhoa = nv.getKhoa() != null
                    && don.getNguoiNop().getKhoa() != null
                    && nv.getKhoa().getId().equals(don.getNguoiNop().getKhoa().getId());
            if (!List.of("TK", "PTK", "TBM").contains(maChucVu) || !cungKhoa) {
                model.addAttribute("error", "Bạn không có quyền xem đơn này.");
                return "redirect:/don-nghi";
            }
        }

        model.addAttribute("don", don);
        model.addAttribute("laNguoiNop", laNguoiNop);
        model.addAttribute("isAdmin", false);
        model.addAttribute("activePage", "don-nghi");
        return "don-nghi/chi-tiet";
    }

    private void ra(Model model) {
        model.addAttribute("error", "Bạn không có quyền xem đơn này.");
    }

    /* =========================================================
       HỦY ĐƠN
    ========================================================= */
    @PostMapping("/{id}/huy")
    public String huy(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                ra.addFlashAttribute("error", "Admin không thực hiện thao tác này.");
                return "redirect:/don-nghi";
            }
            NhanVien nv = getNhanVienHienTai(auth);
            donNghiService.huy(id, nv.getId());
            ra.addFlashAttribute("success", "Đã hủy đơn nghỉ.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/don-nghi";
    }

    /* =========================================================
       DUYỆT
    ========================================================= */
    @PostMapping("/{id}/duyet")
    public String duyet(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            NhanVien nv = isAdmin ? null : getNhanVienHienTai(auth);
            donNghiService.duyet(id, nv != null ? nv.getId() : null);
            ra.addFlashAttribute("success", "Đã duyệt đơn nghỉ thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/don-nghi/duyet";
    }
    /* =========================================================
       TỪ CHỐI
    ========================================================= */
    @PostMapping("/{id}/tu-choi")
    public String tuChoi(@PathVariable Long id,
                         @RequestParam String lyDoTuChoi,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            NhanVien nv = isAdmin ? null : getNhanVienHienTai(auth);
            donNghiService.tuChoi(id, nv != null ? nv.getId() : null, lyDoTuChoi);
            ra.addFlashAttribute("success", "Đã từ chối đơn nghỉ.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/don-nghi/duyet";
    }
}