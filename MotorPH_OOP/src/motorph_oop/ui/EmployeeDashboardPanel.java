
package motorph_oop.ui;

import motorph_oop.model.Employee;
import motorph_oop.service.EmployeeService;
import motorph_oop.util.Constants;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeDashboardPanel extends JPanel {

    // service
    private final EmployeeService employeeService = new EmployeeService();

    // table
    private JTable table;
    private DefaultTableModel model;
    private List<Employee> employeeList;

    // form panel
    private JPanel formPanel;

    // fields
    private JTextField empNo = UIUtils.createTextField(false);
    private JTextField lastName = UIUtils.createTextField(true);
    private JTextField firstName = UIUtils.createTextField(true);
    private JTextField status = UIUtils.createTextField(true);
    private JTextField position = UIUtils.createTextField(true);
    private JTextField supervisor = UIUtils.createTextField(true);

    public EmployeeDashboardPanel() {

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.SOUTH);

        loadEmployees();
    }

    // header
    private JPanel createHeader(){

        JPanel panel = new JPanel(new BorderLayout());

        JLabel title = UIUtils.createHeaderLabel("Employee Database");

        panel.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
        panel.add(title, BorderLayout.WEST);

        return panel;
    }

    // Table section
    private JPanel createTablePanel(){

        JPanel panel = new JPanel(new BorderLayout());

        model = new DefaultTableModel(Constants.EMPLOYEE_COLUMNS,0);

        table = new JTable(model);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ENABLE HORIZONTAL SCROLLING
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getSelectionModel().addListSelectionListener(e -> handleRowSelection());

        JScrollPane scroll = new JScrollPane(
                table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scroll.setPreferredSize(new Dimension(1000,360));

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // form section
    private JPanel createFormPanel(){

        JPanel container = new JPanel(new BorderLayout());

        formPanel = new JPanel(new GridLayout(3,1,5,5));

        formPanel.add(
                UIUtils.createEmployeeInfoPanel(
                        empNo,
                        lastName,
                        firstName,
                        status,
                        position,
                        supervisor
                )
        );

        container.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
        container.add(formPanel, BorderLayout.CENTER);

        return container;
    }

    // load employee
    private void loadEmployees(){

        model.setRowCount(0);

        employeeList = employeeService.getEmployees();

        for(Employee emp : employeeList){
            model.addRow(convertToRow(emp));
        }
    }

    // convert employee table to row
    private Object[] convertToRow(Employee emp){

        return new Object[]{
                emp.getEmpNo(),
                emp.getLastName(),
                emp.getFirstName(),
                emp.getBirthday(),
                emp.getAddress(),
                emp.getPhone(),
                emp.getSss(),
                emp.getPhilhealth(),
                emp.getTin(),
                emp.getPagibig(),
                emp.getStatus(),
                emp.getPosition(),
                emp.getSupervisor(),
                emp.getBasicSalary(),
                emp.getRiceSubsidy(),
                emp.getPhoneAllowance(),
                emp.getClothingAllowance(),
                emp.getGrossRate(),
                emp.getHourlyRate()
        };
    }

    // handle table row click
    private void handleRowSelection(){

        int row = table.getSelectedRow();

        if(row >= 0 && employeeList != null){
            populateFields(employeeList.get(row));
        }
    }

    // Populate the fields
    private void populateFields(Employee emp){

        empNo.setText(emp.getEmpNo());
        lastName.setText(emp.getLastName());
        firstName.setText(emp.getFirstName());
        status.setText(emp.getStatus());
        position.setText(emp.getPosition());
        supervisor.setText(emp.getSupervisor());
    }

    // public methods used by admnin dashboard

    public void reloadEmployees(){
        loadEmployees();
        clearFields();
    }

    public String getSelectedEmployeeNo(){

        int row = table.getSelectedRow();

        if(row >= 0 && employeeList != null){
            return employeeList.get(row).getEmpNo();
        }

        return null;
    }

    public void clearFields(){

        JTextField[] fields = {
                empNo,lastName,firstName,status,position,supervisor
        };

        for(JTextField field : fields){
            field.setText("");
        }
    }
}