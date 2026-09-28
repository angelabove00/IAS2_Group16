
package motorph_oop.ui;

import java.awt.*;
import javax.swing.*;

public class EmployeeMainDashboard extends JFrame {

    // layout
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);

    // logged in user data
    private String userEmpNo;
    private String userLoggedIn;
    private String userFirstname;
    private String userLastname;

    // panels
    public FullDetailsPanel fullEmpPanel = new FullDetailsPanel();
    public LeavePanel leaveApp;
    public TimePanel timeEmpPanel = new TimePanel();

    // buttons
    private JButton btnHome;
    private JButton btnTime;
    private JButton btnLeave;
    private JButton btnLogout;

    public EmployeeMainDashboard(String employeenum,
                                 String loginnum,
                                 String lastname,
                                 String firstname) {

        // store session
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

        setVisible(true);
    }

    // initialize frame
    private void initFrame() {

        setTitle("MotorPH Employee Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    // same button style used in AdminDashboard
    private JButton navBtn(String text) {

        JButton btn = new JButton(text);

        btn.setFont(new Font("Arial", Font.PLAIN, 14));

        btn.setPreferredSize(new Dimension(180,45));
        btn.setMaximumSize(new Dimension(180,45));

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3D texture
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setBackground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);

        return btn;
    }

    // create buttons
    private void createButtons() {

        btnHome = navBtn("Home");
        btnTime = navBtn("Time");
        btnLeave = navBtn("Leave Application");
        btnLogout = navBtn("Log Out");
    }

    // build sidebar navigation
    private JPanel buildNavigation() {

        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(128,0,0));
        navPanel.setPreferredSize(new Dimension(230,0));
        navPanel.setLayout(new BoxLayout(navPanel,BoxLayout.Y_AXIS));

        navPanel.setBorder(BorderFactory.createEmptyBorder(0,15,0,15));

        int spacing = 14;

        JLabel welcome = new JLabel("Welcome to MOTORPH");
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("Arial",Font.PLAIN,14));
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dashboard = new JLabel("Employee Dashboard");
        dashboard.setForeground(Color.WHITE);
        dashboard.setFont(new Font("Arial",Font.BOLD,20));
        dashboard.setAlignmentX(Component.CENTER_ALIGNMENT);

        navPanel.add(Box.createVerticalStrut(25));
        navPanel.add(welcome);
        navPanel.add(Box.createVerticalStrut(5));
        navPanel.add(dashboard);
        navPanel.add(Box.createVerticalStrut(30));

        navPanel.add(btnHome);
        navPanel.add(Box.createVerticalStrut(spacing));

        navPanel.add(btnTime);
        navPanel.add(Box.createVerticalStrut(spacing));

        navPanel.add(btnLeave);

        // pushes logout to bottom
        navPanel.add(Box.createVerticalGlue());

        navPanel.add(btnLogout);
        navPanel.add(Box.createVerticalStrut(20));

        return navPanel;
    }

    // register panels
    private void registerPanels() {

        fullEmpPanel.setUserRole("Employee");
        fullEmpPanel.setEmployeeNo(userEmpNo);

        cardPanel.add(fullEmpPanel,"HOME");
        cardPanel.add(leaveApp,"LEAVE");
        cardPanel.add(timeEmpPanel,"TIME");
    }

    // register button actions
    private void registerActions() {

        btnHome.addActionListener(e ->
                switchPanel("HOME")
        );

        btnLeave.addActionListener(e ->
                switchPanel("LEAVE")
        );

        btnTime.addActionListener(e -> {

            timeEmpPanel.setLoggedIn(
                    userLoggedIn,
                    userLastname,
                    userFirstname
            );

            switchPanel("TIME");
        });

        btnLogout.addActionListener(e -> {

            new LoginPanel();
            dispose();
        });
    }

    // switch panel inside dashboard
    private void switchPanel(String name) {
        cardLayout.show(cardPanel,name);
    }
}