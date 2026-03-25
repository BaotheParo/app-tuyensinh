package com.sgu.tuyensinh.admin.ui.common;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BaseTablePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public BaseTablePanel(String[] columnNames) {
        setLayout(new BorderLayout());

        // Khởi tạo model với tên cột
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        // Thêm JScrollPane để cuộn
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    // Hàm thêm một dòng dữ liệu
    public void addRow(Object[] rowData) {
        tableModel.addRow(rowData);
    }

    // Hàm lấy JTable (nếu cần thao tác thêm)
    public JTable getTable() {
        return table;
    }
}
