import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class StaffFrame extends JFrame {
    // Theme Palette
    private static final Color BG_CREAM = new Color(250, 246, 240);
    private static final Color CARD_CREAM = Color.WHITE;
    private static final Color TERRACOTTA = new Color(184, 93, 59);
    private static final Color TERRA_DARK = new Color(141, 62, 36);
    private static final Color TEXT_DARK = new Color(61, 46, 40);
    private static final Color TEXT_MUTED = new Color(138, 124, 118);
    private static final Color BORDER_COLOR = new Color(230, 219, 206);

    private Staff currentStaff;
    private DBHandler db;

    // Components
    private JTable allComplaintsTable;
    private DefaultTableModel allComplaintsModel;
    private JTable priorityTable;
    private DefaultTableModel priorityModel;

    // Update Form Controls
    private JLabel lblSelectedId;
    private JComboBox<String> comboNewStatus;
    private JTextArea txtRemarks;

    public StaffFrame(Staff staff) {
        this.currentStaff = staff;
        try {
            this.db = DBHandler.getInstance();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + e.getMessage());
        }

        setTitle("CampusFix · Staff Maintenance Portal");
        setSize(1000, 720);
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

        JLabel welcomeLabel = new JLabel("CampusFix · " + currentStaff.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(TERRA_DARK);

        JLabel roleLabel = new JLabel("Maintenance & Estate Administration Portal");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(TEXT_MUTED);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        info.add(welcomeLabel);
        info.add(roleLabel);

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

        bar.add(info, BorderLayout.WEST);
        bar.add(btnLogout, BorderLayout.EAST);
        return bar;
    }

    // ========================================================
    // 2. TABBED CONTENT
    // ========================================================
    private JTabbedPane createTabbedContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(BG_CREAM);
        tabs.setForeground(TEXT_DARK);
        tabs.setBorder(new EmptyBorder(10, 15, 10, 15));

        tabs.addTab("  All Grievances & Status Manager  ", createManagementPanel());
        tabs.addTab("  Most Voted & Priority Queue  ", createPriorityPanel());

        return tabs;
    }

    // ========================================================
    // TAB 1: ALL COMPLAINTS + STATUS UPDATE DOCK
    // ========================================================
    private JPanel createManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Table
        String[] cols = {"ID", "Student ID", "Sem/Dept", "Category", "Location", "Type", "Urgency", "Status", "Remarks"};
        allComplaintsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        allComplaintsTable = createStyledTable(allComplaintsModel);
        JScrollPane scroll = new JScrollPane(allComplaintsTable);

        // Update Dock (Bottom Card)
        JPanel updateDock = new JPanel(new GridBagLayout());
        updateDock.setBackground(CARD_CREAM);
        updateDock.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 10, 6, 10);
        g.fill = GridBagConstraints.HORIZONTAL;

        lblSelectedId = new JLabel("None (Click a table row above)");
        lblSelectedId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSelectedId.setForeground(TERRA_DARK);

        String[] statuses = {"Pending", "In Progress", "Resolved"};
        comboNewStatus = new JComboBox<>(statuses);

        txtRemarks = new JTextArea(2, 22);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        txtRemarks.setBorder(new LineBorder(BORDER_COLOR, 1));

        JButton btnUpdate = new JButton("Update Grievance Status");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnUpdate.setBackground(TERRACOTTA);
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setOpaque(true);
        btnUpdate.setContentAreaFilled(true);
        btnUpdate.setBorderPainted(false);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdate.setPreferredSize(new Dimension(190, 36));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setForeground(TERRACOTTA);
        btnRefresh.setBackground(BG_CREAM);
        btnRefresh.setBorder(new LineBorder(BORDER_COLOR, 1));
        btnRefresh.setPreferredSize(new Dimension(90, 36));

        // Row Selection Listener
        allComplaintsTable.getSelectionModel().addListSelectionListener(e -> {
            int row = allComplaintsTable.getSelectedRow();
            if (row != -1) {
                int id = (int) allComplaintsModel.getValueAt(row, 0);
                String status = (String) allComplaintsModel.getValueAt(row, 7);
                String remarks = (String) allComplaintsModel.getValueAt(row, 8);

                lblSelectedId.setText("#" + id);
                comboNewStatus.setSelectedItem(status);
                txtRemarks.setText("None yet".equals(remarks) ? "" : remarks);
            }
        });

        btnUpdate.addActionListener(e -> {
            int row = allComplaintsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a complaint from the table first!", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = (int) allComplaintsModel.getValueAt(row, 0);
            String newStatus = (String) comboNewStatus.getSelectedItem();
            String remarks = txtRemarks.getText().trim();

            if (db.updateStatus(id, newStatus, remarks)) {
                JOptionPane.showMessageDialog(this, "Status updated for Complaint #" + id + "!", "Updated", JOptionPane.INFORMATION_MESSAGE);
                refreshAllComplaints();
                refreshPriorityQueue();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status in database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRefresh.addActionListener(e -> {
            refreshAllComplaints();
            refreshPriorityQueue();
        });

        // Dock layout
        g.gridx = 0; g.gridy = 0; updateDock.add(new JLabel("Selected Grievance:"), g);
        g.gridx = 1; updateDock.add(lblSelectedId, g);
        g.gridx = 2; updateDock.add(new JLabel("Change Status:"), g);
        g.gridx = 3; updateDock.add(comboNewStatus, g);

        g.gridx = 0; g.gridy = 1; updateDock.add(new JLabel("Staff Remarks:"), g);
        g.gridx = 1; g.gridwidth = 3; updateDock.add(new JScrollPane(txtRemarks), g);

        g.gridx = 4; g.gridy = 1; g.gridwidth = 1;
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(btnRefresh);
        btnGroup.add(btnUpdate);
        updateDock.add(btnGroup, g);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(updateDock, BorderLayout.SOUTH);

        refreshAllComplaints();
        return panel;
    }

    // ========================================================
    // TAB 2: MOST VOTED & PRIORITY QUEUE
    // ========================================================
    private JPanel createPriorityPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Category", "Location", "Urgency", "Votes", "Priority Score", "Status", "Description"};
        priorityModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        priorityTable = createStyledTable(priorityModel);
        JScrollPane scroll = new JScrollPane(priorityTable);

        JLabel info = new JLabel("<html><b>Crowdsourced Priority:</b> High-vote issues are automatically elevated in score for expedited resolution.</html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(TEXT_MUTED);

        panel.add(info, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        refreshPriorityQueue();
        return panel;
    }

    // ========================================================
    // REFRESH METHODS
    // ========================================================
    private void refreshAllComplaints() {
        if (db == null || allComplaintsModel == null) return;
        try {
            allComplaintsModel.setRowCount(0);
            List<Complaint> list = db.getAllComplaints();
            if (list != null) {
                for (Complaint c : list) {
                    allComplaintsModel.addRow(new Object[]{
                        c.getId(), c.getStudentId(), c.getSemester() + " " + c.getDepartment(),
                        c.getCategory(), c.getLocation(), c.getVisibility(),
                        c.getUrgencyLevel(), c.getStatus(),
                        (c.getStaffRemarks() == null || c.getStaffRemarks().isEmpty() ? "None yet" : c.getStaffRemarks())
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshPriorityQueue() {
        if (db == null || priorityModel == null) return;
        try {
            priorityModel.setRowCount(0);
            List<PublicComplaint> list = db.getPublicComplaints();
            if (list != null) {
                for (PublicComplaint pc : list) {
                    priorityModel.addRow(new Object[]{
                        pc.getId(), pc.getCategory(), pc.getLocation(),
                        pc.getUrgencyLevel(), pc.getUpvotes(),
                        pc.calculatePriorityScore(), // Dynamic Polymorphism in action!
                        pc.getStatus(), pc.getDescription()
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Table Styling Helper
    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(244, 219, 208));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(TERRA_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        return table;
    }
}