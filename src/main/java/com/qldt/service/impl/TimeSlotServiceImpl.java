package com.qldt.service.impl;

import com.qldt.model.TimeSlot;
import com.qldt.repository.TimeSlotRepository;
import com.qldt.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepo;

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> findAll() {
        return timeSlotRepo.findByActiveTrueOrderByTietSoAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TimeSlot> findById(Long id) {
        return timeSlotRepo.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TimeSlot> findByTietSo(Integer tietSo) {
        return timeSlotRepo.findByTietSo(tietSo);
    }

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        if (timeSlot.getTietSo() == null || timeSlot.getTietSo() < 1 || timeSlot.getTietSo() > 15) {
            throw new IllegalArgumentException("Số tiết phải từ 1 đến 15");
        }
        if (timeSlot.getGioBatDau() == null || timeSlot.getGioKetThuc() == null) {
            throw new IllegalArgumentException("Giờ bắt đầu và kết thúc không được trống");
        }
        if (timeSlot.getGioBatDau().isAfter(timeSlot.getGioKetThuc())) {
            throw new IllegalArgumentException("Giờ bắt đầu phải trước giờ kết thúc");
        }
        // Kiểm tra trùng tiết (khi thêm mới)
        if (timeSlot.getId() == null) {
            timeSlotRepo.findByTietSo(timeSlot.getTietSo()).ifPresent(existing -> {
                throw new IllegalStateException("Tiết " + timeSlot.getTietSo() + " đã tồn tại");
            });
        }
        return timeSlotRepo.save(timeSlot);
    }

    @Override
    public void delete(Long id) {
        TimeSlot ts = timeSlotRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca học"));
        ts.setActive(false);
        timeSlotRepo.save(ts);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, TimeSlot> buildTietMap() {
        Map<Integer, TimeSlot> map = new LinkedHashMap<>();
        timeSlotRepo.findByActiveTrueOrderByTietSoAsc()
                .forEach(ts -> map.put(ts.getTietSo(), ts));
        return map;
    }
}