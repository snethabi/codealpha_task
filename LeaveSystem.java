import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LeaveSystem extends JFrame {

    private static final String LEAVE_FILE = "leave_applications.txt";
    private JTextArea displayArea;
    private JTextField employeeIdField, startDateField, endDateField, reasonField;

    public LeaveSystem() {
        setTitle("Employee Leave Management System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Application Panel ---
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Employee ID:"));
        employeeIdField = new JTextField();
        inputPanel.add(employeeIdField);
        inputPanel.add(new JLabel("Start Date (YYYY-MM-DD):"));
        startDateField = new JTextField();
        inputPanel.add(startDateField);
        inputPanel.add(new JLabel("End Date (YYYY-MM-DD):"));
        endDateField = new JTextField();
        inputPanel.add(endDateField);
        inputPanel.add(new JLabel("Reason:"));
        reasonField = new JTextField();
        inputPanel.add(reasonField);

        JButton applyButton = new JButton("Apply for Leave");
        applyButton.addActionListener(new ApplyListener());
        inputPanel.add(applyButton);

        JButton adminViewButton = new JButton("View Admin Panel");
        adminViewButton.addActionListener(e -> new AdminPanel().setVisible(true));
        inputPanel.add(adminViewButton);

        add(inputPanel, BorderLayout.NORTH);

        // --- Display Area (History/Status) ---
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        add(scrollPane, BorderLayout.CENTER);

        loadLeaveHistory();
    }

    private void loadLeaveHistory() {
        displayArea.setText("--- Leave History ---\n");
        try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                displayArea.append(line + "\n");
            }
        } catch (IOException e) {
            displayArea.append("No previous leave records found.\n");
        }
    }

    private class ApplyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String empId = employeeIdField.getText();
            String start = startDateField.getText();
            String end = endDateField.getText();
            String reason = reasonField.getText();

            if (empId.isEmpty() || start.isEmpty() || end.isEmpty() || reason.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill all fields.");
                return;
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(LEAVE_FILE, true))) {
                String record = String.format("%s,%s,%s,%s,Pending\n", empId, start, end, reason);
                bw.write(record);
                JOptionPane.showMessageDialog(null, "Leave application submitted successfully.");
                clearFields();
                loadLeaveHistory(); // Refresh the display
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error saving application: " + ex.getMessage());
            }
        }

        private void clearFields() {
            employeeIdField.setText("");
            startDateField.setText("");
            endDateField.setText("");
            reasonField.setText("");
        }
    }

    // --- Admin Panel (Inner Class) ---
    private class AdminPanel extends JFrame {
        private JTextArea adminDisplayArea;

        public AdminPanel() {
            setTitle("Admin View - Pending Applications");
            setSize(400, 300);
            adminDisplayArea = new JTextArea();
            adminDisplayArea.setEditable(false);
            JScrollPane sp = new JScrollPane(adminDisplayArea);
            add(sp, BorderLayout.CENTER);

            JButton refreshButton = new JButton("Refresh/Process (Manual)");
            // In a real app, this would list requests in a table for individual action
            refreshButton.addActionListener(e -> displayPendingRequests());
            add(refreshButton, BorderLayout.SOUTH);

            displayPendingRequests();
        }

        private void displayPendingRequests() {
            adminDisplayArea.setText("--- Pending Applications ---\n");
            List<String> pending = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(LEAVE_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.endsWith("Pending")) {
                        pending.add(line);
                        adminDisplayArea.append(line + "\n");
                    }
                }
            } catch (IOException e) {
                adminDisplayArea.append("Error reading file.\n");
            }
            if (pending.isEmpty()) {
                adminDisplayArea.append("No pending requests found.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LeaveSystem().setVisible(true);
        });
    }
}
