package com.qldt.service;

import com.qldt.model.TimeSlot;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TimeSlotService {

    List<TimeSlot> findAll();

    Optional<TimeSlot> findById(Long id);

    Optional<TimeSlot> findByTietSo(Integer tietSo);

    TimeSlot save(TimeSlot timeSlot);

    void delete(Long id);

    /**
     * Trả về map: tietSo -> TimeSlot
     * Dùng trong controller để truyền vào model cho template
     */
    Map<Integer, TimeSlot> buildTietMap();
}