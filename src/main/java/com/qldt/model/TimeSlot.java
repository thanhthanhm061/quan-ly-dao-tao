package com.qldt.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "time_slots")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimeSlot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tiet_so", nullable = false, unique = true)
    private Integer tietSo; // 1..12

    @Column(name = "gio_bat_dau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gio_ket_thuc", nullable = false)
    private LocalTime gioKetThuc;

    @Column(name = "ten_ca", length = 50)
    private String tenCa; // VD: "Sáng 1", "Ca 1"

    @Column(name = "is_active")
    private boolean active = true;
}