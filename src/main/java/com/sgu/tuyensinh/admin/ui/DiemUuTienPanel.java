package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.ImportPanel;
import com.sgu.tuyensinh.entity.DiemCong;
import com.sgu.tuyensinh.service.DiemCongServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel hiển thị Điểm Ưu Tiên (Read-only để kiểm tra dữ liệu).
 */
@Component
public class DiemUuTienPanel extends JPanel {

    private final DiemCongServiceImpl diemCongService;
    private final com.sgu.tuyensinh.repository.NguyenVongRepository nguyenVongRepository;

    private BaseTablePanel tablePanel;
    private JTextField txtSearch;
    private JButton btnSearch, btnPrev, btnNext, btnRefresh, btnDeleteAll;
    private JLabel lblPage;
    private JButton btnImport;

    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    public DiemUuTienPanel(DiemCongServiceImpl diemCongService, com.sgu.tuyensinh.repository.NguyenVongRepository nguyenVongRepository) {
        this.diemCongService = diemCongService;
        this.nguyenVongRepository = nguyenVongRepository;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        // loadData(); // Defer loading
    }

    private void initComponents() {
        String[] columns = { "ID", "CCCD", "Họ Tên", "Nguyện Vọng", "Tổ Hợp", "PT", "Điểm Anh", "Điểm UT", "Tổng Cộng" };
        tablePanel = new BaseTablePanel(columns);
        tablePanel.getTable().setRowHeight(30);

        // Ẩn cột ID
        tablePanel.getTable().getColumnModel().getColumn(0).setMinWidth(0);
        tablePanel.getTable().getColumnModel().getColumn(0).setMaxWidth(0);
        tablePanel.getTable().getColumnModel().getColumn(0).setWidth(0);

        txtSearch = new JTextField(15);
        btnSearch = new JButton("Tìm Kiếm");
        btnRefresh = new JButton("Làm mới");
        btnDeleteAll = new JButton("Xóa Tất Cả");
        btnDeleteAll.setBackground(new Color(231, 76, 60));
        btnDeleteAll.setForeground(Color.WHITE);
        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPage = new JLabel("Trang: 1/1");

        btnImport = new JButton("Import");
        btnImport.setBackground(new Color(30, 144, 255));
        btnImport.setForeground(Color.WHITE);
    }

    private void layoutComponents() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Bộ lọc dữ liệu Điểm Ưu Tiên"));
        searchPanel.add(new JLabel("Tìm theo CCCD:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);
        searchPanel.add(btnDeleteAll);

        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pagingPanel.add(btnPrev);
        pagingPanel.add(lblPage);
        pagingPanel.add(btnNext);

        JPanel importWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        importWrap.add(btnImport);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(pagingPanel, BorderLayout.CENTER);
        bottomPanel.add(importWrap, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addEventHandlers() {
        btnRefresh.addActionListener(e -> {
            loadData();
        });
        btnDeleteAll.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn XÓA TOÀN BỘ dữ liệu điểm cộng?", "Xác nhận xóa", 
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                diemCongService.deleteAll();
                loadData();
            }
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
            JDialog dialog = new JDialog(parentWindow, "Import Điểm Ưu Tiên", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            ImportPanel importPanel = new ImportPanel(
                    (inputStream, callback) -> diemCongService.importFromExcel(inputStream, callback));

            dialog.add(importPanel);
            dialog.pack();
            dialog.setMinimumSize(new Dimension(500, 280));
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            loadData();
        });
    }

    public void loadData() {
        String keyword = txtSearch.getText().trim();
        Page<DiemCong> pageData = diemCongService.layDanhSachPhanTrang(currentPage, pageSize, keyword);
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        for (DiemCong d : pageData.getContent()) {
            String hoTen = d.getThiSinh() != null ? d.getThiSinh().getHoTen() : "N/A";
            
            // Tìm thông tin nguyện vọng để lấy thứ tự (nv_tt)
            String nvDisplay = d.getManganh();
            java.util.Optional<com.sgu.tuyensinh.entity.NguyenVong> nvOpt = nguyenVongRepository.findByNnCccdAndNvManganh(d.getTsCccd(), d.getManganh());
            if (nvOpt.isPresent()) {
                nvDisplay = "NV " + nvOpt.get().getNvTt() + " - " + d.getManganh();
            }

            tablePanel.addRow(new Object[] {
                    d.getIddiemcong(), 
                    d.getTsCccd(), 
                    hoTen,
                    nvDisplay, 
                    d.getMatohop(),
                    d.getPhuongthuc(), 
                    d.getDiemCC() != null ? d.getDiemCC() : 0.0,
                    // d.getDiemHSG() != null ? d.getDiemHSG() : 0.0, // Đã bỏ cột HSG
                    d.getDiemUtxt() != null ? d.getDiemUtxt() : 0.0,
                    d.getDiemTong() != null ? d.getDiemTong() : 0.0
            });
        }
    }
}
