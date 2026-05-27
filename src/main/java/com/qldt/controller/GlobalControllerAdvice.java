package com.qldt.controller;

import com.qldt.model.NguoiDung;
import com.qldt.model.NhanVien;
import com.qldt.model.enums.VaiTro;
import com.qldt.repository.NguoiDungRepository;
import com.qldt.repository.NhanVienRepository;
import com.qldt.service.DonNghiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final DonNghiService donNghiService;
    private final NhanVienRepository nhanVienRepo;
    private final NguoiDungRepository nguoiDungRepo;
    // Helper method trong controller hoặc tách ra util
    private boolean isQuanLy(NhanVien nv) {
        if (nv == null || nv.getChucVu() == null) return false;
        String ma = nv.getChucVu().getMaChucVu().toUpperCase();
        return ma.equals("TK") || ma.equals("PTK") || ma.equals("TBM") || ma.equals("CNTT");
    }
    /**
     * Đếm số đơn nghỉ đang chờ duyệt — hiển thị badge sidebar
     */
    @ModelAttribute("soDonChoDuyet")
    public long soDonChoDuyet(Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return 0;
        }

        boolean coQuyenDuyet = auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String role = a.getAuthority();
                    return role.equals("ROLE_ADMIN")
                            || role.equals("ROLE_TK")
                            || role.equals("ROLE_PTK")
                            || role.equals("ROLE_TBM")
                            || role.equals("ROLE_CNTT");
                });

        if (!coQuyenDuyet) {
            return 0;
        }

        try {
            return donNghiService.demChoDuyet();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Truyền nhân viên hiện tại vào mọi view
     */
    @ModelAttribute("nhanVienHienTai")
    public NhanVien nhanVienHienTai(Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        try {
            return nhanVienRepo.findByNguoiDungUsername(auth.getName())
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Active page + truyền ID nhân viên + quyền quản lý
     */
    @ModelAttribute
    public void addActivePageAttribute(
            HttpServletRequest request,
            Authentication auth,
            Model model) {

        // active page
        String uri = request.getRequestURI();
        model.addAttribute("activePage", uri);

        // thông tin nhân viên hiện tại
        if (auth != null && auth.isAuthenticated()) {

            NguoiDung nd = nguoiDungRepo
                    .findByUsername(auth.getName())
                    .orElse(null);

            if (nd != null && nd.getVaiTro() == VaiTro.NHAN_VIEN) {

                NhanVien nv = nhanVienRepo
                        .findByNguoiDungId(nd.getId())
                        .orElse(null);

                if (nv != null) {

                    model.addAttribute("currentNvId", nv.getId());

                    boolean isQuanLy =
                            nv.getChucVu() != null
                                    && List.of("TK", "PTK", "TBM" , "CNTT", "ADMIN")
                                    .contains(
                                            nv.getChucVu()
                                                    .getMaChucVu()
                                                    .toUpperCase()
                                    );

                    model.addAttribute("isQuanLy", isQuanLy(nv));

                }
            }
        }
    }
}