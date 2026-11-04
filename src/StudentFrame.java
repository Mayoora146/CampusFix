import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StudentFrame extends JFrame {
    // Theme Colors
    private static final Color BG_CREAM = new Color(250, 246, 240);
    private static final Color CARD_CREAM = Color.WHITE;
    private static final Color TERRACOTTA = new Color(184, 93, 59);
    private static final Color TERRA_DARK = new Color(141, 62, 36);
    private static final Color TEXT_DARK = new Color(61, 46, 40);
    private static final Color TEXT_MUTED = new Color(138, 124, 118);
    private static final Color BORDER_COLOR = new Color(230, 219, 206);

    private Student currentStudent;
    private DBHandler db;

    // Tables
    private JTable myComplaintsTable;
    private DefaultTableModel myComplaintsModel;
    private JTable publicComplaintsTable;
    private DefaultTableModel publicComplaintsModel;

    public StudentFrame(Student student) {
        this.currentStudent = student;
        try {
            this.db = DBHandler.getInstance();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }

        setTitle("CampusFix · Student Portal");
        setSize(950, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_CREAM);
        setLayout(new BorderLayout());

        add(createTopBar(), BorderLayout.NORTH);
        add(createTabbedContent(), BorderLayout.CENTER);
    }

    // ========================================================
    // 1. TOP HEADER BAR
    // ========================================================
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CARD_CREAM);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1),
            new EmptyBorder(14, 25, 14, 25)
        ));

        JLabel welcomeLabel = new JLabel("CampusFix · " + currentStudent.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(TERRA_DARK);

        JLabel collegeLabel = new JLabel(currentStudent.getCollegeName() + " (Student Portal)");
        collegeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        collegeLabel.setForeground(TEXT_MUTED);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        infoPanel.setOpaque(false);
        infoPanel.add(welcomeLabel);
        infoPanel.add(collegeLabel);

        JButton btnLogout = new JButton("Sign Out");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setForeground(TERRACOTTA);
        btnLogout.setBackground(BG_CREAM);
        btnLogout.setBorder(new LineBorder(TERRACOTTA, 1, true));
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(100, 34));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                this.dispose();
                new AuthFrame().setVisible(true);
            }
        });

        bar.add(infoPanel, BorderLayout.WEST);
        bar.add(btnLogout, BorderLayout.EAST);
        return bar;
    }

    // ========================================================
    // 2. TABBED CONTENT CONTAINER
    // ========================================================
    
private JTabbedPane createTabbedContent() {
    JTabbedPane tabs = new JTabbedPane();
    tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
    tabs.setBackground(BG_CREAM);
    tabs.setForeground(TEXT_DARK);
    tabs.setBorder(new EmptyBorder(12, 18, 14, 18));
    tabs.setOpaque(false);

    tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
        @Override
        protected Insets getTabInsets(int placement, int index) {
            return new Insets(12, 22, 12, 22);
        }

        @Override
        protected Insets getTabAreaInsets(int placement) {
            return new Insets(4, 4, 10, 4);
        }

        @Override
        protected void paintContentBorder(
                Graphics g, int placement, int selectedIndex) {
            // Remove the heavy default content border.
        }
    });

    tabs.addTab("  ✨ Lodge Grievance  ", createComplaintFormPanel());
    tabs.addTab("  📋 My Past Grievances  ", createMyComplaintsPanel());
    tabs.addTab("  🗳 Public Board & Voting  ", createPublicBoardPanel());

    return tabs;
}


    // ========================================================
    // TAB 1: LODGE COMPLAINT FORM
    // ========================================================
    private JPanel createComplaintFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_CREAM);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 25, 20, 25)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        String[] semesters = {"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8"};
        JComboBox<String> comboSem = new JComboBox<>(semesters);

        String[] departments = {"Computer Science & Engg", "Electronics & Comm.", "Mechanical Engg", "Civil Engg", "Electrical & Electronics"};
        JComboBox<String> comboDept = new JComboBox<>(departments);

        String[] categories = {"Infrastructure", "Electricity", "Hygiene & Restroom", "Canteen / Food", "Wi-Fi & Network", "Library"};
        JComboBox<String> comboCat = new JComboBox<>(categories);

        JTextField txtLocation = new JTextField(18); // e.g. Room 14, Main Canteen
        String[] urgencies = {"Low", "Medium", "Urgent"};
        JComboBox<String> comboUrgency = new JComboBox<>(urgencies);

        String[] visibilities = {"PUBLIC (Vote-enabled campus board)", "PRIVATE (Confidential to Staff only)"};
        JComboBox<String> comboVisibility = new JComboBox<>(visibilities);

        JTextArea txtDesc = new JTextArea(4, 25);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setBorder(new LineBorder(BORDER_COLOR, 1));
        JScrollPane descScroll = new JScrollPane(txtDesc);

        JButton btnSubmit = new JButton("Submit Grievance");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmit.setBackground(TERRACOTTA);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setOpaque(true);
        btnSubmit.setContentAreaFilled(true);
        btnSubmit.setBorderPainted(false);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setPreferredSize(new Dimension(200, 40));
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));

       
btnSubmit.addActionListener(e -> {
    String sem = (String) comboSem.getSelectedItem();
    String dept = (String) comboDept.getSelectedItem();
    String cat = (String) comboCat.getSelectedItem();
    String area = txtLocation.getText().trim();
    String urgency = (String) comboUrgency.getSelectedItem();
    String desc = txtDesc.getText().trim();
    boolean isPublic = comboVisibility.getSelectedIndex() == 0;

    if (area.isEmpty() || desc.isEmpty()) {
        showNotice(
            "Please fill in Area/Room and Description!",
            "warning"
        );
        return;
    }

    Complaint complaint;

    if (isPublic) {
        complaint = new PublicComplaint(
            currentStudent.getUserId(),
            sem, dept, cat, area, urgency, desc, null
        );
    } else {
        complaint = new PrivateComplaint(
            currentStudent.getUserId(),
            sem, dept, cat, area, urgency, desc, null
        );
    }

    String result = db.addComplaint(complaint);

    if ("SUCCESS".equals(result)) {
        showNotice(
            "Grievance submitted successfully!",
            "success"
        );

        txtLocation.setText("");
        txtDesc.setText("");

        refreshMyComplaints();
        refreshPublicBoard();

    } else {
        showNotice(
            "Failed to submit grievance: " + result,
            "error"
        );
    }
});
 
        // Form Layout
        int row = 0;
        JLabel studentDetailsHeading = new JLabel("Student Details");
        studentDetailsHeading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        studentDetailsHeading.setForeground(TERRA_DARK);
        g.gridx = 0; g.gridy = row; g.gridwidth = 4;
        g.insets = new Insets(8, 10, 2, 10);
        card.add(studentDetailsHeading, g);

        row++;
        g.gridwidth = 1;
        g.insets = new Insets(6, 10, 6, 10);
        g.gridx = 0; g.gridy = row; card.add(new JLabel("Semester:"), g);
        g.gridx = 1; card.add(comboSem, g);
        g.gridx = 2; card.add(new JLabel("Department:"), g);
        g.gridx = 3; card.add(comboDept, g);

        row++;
        JLabel complaintDetailsHeading = new JLabel("Complaint Details");
        complaintDetailsHeading.setFont(new Font("Segoe UI", Font.BOLD, 15));
        complaintDetailsHeading.setForeground(TERRA_DARK);
        g.gridx = 0; g.gridy = row; g.gridwidth = 4;
        g.insets = new Insets(12, 10, 2, 10);
        card.add(complaintDetailsHeading, g);

        row++;
        g.gridwidth = 1;
        g.insets = new Insets(6, 10, 6, 10);
        g.gridx = 0; g.gridy = row; card.add(new JLabel("Issue Category:"), g);
        g.gridx = 1; card.add(comboCat, g);
        g.gridx = 2; card.add(new JLabel("Urgency Level:"), g);
        g.gridx = 3; card.add(comboUrgency, g);

        row++;
        g.gridx = 0; g.gridy = row; card.add(new JLabel("Area / Room:"), g);
        g.gridx = 1; card.add(txtLocation, g);
        g.gridx = 2; card.add(new JLabel("Visibility:"), g);
        g.gridx = 3; card.add(comboVisibility, g);

        row++;
        g.gridx = 0; g.gridy = row; card.add(new JLabel("Description:"), g);
        g.gridx = 1; g.gridwidth = 3; card.add(descScroll, g);

        row++;
        g.gridx = 0; g.gridy = row; g.gridwidth = 4;
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.CENTER;
        card.add(btnSubmit, g);

       
GridBagConstraints outerG = new GridBagConstraints();
outerG.gridx = 0;
outerG.gridy = 0;
outerG.weightx = 1.0;
outerG.weighty = 1.0;
outerG.fill = GridBagConstraints.BOTH;
outerG.anchor = GridBagConstraints.NORTH;
outerG.insets = new Insets(8, 8, 8, 8);

outer.add(card, outerG);
return outer;
 
    }

    // ========================================================
    // TAB 2: MY PAST COMPLAINTS
    // ========================================================
    private JPanel createMyComplaintsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Category", "Location", "Type", "Urgency", "Status", "Remarks"};
        myComplaintsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        myComplaintsTable = createStyledTable(myComplaintsModel);
        JScrollPane scroll = new JScrollPane(myComplaintsTable);

        JButton btnRefresh = new JButton("Refresh My Grievances");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setForeground(TERRACOTTA);
        btnRefresh.setBackground(CARD_CREAM);
        btnRefresh.setBorder(new LineBorder(BORDER_COLOR, 1));
        btnRefresh.addActionListener(e -> refreshMyComplaints());

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnRefresh, BorderLayout.SOUTH);

        refreshMyComplaints();
        return panel;
    }

    // ========================================================
    // TAB 3: PUBLIC GRIEVANCES & UPVOTING
    // ========================================================
    private JPanel createPublicBoardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Category", "Location", "Urgency", "Description", "Upvotes", "Status"};
        publicComplaintsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        publicComplaintsTable = createStyledTable(publicComplaintsModel);
        JScrollPane scroll = new JScrollPane(publicComplaintsTable);

        JButton btnUpvote = new JButton("+1 Upvote Selected Grievance");
        btnUpvote.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpvote.setBackground(TERRACOTTA);
        btnUpvote.setForeground(Color.WHITE);
        btnUpvote.setOpaque(true);
        btnUpvote.setContentAreaFilled(true);
        btnUpvote.setBorderPainted(false);
        btnUpvote.setFocusPainted(false);
        btnUpvote.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpvote.setPreferredSize(new Dimension(280, 38));

        btnUpvote.addActionListener(e -> {
            int selectedRow = publicComplaintsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an issue from the table to upvote!", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int complaintId = (int) publicComplaintsModel.getValueAt(selectedRow, 0);

            try {
                db.upvoteComplaint(complaintId, currentStudent.getUserId());
                JOptionPane.showMessageDialog(this, "Thank you! Your vote has been recorded.", "Vote Added", JOptionPane.INFORMATION_MESSAGE);
                refreshPublicBoard();
            } catch (DuplicateVoteException dve) {
                // Catching Custom Exception (KTU Module 3 Rubric!)
                JOptionPane.showMessageDialog(this, dve.getMessage(), "Duplicate Vote", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        bottomBar.add(btnUpvote);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(bottomBar, BorderLayout.SOUTH);

        refreshPublicBoard();
        return panel;
    }

private void showNotice(String message, String type) {
    Color accent = new Color(184,93,59);

    if ("success".equalsIgnoreCase(type)) {
        accent = new Color(72, 125, 83);
    } else if ("error".equalsIgnoreCase(type)) {
        accent = new Color(166, 57, 45);
    }

    JWindow toast = new JWindow(this);
    JPanel content = new JPanel(new BorderLayout(12, 0));
    content.setBackground(CARD_CREAM);
    content.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        new EmptyBorder(14, 16, 14, 18)
    ));

    JPanel accentBar = new JPanel();
    accentBar.setBackground(accent);
    accentBar.setPreferredSize(new Dimension(5, 38));

    JLabel icon = new JLabel(
        "success".equalsIgnoreCase(type) ? "✓" :
        "error".equalsIgnoreCase(type) ? "!" : "•"
    );
    icon.setFont(new Font("Segoe UI", Font.BOLD, 20));
    icon.setForeground(accent);

    JLabel text = new JLabel(
        "<html><div style='width:270px;'>" +
        message.replace("&", "&amp;")
               .replace("<", "&lt;")
               .replace(">", "&gt;") +
        "</div></html>"
    );
    text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    text.setForeground(TEXT_DARK);

    JPanel messagePanel = new JPanel(new BorderLayout(10, 0));
    messagePanel.setOpaque(false);
    messagePanel.add(icon, BorderLayout.WEST);
    messagePanel.add(text, BorderLayout.CENTER);

    content.add(accentBar, BorderLayout.WEST);
    content.add(messagePanel, BorderLayout.CENTER);
    toast.setContentPane(content);
    toast.pack();

    Point location = getLocationOnScreen();
    int x = location.x + getWidth() - toast.getWidth() - 28;
    int y = location.y + 75;
    toast.setLocation(x, y);
    toast.setAlwaysOnTop(true);
    toast.setVisible(true);

    Timer timer = new Timer(2800, e -> toast.dispose());
    timer.setRepeats(false);
    timer.start();
}


    // Refresh Data Methods
    private void refreshMyComplaints() {
        if (db == null || myComplaintsModel == null) return;
        try {
            myComplaintsModel.setRowCount(0);
            List<Complaint> list = db.getComplaintsByStudent(currentStudent.getUserId());
            if (list != null) {
                for (Complaint c : list) {
                    myComplaintsModel.addRow(new Object[]{
                        c.getId(), c.getCategory(), c.getLocation(),
                        c.getVisibility(), c.getUrgencyLevel(), c.getStatus(),
                        (c.getStaffRemarks() == null ? "None yet" : c.getStaffRemarks())
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshPublicBoard() {
        if (db == null || publicComplaintsModel == null) return;
        try {
            publicComplaintsModel.setRowCount(0);
            List<PublicComplaint> list = db.getPublicComplaints();
            if (list != null) {
                for (PublicComplaint pc : list) {
                    publicComplaintsModel.addRow(new Object[]{
                        pc.getId(), pc.getCategory(), pc.getLocation(),
                        pc.getUrgencyLevel(), pc.getDescription(), pc.getUpvotes(), pc.getStatus()
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Clean Table Styling
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(244, 219, 208)); // Soft Terracotta Highlight
        table.setSelectionForeground(TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);

        // Header Styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(TERRA_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        return table;
    }
}