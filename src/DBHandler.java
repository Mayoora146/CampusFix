import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// ========================================================
// 1. CUSTOM EXCEPTIONS 
// ========================================================
class DuplicateVoteException extends Exception {
    public DuplicateVoteException(String message) {
        super(message);
    }
}

class ComplaintNotFoundException extends Exception {
    public ComplaintNotFoundException(String message) {
        super(message);
    }
}

// ========================================================
// 2. DBHandler with SINGLETON PATTERN 
// ========================================================
public class DBHandler {
    private static DBHandler instance;
    private Connection connection;

    // Database connection details (XAMPP default has empty password)
    private static final String URL = "jdbc:mysql://localhost:3306/complaint_system_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = ""; 

    // Private constructor: Enforces Singleton pattern (cannot be instantiated outside)
    private DBHandler() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver missing. Check .jar file in lib!", e);
        }
    }

    // Thread-safe Singleton accessor
    public static synchronized DBHandler getInstance() throws SQLException {
        if (instance == null || instance.connection == null || instance.connection.isClosed()) {
            instance = new DBHandler();
        }
        return instance;
    }

    public synchronized void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed() || !connection.isValid(2)) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                this.connection = DriverManager.getConnection(URL, USER, PASS);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver missing. Check .jar file in lib!", e);
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // ========================================================
    // USER OPERATIONS (Authentication & Registration)
    // ========================================================

    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        try {
            ensureConnection();
            String sql = "SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) AND password_hash = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email.trim());
                stmt.setString(2, password.trim());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String name = rs.getString("full_name");
                    String college = rs.getString("college_name");
                    String role = rs.getString("role");
                    String dbEmail = rs.getString("email");

                    if ("student".equalsIgnoreCase(role)) {
                        return new Student(id, name, dbEmail, password.trim(), college);
                    } else {
                        return new Staff(id, name, dbEmail, password.trim(), college);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }

    public boolean registerStudent(Student student) {
        return registerUser(student, "student");
    }

    public boolean registerStaff(Staff staff) {
        return registerUser(staff, "staff");
    }

    private boolean registerUser(User user, String role) {
        if (user == null || user.getEmail() == null) return false;
        try {
            ensureConnection();
            String sql = "INSERT INTO users (full_name, email, password_hash, college_name, role) VALUES (?, LOWER(TRIM(?)), ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.getFullName().trim());
                stmt.setString(2, user.getEmail().trim());
                stmt.setString(3, user.getPassword().trim());
                stmt.setString(4, user.getCollegeName().trim());
                stmt.setString(5, role);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    public boolean isEmailRegistered(String email) {
        if (email == null) return false;
        try {
            ensureConnection();
            String sql = "SELECT COUNT(*) FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email.trim());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Email check error: " + e.getMessage());
        }
        return false;
    }

    // ========================================================
    // COMPLAINT CRUD OPERATIONS
    // ========================================================

    public String addComplaint(Complaint c) {
        if (c == null) return "Complaint data is empty.";
        try {
            ensureConnection();
            String sql = "INSERT INTO complaints (student_id, student_sem, student_dept, target_category, " +
                         "area_or_room, visibility, urgency, description, image_path, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending')";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, c.getStudentId());
                stmt.setString(2, c.getSemester());
                stmt.setString(3, c.getDepartment());
                stmt.setString(4, c.getCategory());
                stmt.setString(5, c.getLocation());
                stmt.setString(6, c.getVisibility());
                stmt.setString(7, c.getUrgencyLevel());
                stmt.setString(8, c.getDescription());
                stmt.setString(9, c.getPhotoPath());
                int rows = stmt.executeUpdate();
                return rows > 0 ? "SUCCESS" : "No rows affected in database.";
            }
        } catch (SQLException e) {
            System.err.println("Add complaint error: " + e.getMessage());
            return "MySQL Error: " + e.getMessage();
        }
    }

    public List<Complaint> getComplaintsByStudent(int studentId) {
        List<Complaint> list = new ArrayList<>();
        try {
            ensureConnection();
            String sql = "SELECT * FROM complaints WHERE student_id = ? ORDER BY created_at DESC";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, studentId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    list.add(mapResultSetToComplaint(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Fetch student complaints error: " + e.getMessage());
        }
        return list;
    }

    public List<PublicComplaint> getPublicComplaints() {
        List<PublicComplaint> list = new ArrayList<>();
        try {
            ensureConnection();
            String sql = "SELECT * FROM complaints WHERE visibility = 'PUBLIC' ORDER BY votes_count DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Complaint c = mapResultSetToComplaint(rs);
                    if (c instanceof PublicComplaint) {
                        list.add((PublicComplaint) c);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Fetch public complaints error: " + e.getMessage());
        }
        return list;
    }

    public List<Complaint> getAllComplaints() {
        List<Complaint> list = new ArrayList<>();
        try {
            ensureConnection();
            String sql = "SELECT * FROM complaints ORDER BY created_at DESC";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(mapResultSetToComplaint(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Fetch all complaints error: " + e.getMessage());
        }
        return list;
    }

    // Upvote logic with duplicate vote prevention via custom exception
    public boolean upvoteComplaint(int complaintId, int studentId) throws DuplicateVoteException, SQLException {
        ensureConnection();
        // 1. Check if student already voted
        String checkSql = "SELECT COUNT(*) FROM complaint_votes WHERE complaint_id = ? AND student_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, complaintId);
            checkStmt.setInt(2, studentId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new DuplicateVoteException("You have already voted for this complaint!");
            }
        }

        // 2. Record the vote and increment vote counter
        String voteSql = "INSERT INTO complaint_votes (complaint_id, student_id) VALUES (?, ?)";
        String updateCountSql = "UPDATE complaints SET votes_count = votes_count + 1 WHERE complaint_id = ?";

        connection.setAutoCommit(false); // Transaction management
        try (PreparedStatement voteStmt = connection.prepareStatement(voteSql);
             PreparedStatement countStmt = connection.prepareStatement(updateCountSql)) {

            voteStmt.setInt(1, complaintId);
            voteStmt.setInt(2, studentId);
            voteStmt.executeUpdate();

            countStmt.setInt(1, complaintId);
            countStmt.executeUpdate();

            connection.commit();
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            connection.rollback();
            connection.setAutoCommit(true);
            throw e;
        }
    }

    public boolean updateStatus(int complaintId, String newStatus, String remarks) {
        try {
            ensureConnection();
            String sql = "UPDATE complaints SET status = ?, staff_remarks = ? WHERE complaint_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newStatus);
                stmt.setString(2, remarks);
                stmt.setInt(3, complaintId);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Update status error: " + e.getMessage());
            return false;
        }
    }

    // Helper method to convert a MySQL row into the correct Complaint subclass
    private Complaint mapResultSetToComplaint(ResultSet rs) throws SQLException {
        int id = rs.getInt("complaint_id");
        int studentId = rs.getInt("student_id");
        String sem = rs.getString("student_sem");
        String dept = rs.getString("student_dept");
        String cat = rs.getString("target_category");
        String area = rs.getString("area_or_room");
        String visibility = rs.getString("visibility");
        String urgency = rs.getString("urgency");
        String desc = rs.getString("description");
        String img = rs.getString("image_path");
        String status = rs.getString("status");
        String remarks = rs.getString("staff_remarks");
        Timestamp createdAt = rs.getTimestamp("created_at");
        int votes = rs.getInt("votes_count");

        if ("PUBLIC".equalsIgnoreCase(visibility)) {
            return new PublicComplaint(id, studentId, sem, dept, cat, area, urgency, desc, img, status, remarks, createdAt, votes);
        } else {
            return new PrivateComplaint(id, studentId, sem, dept, cat, area, urgency, desc, img, status, remarks, createdAt);
        }
    }
}