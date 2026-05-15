package com.qldt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Slf4j
public class TKBEventListener {

    // ← @Autowired(required = false): không báo lỗi nếu bean không tồn tại
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private SimpMessagingTemplate wsTemplate;

    @EventListener
    @Async
    public void xuLyTKBEvent(TKBNotificationService.TKBEvent event) {

        String tieuDe = switch (event.type()) {
            case THEM -> "[TKB] Lịch mới: "      + event.tenMon();
            case XOA  -> "[TKB] Đã hủy lịch: "   + event.tenMon();
            case SUA  -> "[TKB] Cập nhật lịch: "  + event.tenMon();
        };

        String noiDung = """
                Môn học : %s
                Thứ     : %s
                Tiết    : %d -> %d
                Phòng   : %s
                Học kỳ  : %s
                """.formatted(
                event.tenMon(), event.thu(),
                event.tietBatDau(), event.tietKetThuc(),
                event.phongHoc(), event.hocKy()
        );

        // Gửi email — chỉ chạy nếu mailSender được cấu hình
        if (mailSender != null
                && event.emailGV() != null
                && !event.emailGV().isBlank()) {
            try {
                SimpleMailMessage msg = new SimpleMailMessage();
                msg.setTo(event.emailGV());
                msg.setSubject(tieuDe);
                msg.setText(noiDung);
                mailSender.send(msg);
                log.info("Đã gửi email TKB đến {}", event.emailGV());
            } catch (Exception e) {
                log.warn("Lỗi gửi email: {}", e.getMessage());
            }
        } else if (mailSender == null) {
            log.debug("JavaMailSender chưa cấu hình — bỏ qua gửi email");
        }

        // WebSocket — chỉ chạy nếu wsTemplate tồn tại
        if (wsTemplate != null) {
            try {
                wsTemplate.convertAndSend(
                        "/topic/tkb-updates",
                        Map.of(
                                "type", event.type().name(),
                                "mon",  event.tenMon(),
                                "thu",  event.thu()
                        )
                );
            } catch (Exception e) {
                log.debug("WebSocket không khả dụng: {}", e.getMessage());
            }
        }
    }
}