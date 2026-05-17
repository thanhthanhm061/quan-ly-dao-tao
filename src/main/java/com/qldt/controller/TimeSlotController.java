package com.qldt.controller;

// =====================================================================
// TIME SLOT CONTROLLER (Admin quản lý ca học)
// =====================================================================

import com.qldt.model.TimeSlot;
import com.qldt.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/time-slots")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("timeSlots", timeSlotService.findAll());
        return "timeslot/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("timeSlot", new TimeSlot());
        return "timeslot/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute TimeSlot ts, RedirectAttributes ra) {
        try {
            timeSlotService.save(ts);
            ra.addFlashAttribute("success", "Thêm ca học thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/time-slots";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("timeSlot", timeSlotService.findById(id).orElseThrow());
        return "timeslot/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @ModelAttribute TimeSlot ts, RedirectAttributes ra) {
        try {
            ts.setId(id);
            timeSlotService.save(ts);
            ra.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/time-slots";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            timeSlotService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa ca học!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/time-slots";
    }
}