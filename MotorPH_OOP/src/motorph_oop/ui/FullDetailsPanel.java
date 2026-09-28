
package motorph_oop.ui;

import motorph_oop.dao.AttendanceDAO;
import motorph_oop.dao.EmployeeDAO;
import motorph_oop.model.Employee;
import motorph_oop.service.EmployeeService;
import motorph_oop.util.Constants;
import motorph_oop.utils.PayrollService;
import motorph_oop.utils.PayrollService.PayrollResult;
import motorph_oop.util.Session;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.JFileChooser;
import java.io.FileOutputStream;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class FullDetailsPanel extends JPanel {
    
    private String employeeNum;
    private String userRole;

    private final EmployeeService employeeService = new EmployeeService();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private JTable attendanceTable;
    private DefaultTableModel attendanceModel;

    private JLabel totalSalary;
    private JComboBox<String> comboMonth;

    // Employee fields
    private JTextField empNo = UIUtils.createTextField(false);
    private JTextField lastName = UIUtils.createTextField(true);
    private JTextField firstName = UIUtils.createTextField(true);
    private JTextField status = UIUtils.createTextField(true);
    private JTextField position = UIUtils.createTextField(true);
    private JTextField supervisor = UIUtils.createTextField(true);
    private JTextField birthday = UIUtils.createTextField(true);
    private JTextField address = UIUtils.createTextField(true);
    private JTextField phone = UIUtils.createTextField(true);
    private JTextField sss = UIUtils.createTextField(true);
    private JTextField philhealth = UIUtils.createTextField(true);
    private JTextField tin = UIUtils.createTextField(true);
    private JTextField pagibig = UIUtils.createTextField(true);
    private JTextField basicSalary = UIUtils.createTextField(true);
    private JTextField riceSubsidyField = UIUtils.createTextField(true);
    private JTextField phoneAllowanceField = UIUtils.createTextField(true);
    private JTextField clothingAllowanceField = UIUtils.createTextField(true);
    private JTextField grossRate = UIUtils.createTextField(true);
    private JTextField hourlyRateField = UIUtils.createTextField(true);

    // Buttons
    private JButton btnCalculate;
    private JButton setPasswordBtn;
    private JButton updateBtn;
    private JButton deleteBtn;

    // Panels
    private JPanel bottomPanel = new JPanel(new BorderLayout());
    private JPanel tablePanel = new JPanel();
    private JPanel centerContainer = new JPanel(new BorderLayout());
    private JPanel buttons = new JPanel(new GridLayout(4, 1, 20, 20));

    public FullDetailsPanel() {
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, getHeight()));

        attendanceModel = new DefaultTableModel(Constants.ATTENDANCE_COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
        return false; // prevents editing
        }
        };
        attendanceTable = new JTable(attendanceModel);

        JScrollPane tableScroll = new JScrollPane(attendanceTable);
        tableScroll.setPreferredSize(new Dimension(950, 150));

        tablePanel.add(tableScroll);

        totalSalary = new JLabel("Total Salary: ₱0.00");
        totalSalary.setFont(new Font("Arial", Font.BOLD, 16));

        comboMonth = new JComboBox<>();
        comboMonth.addItem("All");
        comboMonth.addActionListener(e -> reloadTable());

        JPanel topPanel = new JPanel(new GridLayout(1, 3, 5, 5));

        topPanel.add(new JLabel("Filter by Month:"));
        topPanel.add(comboMonth);
        topPanel.add(totalSalary);

        add(topPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(3, 1, 5, 2));

        formPanel.add(UIUtils.createEmployeeInfoPanel(
                empNo,
                lastName,
                firstName,
                status,
                position,
                supervisor
        ));

        formPanel.add(UIUtils.createPersonalInfoPanel(
                birthday,
                address,
                phone,
                sss,
                philhealth,
                tin,
                pagibig
        ));

        formPanel.add(UIUtils.createFinancialInfoPanel(
                basicSalary,
                riceSubsidyField,
                phoneAllowanceField,
                clothingAllowanceField,
                grossRate,
                hourlyRateField
        ));

        btnCalculate = UIUtils.createButton(
                "View Salary",
                new Color(52, 58, 235),
                Color.WHITE
        );

        setPasswordBtn = UIUtils.createButton(
                "Generate Payslip",
                new Color(235, 122, 52),
                Color.WHITE
        );

        updateBtn = UIUtils.createButton(
                "Update Record",
                new Color(0, 180, 0),
                Color.WHITE
        );

        deleteBtn = UIUtils.createButton(
                "Delete Record",
                new Color(168, 0, 0),
                Color.WHITE
        );

        btnCalculate.addActionListener(e -> calculateSalary());
        updateBtn.addActionListener(e -> updateEmployee());
        deleteBtn.addActionListener(e -> deleteSelectedRow());
        setPasswordBtn.addActionListener(e -> generatePayslipPDF());


        buttons.add(btnCalculate);
        buttons.add(setPasswordBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);

        bottomPanel.add(formPanel, BorderLayout.CENTER);
        bottomPanel.add(buttons, BorderLayout.EAST);

        centerContainer.add(tablePanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
        add(centerContainer, BorderLayout.CENTER);
        setUserRole(Session.role);
    }

    // Role from login/dashboard
    public void setUserRole(String role) {
        this.userRole = role;
        applyEmployeeRestrictions();
    }

    // Disable fields if employee
    private void applyEmployeeRestrictions() {

    if (userRole == null) return;

    JTextField[] allFields = {
            lastName, firstName, status, position, supervisor,
            birthday, sss, philhealth, tin, pagibig,
            basicSalary, riceSubsidyField, phoneAllowanceField,
            clothingAllowanceField, grossRate, hourlyRateField, address, phone
    };

    String role = userRole.toUpperCase();

    // Reset everything first (safe default)
    for (JTextField f : allFields) {
        f.setEditable(true);
    }

        btnCalculate.setVisible(true);
        setPasswordBtn.setVisible(true);
        updateBtn.setVisible(true);
        deleteBtn.setVisible(true);
    


    switch (role) {

        case "EMPLOYEE":

            for (JTextField f : allFields) {
                f.setEditable(false);
            }

            setPasswordBtn.setVisible(false);
            deleteBtn.setVisible(false);
            updateBtn.setVisible(false);

            break;

        case "IT":

            for (JTextField f : allFields) {
                f.setEditable(false);
            }
            btnCalculate.setVisible(false);
            updateBtn.setVisible(false);
            deleteBtn.setVisible(false);

            break;

        case "HR":

            btnCalculate.setVisible(false);
            setPasswordBtn.setVisible(false);

            break;

        case "FINANCE":

            for (JTextField f : allFields) {
                f.setEditable(false);
            }
            
            btnCalculate.setVisible(false);
            updateBtn.setVisible(false);
            deleteBtn.setVisible(false);
            setPasswordBtn.setVisible(true);

            break;

        case "ADMIN":
        default:
            // Admin keeps full access
            break;
    }
}
    public void setEmployeeNo(String employeeNo) {

        this.employeeNum = employeeNo;

        loadEmployee();
        loadAvailableMonths();
        reloadTable();
    }
    
    // ensures a valid month is selected
    private boolean validateMonthSelection() {

    String selectedMonth = (String) comboMonth.getSelectedItem();

    if (selectedMonth == null || selectedMonth.equals("All")) {

        JOptionPane.showMessageDialog(
                this,
                "Please select a specific month first."
        );

        return false;
    }

    return true;
    }

    private void loadEmployee() {

        Employee employee = employeeDAO.findEmployeeById(employeeNum);

        if (employee == null) return;

        empNo.setText(employee.getEmpNo());
        lastName.setText(employee.getLastName());
        firstName.setText(employee.getFirstName());
        birthday.setText(employee.getBirthday());
        address.setText(employee.getAddress());
        phone.setText(employee.getPhone());
        sss.setText(employee.getSss());
        philhealth.setText(employee.getPhilhealth());
        tin.setText(employee.getTin());
        pagibig.setText(employee.getPagibig());
        status.setText(employee.getStatus());
        position.setText(employee.getPosition());
        supervisor.setText(employee.getSupervisor());

        basicSalary.setText(String.valueOf(employee.getBasicSalary()));
        riceSubsidyField.setText(String.valueOf(employee.getRiceSubsidy()));
        phoneAllowanceField.setText(String.valueOf(employee.getPhoneAllowance()));
        clothingAllowanceField.setText(String.valueOf(employee.getClothingAllowance()));
        grossRate.setText(String.valueOf(employee.getGrossRate()));
        hourlyRateField.setText(String.valueOf(employee.getHourlyRate()));
    }

    private void loadAvailableMonths() {

    String currentSelection = (String) comboMonth.getSelectedItem();

    comboMonth.removeAllItems();
    comboMonth.addItem("All");

    List<String> months = attendanceDAO.getAvailableMonths(employeeNum);

    for (String m : months) {
        comboMonth.addItem(m);
        }

    // restore previous selection if it still exists
    if (currentSelection != null) {
        comboMonth.setSelectedItem(currentSelection);
        }
    }

    private void reloadTable() {

        // refresh dropdown months first
        loadAvailableMonths();
        
        attendanceModel.setRowCount(0);

        List<Object[]> rows =
                attendanceDAO.getAttendanceByEmployeeAndMonth(
                        employeeNum,
                        (String) comboMonth.getSelectedItem(),
                        Double.parseDouble(hourlyRateField.getText())
                );

        double total = 0;

        for (Object[] row : rows) {

            total += (double) row[4];

            attendanceModel.addRow(new Object[]{
                    row[0],
                    row[1],
                    row[2],
                    String.format("%.2f", row[3]),
                    "₱" + String.format("%.2f", row[4])
            });
        }

        totalSalary.setText(
                "Total Salary: ₱" +
                        new DecimalFormat("#,##0.00").format(total)
        );
    }

    private void calculateSalary() {

        if (!validateMonthSelection()) return;
        Employee employee = employeeDAO.findEmployeeById(employeeNum);
        if (employee == null) return;

        PayrollResult result =
                PayrollService.calculatePayroll(
                        employee,
                        Constants.ATTENDANCE_CSV,
                        (String) comboMonth.getSelectedItem()
                );

        String salaryDetails =
        String.format(
                "========================================\n" +
                "              MOTORPH PAYSLIP\n" +
                "========================================\n" +
                " Employee No : %-20s\n" +
                " Month       : %-20s\n" +
                "----------------------------------------\n" +
                " Basic Salary        : ₱%,12.2f\n" +
                " Gross Salary        : ₱%,12.2f\n" +
                " SSS                 : ₱%,12.2f\n" +
                " PhilHealth          : ₱%,12.2f\n" +
                " Pag-IBIG            : ₱%,12.2f\n" +
                " Withholding Tax     : ₱%,12.2f\n" +
                "----------------------------------------\n" +
                " NET PAY             : ₱%,12.2f\n" +
                "========================================\n",
                employee.getEmpNo(),
                comboMonth.getSelectedItem(),
                employee.getBasicSalary(),
                result.getGrossSalary(),
                result.getSssDeduction(),
                result.getPhilhealthDeduction(),
                result.getPagibigDeduction(),
                result.getWithholdingTax(),
                result.getNetSalary()
                );

        JTextArea textArea = new JTextArea(salaryDetails);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JOptionPane.showMessageDialog(
                this,
                textArea,
                "Payroll Summary",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void generatePayslipPDF() {

        if (!validateMonthSelection()) return;
        try {
        
        Employee employee = employeeDAO.findEmployeeById(employeeNum);
        if (employee == null) return;

        PayrollResult result =
                PayrollService.calculatePayroll(
                        employee,
                        Constants.ATTENDANCE_CSV,
                        (String) comboMonth.getSelectedItem()
                );

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Payslip");

        fileChooser.setSelectedFile(
                new java.io.File("Payslip_" + employee.getEmpNo() + ".pdf")
        );

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();

        Document document = new Document();

        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();

        com.itextpdf.text.Font titleFont =
        new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 18,
                com.itextpdf.text.Font.BOLD
        );

        com.itextpdf.text.Font textFont =
        new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12
        );

        Paragraph title = new Paragraph("MOTORPH PAYSLIP", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);

        document.add(title);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Employee No: " + employee.getEmpNo(), textFont));
        document.add(new Paragraph("Name: " + employee.getFirstName() + " " + employee.getLastName(), textFont));
        document.add(new Paragraph("Position: " + employee.getPosition(), textFont));
        document.add(new Paragraph("Month: " + comboMonth.getSelectedItem(), textFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("--------------- SALARY DETAILS ---------------", textFont));
        document.add(new Paragraph("Basic Salary: ₱" + String.format("%,.2f", employee.getBasicSalary()), textFont));
        document.add(new Paragraph("Gross Salary: ₱" + String.format("%,.2f", result.getGrossSalary()), textFont));
        document.add(new Paragraph("SSS Deduction: ₱" + String.format("%,.2f", result.getSssDeduction()), textFont));
        document.add(new Paragraph("PhilHealth Deduction: ₱" + String.format("%,.2f", result.getPhilhealthDeduction()), textFont));
        document.add(new Paragraph("Pag-IBIG Deduction: ₱" + String.format("%,.2f", result.getPagibigDeduction()), textFont));
        document.add(new Paragraph("Withholding Tax: ₱" + String.format("%,.2f", result.getWithholdingTax()), textFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("NET PAY: ₱" + String.format("%,.2f", result.getNetSalary()), textFont));

        document.close();

        JOptionPane.showMessageDialog(this,
        "Payslip PDF generated successfully!");

    } catch (Exception ex) {

        ex.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                "Error generating PDF: " + ex.getMessage()
        );
    }
}
    private void updateEmployee() {

        Employee updated = employeeDAO.findEmployeeById(employeeNum);
        if (updated == null) return;

        updated.setLastName(lastName.getText());
        updated.setFirstName(firstName.getText());
        updated.setBirthday(birthday.getText());
        updated.setAddress(address.getText());
        updated.setPhone(phone.getText());
        updated.setSss(sss.getText());
        updated.setPhilhealth(philhealth.getText());
        updated.setTin(tin.getText());
        updated.setPagibig(pagibig.getText());
        updated.setStatus(status.getText());
        updated.setPosition(position.getText());
        updated.setSupervisor(supervisor.getText());

        updated.setBasicSalary(Double.parseDouble(basicSalary.getText()));
        updated.setRiceSubsidy(Double.parseDouble(riceSubsidyField.getText()));
        updated.setPhoneAllowance(Double.parseDouble(phoneAllowanceField.getText()));
        updated.setClothingAllowance(Double.parseDouble(clothingAllowanceField.getText()));
        updated.setGrossRate(Double.parseDouble(grossRate.getText()));
        updated.setHourlyRate(Double.parseDouble(hourlyRateField.getText()));

        employeeDAO.updateEmployee(updated);

        JOptionPane.showMessageDialog(this, "Employee updated.");
    }

    private void deleteSelectedRow() {

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            employeeDAO.deleteEmployee(employeeNum);

            JOptionPane.showMessageDialog(this, "Employee deleted.");
            clearFields();
        }
    }

    private void clearFields() {

        JTextField[] fields = {
                empNo,lastName,firstName,status,position,supervisor,
                birthday,address,phone,sss,philhealth,tin,pagibig,
                basicSalary,riceSubsidyField,phoneAllowanceField,
                clothingAllowanceField,grossRate,hourlyRateField
        };

        for (JTextField f : fields) {
            f.setText("");
        }
    }
}