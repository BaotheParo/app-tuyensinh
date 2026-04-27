package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.service.NguyenVongService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel hiển thị Nguyện Vọng (Read-only để kiểm tra dữ liệu).
 */
@Component
public class NguyenVongPanel extends JPanel {

    private final NguyenVongService nguyenVongService;

    private BaseTablePanel tablePanel;
    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;
    private JButton btnSearch, btnPrev, btnNext;
    private JLabel lblPage;

    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    public NguyenVongPanel(NguyenVongService nguyenVongService) {
        this.nguyenVongService = nguyenVongService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        loadData();
    }

    private void initComponents() {
        String[] columns = {"Thứ Tự", "CCCD Thí Sinh", "Mã Ngành", "Mã Tổ Hợp", "Phương Thức", "Trạng Thái"};
        tablePanel = new BaseTablePanel(columns);
        
        txtSearch = new JTextField(20);
        btnSearch = new JButton("Tìm Kiếm");
        cbStatusFilter = new JComboBox<>(new String[]{"Tất cả", "TRUNG_TUYEN", "TRUOT", "DANG_XET", "ERROR", "KHONG_HOP_LE"});
        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPage = new JLabel("Trang: 1/1");
    }

    private void layoutComponents() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Bộ lọc dữ liệu Nguyện Vọng"));
        searchPanel.add(new JLabel("Tìm theo CCCD / Mã Ngành:"));
        searchPanel.add(txtSearch);
        searchPanel.add(new JLabel("  Trạng thái:"));
        searchPanel.add(cbStatusFilter);
        searchPanel.add(btnSearch);

        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pagingPanel.add(btnPrev);
        pagingPanel.add(lblPage);
        pagingPanel.add(btnNext);

        add(searchPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(pagingPanel, BorderLayout.SOUTH);
    }

    private void addEventHandlers() {
        btnSearch.addActionListener(e -> { currentPage = 0; loadData(); });
        txtSearch.addActionListener(e -> { currentPage = 0; loadData(); });
        btnPrev.addActionListener(e -> { if (currentPage > 0) { currentPage--; loadData(); } });
        btnNext.addActionListener(e -> { if (currentPage < totalPages - 1) { currentPage++; loadData(); } });
    }

    private void loadData() {
        String keyword = txtSearch.getText().trim();
                            String status = cbStatusFilter.getSelectedItem().toString();
                    Page<NguyenVong> pageData = nguyenVongService.layDanhSachPhanTrang(currentPage, pageSize, keyword, status);
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        for (NguyenVong n : pageData.getContent()) {
            tablePanel.addRow(new Object[]{
                n.getNvTt(), n.getNnCccd(), n.getNvManganh(), n.getTtThm(),
                n.getTtPhuongthuc(), n.getNvKetQua()
            });
        }
    }
}
