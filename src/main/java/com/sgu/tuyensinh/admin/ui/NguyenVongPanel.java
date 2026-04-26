package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.ImportPanel;
import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.service.impl.NguyenVongServiceImpl;
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
    private JButton btnSearch, btnPrev, btnNext;
    private JLabel lblPage;
    private JButton btnImport;

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
        loadData();
    }

    private void initComponents() {
        String[] columns = { "Thứ Tự", "CCCD Thí Sinh", "Mã Ngành", "Mã Tổ Hợp", "Phương Thức", "Trạng Thái" };
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
        searchPanel.setBorder(BorderFactory.createTitledBorder("Bộ lọc dữ liệu Nguyện Vọng"));
        searchPanel.add(new JLabel("Tìm theo CCCD / Mã Ngành:"));
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

        // FIX: truyền parent window vào JDialog để định vị đúng
        btnImport.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parentWindow, "Import Ngành", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            ImportPanel importPanel = new ImportPanel(
                    (inputStream, callback) -> nguyenVongService.importFromExcel(inputStream, callback)
            );

            dialog.add(importPanel);
            dialog.pack();
            dialog.setMinimumSize(new Dimension(500, 280));
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            // Sau khi dialog đóng → reload lại bảng
            loadData();
        });
    }

    private void loadData() {
        String keyword = txtSearch.getText().trim();
        Page<NguyenVong> pageData = nguyenVongService.layDanhSachPhanTrang(currentPage, pageSize, keyword);
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        for (NguyenVong n : pageData.getContent()) {
            tablePanel.addRow(new Object[] {
                    n.getNvTt(), n.getNnCccd(), n.getNvManganh(), n.getTtThm(),
                    n.getTtPhuongthuc(), n.getNvKetQua()
            });
        }
    }
}
