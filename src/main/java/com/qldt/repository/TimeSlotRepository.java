package com.qldt.repository;

import com.qldt.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByActiveTrueOrderByTietSoAsc();

    Optional<TimeSlot> findByTietSo(Integer tietSo);
}