import java.sql.Timestamp;

// ========================================================
// 1. ABSTRACT BASE CLASS: Complaint
// ========================================================
public abstract class Complaint {
    private int id;
    private int studentId;
    private String semester;
    private String department;
    private String category;      // e.g. Electricity, Infrastructure, Hygiene
    private String location;      // e.g. Room 14, Canteen, Central Library
    private String urgencyLevel;  // Urgent, Medium, Low
    private String description;
    private String photoPath;
    private String status;        // Pending, In Progress, Resolved
    private String staffRemarks;
    private Timestamp createdAt;

    // Full constructor for records retrieved from the database
    public Complaint(int id, int studentId, String semester, String department,
                     String category, String location, String urgencyLevel,
                     String description, String photoPath, String status,
                     String staffRemarks, Timestamp createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.semester = semester;
        this.department = department;
        this.category = category;
        this.location = location;
        this.urgencyLevel = urgencyLevel;
        this.description = description;
        this.photoPath = photoPath;
        this.status = status;
        this.staffRemarks = staffRemarks;
        this.createdAt = createdAt;
    }

    // Overloaded constructor for newly submitted complaints
    public Complaint(int studentId, String semester, String department,
                     String category, String location, String urgencyLevel,
                     String description, String photoPath) {
        this(0, studentId, semester, department, category, location, urgencyLevel,
             description, photoPath, "Pending", "", null);
    }

    // Abstract methods: Must be implemented by concrete child classes
    public abstract String getVisibility();
    public abstract int calculatePriorityScore();

    // Getters and Setters ensuring strict Encapsulation
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStaffRemarks() { return staffRemarks; }
    public void setStaffRemarks(String staffRemarks) { this.staffRemarks = staffRemarks; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

// ========================================================
// 2. SUBCLASS: PublicComplaint (Inheritance & Polymorphism)
// ========================================================
class PublicComplaint extends Complaint {
    private int upvotes;

    public PublicComplaint(int id, int studentId, String semester, String department,
                           String category, String location, String urgencyLevel,
                           String description, String photoPath, String status,
                           String staffRemarks, Timestamp createdAt, int upvotes) {
        super(id, studentId, semester, department, category, location, urgencyLevel,
              description, photoPath, status, staffRemarks, createdAt);
        this.upvotes = upvotes;
    }

    public PublicComplaint(int studentId, String semester, String department,
                           String category, String location, String urgencyLevel,
                           String description, String photoPath) {
        super(studentId, semester, department, category, location, urgencyLevel, description, photoPath);
        this.upvotes = 0;
    }

    @Override
    public String getVisibility() {
        return "PUBLIC";
    }

    // Dynamic Method Dispatch: Public issues gain priority with student upvotes
    @Override
    public int calculatePriorityScore() {
        int base = "Urgent".equalsIgnoreCase(getUrgencyLevel()) ? 50 : 20;
        return base + (this.upvotes * 5);
    }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }
}

// ========================================================
// 3. SUBCLASS: PrivateComplaint (Inheritance & Polymorphism)
// ========================================================
class PrivateComplaint extends Complaint {

    public PrivateComplaint(int id, int studentId, String semester, String department,
                            String category, String location, String urgencyLevel,
                            String description, String photoPath, String status,
                            String staffRemarks, Timestamp createdAt) {
        super(id, studentId, semester, department, category, location, urgencyLevel,
              description, photoPath, status, staffRemarks, createdAt);
    }

    public PrivateComplaint(int studentId, String semester, String department,
                            String category, String location, String urgencyLevel,
                            String description, String photoPath) {
        super(studentId, semester, department, category, location, urgencyLevel, description, photoPath);
    }

    @Override
    public String getVisibility() {
        return "PRIVATE";
    }

    // Dynamic Method Dispatch: Private issues prioritize based on urgency level
    @Override
    public int calculatePriorityScore() {
        if ("Urgent".equalsIgnoreCase(getUrgencyLevel())) return 80;
        if ("Medium".equalsIgnoreCase(getUrgencyLevel())) return 40;
        return 15;
    }
}