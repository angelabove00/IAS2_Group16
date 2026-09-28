
package motorph_oop.ui;

import motorph_oop.service.EmployeeService;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

public class AddEmployeePanel extends JPanel {

    private final EmployeeService employeeService = new EmployeeService();

    // employee fields
    private JTextField empNo = UIUtils.createTextField(false);
    private JTextField lastName = UIUtils.createTextField(true);
    private JTextField firstName = UIUtils.createTextField(true);
    private JTextField status = UIUtils.createTextField(true);
    private JComboBox<String> position;
    private JTextField supervisor = UIUtils.createTextField(true);

    // personal info
    private DatePicker birthday;
    private JTextField address = UIUtils.createTextField(true);
    private JTextField phone = UIUtils.createTextField(true);
    private JTextField sss = UIUtils.createTextField(true);
    private JTextField philhealth = UIUtils.createTextField(true);
    private JTextField tin = UIUtils.createTextField(true);
    private JTextField pagibig = UIUtils.createTextField(true);

    // financial info
    private JTextField basicSalary = UIUtils.createTextField(true);
    private JTextField riceSubsidy = UIUtils.createTextField(true);
    private JTextField phoneAllowance = UIUtils.createTextField(true);
    private JTextField clothingAllowance = UIUtils.createTextField(true);
    private JTextField grossRate = UIUtils.createTextField(true);
    private JTextField hourlyRate = UIUtils.createTextField(false);

    public AddEmployeePanel() {

        setLayout(new BorderLayout());
        initDatePicker();
        initPositionDropdown();
        initLayout();
        initDefaults();
        initListeners();
    }

    // create date picker
    private void initDatePicker() {
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("MM/dd/yyyy");
        birthday = new DatePicker(settings);
    }

    // position dropdown
    private void initPositionDropdown() {
        position = new JComboBox<>(new String[]{
                "Chief Executive Officer","Chief Operating Officer",
                "Chief Finance Officer","Chief Marketing Officer",
                "IT Operations and Systems","HR Manager","HR Team Leader",
                "HR Rank and File","Accounting Head","Payroll Manager",
                "Payroll Team Leader","Payroll Rank and File",
                "Account Manager","Account Team Leader","Account Rank and File",
                "Sales & Marketing","Supply Chain and Logistics",
                "Customer Service and Relations"
        });

        position.addActionListener(e -> updateSupervisor());
    }

    // layout builder
    private void initLayout() {

        JButton addButton = UIUtils.createButton("Add New Employee", new Color(0,180,0), Color.WHITE);
        JButton clearButton = UIUtils.createButton("Clear", null, null);

        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(clearButton);

        JPanel formPanel = new JPanel(new GridLayout(3,1,5,2));

        formPanel.add(UIUtils.createEmployeeInfoPanel(
                empNo,lastName,firstName,status,position,supervisor));

        formPanel.add(UIUtils.createPersonalInfoPanel(
                birthday,address,phone,sss,philhealth,tin,pagibig));

        formPanel.add(UIUtils.createFinancialInfoPanel(
                basicSalary,riceSubsidy,phoneAllowance,
                clothingAllowance,grossRate,hourlyRate));

        add(buttons,BorderLayout.WEST);
        add(formPanel,BorderLayout.CENTER);

        addButton.addActionListener(e -> addEmployee());
        clearButton.addActionListener(e -> clearFields());
    }

    // initialize default values
    private void initDefaults() {
        setNextEmployeeNumber();
        updateSupervisor();
        status.setText("Probationary");
    }

    // register listeners
    private void initListeners() {

        addListener(basicSalary, this::calculateHourly);

        Runnable grossCalc = this::calculateGross;

        addListener(basicSalary, grossCalc);
        addListener(riceSubsidy, grossCalc);
        addListener(phoneAllowance, grossCalc);
        addListener(clothingAllowance, grossCalc);
    }

    // reusable document listener
    private void addListener(JTextField field, Runnable action) {

        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e){ action.run(); }
            public void removeUpdate(DocumentEvent e){ action.run(); }
            public void changedUpdate(DocumentEvent e){ action.run(); }
        });
    }
  
    // update supervisor
    private void updateSupervisor() {
        String pos = position.getSelectedItem().toString();
        supervisor.setText(employeeService.determineSupervisor(pos));
    }

    private void validateGovernmentIds() {

    String sssText = sss.getText().trim();
    String philhealthText = philhealth.getText().trim();
    String tinText = tin.getText().trim();
    String pagibigText = pagibig.getText().trim();

    // SSS: 10 digits
    if (!sssText.matches("\\d{10}")) {
        throw new IllegalArgumentException(
                "Invalid SSS Number.\nIt must contain exactly 10 digits and numbers only."
        );
    }

    // PhilHealth: 12 digits
    if (!philhealthText.matches("\\d{12}")) {
        throw new IllegalArgumentException(
                "Invalid PhilHealth Number.\nIt must contain exactly 12 digits and numbers only."
        );
    }

    // TIN: 9 digits
    if (!tinText.matches("\\d{9}")) {
        throw new IllegalArgumentException(
                "Invalid TIN Number.\nIt must contain exactly 9 digits and numbers only."
        );
    }

    // Pag-IBIG: 12 digits
    if (!pagibigText.matches("\\d{12}")) {
        throw new IllegalArgumentException(
                "Invalid Pag-IBIG Number.\nIt must contain exactly 12 digits and numbers only."
        );
        }
    }
    
    private void validateRequiredFields() {

    StringBuilder missing = new StringBuilder();

    if (lastName.getText().trim().isEmpty())
        missing.append("• Last Name\n");

    if (firstName.getText().trim().isEmpty())
        missing.append("• First Name\n");

    if (birthday.getText().trim().isEmpty())
        missing.append("• Birthday\n");

    if (address.getText().trim().isEmpty())
        missing.append("• Address\n");

    if (phone.getText().trim().isEmpty())
        missing.append("• Phone\n");

    if (sss.getText().trim().isEmpty())
        missing.append("• SSS Number\n");

    if (philhealth.getText().trim().isEmpty())
        missing.append("• PhilHealth Number\n");

    if (tin.getText().trim().isEmpty())
        missing.append("• TIN Number\n");

    if (pagibig.getText().trim().isEmpty())
        missing.append("• Pag-IBIG Number\n");

    if (basicSalary.getText().trim().isEmpty())
        missing.append("• Basic Salary\n");

    if (riceSubsidy.getText().trim().isEmpty())
        missing.append("• Rice Subsidy\n");

    if (phoneAllowance.getText().trim().isEmpty())
        missing.append("• Phone Allowance\n");

    if (clothingAllowance.getText().trim().isEmpty())
        missing.append("• Clothing Allowance\n");
    
    // Phone validation (11 digits and must start with 09)
    if (!phone.getText().trim().isEmpty()) {
    String phoneText = phone.getText().trim();

    if (!phoneText.matches("09\\d{9}")) {
        missing.append("• Phone must be 11 digits and start with 09\n");
    }
    }
    
    // Prevent negative values and zero
    if (!basicSalary.getText().trim().isEmpty()) {
        try {
        double val = Double.parseDouble(basicSalary.getText());
        if (val <= 0) {
            missing.append("• Basic Salary must be greater than 0\n");
        }
        } catch (NumberFormatException e) {
        missing.append("• Basic Salary must be a valid number\n");
        }
        }

     if (!riceSubsidy.getText().trim().isEmpty()) {
        try {
        double val = Double.parseDouble(riceSubsidy.getText());
        if (val <= 0) {
            missing.append("• Rice Subsidy must be greater than 0\n");
        }
        } catch (NumberFormatException e) {
        missing.append("• Rice Subsidy must be a valid number\n");
        }
        }
    
    if (!phoneAllowance.getText().trim().isEmpty()) {
        try {
        double val = Double.parseDouble(phoneAllowance.getText());
        if (val <= 0) {
            missing.append("• Phone Allowance must be greater than 0\n");
        }
        } catch (NumberFormatException e) {
        missing.append("• Phone Allowance must be a valid number\n");
        }
        }

    if (!clothingAllowance.getText().trim().isEmpty()) {
        try {
        double val = Double.parseDouble(clothingAllowance.getText());
        if (val <= 0) {
            missing.append("• Clothing Allowance must be greater than 0\n");
        }
        } catch (NumberFormatException e) {
        missing.append("• Clothing Allowance must be a valid number\n");
        }
        }
    
    // Prevent repeating digits

    if (!phone.getText().trim().isEmpty()) {
    if (isAllSameDigits(phone.getText().trim())) {
        missing.append("• Phone cannot contain repeating digits\n");
        }
        }

    if (!sss.getText().trim().isEmpty()) {
    if (isAllSameDigits(sss.getText().trim())) {
        missing.append("• SSS Number cannot contain repeating digits\n");
        }
        }

    if (!philhealth.getText().trim().isEmpty()) {
    if (isAllSameDigits(philhealth.getText().trim())) {
        missing.append("• PhilHealth Number cannot contain repeating digits\n");
        }
        }

    if (!tin.getText().trim().isEmpty()) {
    if (isAllSameDigits(tin.getText().trim())) {
        missing.append("• TIN Number cannot contain repeating digits\n");
        }
        }

    if (!pagibig.getText().trim().isEmpty()) {
    if (isAllSameDigits(pagibig.getText().trim())) {
        missing.append("• Pag-IBIG Number cannot contain repeating digits\n");
        }
        }
    
    if (missing.length() > 0) {
        throw new IllegalArgumentException(
                "Please fill in the following required fields:\n\n" + missing
        );
        }
        }
    
    // check if all digits are the same (e.g., 1111111111)
    private boolean isAllSameDigits(String value) {
    if (value == null || value.isEmpty()) return false;

    char first = value.charAt(0);
    for (char c : value.toCharArray()) {
        if (c != first) return false;
    }
    return true;
    }
    
    
    
    // calculate gross salary
    private void calculateGross() {

        try {
            double basic = parse(basicSalary);
            double rice = parse(riceSubsidy);
            double phone = parse(phoneAllowance);
            double clothing = parse(clothingAllowance);

            double gross = employeeService.calculateGrossRate(basic,rice,phone,clothing);
            grossRate.setText(String.format("%.2f",gross));

        } catch(Exception e){
            grossRate.setText("");
        }
    }

    // calculate hourly rate
    private void calculateHourly() {

        try {
            double salary = Double.parseDouble(basicSalary.getText());
            double hourly = employeeService.calculateHourlyRate(salary);
            hourlyRate.setText(String.format("%.2f",hourly));

        } catch(Exception e){
            hourlyRate.setText("");
        }
    }

    // helper for safe number parsing
    private double parse(JTextField field) {
        return field.getText().isEmpty() ? 0 : Double.parseDouble(field.getText());
    }

    // generate employee number
    private void setNextEmployeeNumber() {
        empNo.setText(employeeService.generateNextEmployeeNumber());
    }

    // clear form fields
    private void clearFields() {

        JTextField[] fields = {
                lastName,firstName,address,phone,sss,
                philhealth,tin,pagibig,basicSalary,
                riceSubsidy,phoneAllowance,clothingAllowance,
                grossRate,hourlyRate
        };

        for (JTextField f : fields) f.setText("");

        birthday.clear();
        updateSupervisor();
        status.setText("Probationary");
        position.setSelectedIndex(0);
    }

    // save employee
    private void addEmployee() {

        try {
            
            validateGovernmentIds();
            validateRequiredFields();
            
            employeeService.addEmployee(
                    empNo.getText().trim(),
                    lastName.getText().trim(),
                    firstName.getText().trim(),
                    status.getText().trim(),
                    position.getSelectedItem().toString(),
                    supervisor.getText().trim(),
                    birthday.getText(),
                    address.getText().trim(),
                    phone.getText().trim(),
                    sss.getText().trim(),
                    philhealth.getText().trim(),
                    tin.getText().trim(),
                    pagibig.getText().trim(),
                    basicSalary.getText().trim(),
                    riceSubsidy.getText().trim(),
                    phoneAllowance.getText().trim(),
                    clothingAllowance.getText().trim(),
                    grossRate.getText().trim(),
                    hourlyRate.getText().trim()
            );

            JOptionPane.showMessageDialog(this,"New employee added.");

            clearFields();
            setNextEmployeeNumber();

        } catch(IllegalArgumentException e){

            JOptionPane.showMessageDialog(
                    this,e.getMessage(),"Error",
                    JOptionPane.WARNING_MESSAGE);

        } catch(Exception e){

            JOptionPane.showMessageDialog(
                    this,"Unexpected error occurred.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}