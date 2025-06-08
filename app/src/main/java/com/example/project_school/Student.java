package com.example.project_school;


public class Student {
    private int studentId;
    private String name;
    private String email;
    private String phone;
    private int classNum;
    private String classBranch;
    private String admissionDate;
    private String status;
    private String bloodGroup;

    // Constructors
    public Student() {}

    public Student(int studentId, String name, String email, String phone,
                   int classNum, String classBranch, String admissionDate,
                   String status, String bloodGroup) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.classNum = classNum;
        this.classBranch = classBranch;
        this.admissionDate = admissionDate;
        this.status = status;
        this.bloodGroup = bloodGroup;
    }

    // Getters and Setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getClassNum() { return classNum; }
    public void setClassNum(int classNum) { this.classNum = classNum; }

    public String getClassBranch() { return classBranch; }
    public void setClassBranch(String classBranch) { this.classBranch = classBranch; }

    public String getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getFullClass() {
        return "Class " + classNum + classBranch;
    }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    public String getInitials() {
        if (name == null || name.trim().isEmpty()) return "??";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        } else {
            return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }
}