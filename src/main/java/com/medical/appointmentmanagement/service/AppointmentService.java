package com.medical.appointmentmanagement.service;

import com.medical.appointmentmanagement.model.Appointment;
import com.medical.appointmentmanagement.validation.InputValidation;
import com.medical.appointmentmanagement.model.OnlineAppointment;
import com.medical.appointmentmanagement.model.PhysicalAppointment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {
    private final List<Appointment> appointments = new ArrayList<>();
    private final Path file = Paths.get("data", "appointments.txt");
    private int nextId = 1;

    public AppointmentService() { loadAppointments(); }

    public synchronized List<Appointment> getAllAppointments() {
        return new ArrayList<>(appointments);
    }

    public synchronized Appointment getAppointmentById(int id) {
        return find(id);
    }

    private Appointment find(int id) {
        for (Appointment a : appointments) {
            if (a.getId() == id) return a;
        }
        return null;
    }

    private String safeText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        String clean = value.trim();
        if (clean.contains("|") || clean.contains("\n") || clean.contains("\r")) {
            throw new IllegalArgumentException(label + " contains invalid characters.");
        }
        return clean;
    }

    private void checkFuture(String date, String time) {
        InputValidation.futureDateTime(date, time);
    }

    public synchronized boolean isBooked(String doctor, String date,
                                          String time, int excludeId) {
        for (Appointment a : appointments) {
            if (a.getId() != excludeId && !"Cancelled".equals(a.getStatus())
                    && a.getDoctorName().equalsIgnoreCase(doctor.trim())
                    && a.getDate().equals(date) && a.getTime().equals(time)) {
                return true;
            }
        }
        return false;
    }

    public synchronized Appointment bookAppointment(String patient, String doctor,
            String date, String time, String type) {
        patient = safeText(patient, "Patient name");
        doctor = safeText(doctor, "Doctor name");
        checkFuture(date, time);
        if (type == null || !(type.equalsIgnoreCase("Online") || type.equalsIgnoreCase("Physical"))) {
            throw new IllegalArgumentException("Select Online or Physical appointment.");
        }
        if (isBooked(doctor, date, time, -1)) {
            throw new IllegalArgumentException("Doctor already booked at that date and time.");
        }
        int id = nextId;
        Appointment a = type.equalsIgnoreCase("Online")
                ? new OnlineAppointment(id, patient, doctor, date, time, "Pending")
                : new PhysicalAppointment(id, patient, doctor, date, time, "Pending");
        appointments.add(a);
        try {
            saveAppointments();
            nextId++;
        } catch (RuntimeException e) {
            appointments.remove(a);
            throw e;
        }
        return a;
    }

    public synchronized Appointment updateAppointment(int id, String date, String time) {
        Appointment a = find(id);
        if (a == null) throw new IllegalArgumentException("Appointment not found.");
        if ("Cancelled".equals(a.getStatus())) {
            throw new IllegalArgumentException("Cancelled appointment cannot be updated.");
        }
        checkFuture(date, time);
        if (isBooked(a.getDoctorName(), date, time, id)) {
            throw new IllegalArgumentException("Doctor already booked at that date and time.");
        }
        String oldDate = a.getDate();
        String oldTime = a.getTime();
        a.reschedule(date, time);
        try {
            saveAppointments();
        } catch (RuntimeException e) {
            a.reschedule(oldDate, oldTime);
            throw e;
        }
        return a;
    }

    public synchronized Appointment cancelAppointment(int id) {
        Appointment a = find(id);
        if (a == null) throw new IllegalArgumentException("Appointment not found.");
        if ("Cancelled".equals(a.getStatus())) {
            throw new IllegalArgumentException("Appointment already cancelled.");
        }
        a.cancel();
        try {
            saveAppointments();
        } catch (RuntimeException e) {
            loadAppointments();
            throw e;
        }
        return a;
    }

    public synchronized void deleteAppointment(int id) {
        Appointment a = find(id);
        if (a == null) throw new IllegalArgumentException("Appointment not found.");
        int position = appointments.indexOf(a);
        appointments.remove(position);
        try {
            saveAppointments();
        } catch (RuntimeException e) {
            appointments.add(position, a);
            throw e;
        }
    }

    private void saveAppointments() {
        Path temp = null;
        try {
            Files.createDirectories(file.getParent());
            temp = Files.createTempFile(file.getParent(), "appointments-", ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (Appointment a : appointments) {
                    writer.write(a.getId() + "|" + a.getPatientName() + "|" + a.getDoctorName()
                            + "|" + a.getDate() + "|" + a.getTime() + "|" + a.getStatus()
                            + "|" + a.getAppointmentType());
                    writer.newLine();
                }
            }
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not save appointments: " + e.getMessage(), e);
        } finally {
            if (temp != null) {
                try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
            }
        }
    }

    private void loadAppointments() {
        if (!Files.exists(file)) return;
        List<Appointment> loaded = new ArrayList<>();
        int max = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length != 7) throw new IllegalStateException("Invalid saved appointment record.");
                int id = Integer.parseInt(p[0]);
                Appointment a;
                if ("Online".equals(p[6])) {
                    a = new OnlineAppointment(id, p[1], p[2], p[3], p[4], p[5]);
                } else if ("Physical".equals(p[6])) {
                    a = new PhysicalAppointment(id, p[1], p[2], p[3], p[4], p[5]);
                } else {
                    throw new IllegalStateException("Unknown appointment type in file.");
                }
                loaded.add(a);
                max = Math.max(max, id);
            }
            appointments.clear();
            appointments.addAll(loaded);
            nextId = max + 1;
        } catch (IOException | NumberFormatException e) {
            throw new IllegalStateException("Could not load saved appointments: " + e.getMessage(), e);
        }
    }
}
