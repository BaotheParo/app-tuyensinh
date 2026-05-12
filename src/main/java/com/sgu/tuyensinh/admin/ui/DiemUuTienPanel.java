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

    private BaseTablePanel tablePanel;
    private JTextField txtSearch;
    private JButton btnSearch, btnPrev, btnNext;
    private JLabel lblPage;
    private JButton btnImport;

    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    public DiemUuTienPanel(DiemCongServiceImpl diemCongService) {
        this.diemCongService = diemCongService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        loadData();
    }

    private void initComponents() {
        String[] columns = { "ID", "CCCD Thí Sinh", "Mã Ngành", "Tổ Hợp", "Phương Thức", "Điểm CC", "Điểm Ưu Tiên",
                "Ghi Chú" };
        tablePanel = new BaseTablePanel(columns);

        txtSearch = new JTextField(20);
        btnSearch = new JButton("Tìm Kiếm");
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

    private void loadData() {
        String keyword = txtSearch.getText().trim();
        Page<DiemCong> pageData = diemCongService.layDanhSachPhanTrang(currentPage, pageSize, keyword);
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        for (DiemCong d : pageData.getContent()) {
            tablePanel.addRow(new Object[] {
                    d.getIddiemcong(), d.getTsCccd(), d.getManganh(), d.getMatohop(),
                    d.getPhuongthuc(), d.getDiemCC(), d.getDiemUtxt(), d.getGhichu()
            });
        }
    }
}
