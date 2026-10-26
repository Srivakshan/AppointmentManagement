package com.medical.appointmentmanagement.model;

public abstract class Appointment {
    private final int id;
    private final String patientName;
    private final String doctorName;
    private String date;
    private String time;
    private String status;

    public Appointment(int id, String patientName, String doctorName,
                       String date, String time, String status) {
        this.id = id;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }

    public void reschedule(String newDate, String newTime) {
        date = newDate;
        time = newTime;
    }

    public void cancel() { status = "Cancelled"; }

    // ABSTRACTION: child classes provide their own appointment type.
    public abstract String getAppointmentType();
}
