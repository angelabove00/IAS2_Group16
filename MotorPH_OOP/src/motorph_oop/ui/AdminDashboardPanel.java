
package motorph_oop.ui;

import java.awt.*;
import javax.swing.*;
import motorph_oop.security.AccessControl;
import motorph_oop.security.Permission;

public class AdminDashboardPanel extends JFrame {

    // layout
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    // session data
    private String userRole;
    private String userEmpNo;
    private String userLoggedIn;
    private String userFirstname;
    private String userLastname;
    private String selectedEmpNo;

    // panels
    public EmployeeDashboardPanel homePanel = new EmployeeDashboardPanel();
    public AddEmployeePanel addEmpPanel = new AddEmployeePanel();
    public FullDetailsPanel fullEmpPanel = new FullDetailsPanel();
    public LeavePanel leaveApp;
    public TimePanel timeEmpPanel = new TimePanel();

    // buttons
    private JButton btnDatabase;
    private JButton btnAdd;
    private JButton btnFullDetails;
    private JButton btnTime;
    private JButton btnLeaveRequests;
    private JButton btnManageLogin;
    private JButton btnLogout;

    public AdminDashboardPanel(String employeenum,
                               String accesslevel,
                               String loginnum,
                               String lastname,
                               String firstname) {

        userRole = accesslevel;
        userEmpNo = employeenum;
        userLoggedIn = loginnum;
        userFirstname = firstname;
        userLastname = lastname;

        leaveApp = new LeavePanel(userFirstname + " " + userLastname);

        initFrame();
        createButtons();

        JPanel navPanel = buildNavigation();

        registerPanels();
        registerActions();

        add(navPanel, BorderLayout.WEST);
        add(cardPanel, BorderLayout.CENTER);

        configurePermissions();

        setVisible(true);
    }

    // initialize frame
    private void initFrame() {

        setTitle("MotorPH Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    // create navigation button (3D style)
    private JButton navBtn(String text) {

        JButton btn = new JButton(text);

        btn.setFont(new Font("Arial", Font.PLAIN, 14));

        btn.setPreferredSize(new Dimension(180, 45));
        btn.setMaximumSize(new Dimension(180, 45));

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3D appearance
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setBackground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);

        return btn;
    }

    // create buttons
    private void createButtons() {

        btnDatabase = navBtn("Employee Database");
        btnAdd = navBtn("Add Employee");
        btnFullDetails = navBtn("View Full Details");
        btnTime = navBtn("Time");
        btnLeaveRequests = navBtn("Leave Requests");
        btnManageLogin = navBtn("Login Management");
        btnLogout = navBtn("Log Out");
    }

    // build sidebar navigation
    private JPanel buildNavigation() {

        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(128, 0, 0));
        navPanel.setPreferredSize(new Dimension(230, 0));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));

        // side padding
        navPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        int spacing = 14;

        JLabel welcome = new JLabel("Welcome to MOTORPH");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Arial", Font.PLAIN, 14));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel admin = new JLabel(getDashboardTitle());
        admin.setForeground(Color.WHITE);
        admin.setFont(new Font("Arial", Font.BOLD, 20));
        admin.setAlignmentX(Component.CENTER_ALIGNMENT);

        navPanel.add(Box.createVerticalStrut(25));
        navPanel.add(welcome);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(admin);
        navPanel.add(Box.createVerticalStrut(30));

        navPanel.add(btnDatabase);
        navPanel.add(Box.createVerticalStrut(spacing));

        navPanel.add(btnFullDetails);
        navPanel.add(Box.createVerticalStrut(spacing));

        navPanel.add(btnAdd);
        navPanel.add(Box.createVerticalStrut(spacing));

        navPanel.add(btnTime);
        navPanel.add(Box.createVerticalStrut(spacing));

        navPanel.add(btnLeaveRequests);
        navPanel.add(Box.createVerticalStrut(spacing));

        navPanel.add(btnManageLogin);

        navPanel.add(Box.createVerticalGlue());

        navPanel.add(btnLogout);
        navPanel.add(Box.createVerticalStrut(20));

        return navPanel;
    }

    // register panels
    private void registerPanels() {

        cardPanel.add(homePanel, "HOME");
        cardPanel.add(fullEmpPanel, "DETAILS");
        cardPanel.add(addEmpPanel, "ADD");
        cardPanel.add(timeEmpPanel, "TIME");
    }

    // register actions
    private void registerActions() {

        btnDatabase.addActionListener(e -> {

            if (!btnDatabase.isEnabled()) {
                showUnauthorized();
                return;
            }

            selectedEmpNo = null;
            homePanel.reloadEmployees();
            homePanel.clearFields();

            switchPanel("HOME");
        });

        btnAdd.addActionListener(e -> {

            if (!btnAdd.isEnabled()) {
                showUnauthorized();
                return;
            }

            switchPanel("ADD");
        });

        btnFullDetails.addActionListener(e -> {

            if (!btnFullDetails.isEnabled()) {
                showUnauthorized();
                return;
            }

            selectedEmpNo = homePanel.getSelectedEmployeeNo();

            if (selectedEmpNo == null || selectedEmpNo.trim().isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select an employee.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            fullEmpPanel.setUserRole(userRole);
            fullEmpPanel.setEmployeeNo(selectedEmpNo);

            switchPanel("DETAILS");
        });

        btnLeaveRequests.addActionListener(e -> {

            if (!btnLeaveRequests.isEnabled()) {
                showUnauthorized();
                return;
            }

            new LeaveApprovalPanel().setVisible(true);
        });

        btnManageLogin.addActionListener(e -> {

            if (!btnManageLogin.isEnabled()) {
                showUnauthorized();
                return;
            }

            new LoginManagementPanel();
        });

        btnTime.addActionListener(e -> {

            timeEmpPanel.setLoggedIn(
                    userLoggedIn,
                    userLastname,
                    userFirstname
            );

            switchPanel("TIME");
        });

        btnLogout.addActionListener(e -> {

            motorph_oop.util.Session.clear();   // clear the session on logout
            new LoginPanel();
            dispose();
        });
    }

    // switch panels
    private void switchPanel(String name) {
        cardLayout.show(cardPanel, name);
    }

    // unauthorized message
    private void showUnauthorized() {
        JOptionPane.showMessageDialog(this, "Your login is not authorized.");
    }

    private String getDashboardTitle() {

    if (userRole == null) return "Dashboard";

    switch (userRole.toUpperCase()) {

        case "ADMIN":
            return "Admin Dashboard";

        case "HR":
            return "HR Dashboard";

        case "IT":
            return "IT Dashboard";

        case "FINANCE":
            return "Finance Dashboard";

        default:
            return userRole + " Dashboard";
        }
    }
    
    // configure what this role can see (based on the permission matrix)
    private void configurePermissions() {

        boolean canViewAll = AccessControl.can(Permission.VIEW_ALL_EMPLOYEES);
        boolean canManageUsers = AccessControl.can(Permission.MANAGE_USER_ACCOUNTS);

        btnDatabase.setVisible(canViewAll);
        btnFullDetails.setVisible(canViewAll);
        btnAdd.setVisible(AccessControl.can(Permission.ADD_EMPLOYEE));
        btnLeaveRequests.setVisible(AccessControl.can(Permission.APPROVE_LEAVE));
        btnManageLogin.setVisible(canManageUsers);
        btnTime.setVisible(false);

        // a role with no permissions at all is not allowed in this dashboard
        if (!canViewAll && !canManageUsers) {
            showUnauthorized();
        }
    }
}