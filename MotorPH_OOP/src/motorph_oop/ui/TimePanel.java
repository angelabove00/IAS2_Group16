
package motorph_oop.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import motorph_oop.service.AttendanceService;
import motorph_oop.util.Constants;

// time tracking panel
// allows employee to check in, check out, and view attendance records

public class TimePanel extends JPanel {

    // logged-in employee details
    private String loggedInEmployee;
    private String loggedInLastName;
    private String loggedInFirstName;

    // table components
    private JTable table;
    private DefaultTableModel model;

    // month filter
    private JComboBox<String> comboMonth;

    // top panel
    private JPanel topPanel = new JPanel(new GridLayout(1, 1, 5, 5));

    // buttons
    private JButton btnTimeIn =
            UIUtils.createButton("Check In", null, null);

    private JButton btnTimeOut =
            UIUtils.createButton("Check Out", null, null);

    // service layer
    private final AttendanceService attendanceService =
            new AttendanceService();

    // constructor: builds ui layout
    public TimePanel() {

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, getHeight()));

        // header section
        JLabel heading = UIUtils.createHeaderLabel("Time");

        topPanel.add(heading);
        topPanel.add(btnTimeIn);

        add(topPanel, BorderLayout.NORTH);

        // attendance table
        model = new DefaultTableModel(
        new String[]{"Date", "Log In", "Log Out"}, 0
        ) {
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // makes all cells non-editable
        }
        };

        table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);

        scroll.setPreferredSize(new Dimension(950, 650));

        // month filter
        comboMonth = new JComboBox<>();
        comboMonth.addItem("All");
        comboMonth.addActionListener(e -> reloadTable());

        JPanel center = new JPanel(new BorderLayout());
        center.add(comboMonth, BorderLayout.NORTH);
        center.add(scroll);

        add(center, BorderLayout.SOUTH);

        // button actions
        btnTimeIn.addActionListener(e -> checkIn());
        btnTimeOut.addActionListener(e -> checkOut());
    }

    // sets logged-in employee details
    public void setLoggedIn(String empId, String lastName, String firstName) {

        this.loggedInEmployee = empId;
        this.loggedInLastName = lastName;
        this.loggedInFirstName = firstName;

        loadMonths();
        reloadTable();
    }

    // loads available months for attendance
    private void loadMonths() {

        comboMonth.removeAllItems();
        comboMonth.addItem("All");

        attendanceService
                .getAvailableMonths(loggedInEmployee)
                .forEach(comboMonth::addItem);
    }

    // reloads attendance table
    private void reloadTable() {

        model.setRowCount(0);

        List<Object[]> rows =
                attendanceService.getAttendance(loggedInEmployee);

        for (Object[] row : rows) {
            model.addRow(row);
        }
    }

    // handles employee check-in
    private void checkIn() {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confirm Check In?",
                "Check In",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            attendanceService.checkIn(
                    loggedInEmployee,
                    loggedInLastName,
                    loggedInFirstName
            );

            loadMonths();
            reloadTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Checked In Successfully!"
            );

            topPanel.remove(btnTimeIn);
            topPanel.add(btnTimeOut);
            topPanel.revalidate();
            topPanel.repaint();
        }
    }

    // handles employee check-out
    private void checkOut() {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confirm Check Out?",
                "Check Out",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            attendanceService.checkOut(loggedInEmployee);

            loadMonths();
            reloadTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Checked Out Successfully!"
            );

            topPanel.remove(btnTimeOut);
            topPanel.add(btnTimeIn);
            topPanel.revalidate();
            topPanel.repaint();
        }
    }
}