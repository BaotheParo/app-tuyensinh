package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.service.NguyenVongServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel hiển thị Danh sách Điểm Xét Tuyển theo nguyện vọng.
 * Bao gồm: CCCD, Họ, Tên, Nguyện Vọng, THM, Điểm THM, Điểm Cộng, Điểm Ưu Tiên, Điểm Xét Tuyển.
 */
@Component
public class DiemXetTuyenPanel extends JPanel {

    private final NguyenVongServiceImpl nguyenVongService;
    private BaseTablePanel tablePanel;
    private JTextField txtSearch;
    private JButton btnSearch, btnRefresh, btnPrev, btnNext;
    private JLabel lblPage;

    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    public DiemXetTuyenPanel(NguyenVongServiceImpl nguyenVongService) {
        this.nguyenVongService = nguyenVongService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        // loadData(); // Defer loading to speed up startup
    }

    private void initComponents() {
        String[] columns = { "CCCD", "Họ Tên", "Nguyện Vọng", "THM", "Điểm THM", "Điểm Cộng", "Điểm ƯT", "Điểm Xét Tuyển" };
        tablePanel = new BaseTablePanel(columns);
        tablePanel.getTable().setRowHeight(30);

        txtSearch = new JTextField(20);
        btnSearch = new JButton("Tìm Kiếm");
        btnRefresh = new JButton("Làm Mới");
        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPage = new JLabel("Trang: 1/1");
        
        btnSearch.setBackground(new Color(52, 152, 219));
        btnSearch.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(46, 204, 113));
        btnRefresh.setForeground(Color.WHITE);
    }

    private void layoutComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm (CCCD/Mã Ngành):"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        
        topPanel.add(searchPanel, BorderLayout.WEST);
        
        JLabel lblTitle = new JLabel("BẢNG TỔNG HỢP ĐIỂM XÉT TUYỂN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        topPanel.add(lblTitle, BorderLayout.SOUTH);

        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
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
        btnRefresh.addActionListener(e -> {
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
    }

    public void loadData() {
        String keyword = txtSearch.getText().trim();
        Page<NguyenVong> pageData = nguyenVongService.layDanhSachPhanTrang(currentPage, pageSize, keyword);
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        for (NguyenVong n : pageData.getContent()) {
            String hoTen = n.getThiSinh() != null ? n.getThiSinh().getHoTen() : "N/A";
            String nvDisplay = "NV " + n.getNvTt() + " - " + n.getNvManganh();
            
            tablePanel.addRow(new Object[] {
                    n.getNnCccd(),
                    hoTen,
                    nvDisplay,
                    n.getTtThm() != null ? n.getTtThm() : "Chưa chọn",
                    formatScore(n.getDiemThxt()),
                    formatScore(n.getDiemCong()),
                    formatScore(n.getDiemUtqd()),
                    formatScore(n.getDiemXetTuyen())
            });
        }
        
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
    }

    private String formatScore(Double score) {
        if (score == null) return "0.0";
        return String.format("%.2f", score);
    }
}
