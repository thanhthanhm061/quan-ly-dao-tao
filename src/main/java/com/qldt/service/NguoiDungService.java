package com.qldt.service;

import com.qldt.model.NguoiDung;
import com.qldt.model.enums.VaiTro;
import com.qldt.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class NguoiDungService {

    private final NguoiDungRepository repo;
    private final PasswordEncoder passwordEncoder;

    // -- Tạo tài khoản nhanh (dùng trong GiangVienService, NhanVienService) --
    public NguoiDung taoTaiKhoan(String username, String hoTen, String email, VaiTro vaiTro) {
        return repo.findByUsername(username)
                .orElseGet(() -> {
                    NguoiDung nd = NguoiDung.builder()
                            .username(username)
                            .matKhau(passwordEncoder.encode("Admin@123"))
                            .hoTen(hoTen)
                            .email(email != null ? email : username + "@qldt.edu.vn")
                            .vaiTro(vaiTro)
                            .kichHoat(true)
                            .build();
                    return repo.save(nd);
                });
    }

    // -- Tạo tài khoản từ form Admin --
    public NguoiDung taoTuForm(TaoNguoiDungDTO dto) {
        if (repo.existsByUsername(dto.getUsername()))
            throw new IllegalArgumentException("Tên đăng nhập '" + dto.getUsername() + "' đã tồn tại");
        if (dto.getEmail() != null && !dto.getEmail().isBlank() && repo.existsByEmail(dto.getEmail()))
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' đã được sử dụng");

        NguoiDung nd = NguoiDung.builder()
                .username(dto.getUsername())
                .matKhau(passwordEncoder.encode(dto.getMatKhau()))
                .hoTen(dto.getHoTen())
                .email(dto.getEmail())
                .soDienThoai(dto.getSoDienThoai())
                .vaiTro(dto.getVaiTro())
                .ghiChu(dto.getGhiChu())
                .kichHoat(dto.isKichHoat())
                .build();
        return repo.save(nd);
    }

    // -- CRUD cơ bản --
    @Transactional(readOnly = true)
    public List<NguoiDung> findAll() { return repo.findAllByOrderByNgayTaoDesc(); }

    @Transactional(readOnly = true)
    public List<NguoiDung> timKiem(String kw) {
        return kw == null || kw.isBlank() ? findAll() : repo.timKiem(kw);
    }

    @Transactional(readOnly = true)
    public List<NguoiDung> findByVaiTro(VaiTro vaiTro) {
        return repo.findByVaiTroOrderByHoTen(vaiTro);
    }

    @Transactional(readOnly = true)
    public Optional<NguoiDung> findById(Long id) { return repo.findById(id); }

    @Transactional(readOnly = true)
    public Optional<NguoiDung> findByUsername(String username) { return repo.findByUsername(username); }

    public NguoiDung capNhat(Long id, NguoiDung form) {
        NguoiDung nd = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // Kiểm tra email trùng (trừ chính nó)
        if (form.getEmail() != null && !form.getEmail().equals(nd.getEmail())
                && repo.existsByEmail(form.getEmail()))
            throw new IllegalArgumentException("Email '" + form.getEmail() + "' đã được sử dụng");

        nd.setHoTen(form.getHoTen());
        nd.setEmail(form.getEmail());
        nd.setSoDienThoai(form.getSoDienThoai());
        nd.setVaiTro(form.getVaiTro());
        nd.setGhiChu(form.getGhiChu());
        nd.setKichHoat(form.isKichHoat());
        return repo.save(nd);
    }

    // -- Đổi mật khẩu (Admin đặt lại) --
    public void datLaiMatKhau(Long id, String matKhauMoi) {
        NguoiDung nd = repo.findById(id).orElseThrow();
        nd.setMatKhau(passwordEncoder.encode(matKhauMoi));
        nd.setSoLanDangNhapThatBai(0);
        nd.setBiKhoaDen(null);
        repo.save(nd);
    }

    // -- Đổi mật khẩu (người dùng tự đổi) --
    public void doiMatKhau(Long id, DoiMatKhauDTO dto) {
        NguoiDung nd = repo.findById(id).orElseThrow();

        if (!passwordEncoder.matches(dto.getMatKhauCu(), nd.getMatKhau()))
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        if (!dto.isKhop())
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận không khớp");

        nd.setMatKhau(passwordEncoder.encode(dto.getMatKhauMoi()));
        repo.save(nd);
    }

    // -- Khóa / Mở khóa tài khoản --
    public void khoaTaiKhoan(Long id, int soGio) {
        NguoiDung nd = repo.findById(id).orElseThrow();
        nd.setBiKhoaDen(soGio <= 0 ? LocalDateTime.now().plusYears(99)
                : LocalDateTime.now().plusHours(soGio));
        nd.setKichHoat(false);
        repo.save(nd);
    }

    public void moKhoaTaiKhoan(Long id) {
        NguoiDung nd = repo.findById(id).orElseThrow();
        nd.setBiKhoaDen(null);
        nd.setKichHoat(true);
        nd.setSoLanDangNhapThatBai(0);
        repo.save(nd);
    }

    public void doiTrangThai(Long id) {
        NguoiDung nd = repo.findById(id).orElseThrow();
        nd.setKichHoat(!nd.isKichHoat());
        if (nd.isKichHoat()) {
            nd.setBiKhoaDen(null);
            nd.setSoLanDangNhapThatBai(0);
        }
        repo.save(nd);
    }

    public void xoa(Long id) {
        NguoiDung nd = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // Không cho xóa Admin cuối cùng
        if (nd.getVaiTro() == VaiTro.ADMIN && repo.demTheoVaiTro(VaiTro.ADMIN) <= 1)
            throw new IllegalStateException("Không thể xóa! Đây là tài khoản Admin duy nhất");

        // Không xóa nếu có liên kết (sinh viên, giảng viên, nhân viên)
        if (nd.getSinhVien() != null || nd.getNhanVien() != null || nd.getNhanVien() != null)
            throw new IllegalStateException("Không thể xóa! Tài khoản đang liên kết với hồ sơ người dùng");

        repo.deleteById(id);
    }

    // -- Thống kê --
    @Transactional(readOnly = true)
    public Map<String, Long> thongKe() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("tongNguoiDung", repo.count());
        stats.put("dangHoatDong", repo.demHoatDong());
        stats.put("soAdmin", repo.demTheoVaiTro(VaiTro.ADMIN));
        stats.put("soGiangVien", repo.demTheoVaiTro(VaiTro.NHAN_VIEN));
        stats.put("soNhanVien", repo.demTheoVaiTro(VaiTro.NHAN_VIEN));
        stats.put("soSinhVien", repo.demTheoVaiTro(VaiTro.SINH_VIEN));
        return stats;
    }

    @Transactional(readOnly = true)
    public long count() { return repo.count(); }
}