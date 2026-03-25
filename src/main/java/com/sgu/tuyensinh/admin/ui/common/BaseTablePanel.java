package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BaseTablePanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public BaseTablePanel(String[] columnNames) {
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addRow(Object[] rowData) {
        tableModel.addRow(rowData);
    }

    public JTable getTable() {
        return table;
    }
}
