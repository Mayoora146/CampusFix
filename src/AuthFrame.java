import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.SQLException;

public class AuthFrame extends JFrame {
    // Warm Terracotta & Cream Theme Palette
    public static final Color COLOR_BG = new Color(250, 246, 240);          // Soft Ivory Cream
    public static final Color COLOR_CARD = new Color(255, 255, 255);        // Pure White Card
    public static final Color COLOR_TERRACOTTA = new Color(184, 85, 48);    // Rich Terracotta
    public static final Color COLOR_TERRA_DARK = new Color(130, 52, 24);    // Deep Warm Brown
    public static final Color COLOR_TEXT_DARK = new Color(48, 36, 32);      // Espresso Dark Text
    public static final Color COLOR_TEXT_MUTED = new Color(120, 106, 100);  // Warm Slate Gray
    public static final Color COLOR_BORDER = new Color(226, 214, 200);      // Subtle Border

    private static final String SESSION_FILE = "session_user.txt";

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private DBHandler db;
    private JTextField txtLoginEmail;
    private JTextField txtRegisterEmail;
    private boolean staffMode = false; // role currently chosen (shared by sign-in & register pages)
    // Staff accounts need this code so students cannot self-register as staff. Change it as you like.
    private static final String STAFF_ACCESS_CODE = "STAFF2026";
    private final JToggleButton[] loginRole = new JToggleButton[2];    // [0]=Student, [1]=Staff
    private final JToggleButton[] registerRole = new JToggleButton[2];
    private final java.util.List<Runnable> roleListeners = new java.util.ArrayList<>();

    public AuthFrame() {
        setTitle("CampusFix - College Grievance Resolution Portal");
        setSize(560, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(COLOR_BG);

        try {
            db = DBHandler.getInstance();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error!\nPlease verify MySQL is running in XAMPP.", 
                "Database Alert", JOptionPane.ERROR_MESSAGE);
        }

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setOpaque(false);

        mainContainer.add(createLandingView(), "LANDING");
        mainContainer.add(createLoginView(), "LOGIN");
        mainContainer.add(createRegisterView(), "REGISTER");
        setRole(false); // apply initial Student styling to both role selectors

        add(mainContainer);


        // Always show the landing page when the application opens.
        // The user must click Sign In to open the login page.
        cardLayout.show(mainContainer, "LANDING");}


    // ========================================================
    // SESSION PERSISTENCE HELPERS
    // ========================================================
    private void saveSession(String email) {
        try (FileWriter fw = new FileWriter(SESSION_FILE)) {
            fw.write(email);
        } catch (Exception ignored) {}
    }

    private String getSavedSession() {
        File file = new File(SESSION_FILE);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                return br.readLine();
            } catch (Exception ignored) {}
        }
        return null;
    }

    // ========================================================
    // 1. UNMISTAKABLE LANDING / WELCOME SCREEN
    // ========================================================
    private JPanel createLandingView() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = createElevatedCard(480, 630);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // A clean welcome header (keeps the existing terracotta-and-cream theme)
        JLabel eyebrow = new JLabel("YOUR CAMPUS. YOUR VOICE.");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(COLOR_TERRACOTTA);
        eyebrow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("CampusFix");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(COLOR_TERRA_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("A simpler way to report, track, and resolve campus issues");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(COLOR_TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel featureHeading = new JLabel("Everything you need, in one place");
        featureHeading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        featureHeading.setForeground(COLOR_TEXT_DARK);
        featureHeading.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Three consistently sized feature cards keep the information easy to scan.
        JPanel infoBox = new JPanel(new GridLayout(3, 1, 0, 9));
        infoBox.setOpaque(false);
        infoBox.setMaximumSize(new Dimension(400, 222));
        infoBox.setPreferredSize(new Dimension(400, 222));
        infoBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoBox.add(createFeatureItem("01", "Lodge a Grievance",
            "Report campus issues with a category, urgency, and privacy option."));
        infoBox.add(createFeatureItem("02", "Make Issues Visible",
            "Upvote public complaints to help highlight shared priorities."));
        infoBox.add(createFeatureItem("03", "Track Every Update",
            "Follow progress from Pending to In Progress and Resolved."));

        JButton btnGoLogin = createPillButton("Sign In to Portal", COLOR_TERRACOTTA, Color.WHITE);
        JButton btnGoRegister = createPillButton("New User? Create an Account", new Color(244, 235, 224), COLOR_TERRA_DARK);
        btnGoRegister.setBorder(new LineBorder(COLOR_TERRACOTTA, 2, true));

        // Existing navigation actions are intentionally unchanged.
        btnGoLogin.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));
        btnGoRegister.addActionListener(e -> cardLayout.show(mainContainer, "REGISTER"));

        card.add(Box.createVerticalStrut(8));
        card.add(eyebrow);
        card.add(Box.createVerticalStrut(7));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(22));
        card.add(featureHeading);
        card.add(Box.createVerticalStrut(12));
        card.add(infoBox);
        card.add(Box.createVerticalStrut(20));
        card.add(btnGoLogin);
        card.add(Box.createVerticalStrut(12));
        card.add(btnGoRegister);
        card.add(Box.createVerticalGlue());

        outer.add(card);
        return outer;
    }

    private JPanel createFeatureItem(String number, String title, String desc) {
        JPanel item = new JPanel(new BorderLayout(13, 0));
        item.setBackground(new Color(252, 249, 244));
        item.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(10, 12, 10, 12)
        ));
        item.setPreferredSize(new Dimension(400, 68));
        item.setMinimumSize(new Dimension(400, 68));
        item.setMaximumSize(new Dimension(400, 68));

        JLabel numberLabel = new JLabel(number, SwingConstants.CENTER);
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        numberLabel.setForeground(COLOR_TERRACOTTA);
        numberLabel.setOpaque(true);
        numberLabel.setBackground(new Color(244, 235, 224));
        numberLabel.setBorder(new LineBorder(COLOR_BORDER, 1, true));
        numberLabel.setPreferredSize(new Dimension(42, 42));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel featureTitle = new JLabel(title);
        featureTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        featureTitle.setForeground(COLOR_TEXT_DARK);
        featureTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("<html><div style='width: 285px;'>" + desc + "</div></html>");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        description.setForeground(COLOR_TEXT_MUTED);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(featureTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(description);
        item.add(numberLabel, BorderLayout.WEST);
        item.add(textPanel, BorderLayout.CENTER);
        return item;
    }

    // ========================================================
    // 2. COMMON LOGIN VIEW (Student & Staff)
    // ========================================================
    private JPanel createLoginView() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = createElevatedCard(480, 630);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Student Sign In");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(COLOR_TERRA_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Choose your role, then enter your credentials");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(COLOR_TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 16, 0));

        JPanel rolePanel = buildRolePanel(loginRole);

        txtLoginEmail = createStyledTextField();
        JPasswordField txtPass = createStyledPasswordField();

        JLabel hint = new JLabel();
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setBorder(new EmptyBorder(8, 0, 16, 0));

        JButton btnSignIn = createPillButton("Sign In", COLOR_TERRACOTTA, Color.WHITE);
        JButton btnGoRegister = createTextLink("Need an account? Register as Student");
        JButton btnBack = createTextLink("< Back to Welcome");

        // Refresh texts whenever the role changes
        roleListeners.add(() -> {
            title.setText(staffMode ? "Staff Sign In" : "Student Sign In");
            hint.setText(staffMode
                ? "<html><center><font color='#8A7C76'>Demo staff login: staff@college.edu / staff123</font></center></html>"
                : "<html><center><font color='#8A7C76'>Use the personal email you registered with</font></center></html>");
            btnGoRegister.setText(staffMode ? "Need an account? Register as Staff" : "Need an account? Register as Student");
        });

        btnSignIn.addActionListener(e -> {
            String email = txtLoginEmail.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();

            if (email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both Email and Password!", "Input Needed", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (db == null) {
                try {
                    db = DBHandler.getInstance();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Cannot connect to Database!\nPlease make sure MySQL is started in XAMPP.\n\nError: " + ex.getMessage(), 
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (!db.isEmailRegistered(email)) {
                String roleName = staffMode ? "staff" : "student";
                int choice = JOptionPane.showConfirmDialog(this, 
                    "No account found for:\n" + email + "\n\nWould you like to create a new " + roleName + " account now?", 
                    "Account Not Found", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    txtRegisterEmail.setText(email);
                    cardLayout.show(mainContainer, "REGISTER");
                }
                return;
            }

            User user = db.authenticate(email, pass);
            if (user == null) {
                JOptionPane.showMessageDialog(this, 
                    "Incorrect password for: " + email + "\nPlease check your password and try again.", 
                    "Incorrect Password", JOptionPane.ERROR_MESSAGE);
            } else if (staffMode && !(user instanceof Staff)) {
                JOptionPane.showMessageDialog(this,
                    "This account is a Student account.\nPlease switch to the Student tab to sign in.",
                    "Wrong Role", JOptionPane.WARNING_MESSAGE);
            } else if (!staffMode && !(user instanceof Student)) {
                JOptionPane.showMessageDialog(this,
                    "This account is a Staff account.\nPlease switch to the Staff tab to sign in.",
                    "Wrong Role", JOptionPane.WARNING_MESSAGE);
            } else {
                saveSession(email); // Save user session
                openPortal(user);
            }
        });

        btnGoRegister.addActionListener(e -> cardLayout.show(mainContainer, "REGISTER"));
        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "LANDING"));

        card.add(title);
        card.add(sub);
        card.add(rolePanel);
        card.add(Box.createVerticalStrut(16));
        card.add(createFieldLabel("Email Address"));
        card.add(txtLoginEmail);
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldLabel("Password"));
        card.add(txtPass);
        card.add(hint);
        card.add(btnSignIn);
        card.add(Box.createVerticalStrut(14));
        card.add(btnGoRegister);
        card.add(btnBack);

        outer.add(card);
        return outer;
    }

    // ========================================================
    // 3. REGISTRATION VIEW (Student or Staff)
    // ========================================================
    private JPanel createRegisterView() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = createElevatedCard(480, 630);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Student Registration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(COLOR_TERRA_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Create an account to submit and upvote issues");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(COLOR_TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setBorder(new EmptyBorder(4, 0, 14, 0));

        JPanel rolePanel = buildRolePanel(registerRole);

        JTextField txtName = createStyledTextField();
        JTextField txtCollege = createStyledTextField();
        txtRegisterEmail = createStyledTextField();
        JPasswordField txtPass = createStyledPasswordField();
        JPasswordField txtCode = createStyledPasswordField();

        JLabel lblCode = createFieldLabel("Staff Access Code");
        JLabel lblEmail = createFieldLabel("Personal Email Address");

        JButton btnRegister = createPillButton("Create Student Account", COLOR_TERRACOTTA, Color.WHITE);
        JButton btnBackToLogin = createTextLink("Already registered? Sign In here");

        // Texts / extra staff-only field change with the selected role
        roleListeners.add(() -> {
            title.setText(staffMode ? "Staff Registration" : "Student Registration");
            sub.setText(staffMode ? "Create an account to manage and resolve complaints"
                                  : "Create an account to submit and upvote issues");
            lblEmail.setText(staffMode ? "Official Email Address" : "Personal Email Address");
            btnRegister.setText(staffMode ? "Create Staff Account" : "Create Student Account");
            lblCode.setVisible(staffMode);
            txtCode.setVisible(staffMode);
            if (!staffMode) txtCode.setText("");
        });

        btnRegister.addActionListener(e -> {
            String name = txtName.getText().trim();
            String college = txtCollege.getText().trim();
            String email = txtRegisterEmail.getText().trim();
            String pass = new String(txtPass.getPassword()).trim();
            String code = new String(txtCode.getPassword()).trim();

            if (name.isEmpty() || college.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields!", "Required Fields", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (staffMode && !STAFF_ACCESS_CODE.equals(code)) {
                JOptionPane.showMessageDialog(this,
                    "Invalid Staff Access Code.\nPlease ask your administrator for the correct code.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (db == null) {
                try {
                    db = DBHandler.getInstance();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Cannot connect to Database!\nPlease make sure MySQL is started in XAMPP.\n\nError: " + ex.getMessage(), 
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (db.isEmailRegistered(email)) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "The email '" + email + "' is already registered!\n\nWould you like to go to the Sign In page now?",
                    "Email Already Registered", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    txtLoginEmail.setText(email);
                    cardLayout.show(mainContainer, "LOGIN");
                }
                return;
            }

            boolean created = staffMode
                ? db.registerStaff(new Staff(name, email, pass, college))
                : db.registerStudent(new Student(name, email, pass, college));

            if (created) {
                saveSession(email);
                User loggedIn = db.authenticate(email, pass);
                JOptionPane.showMessageDialog(this, "Welcome to CampusFix, " + name + "!\nYour " + (staffMode ? "staff" : "student") + " account has been created successfully.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                openPortal(loggedIn);
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBackToLogin.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));

        card.add(title);
        card.add(sub);
        card.add(rolePanel);
        card.add(Box.createVerticalStrut(12));
        card.add(createFieldLabel("Full Name"));
        card.add(txtName);
        card.add(Box.createVerticalStrut(6));
        card.add(createFieldLabel("College Name"));
        card.add(txtCollege);
        card.add(Box.createVerticalStrut(6));
        card.add(lblEmail);
        card.add(txtRegisterEmail);
        card.add(Box.createVerticalStrut(6));
        card.add(createFieldLabel("Create Password"));
        card.add(txtPass);
        card.add(Box.createVerticalStrut(6));
        card.add(lblCode);
        card.add(txtCode);
        card.add(Box.createVerticalStrut(14));
        card.add(btnRegister);
        card.add(Box.createVerticalStrut(6));
        card.add(btnBackToLogin);

        outer.add(card);
        return outer;
    }

    // ========================================================
    // SHARED ROLE SELECTOR (Student / Staff) used on Sign In + Register
    // ========================================================
    private JPanel buildRolePanel(JToggleButton[] holder) {
        holder[0] = new JToggleButton("Student", !staffMode);
        holder[1] = new JToggleButton("Staff", staffMode);
        ButtonGroup group = new ButtonGroup();
        group.add(holder[0]);
        group.add(holder[1]);
        for (JToggleButton b : holder) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setContentAreaFilled(true);
            b.setBorder(new LineBorder(COLOR_TERRACOTTA, 1, true));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> setRole(holder[1].isSelected()));
        }
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(380, 42));
        panel.setPreferredSize(new Dimension(380, 42));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(holder[0]);
        panel.add(holder[1]);
        return panel;
    }

    // Switch role everywhere so both pages always stay in sync
    private void setRole(boolean staff) {
        staffMode = staff;
        for (JToggleButton[] h : new JToggleButton[][]{loginRole, registerRole}) {
            if (h[0] == null) continue;
            h[0].setSelected(!staff);
            h[1].setSelected(staff);
            h[0].setBackground(staff ? Color.WHITE : COLOR_TERRACOTTA);
            h[0].setForeground(staff ? COLOR_TERRACOTTA : Color.WHITE);
            h[1].setBackground(staff ? COLOR_TERRACOTTA : Color.WHITE);
            h[1].setForeground(staff ? Color.WHITE : COLOR_TERRACOTTA);
        }
        for (Runnable r : roleListeners) r.run();
    }

    private void openPortal(User user) {
        try {
            if (user instanceof Student) {
                new StudentFrame((Student) user).setVisible(true);
                this.dispose();
            } else if (user instanceof Staff) {
                new StaffFrame((Staff) user).setVisible(true);
                this.dispose();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error launching portal: " + t.toString(),
                "Portal Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========================================================
    // UI BUILDER HELPERS
    // ========================================================
    private JPanel createElevatedCard(int width, int height) {
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(width, height));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(25, 35, 25, 35)
        ));
        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT_DARK);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        return label;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setMaximumSize(new Dimension(380, 38));
        field.setPreferredSize(new Dimension(380, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(COLOR_TEXT_DARK);
        field.setBackground(COLOR_BG);
        field.setCaretColor(COLOR_TERRACOTTA);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(5, 12, 5, 12)
        ));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setMaximumSize(new Dimension(380, 38));
        field.setPreferredSize(new Dimension(380, 38));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(COLOR_TEXT_DARK);
        field.setBackground(COLOR_BG);
        field.setCaretColor(COLOR_TERRACOTTA);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(5, 12, 5, 12)
        ));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }

    private JButton createPillButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(380, 44));
        btn.setPreferredSize(new Dimension(380, 44));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createTextLink(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(COLOR_TERRACOTTA);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}