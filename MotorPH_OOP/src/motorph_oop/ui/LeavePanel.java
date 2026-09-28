
package motorph_oop.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.github.lgooddatepicker.components.DatePicker;

import motorph_oop.model.LeaveRequest;
import motorph_oop.service.LeaveService;

public class LeavePanel extends JPanel {

    private final LeaveService leaveService = new LeaveService();

    private String employeeName;

    private JComboBox<String> leaveTypeBox;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private JTextArea reasonArea;

    private JTable statusTable;
    private DefaultTableModel statusModel;

    private int sickLeaveBalance = 10;
    private int vacationLeaveBalance = 10;

    public LeavePanel(String employeeName) {

        this.employeeName = employeeName;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200,700));

        JLabel heading = new JLabel("Leave Application");
        heading.setFont(new Font("Arial",Font.BOLD,20));
        heading.setBorder(BorderFactory.createEmptyBorder(20,20,10,0));

        add(heading,BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));

        mainPanel.add(createFormPanel());
        mainPanel.add(createStatusSection());

        add(mainPanel,BorderLayout.CENTER);

        refreshStatusTable();
    }

    private JPanel createFormPanel(){

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(10,5,10,20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx=0; gbc.gridy=0;
        form.add(new JLabel("Leave Type:"),gbc);

        gbc.gridx=1;
        leaveTypeBox = new JComboBox<>(new String[]{
                "Sick Leave",
                "Vacation Leave",
                "Emergency Leave",
                "Maternity Leave",
                "Paternity Leave"
        });
        form.add(leaveTypeBox,gbc);

        gbc.gridx=0; gbc.gridy=1;
        form.add(new JLabel("Start Date:"),gbc);

        gbc.gridx=1;
        startDatePicker = new DatePicker();
        form.add(startDatePicker,gbc);

        gbc.gridx=0; gbc.gridy=2;
        form.add(new JLabel("End Date:"),gbc);

        gbc.gridx=1;
        endDatePicker = new DatePicker();
        form.add(endDatePicker,gbc);

        gbc.gridx=0; gbc.gridy=3;
        form.add(new JLabel("Reason:"),gbc);

        gbc.gridx=1;
        reasonArea = new JTextArea(4,18);
        form.add(new JScrollPane(reasonArea),gbc);

        JButton submitBtn = new JButton("Submit");

        gbc.gridx=1; gbc.gridy=4;
        form.add(submitBtn,gbc);

        submitBtn.addActionListener(e -> submitLeave());

        return form;
    }

    private JPanel createStatusSection(){

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));

        JLabel label = new JLabel("My Leave Requests");
        label.setFont(new Font("Arial",Font.BOLD,16));

        panel.add(label);

        statusModel = new DefaultTableModel(
                new String[]{"Type","Start","End","Days","Status"},0
        );

        statusTable = new JTable(statusModel);

        statusTable.getColumnModel().getColumn(4)
                .setCellRenderer(new DefaultTableCellRenderer(){

                    public Component getTableCellRendererComponent(
                            JTable table,Object value,boolean selected,
                            boolean focus,int row,int column){

                        Component c = super.getTableCellRendererComponent(
                                table,value,selected,focus,row,column);

                        if(value!=null){

                            String status=value.toString().toLowerCase();

                            if(status.contains("approved"))
                                c.setForeground(new Color(0,130,0));

                            else if(status.contains("rejected"))
                                c.setForeground(Color.RED);

                            else
                                c.setForeground(new Color(255,140,0));
                        }

                        return c;
                    }
                });

        JScrollPane scroll = new JScrollPane(statusTable);
        scroll.setPreferredSize(new Dimension(900,150));

        panel.add(scroll);

        return panel;
    }

    private void submitLeave(){

        LocalDate start = startDatePicker.getDate();
        LocalDate end = endDatePicker.getDate();

        if(start==null || end==null){

            JOptionPane.showMessageDialog(this,"Select start and end dates.");
            return;
        }

        int days = (int)ChronoUnit.DAYS.between(start,end)+1;

        if(days<=0){

            JOptionPane.showMessageDialog(this,"End date must be after start date.");
            return;
        }

        String type = (String)leaveTypeBox.getSelectedItem();

        if(type.equals("Sick Leave") && sickLeaveBalance<=0){

            JOptionPane.showMessageDialog(this,"No Sick Leave balance.");
            return;
        }

        if(type.equals("Vacation Leave") && vacationLeaveBalance<=0){

            JOptionPane.showMessageDialog(this,"No Vacation Leave balance.");
            return;
        }

        try{

            LeaveRequest request = leaveService.submitLeave(
                    employeeName,
                    type,
                    start,
                    end,
                    reasonArea.getText().trim()
            );

            if(type.equals("Sick Leave")) sickLeaveBalance--;
            if(type.equals("Vacation Leave")) vacationLeaveBalance--;

            JOptionPane.showMessageDialog(this,
                    "Leave Request Submitted!\nStatus: "+request.getStatus());

            refreshStatusTable();
            clearFields();

        }
        catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }

    private void refreshStatusTable(){

        statusModel.setRowCount(0);

        List<LeaveRequest> leaves =
                leaveService.getLeavesByEmployee(employeeName);

        for(LeaveRequest r:leaves){

            statusModel.addRow(new Object[]{
                    r.getLeaveType(),
                    r.getStartDate(),
                    r.getEndDate(),
                    r.getTotalDays(),
                    r.getStatus()
            });
        }
    }

    private void clearFields(){

        startDatePicker.clear();
        endDatePicker.clear();
        reasonArea.setText("");
        leaveTypeBox.setSelectedIndex(0);
    }
}