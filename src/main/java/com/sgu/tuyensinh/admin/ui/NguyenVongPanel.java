package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.ImportPanel;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.service.NguyenVongServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Panel hiển thị Nguyện Vọng (Read-only để kiểm tra dữ liệu).
 */
@Component
public class NguyenVongPanel extends JPanel {

    private final NguyenVongServiceImpl nguyenVongService;
    private BaseTablePanel tablePanel;
    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;
    private JButton btnSearch, btnPrev, btnNext, btnRefresh;
    private JLabel lblPage;
    private JButton btnImport, btnDeleteAll;
    private JLabel lblStatTotal, lblStatTrungTuyen, lblStatChoXet;

    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    public NguyenVongPanel(NguyenVongServiceImpl nguyenVongService) {
        this.nguyenVongService = nguyenVongService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
    }

    private void initComponents() {
        String[] columns = { 
            "STT", "CCCD", "Họ Tên", "NV", "Mã Ngành", 
            "Điểm Sàn", "Tổ Hợp", "Điểm THM", "Điểm Cộng", "Điểm ƯT", "Điểm XT", "Kết Quả" 
        };
        tablePanel = new BaseTablePanel(columns);
        tablePanel.getTable().setRowHeight(32);
        
        // Căn chỉnh độ rộng cột
        tablePanel.getTable().getColumnModel().getColumn(0).setPreferredWidth(40);
        tablePanel.getTable().getColumnModel().getColumn(3).setPreferredWidth(40);
        tablePanel.getTable().getColumnModel().getColumn(5).setPreferredWidth(60); // Điểm Sàn

        txtSearch = new JTextField(20);
        btnSearch = new JButton("Tìm Kiếm");
        btnRefresh = new JButton("Làm mới");
        
        cbStatusFilter = new JComboBox<>(new String[]{"Tất cả", "TRUNG_TUYEN", "TRUOT", "DANG_XET", "ERROR", "KHONG_HOP_LE"});
        
        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPage = new JLabel("Trang: 1/1");

        btnImport = new JButton("Import Nguyện Vọng");
        btnImport.setBackground(new Color(30, 144, 255));
        btnImport.setForeground(Color.WHITE);
        
        btnDeleteAll = new JButton("Xóa Tất Cả");
        btnDeleteAll.setBackground(new Color(220, 20, 60));
        btnDeleteAll.setForeground(Color.WHITE);
        
        lblStatTotal = new JLabel("Tổng NV: 0");
        lblStatTrungTuyen = new JLabel("Trúng tuyển: 0");
        lblStatChoXet = new JLabel("Đang xét: 0");
    }

    private void layoutComponents() {
        // Dashboard thống kê
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 10));
        statsPanel.setBackground(new Color(245, 245, 250));
        statsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        statsPanel.add(new JLabel("📊 TỔNG QUAN NGUYỆN VỌNG:"));
        statsPanel.add(lblStatTotal);
        statsPanel.add(lblStatTrungTuyen);
        statsPanel.add(lblStatChoXet);

        // Thanh tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Bộ lọc dữ liệu Nguyện Vọng"));
        searchPanel.add(new JLabel("CCCD / Mã Ngành:"));
        searchPanel.add(txtSearch);
        searchPanel.add(new JLabel("  Trạng thái:"));
        searchPanel.add(cbStatusFilter);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(statsPanel, BorderLayout.NORTH);
        topContainer.add(searchPanel, BorderLayout.SOUTH);

        // Phân trang & Actions
        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pagingPanel.add(btnPrev);
        pagingPanel.add(lblPage);
        pagingPanel.add(btnNext);

        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionWrap.add(btnDeleteAll);
        actionWrap.add(btnImport);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(pagingPanel, BorderLayout.CENTER);
        bottomPanel.add(actionWrap, BorderLayout.EAST);

        add(topContainer, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
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

        btnImport.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parentWindow, "Import Nguyện Vọng", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            ImportPanel importPanel = new ImportPanel(
                    (inputStream, callback) -> nguyenVongService.importFromExcel(inputStream, callback)
            );

            dialog.add(importPanel);
            dialog.pack();
            dialog.setMinimumSize(new Dimension(500, 300));
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            loadData();
        });

        btnDeleteAll.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn XÓA TOÀN BỘ nguyện vọng trong hệ thống?\nThao tác này không thể hoàn tác!", 
                "Xác nhận xóa sạch", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                nguyenVongService.deleteAll();
                JOptionPane.showMessageDialog(this, "Đã xóa sạch toàn bộ nguyện vọng.");
                loadData();
            }
        });
    }

    public void loadData() {
        String keyword = txtSearch.getText().trim();
        String status = (String) cbStatusFilter.getSelectedItem();
        if ("Tất cả".equals(status)) status = null;
        
        Page<NguyenVong> pageData = nguyenVongService.layDanhSachPhanTrangVoiStatus(currentPage, pageSize, keyword, status);
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        // Fetch Map Điểm Sàn để hiển thị so sánh
        java.util.Map<String, Double> mapDiemSan = nguyenVongService.getMapDiemSan();

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        int stt = currentPage * pageSize + 1;
        int countTrungTuyen = 0;
        int countChoXet = 0;

        for (NguyenVong n : pageData.getContent()) {
            String hoTen = n.getThiSinh() != null ? n.getThiSinh().getHoTen() : "N/A";
            
            // Format điểm hiển thị 2 chữ số thập phân
            String dThxt = n.getDiemThxt() != null ? String.format("%.2f", n.getDiemThxt()) : "0.00";
            String dCong = n.getDiemCong() != null ? String.format("%.2f", n.getDiemCong()) : "0.00";
            String dUtqd = n.getDiemUtqd() != null ? String.format("%.2f", n.getDiemUtqd()) : "0.00";
            String dXt = n.getDiemXetTuyen() != null ? String.format("%.2f", n.getDiemXetTuyen()) : "0.00";
            
            Double diemSanVal = mapDiemSan.getOrDefault(n.getNvManganh(), 0.0);
            String dSan = String.format("%.2f", diemSanVal);

            tablePanel.addRow(new Object[] {
                    stt++,
                    n.getNnCccd(), 
                    hoTen,
                    n.getNvTt(), 
                    n.getNvManganh(), 
                    dSan, // Cột Điểm Sàn mới thêm
                    n.getTtThm() != null ? n.getTtThm() : "---",
                    dThxt,
                    dCong,
                    dUtqd,
                    dXt,
                    n.getNvKetQua() != null ? n.getNvKetQua() : "DANG_XET"
            });

            if ("TRUNG_TUYEN".equals(n.getNvKetQua())) countTrungTuyen++;
            if ("DANG_XET".equals(n.getNvKetQua())) countChoXet++;
        }

        lblStatTotal.setText("Tổng NV: " + pageData.getTotalElements());
        lblStatTrungTuyen.setText("Trúng tuyển: " + countTrungTuyen + " (trong trang này)");
        lblStatChoXet.setText("Đang xét: " + countChoXet + " (trong trang này)");
    }
}
