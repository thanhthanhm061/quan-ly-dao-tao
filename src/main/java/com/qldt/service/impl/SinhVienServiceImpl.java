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
        if (sv.getId() == null) {
            // Kiểm tra mã SV trùng
            if (sinhVienRepo.existsByMaSv(sv.getMaSv())) {
                throw new IllegalArgumentException("Mã sinh viên '" + sv.getMaSv() + "' đã tồn tại");
            }

            // Chỉ tạo tài khoản nếu chưa có
            if (sv.getNguoiDung() == null) {
                String username = sv.getMaSv().toLowerCase();

                // Nếu username đã tồn tại thì dùng lại, không tạo mới
                NguoiDung nd = nguoiDungRepo.findByUsername(username)
                        .orElseGet(() -> {
                            NguoiDung newNd = NguoiDung.builder()
                                    .username(username)
                                    .matKhau(passwordEncoder.encode("Sv@" + sv.getMaSv()))
                                    .hoTen(sv.getHoTen())
                                    .email(sv.getEmail())
                                    .vaiTro(VaiTro.SINH_VIEN)
                                    .kichHoat(true)
                                    .build();
                            return nguoiDungRepo.save(newNd);
                        });

                sv.setNguoiDung(nd);
            }
        }
        return sinhVienRepo.save(sv);
    }

    @Override
    public void delete(Long id) {
        SinhVien sv = sinhVienRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));
        if (sv.getDangKys() != null && !sv.getDangKys().isEmpty()) {
            throw new IllegalStateException("Không thể xóa! Sinh viên đang có "
                    + sv.getDangKys().size() + " đăng ký học phần");
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