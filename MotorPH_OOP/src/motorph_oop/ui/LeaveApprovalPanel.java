
package motorph_oop.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

import motorph_oop.model.LeaveRequest;
import motorph_oop.service.LeaveService;

public class LeaveApprovalPanel extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private JTextArea rejectionArea;

    private JButton approveBtn;
    private JButton rejectBtn;

    private final LeaveService leaveService = new LeaveService();

    private List<LeaveRequest> currentLeaves;

    public LeaveApprovalPanel(){

        setTitle("Leave Requests");
        setSize(800,450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new String[]{"Employee","Type","Start","End","Days","Reason","Status"},0){

            public boolean isCellEditable(int r,int c){
                return false;
            }
        };

        table = new JTable(model);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(6)
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
                                c.setForeground(Color.BLACK);
                        }

                        return c;
                    }
                });

        add(new JScrollPane(table),BorderLayout.CENTER);

        rejectionArea = new JTextArea(3,20);
        rejectionArea.setBorder(BorderFactory.createTitledBorder("Rejection Reason"));

        approveBtn = new JButton("Approve");
        rejectBtn = new JButton("Reject");

        approveBtn.setEnabled(false);
        rejectBtn.setEnabled(false);

        JPanel btnPanel = new JPanel();
        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(rejectionArea,BorderLayout.CENTER);
        bottom.add(btnPanel,BorderLayout.SOUTH);

        add(bottom,BorderLayout.SOUTH);

        approveBtn.addActionListener(e->approveSelected());
        rejectBtn.addActionListener(e->rejectSelected());

        table.getSelectionModel().addListSelectionListener(e->{

            boolean selected = table.getSelectedRow()!=-1;

            approveBtn.setEnabled(selected);
            rejectBtn.setEnabled(selected);
        });

        loadTableData();
    }

    private void loadTableData(){

        model.setRowCount(0);

        currentLeaves = leaveService.getAllLeaves();

        for(LeaveRequest r:currentLeaves){

            model.addRow(new Object[]{
                    r.getEmployeeName(),
                    r.getLeaveType(),
                    r.getStartDate(),
                    r.getEndDate(),
                    r.getTotalDays(),
                    r.getReason(),
                    r.getStatus()
            });
        }
    }

    private void approveSelected(){

        int row = table.getSelectedRow();

        if(row==-1){

            JOptionPane.showMessageDialog(this,"Select a request first.");
            return;
        }

        leaveService.approveLeave(currentLeaves.get(row));

        loadTableData();

        JOptionPane.showMessageDialog(this,"Request Approved.");
    }

    private void rejectSelected(){

        int row = table.getSelectedRow();

        if(row==-1){

            JOptionPane.showMessageDialog(this,"Select a request first.");
            return;
        }

        leaveService.rejectLeave(currentLeaves.get(row));

        loadTableData();

        JOptionPane.showMessageDialog(this,"Request Rejected.");
    }
}