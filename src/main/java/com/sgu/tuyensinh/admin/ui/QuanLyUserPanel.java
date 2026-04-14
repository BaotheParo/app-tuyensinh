package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.entity.User;
import com.sgu.tuyensinh.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

@Component
public class QuanLyUserPanel extends JPanel {
    private final UserService userService;
    private JTable userTable;
    private DefaultTableModel tableModel;

    // Các nhãn hiển thị thông tin nhanh
    private JLabel lblId, lblUsername, lblRole, lblStatus;

    @Autowired
    public QuanLyUserPanel(UserService userService) {
        this.userService = userService;
        initComponents();
        loadUsers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- PHẦN TRÊN: THÔNG TIN CHI TIẾT & NÚT ĐIỀU KHIỂN ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        TitledBorder border = BorderFactory.createTitledBorder("Bảng điều khiển người dùng");
        border.setTitleFont(new Font("Arial", Font.BOLD, 16)); // font Arial, in đậm, cỡ 16
        border.setTitleColor(Color.BLUE);
        topPanel.setBorder(border);
        topPanel.setPreferredSize(new Dimension(0, 150));

        // Panel hiển thị thông tin (dàn hàng ngang)
        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 20, 10));
        infoPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        lblId = new JLabel("ID: --");
        lblUsername = new JLabel("Username: --");
        lblRole = new JLabel("Role: --");
        lblStatus = new JLabel("Status: --");

        // Font cho các label thông tin
        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        lblId.setFont(labelFont);
        lblUsername.setFont(labelFont);
        lblRole.setFont(labelFont);
        lblStatus.setFont(labelFont);

        infoPanel.add(lblId);
        infoPanel.add(lblUsername);
        infoPanel.add(lblRole);
        infoPanel.add(lblStatus);

        // Panel chứa các nút bấm (bên phải)
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton addUserBtn = new JButton("Add User");
        JButton changeRoleBtn = new JButton("Đổi quyền");
        JButton changePasswordBtn = new JButton("Đổi Password");
        JButton toggleStatusBtn = new JButton("Active/Block");

        buttonPanel.add(addUserBtn);
        buttonPanel.add(changeRoleBtn);
        buttonPanel.add(changePasswordBtn);
        buttonPanel.add(toggleStatusBtn);

        topPanel.add(infoPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // --- PHẦN DƯỚI: DANH SÁCH BẢNG ---
        String[] columnNames = { "ID", "Username", "Role", "Status" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho edit trực tiếp trên table
            }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(30);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Thêm vào Panel chính
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // --- LOGIC SỰ KIỆN (Giữ nguyên như cũ) ---
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailLabels();
            }
        });

        addUserBtn.addActionListener(e -> openAddUserDialog());

        changeRoleBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                Long id = (Long) tableModel.getValueAt(row, 0);
                userService.toggleRole(id);

                loadUsers(); // Load lại dữ liệu vào TableModel

                // QUAN TRỌNG: Chọn lại dòng cũ để updateDetailLabels() lấy được dữ liệu mới
                userTable.setRowSelectionInterval(row, row);
                updateDetailLabels(); // Cập nhật lại các chữ hiển thị bên trên
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng!");
            }
        });

        changePasswordBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                Long id = (Long) tableModel.getValueAt(row, 0);
                String oldPass = JOptionPane.showInputDialog(this, "Nhập mật khẩu cũ:");
                if (oldPass == null)
                    return;
                String newPass = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới:");
                if (newPass == null)
                    return;

                if (userService.changePassword(id, oldPass, newPass)) {
                    JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Sai mật khẩu cũ!");
                }
                loadUsers();
            }
        });

        toggleStatusBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                Long id = (Long) tableModel.getValueAt(row, 0);
                userService.toggleStatus(id);

                loadUsers();

                userTable.setRowSelectionInterval(row, row); // Giữ vùng chọn
                updateDetailLabels(); // Cập nhật chữ
            }
        });
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        for (User u : userService.getAllUsers()) {
            tableModel.addRow(new Object[] {
                    u.getId(), u.getUsername(), u.getRole(),
                    u.getIsActive() != null && u.getIsActive() ? "Active" : "Blocked"
            });
        }
    }

    private void openAddUserDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[] { "USER", "ADMIN" });

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Thêm User mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            User newUser = new User();
            newUser.setUsername(usernameField.getText());
            newUser.setPassword(new String(passwordField.getPassword()));
            newUser.setRole((String) roleBox.getSelectedItem());
            userService.createUser(newUser);
            loadUsers();
        }
    }

    // 1. Viết hàm cập nhật Label riêng để tái sử dụng
    private void updateDetailLabels() {
        int row = userTable.getSelectedRow();
        if (row >= 0) {
            lblId.setText("ID: " + tableModel.getValueAt(row, 0));
            lblUsername.setText("Username: " + tableModel.getValueAt(row, 1));
            lblRole.setText("Role: " + tableModel.getValueAt(row, 2));
            lblStatus.setText("Status: " + tableModel.getValueAt(row, 3));
        } else {
            // Reset nếu không có dòng nào được chọn
            lblId.setText("ID: --");
            lblUsername.setText("Username: --");
            lblRole.setText("Role: --");
            lblStatus.setText("Status: --");
        }
    }

}