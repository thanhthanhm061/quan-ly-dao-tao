package com.qldt.service.impl;

import com.qldt.model.NguoiDung;
import com.qldt.model.SinhVien;
import com.qldt.model.enums.VaiTro;
import com.qldt.repository.NguoiDungRepository;
import com.qldt.repository.SinhVienRepository;
import com.qldt.service.SinhVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SinhVienServiceImpl implements SinhVienService {

    private final SinhVienRepository sinhVienRepo;
    private final NguoiDungRepository nguoiDungRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<SinhVien> findAll() {
        return sinhVienRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SinhVien> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        return sinhVienRepo.findByHoTenContainingIgnoreCase(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SinhVien> findById(Long id) {
        return sinhVienRepo.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SinhVien> findByMaSv(String maSv) {
        return sinhVienRepo.findByMaSv(maSv);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SinhVien> findByNguoiDungId(Long nguoiDungId) {
        return sinhVienRepo.findByNguoiDungId(nguoiDungId);
    }

    @Override
    public SinhVien save(SinhVien sv) {
        // Nếu là sinh viên mới (chưa có ID) → tạo tài khoản tự động
        if (sv.getId() == null) {
            if (sinhVienRepo.existsByMaSv(sv.getMaSv())) {
                throw new IllegalArgumentException("Mã sinh viên '" + sv.getMaSv() + "' đã tồn tại");
            }
            // Tạo tài khoản: username = maSv, mật khẩu = Sv@maSv
            String username = sv.getMaSv().toLowerCase();
            String password = "Sv@" + sv.getMaSv();
            NguoiDung nd = NguoiDung.builder()
                .username(username)
                .matKhau(passwordEncoder.encode(password))
                .hoTen(sv.getHoTen())
                .email(sv.getEmail())
                .vaiTro(VaiTro.SINH_VIEN)
                .kichHoat(true)
                .build();
            nd = nguoiDungRepo.save(nd);
            sv.setNguoiDung(nd);
        }
        return sinhVienRepo.save(sv);
    }

    @Override
    public void delete(Long id) {
        SinhVien sv = sinhVienRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));
        if (sv.getDangKys() != null && !sv.getDangKys().isEmpty()) {
            throw new IllegalStateException("Không thể xóa! Sinh viên đang có " +
                sv.getDangKys().size() + " đăng ký học phần");
        }
        sinhVienRepo.deleteById(id);
        if (sv.getNguoiDung() != null) {
            nguoiDungRepo.deleteById(sv.getNguoiDung().getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByMaSv(String maSv) {
        return sinhVienRepo.existsByMaSv(maSv);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return sinhVienRepo.count();
    }
}
