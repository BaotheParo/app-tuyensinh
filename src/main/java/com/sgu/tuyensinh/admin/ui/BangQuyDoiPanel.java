package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.ImportPanel;
import com.sgu.tuyensinh.entity.BangQuyDoi;
import com.sgu.tuyensinh.service.BangQuyDoiImportService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel hiển thị Bảng Quy Đổi (Read-only để kiểm tra dữ liệu).
 */
@Component
public class BangQuyDoiPanel extends JPanel {

    private final BangQuyDoiImportService bangQuyDoiService;

    private BaseTablePanel tablePanel;
    private JTextField txtSearch;
    private JButton btnSearch, btnPrev, btnNext;
    private JLabel lblPage;
    private JButton btnImport;

    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    public BangQuyDoiPanel(BangQuyDoiImportService bangQuyDoiService) {
        this.bangQuyDoiService = bangQuyDoiService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        // loadData(); // Defer loading
    }

    private void initComponents() {
        // Rút gọn các cột dư thừa, tập trung vào thông tin quan trọng
        String[] columns = { "Mã", "Phương thức", "Đối tượng quy đổi", "Mức điểm gốc", "Điểm quy đổi", "Ghi chú" };
        tablePanel = new BaseTablePanel(columns);
        
        // Chỉnh style table giống các panel khác (ThiSinh, Nganh)
        JTable table = tablePanel.getTable();
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Căn giữa cho các cột điểm số
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Mức điểm gốc
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Điểm quy đổi

        txtSearch = new JTextField(25);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo phương thức hoặc môn...");
        
        btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(new Color(70, 130, 180));
        btnSearch.setForeground(Color.WHITE);

        btnPrev = new JButton("Trước");
        btnNext = new JButton("Sau");
        lblPage = new JLabel("Trang: 1/1");
        lblPage.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnImport = new JButton("Nạp dữ liệu quy đổi (Excel)");
        btnImport.setBackground(new Color(46, 204, 113));
        btnImport.setForeground(Color.WHITE);
        btnImport.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void layoutComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(20, 0));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchWrap.setOpaque(false);
        searchWrap.add(new JLabel("Bộ lọc:"));
        searchWrap.add(txtSearch);
        searchWrap.add(btnSearch);

        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionWrap.setOpaque(false);
        actionWrap.add(btnImport);

        topPanel.add(searchWrap, BorderLayout.WEST);
        topPanel.add(actionWrap, BorderLayout.EAST);

        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pagingPanel.setOpaque(false);
        pagingPanel.add(btnPrev);
        pagingPanel.add(lblPage);
        pagingPanel.add(btnNext);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(pagingPanel, BorderLayout.SOUTH);
    }

    private void addEventHandlers() {
        btnSearch.addActionListener(e -> {
            currentPage = 0;
            loadData();
        });
        txtSearch.addActionListener(e -> {
            currentPage = 0;
            loadData();
        });
        btnPrev.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadData();
            }
        });
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadData();
            }
        });

        btnImport.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parentWindow, "Import Bảng Quy Đổi", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            ImportPanel importPanel = new ImportPanel(
                    (inputStream, callback) -> bangQuyDoiService.importFromExcel(inputStream, callback));

            dialog.add(importPanel);
            dialog.pack();
            dialog.setMinimumSize(new Dimension(550, 300));
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            loadData();
        });
    }

    public void loadData() {
        String keyword = txtSearch.getText().trim();
        Page<BangQuyDoi> pageData = bangQuyDoiService.layDanhSachPhanTrang(currentPage, pageSize, keyword);
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        for (BangQuyDoi b : pageData.getContent()) {
            // Logic hiển thị khoảng điểm thông minh hơn
            String mucDiemGoc;
            if (b.getDiemGocB() != null && b.getDiemGocB() > b.getDiemGocA()) {
                mucDiemGoc = b.getDiemGocA() + " - " + b.getDiemGocB();
            } else {
                mucDiemGoc = String.valueOf(b.getDiemGocA());
            }

            tablePanel.addRow(new Object[] {
                    b.getMaQuyDoi(),
                    b.getPhuongThuc(),
                    (b.getMon() != null ? b.getMon() : b.getToHop()),
                    mucDiemGoc,
                    b.getDiemQuyDoiC(),
                    b.getPhanVi()
            });
        }
    }
}
