package com.medical.appointmentmanagement.controller;

import com.medical.appointmentmanagement.model.Appointment;
import com.medical.appointmentmanagement.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service = new AppointmentService();

    @GetMapping
    public List<Appointment> all() {
        return service.getAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable int id) {
        Appointment a = service.getAppointmentById(id);
        if (a == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Appointment not found.");
        return ResponseEntity.ok(a);
    }

    @GetMapping("/availability")
    public Map<String, Boolean> availability(@RequestParam String doctor,
            @RequestParam String date, @RequestParam String time) {
        return Map.of("available", !service.isBooked(doctor, date, time, -1));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        try {
            Appointment a = service.bookAppointment(body.get("patientName"), body.get("doctorName"),
                    body.get("date"), body.get("time"), body.get("type"));
            return ResponseEntity.status(HttpStatus.CREATED).body(a);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable int id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(service.updateAppointment(id, body.get("date"), body.get("time")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable int id) {
        try {
            return ResponseEntity.ok(service.cancelAppointment(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        try {
            service.deleteAppointment(id);
            return ResponseEntity.ok("Appointment deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
