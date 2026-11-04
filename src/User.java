 /**
 * Concepts: Encapsulation (private fields + getters/setters) & Inheritance
 */

// ==========================================
// 1. BASE CLASS: User (Parent)
// ==========================================
public class User {
    // Encapsulation: State fields are strictly PRIVATE
    private int userId;
    private String fullName;
    private String email;
    private String password;
    private String collegeName;
    private String role; // "student" or "staff"

    // Overloaded Constructor 1: For existing users loaded from database (with ID)
    public User(int userId, String fullName, String email, String password, String collegeName, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.collegeName = collegeName;
        this.role = role;
    }

    // Overloaded Constructor 2: For brand new registrations (before ID is assigned by MySQL)
    public User(String fullName, String email, String password, String collegeName, String role) {
        this(0, fullName, email, password, collegeName, role);
    }

    // Public Getters and Setters (Encapsulated controlled access)
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

// ==========================================
// 2. CHILD CLASS 1: Student (Inherits User)
// ==========================================
class Student extends User {
    // Constructor calling super() to reuse parent initialization
    public Student(int userId, String fullName, String email, String password, String collegeName) {
        super(userId, fullName, email, password, collegeName, "student");
    }

    public Student(String fullName, String email, String password, String collegeName) {
        super(fullName, email, password, collegeName, "student");
    }
}

// ==========================================
// 3. CHILD CLASS 2: Staff (Inherits User)
// ==========================================
class Staff extends User {
    // Constructor calling super() to set role explicitly as "staff"
    public Staff(int userId, String fullName, String email, String password, String collegeName) {
        super(userId, fullName, email, password, collegeName, "staff");
    }

    public Staff(String fullName, String email, String password, String collegeName) {
        super(fullName, email, password, collegeName, "staff");
    }
}