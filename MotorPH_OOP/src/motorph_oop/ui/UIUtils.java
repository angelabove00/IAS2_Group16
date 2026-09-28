
package motorph_oop.ui;

import javax.swing.*;
import java.awt.*;

// Utility class for creating reusable UI components.

public class UIUtils {

    // Creates a titled panel with specified layout.
    public static JPanel createTitledPanel(String title,
                                           LayoutManager layout) {

        JPanel panel = new JPanel(layout);
        panel.setBorder(
                BorderFactory.createTitledBorder(title)
        );

        return panel;
    }

    // Creates a text field with editable option.
    public static JTextField createTextField(boolean editable) {

        JTextField textField = new JTextField();
        textField.setEditable(editable);

        return textField;
    }

    // Creates a standard button.
    public static JButton createButton(String text,
                                       Color bgColor,
                                       Color fgColor) {

        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setMaximumSize(new Dimension(180, 30));

        return button;
    }

    // Creates a navigation button.
    public static JButton createNavButton(String text,
                                          Color bgColor,
                                          Color fgColor) {

        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Padding: top, left, bottom, right
        button.setBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20)
        );

        button.setMaximumSize(new Dimension(180, 30));

        return button;
    }

    // Creates a header label.
    public static JLabel createHeaderLabel(String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(
                BorderFactory.createEmptyBorder(20, 10, 10, 0)
        );

        return label;
    }

    // Creates employee information panel.
    public static JPanel createEmployeeInfoPanel(
            JTextField empNo,
            JTextField lastName,
            JTextField firstName,
            JTextField status,
            JComponent position,
            JTextField supervisor) {

        JPanel empInfo =
                createTitledPanel(
                        "Employee Information",
                        new GridLayout(3, 2, 5, 5)
                );

        empInfo.add(new JLabel("EmployeeNo:"));
        empInfo.add(empNo);

        empInfo.add(new JLabel("LastName:"));
        empInfo.add(lastName);

        empInfo.add(new JLabel("FirstName:"));
        empInfo.add(firstName);

        empInfo.add(new JLabel("Status:"));
        empInfo.add(status);

        empInfo.add(new JLabel("Position:"));
        empInfo.add(position);

        empInfo.add(new JLabel("Supervisor:"));
        empInfo.add(supervisor);

        return empInfo;
    }

    // Creates personal information panel.
    public static JPanel createPersonalInfoPanel(
            JComponent birthday,
            JTextField address,
            JTextField phone,
            JTextField sss,
            JTextField philhealth,
            JTextField tin,
            JTextField pagibig) {

        JPanel personalInfo =
                createTitledPanel(
                        "Personal Information",
                        new GridLayout(7, 2, 5, 5)
                );

        personalInfo.add(new JLabel("Birthday:"));
        personalInfo.add(birthday);

        personalInfo.add(new JLabel("Address:"));
        personalInfo.add(address);

        personalInfo.add(new JLabel("Phone:"));
        personalInfo.add(phone);

        personalInfo.add(new JLabel("SSS:"));
        personalInfo.add(sss);

        personalInfo.add(new JLabel("PhilHealth:"));
        personalInfo.add(philhealth);

        personalInfo.add(new JLabel("TIN:"));
        personalInfo.add(tin);

        personalInfo.add(new JLabel("Pagibig:"));
        personalInfo.add(pagibig);

        return personalInfo;
    }

    // Creates financial information panel.
    public static JPanel createFinancialInfoPanel(
            JTextField basicSalary,
            JTextField riceSubsidy,
            JTextField phoneAllowance,
            JTextField clothingAllowance,
            JTextField grossRate,
            JTextField hourlyRate) {

        JPanel financialInfo =
                createTitledPanel(
                        "Financial Information",
                        new GridLayout(6, 2, 5, 5)
                );

        financialInfo.add(new JLabel("Basic Salary:"));
        financialInfo.add(basicSalary);

        financialInfo.add(new JLabel("Rice Subsidy:"));
        financialInfo.add(riceSubsidy);

        financialInfo.add(new JLabel("Phone Allowance:"));
        financialInfo.add(phoneAllowance);

        financialInfo.add(new JLabel("Clothing Allowance:"));
        financialInfo.add(clothingAllowance);

        financialInfo.add(new JLabel("Gross Rate:"));
        financialInfo.add(grossRate);

        financialInfo.add(new JLabel("Hourly Rate:"));
        financialInfo.add(hourlyRate);

        return financialInfo;
    }

    static PopupMenu createEmployeeInfoPanel(JTextField empNo, JTextField lastName, JTextField firstName, JComboBox<String> statusBox, JComboBox<String> position, JTextField supervisor) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}