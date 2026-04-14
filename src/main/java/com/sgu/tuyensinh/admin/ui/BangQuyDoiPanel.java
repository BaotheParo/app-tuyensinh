package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.entity.BangQuyDoi;
import com.sgu.tuyensinh.service.BangQuyDoiService;
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

    private final BangQuyDoiService bangQuyDoiService;

    private BaseTablePanel tablePanel;
    private JTextField txtSearch;
    private JButton btnSearch, btnPrev, btnNext;
    private JLabel lblPage;

    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    public BangQuyDoiPanel(BangQuyDoiService bangQuyDoiService) {
        this.bangQuyDoiService = bangQuyDoiService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        loadData();
    }

    private void initComponents() {
        String[] columns = {"ID", "Phương Thức", "Môn/Tổ Hợp", "Điểm Gốc A", "Điểm Gốc B", "Quy Đổi C", "Quy Đổi D", "Ghi Chú"};
        tablePanel = new BaseTablePanel(columns);
        
        txtSearch = new JTextField(20);
        btnSearch = new JButton("Tìm Kiếm");
        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPage = new JLabel("Trang: 1/1");
    }

    private void layoutComponents() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Bộ lọc dữ liệu Quy Đổi"));
        searchPanel.add(new JLabel("Tìm theo Phương thức/Môn:"));
        searchPanel.add(txtSearch);
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
        Page<BangQuyDoi> pageData = bangQuyDoiService.layDanhSachPhanTrang(currentPage, pageSize, keyword);
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        for (BangQuyDoi b : pageData.getContent()) {
            tablePanel.addRow(new Object[]{
                b.getMaQuyDoi(), b.getPhuongThuc(), 
                (b.getMon() != null ? b.getMon() : b.getToHop()),
                b.getDiemGocA(), b.getDiemGocB(),
                b.getDiemQuyDoiC(), b.getDiemQuyDoiD(),
                b.getPhanVi()
            });
        }
    }
}
