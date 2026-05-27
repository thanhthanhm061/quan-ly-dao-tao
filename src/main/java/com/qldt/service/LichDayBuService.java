package com.qldt.service;

import com.qldt.model.LichDayBu;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LichDayBuService {

    // ── CRUD cơ bản ──────────────────────────────────────────────────────────

    /** Lấy tất cả lịch bù (Admin) */
    List<LichDayBu> findAll();

    /** Lấy lịch bù theo ID */
    Optional<LichDayBu> findById(Long id);

    /**
     * Xếp lịch dạy bù.
     * Người xếp phải là Admin hoặc TK / PTK / TBM cùng khoa với lớp học phần.
     *
     * @param lhpId          ID lớp học phần cần dạy bù
     * @param giangVienId    ID giảng viên dạy bù
     * @param donNghiId      ID đơn nghỉ liên quan (nullable)
     * @param ngayNghiGoc    Ngày buổi học gốc bị nghỉ
     * @param ngayDayBu      Ngày dạy bù
     * @param thuTrongTuan   Thứ trong tuần (2–7)
     * @param tietBatDau     Tiết bắt đầu (1–12)
     * @param soTiet         Số tiết
     * @param phongHoc       Phòng học (mã phòng)
     * @param ghiChu         Ghi chú thêm
     * @param nguoiXepId     ID người xếp lịch
     */
    LichDayBu xepLich(Long lhpId, Long giangVienId, Long donNghiId,
                      LocalDate ngayNghiGoc, LocalDate ngayDayBu,
                      int thuTrongTuan, int tietBatDau, int soTiet,
                      String phongHoc, String ghiChu, Long nguoiXepId);

    /** Cập nhật lịch bù đã xếp (chỉ khi chưa hoàn thành) */
    LichDayBu capNhat(Long lichBuId, LocalDate ngayDayBu,
                      int thuTrongTuan, int tietBatDau, int soTiet,
                      String phongHoc, String ghiChu, Long nguoiXepId);

    //
    List<LichDayBu> findByGiangVienTuan(
            Long gvId,
            LocalDate ngayTrongTuan
    );

    List<LichDayBu> findByGiangVienThang(
            Long gvId,
            String hocKy,
            LocalDate dauThang
    );

    List<LichDayBu> findByLhpId(Long lhpId);

    // Lấy theo LHP


    /** Hủy lịch bù */
    void huy(Long lichBuId, Long nguoiXepId);

    /** Đánh dấu hoàn thành (sau khi buổi dạy bù kết thúc) */
    void hoanThanh(Long lichBuId);

    // ── Query ────────────────────────────────────────────────────────────────

    /** Lịch bù theo lớp học phần */
    List<LichDayBu> findByLopHocPhan(Long lhpId);

    /** Lịch bù theo giảng viên */
    List<LichDayBu> findByGiangVien(Long giangVienId);

    /** Lịch bù liên quan đến đơn nghỉ */
    List<LichDayBu> findByDonNghi(Long donNghiId);

    /** Lịch bù của giảng viên trong khoảng ngày */
    List<LichDayBu> findByGiangVienAndKhoangNgay(Long giangVienId,
                                                 LocalDate from, LocalDate to);

    /** Lịch bù thuộc khoa (TK/PTK xem) */
    List<LichDayBu> findByKhoa(Long khoaId);
}