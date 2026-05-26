package com.qldt.interceptor;

import com.qldt.model.NguoiDung;
import com.qldt.model.NhanVien;
import com.qldt.repository.NguoiDungRepository;
import com.qldt.service.NhanVienService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private final NguoiDungRepository nguoiDungRepo;
    private final NhanVienService nhanVienService;

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {

        if (modelAndView == null) return;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) return;

        // Inject currentUri để active menu
        modelAndView.addObject("currentUri", request.getRequestURI());

        // Chỉ xử lý NHAN_VIEN — ADMIN và SINH_VIEN không cần isGiangVien
        boolean isNhanVien = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_NHAN_VIEN"));

        if (!isNhanVien) return;

        try {
            NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElse(null);
            if (nd == null) return;

            NhanVien nv = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);

            // isGiangVien = true  → hiện menu giảng viên
            // isGiangVien = false → hiện menu nhân viên hành chính
            boolean isGiangVien = (nv != null && nv.isGiangVien());
            modelAndView.addObject("isGiangVien", isGiangVien);

        } catch (Exception ignored) {
            modelAndView.addObject("isGiangVien", false);
        }
    }
}