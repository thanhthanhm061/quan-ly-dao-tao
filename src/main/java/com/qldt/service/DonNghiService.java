package com.qldt.service;

import com.qldt.model.DonNghi;
import com.qldt.model.enums.TrangThaiDonNghi;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DonNghiService {

    long demChoDuyetTheoKhoa(Long khoaId);

    // ── CRUD cơ bản ──────────────────────────────────────────────────────────

    /** Lấy tất cả đơn (Admin) */
    List<DonNghi> findAll();

    /** Lấy đơn theo ID */
    Optional<DonNghi> findById(Long id);

    /** Tạo đơn xin nghỉ mới */
    DonNghi taoMoi(Long nguoiNopId, LocalDate ngayBatDau, LocalDate ngayKetThuc,
                   String lyDo, String loaiNghi, String fileDinhKem);

    /** Hủy đơn (chỉ người nộp, khi còn CHO_DUYET) */
    void huy(Long donNghiId, Long nguoiNopId);

    /**
     * Hủy đơn bởi Admin — không kiểm tra người nộp,
     * có thể hủy ở bất kỳ trạng thái nào.
     *
     * @param donNghiId ID đơn cần hủy
     */
    void huyByAdmin(Long donNghiId);

    // ── Duyệt / Từ chối ──────────────────────────────────────────────────────

    /**
     * Duyệt đơn nghỉ.
     * Người duyệt phải có chức vụ TK / PTK / TBM (cùng khoa)
     * hoặc là Admin (nguoiDuyetId = null).
     *
     * @param donNghiId    ID đơn cần duyệt
     * @param nguoiDuyetId ID nhân viên duyệt, null nếu là Admin
     */
    void duyet(Long donNghiId, Long nguoiDuyetId);

    /**
     * Từ chối đơn nghỉ.
     *
     * @param donNghiId    ID đơn
     * @param nguoiDuyetId ID nhân viên từ chối, null nếu là Admin
     * @param lyDoTuChoi   Lý do bắt buộc
     */
    void tuChoi(Long donNghiId, Long nguoiDuyetId, String lyDoTuChoi);

    // ── Query ────────────────────────────────────────────────────────────────

    /** Đơn của một nhân viên */
    List<DonNghi> findByNguoiNop(Long nguoiNopId);

    /** Đơn theo trạng thái (Admin) */
    List<DonNghi> findByTrangThai(TrangThaiDonNghi trangThai);

    /**
     * Đơn chờ duyệt thuộc khoa — dùng cho TK/PTK/TBM.
     *
     * @param khoaId ID khoa mà người duyệt quản lý
     */
    List<DonNghi> findChoDuyetTheoKhoa(Long khoaId);

    /** Đếm đơn chờ duyệt (badge thông báo) */
    long demChoDuyet();
}