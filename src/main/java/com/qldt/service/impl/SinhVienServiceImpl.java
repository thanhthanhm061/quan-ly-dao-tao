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

    // =========================
    // LẤY TẤT CẢ SINH VIÊN
    // =========================
    @Override
    @Transactional(readOnly = true)
    public List<SinhVien> findAll() {

        return sinhVienRepo.findAll();

    }

    // =========================
    // TÌM KIẾM SINH VIÊN
    // =========================
    @Override
    @Transactional(readOnly = true)
    public List<SinhVien> search(String keyword) {

        if (keyword == null || keyword.isBlank()) {

            return findAll();

        }

        return sinhVienRepo.findByHoTenContainingIgnoreCase(keyword);

    }

    // =========================
    // TÌM THEO ID
    // =========================
    @Override
    @Transactional(readOnly = true)
    public Optional<SinhVien> findById(Long id) {

        return sinhVienRepo.findById(id);

    }

    // =========================
    // TÌM THEO MÃ SV
    // =========================
    @Override
    @Transactional(readOnly = true)
    public Optional<SinhVien> findByMaSv(String maSv) {

        return sinhVienRepo.findByMaSv(maSv);

    }

    // =========================
    // TÌM THEO NGƯỜI DÙNG
    // =========================
    @Override
    @Transactional(readOnly = true)
    public Optional<SinhVien> findByNguoiDungId(Long nguoiDungId) {

        return sinhVienRepo.findByNguoiDungId(nguoiDungId);

    }

    // =========================
    // THÊM / CẬP NHẬT SINH VIÊN
    // =========================
    @Override
    public SinhVien save(SinhVien sv) {

        // THÊM MỚI
        if (sv.getId() == null) {

            // KIỂM TRA MÃ SV
            if (sinhVienRepo.existsByMaSv(sv.getMaSv())) {

                throw new IllegalArgumentException(
                        "Mã sinh viên '" + sv.getMaSv() + "' đã tồn tại"
                );

            }

            // CHƯA CÓ TÀI KHOẢN
            if (sv.getNguoiDung() == null) {

                String username = sv.getMaSv().toLowerCase();

                // NẾU USERNAME ĐÃ CÓ THÌ DÙNG LẠI
                NguoiDung nd = nguoiDungRepo.findByUsername(username)

                        .orElseGet(() -> {

                            NguoiDung newNd = NguoiDung.builder()

                                    .username(username)

                                    .matKhau(
                                            passwordEncoder.encode(
                                                    "Sv@" + sv.getMaSv()
                                            )
                                    )

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

    // =========================
    // LẤY DS SINH VIÊN THEO LỚP
    // =========================
    @Override
    @Transactional(readOnly = true)
    public List<SinhVien> findByLopId(Long lopId) {

        return sinhVienRepo.findByLopId(lopId);

    }

    // =========================
    // XÓA SINH VIÊN
    // =========================
    @Override
    public void delete(Long id) {

        SinhVien sv = sinhVienRepo.findById(id)

                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy sinh viên"
                        )
                );

        // KIỂM TRA ĐĂNG KÝ
        if (sv.getDangKys() != null &&
                !sv.getDangKys().isEmpty()) {

            throw new IllegalStateException(

                    "Không thể xóa! Sinh viên đang có "
                            + sv.getDangKys().size()
                            + " đăng ký học phần"

            );

        }

        // XÓA SINH VIÊN
        sinhVienRepo.deleteById(id);

        // XÓA NGƯỜI DÙNG
        if (sv.getNguoiDung() != null) {

            nguoiDungRepo.deleteById(
                    sv.getNguoiDung().getId()
            );

        }

    }

    // =========================
    // KIỂM TRA MÃ SV TỒN TẠI
    // =========================
    @Override
    @Transactional(readOnly = true)
    public boolean existsByMaSv(String maSv) {

        return sinhVienRepo.existsByMaSv(maSv);

    }

    // =========================
    // ĐẾM TỔNG SINH VIÊN
    // =========================
    @Override
    @Transactional(readOnly = true)
    public long count() {

        return sinhVienRepo.count();

    }

}