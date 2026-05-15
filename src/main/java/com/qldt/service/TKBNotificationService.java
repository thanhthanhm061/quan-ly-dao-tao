package com.qldt.service;

import com.qldt.model.LopHocPhan;
import com.qldt.model.ThoiKhoaBieu;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TKBNotificationService {

    private final ApplicationEventPublisher eventPublisher;

    public void guiThongBaoThemLich(ThoiKhoaBieu tkb) {
        eventPublisher.publishEvent(
                taoEvent(tkb, TKBEvent.Type.THEM)
        );
    }

    public void guiThongBaoXoaLich(ThoiKhoaBieu tkb) {
        eventPublisher.publishEvent(
                taoEvent(tkb, TKBEvent.Type.XOA)
        );
    }

    public void guiThongBaoSuaLich(ThoiKhoaBieu tkb) {
        eventPublisher.publishEvent(
                taoEvent(tkb, TKBEvent.Type.SUA)
        );
    }

    private TKBEvent taoEvent(
            ThoiKhoaBieu tkb,
            TKBEvent.Type type
    ) {

        LopHocPhan lhp = tkb.getLopHocPhan();

        String tenMon =
                lhp.getMonHoc() != null
                        ? lhp.getMonHoc().getTenMon()
                        : "---";

        String emailGV = null;

        if (lhp.getGiangVien() != null
                && lhp.getGiangVien().getNguoiDung() != null) {

            emailGV =
                    lhp.getGiangVien()
                            .getNguoiDung()
                            .getEmail();
        }

        return new TKBEvent(
                type,
                tenMon,
                emailGV,
                tkb.getTenThu(),
                tkb.getTietBatDau(),
                tkb.getTietBatDau() + tkb.getSoTiet() - 1,
                tkb.getPhongHoc(),
                lhp.getHocKy()
        );
    }

    public record TKBEvent(
            Type type,
            String tenMon,
            String emailGV,
            String thu,
            int tietBatDau,
            int tietKetThuc,
            String phongHoc,
            String hocKy
    ) {

        public enum Type {
            THEM,
            XOA,
            SUA
        }
    }
}