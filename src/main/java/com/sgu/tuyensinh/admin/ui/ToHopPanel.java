package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.entity.ToHop;
import com.sgu.tuyensinh.service.ToHopImportService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

@Component
public class ToHopPanel extends JPanel {

    private final ToHopImportService toHopService;

    // Components
    private BaseTablePanel tablePanel;
    private JTextField txtMaToHop, txtTenToHop, txtMon1, txtMon2, txtMon3;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnPrev, btnNext;
    private JLabel lblPage;

    private Integer currentSelectedId = null; // Lưu ID (khóa chính) của dòng đang chọn

    // Phân trang
    private int currentPage = 0;
    private final int pageSize = 15;
    private int totalPages = 1;

    public ToHopPanel(ToHopImportService toHopService) {
        this.toHopService = toHopService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        loadData();
    }

    private void initComponents() {
        // Cột ID ẩn đi (chỉ lưu trữ ngầm)
        String[] columns = {"ID", "Mã Tổ Hợp", "Tên Tổ Hợp", "Môn 1", "Môn 2", "Môn 3"};
        tablePanel = new BaseTablePanel(columns);
        tablePanel.getTable().setRowHeight(30);

        // Ẩn cột ID trên UI nhưng vẫn giữ data
        tablePanel.getTable().getColumnModel().getColumn(0).setMinWidth(0);
        tablePanel.getTable().getColumnModel().getColumn(0).setMaxWidth(0);
        tablePanel.getTable().getColumnModel().getColumn(0).setWidth(0);

        txtMaToHop = new JTextField(10);
        txtTenToHop = new JTextField(20);
        txtMon1 = new JTextField(10);
        txtMon2 = new JTextField(10);
        txtMon3 = new JTextField(10);

        btnAdd = new JButton("Thêm Mới");
        btnUpdate = new JButton("Cập Nhật");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Làm Mới Form");

        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPage = new JLabel("Trang: 1/1");
    }

    private void layoutComponents() {
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Tổ Hợp"));

        formPanel.add(new JLabel("Mã Tổ Hợp:")); formPanel.add(txtMaToHop);
        formPanel.add(new JLabel("Tên Tổ Hợp:")); formPanel.add(txtTenToHop);
        formPanel.add(new JLabel("Môn 1:")); formPanel.add(txtMon1);
        formPanel.add(new JLabel("Môn 2:")); formPanel.add(txtMon2);
        formPanel.add(new JLabel("Môn 3:")); formPanel.add(txtMon3);

        JPanel actionPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnClear);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(actionPanel, BorderLayout.EAST);

        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pagingPanel.add(btnPrev);
        pagingPanel.add(lblPage);
        pagingPanel.add(btnNext);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(pagingPanel, BorderLayout.SOUTH);
    }

    private void addEventHandlers() {
        JTable table = tablePanel.getTable();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                currentSelectedId = Integer.parseInt(table.getValueAt(row, 0).toString());
                txtMaToHop.setText(table.getValueAt(row, 1).toString());
                txtTenToHop.setText(table.getValueAt(row, 2) != null ? table.getValueAt(row, 2).toString() : "");
                txtMon1.setText(table.getValueAt(row, 3).toString());
                txtMon2.setText(table.getValueAt(row, 4).toString());
                txtMon3.setText(table.getValueAt(row, 5).toString());

                txtMaToHop.setEditable(false); // Tránh sửa mã khi update
            }
        });

        btnAdd.addActionListener(e -> executeSave(true));
        btnUpdate.addActionListener(e -> executeSave(false));
        btnDelete.addActionListener(e -> executeDelete());
        btnClear.addActionListener(e -> clearForm());

        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages - 1) { currentPage++; loadData(); } });
    }

    private void loadData() {
        Page<ToHop> pageData = toHopService.layDanhSachPhanTrang(currentPage, pageSize, "");
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        for (ToHop t : pageData.getContent()) {
            tablePanel.addRow(new Object[]{
                    t.getIdtohop(), t.getMaToHop(), t.getTenToHop(),
                    t.getMon1(), t.getMon2(), t.getMon3()
            });
        }
    }

    private void executeSave(boolean isNew) {
        try {
            ToHop t = new ToHop();
            if (!isNew && currentSelectedId != null) {
                t.setIdtohop(currentSelectedId); // Bắt buộc phải có ID để JPA hiểu là Update
            }

            t.setMaToHop(txtMaToHop.getText().trim().toUpperCase());
            t.setTenToHop(txtTenToHop.getText().trim());
            t.setMon1(txtMon1.getText().trim().toUpperCase());
            t.setMon2(txtMon2.getText().trim().toUpperCase());
            t.setMon3(txtMon3.getText().trim().toUpperCase());

            toHopService.luuToHop(t);
            JOptionPane.showMessageDialog(this, (isNew ? "Thêm" : "Cập nhật") + " thành công!");
            clearForm();
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeDelete() {
        if (currentSelectedId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một tổ hợp để xóa.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa tổ hợp này?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                toHopService.xoaToHop(currentSelectedId);
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                clearForm();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa do tổ hợp đang được liên kết với Ngành.", "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        currentSelectedId = null;
        txtMaToHop.setText(""); txtTenToHop.setText("");
        txtMon1.setText(""); txtMon2.setText(""); txtMon3.setText("");
        txtMaToHop.setEditable(true);
        tablePanel.getTable().clearSelection();
    }
}