package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.ImportPanel;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.service.ThiSinhImportService;
import com.sgu.tuyensinh.service.ThiSinhService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.sgu.tuyensinh.admin.ui.common.ThiSinhDashboard;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class ThiSinhPanel extends JPanel {

    private final ThiSinhService thiSinhService;
    private final ThiSinhImportService thiSinhImportService;
    private final com.sgu.tuyensinh.service.DiemQuyDoiNgoaiNguImportService englishImportService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // UI Components
    private BaseTablePanel tablePanel;
    private JTextField txtCCCD, txtHoTen, txtNgaySinh, txtMaTruong, txtMaTinh;
    private JComboBox<String> cbGioiTinh, cbDoiTuong, cbKhuVuc;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnPrev, btnNext, btnSearch, btnViewDetail, btnRefresh;
    private JTextField txtSearch;
    private JLabel lblPage;
    private JLabel lblStatTotal, lblStatDoiTuong, lblStatKhuVuc;
    private JButton btnImport, btnImportEnglish;

    // Phân trang
    private int currentPage = 0;
    private final int pageSize = 15;
    private int totalPages = 1;

    public ThiSinhPanel(ThiSinhService thiSinhService, ThiSinhImportService thiSinhImportService, com.sgu.tuyensinh.service.DiemQuyDoiNgoaiNguImportService englishImportService) {
        this.thiSinhService = thiSinhService;
        this.thiSinhImportService = thiSinhImportService;
        this.englishImportService = englishImportService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        // loadData(); // Moved to MainFrame action listener to speed up startup
    }

    private void initComponents() {
        String[] columns = { "CCCD", "Họ Tên", "Ngày Sinh", "Giới Tính", "Mã Trường", "Mã Tỉnh", "Đối Tượng ƯT",
                "Khu Vực ƯT" };
        tablePanel = new BaseTablePanel(columns);
        tablePanel.getTable().setRowHeight(30);

        txtCCCD = new JTextField(15);
        txtHoTen = new JTextField(20);
        txtNgaySinh = new JTextField(15);
        txtMaTruong = new JTextField(10);
        txtMaTinh = new JTextField(10);

        // Setup Combo box options theo chuẩn PRD thường thấy
        cbGioiTinh = new JComboBox<>(new String[] { "Nam", "Nữ" });
        cbDoiTuong = new JComboBox<>(new String[] { "", "01", "02", "03", "04", "05", "06", "07" });
        cbKhuVuc = new JComboBox<>(new String[] { "", "KV1", "KV2", "KV2-NT", "KV3" });

        btnAdd = new JButton("Thêm Mới");
        btnUpdate = new JButton("Cập Nhật");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Làm Mới Form");

        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPage = new JLabel("Trang: 1/1");

        btnImport = new JButton("Import Thí Sinh");
        btnImport.setBackground(new Color(30, 144, 255));
        btnImport.setForeground(Color.WHITE);

        btnImportEnglish = new JButton("Import Quy Đổi TA");
        btnImportEnglish.setBackground(new Color(155, 89, 182));
        btnImportEnglish.setForeground(Color.WHITE);

        txtSearch = new JTextField(20);
        btnSearch = new JButton("Tìm Kiếm");
        btnRefresh = new JButton("Làm mới");
        btnViewDetail = new JButton("Xem Chi Tiết");
        btnViewDetail.setBackground(new Color(46, 204, 113));
        btnViewDetail.setForeground(Color.WHITE);

        lblStatTotal = new JLabel("Tổng: 0");
        lblStatDoiTuong = new JLabel("Theo ĐT: —");
        lblStatKhuVuc = new JLabel("Theo KV: —");
    }

    private void layoutComponents() {
        // Khối nhập liệu (Grid 4x4)
        JPanel formPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Thí sinh"));

        formPanel.add(new JLabel("CCCD:"));
        formPanel.add(txtCCCD);
        formPanel.add(new JLabel("Họ Tên:"));
        formPanel.add(txtHoTen);
        formPanel.add(new JLabel("Ngày Sinh (dd/MM/yyyy):"));
        formPanel.add(txtNgaySinh);
        formPanel.add(new JLabel("Giới Tính:"));
        formPanel.add(cbGioiTinh);
        formPanel.add(new JLabel("Mã Trường:"));
        formPanel.add(txtMaTruong);
        formPanel.add(new JLabel("Mã Tỉnh:"));
        formPanel.add(txtMaTinh);
        formPanel.add(new JLabel("Đối Tượng ƯT:"));
        formPanel.add(cbDoiTuong);
        formPanel.add(new JLabel("Khu Vực ƯT:"));
        formPanel.add(cbKhuVuc);

        // Khối nút bấm
        JPanel actionPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnClear);

        // Khối Tìm kiếm (Search Bar)
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Bộ lọc tìm kiếm"));
        searchPanel.add(new JLabel("Tìm theo CCCD hoặc Họ Tên:"));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRefresh);

        // Khối Thống kê nhanh
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statsPanel.setBackground(new Color(240, 248, 255));
        statsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        statsPanel.add(new JLabel("📊 THỐNG KÊ NHANH:"));
        statsPanel.add(lblStatTotal);
        statsPanel.add(lblStatDoiTuong);
        statsPanel.add(lblStatKhuVuc);

        // Gắn vào Top Panel (Bao gồm Stats, Form và Search)
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(statsPanel, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(actionPanel, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        // Khối phân trang
        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pagingPanel.add(btnPrev);
        pagingPanel.add(lblPage);
        pagingPanel.add(btnNext);

        JPanel importWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        importWrap.add(btnViewDetail);
        importWrap.add(btnImportEnglish);
        importWrap.add(btnImport);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(pagingPanel, BorderLayout.CENTER);
        bottomPanel.add(importWrap, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addEventHandlers() {
        JTable table = tablePanel.getTable();

        // Đổ dữ liệu từ Table ngược lên Form khi click
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtCCCD.setText(table.getValueAt(row, 0).toString());
                txtHoTen.setText(table.getValueAt(row, 1).toString());
                txtNgaySinh.setText(table.getValueAt(row, 2) != null ? table.getValueAt(row, 2).toString() : "");
                cbGioiTinh.setSelectedItem(table.getValueAt(row, 3));
                txtMaTruong.setText(table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString() : "");
                txtMaTinh.setText(table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString() : "");
                cbDoiTuong.setSelectedItem(table.getValueAt(row, 6) != null ? table.getValueAt(row, 6).toString() : "");
                cbKhuVuc.setSelectedItem(table.getValueAt(row, 7) != null ? table.getValueAt(row, 7).toString() : "");

                txtCCCD.setEditable(false); // Khóa CCCD không cho sửa
            }
        });

        // Nút hành động
        btnAdd.addActionListener(e -> executeSave(true));
        btnUpdate.addActionListener(e -> executeSave(false));
        btnDelete.addActionListener(e -> executeDelete());
        btnClear.addActionListener(e -> clearForm());
        btnViewDetail.addActionListener(e -> showDetail());

        // Nút phân trang
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

        // Tìm kiếm
        btnSearch.addActionListener(e -> {
            currentPage = 0;
            loadData();
        });

        btnRefresh.addActionListener(e -> {
            loadData();
            updateStats();
        });

        txtSearch.addActionListener(e -> {
            currentPage = 0;
            loadData();
        });

        // FIX: truyền parent window vào JDialog để định vị đúng
        btnImport.addActionListener(e -> showImportDialog());
        btnImportEnglish.addActionListener(e -> showImportEnglishDialog());
    }

    private void showImportDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Import Thí sinh & Điểm thi", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Hướng dẫn cấu trúc file
        JPanel infoPanel = createInfoPanel("<html><b>File:</b> Ds thi sinh.xlsx (50,000+ dòng)<br>" +
            "<b>Cấu trúc:</b> Col1:CCCD | Col2:Họ Tên | Col3:Ngày sinh | Col7:Toán | Col8:Văn | Col15:NN...<br>" +
            "<i>Hệ thống sử dụng đa luồng và cơ chế chống Deadlock.</i></html>");

        ImportPanel importPanel = new ImportPanel(thiSinhImportService::importFromExcel, () -> {
            loadData();
            updateStats();
        });

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(importPanel, BorderLayout.CENTER);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setVisible(true);
    }

    private void showImportEnglishDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Import Quy đổi Tiếng Anh", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel infoPanel = createInfoPanel("<html><b>File:</b> Ds quy doi tieng Anh.xlsx<br>" +
            "<b>Cấu trúc:</b> Col1:CCCD | Col4:Điểm Quy đổi | Col5:Điểm cộng<br>" +
            "<i>Dùng để nạp điểm chứng chỉ ngoại ngữ cho từng thí sinh.</i></html>");

        ImportPanel importPanel = new ImportPanel(englishImportService::importFromExcel, () -> {
            loadData();
        });

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(importPanel, BorderLayout.CENTER);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setVisible(true);
    }

    private JPanel createInfoPanel(String htmlText) {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(0xE3F2FD));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x2196F3)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        JLabel lblInfo = new JLabel(htmlText);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoPanel.add(lblInfo);
        return infoPanel;
    }

    public void loadData() {
        String keyword = txtSearch.getText().trim();
        Page<ThiSinh> pageData = thiSinhService.layDanhSachPhanTrang(currentPage, pageSize, keyword);
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);
        
        updateStats(); // Tự động cập nhật thống kê mỗi khi load data

        for (ThiSinh ts : pageData.getContent()) {
            String ngaySinhStr = ts.getNgaySinh() != null ? ts.getNgaySinh().format(dateFormatter) : "";
            tablePanel.addRow(new Object[] {
                    ts.getId(), ts.getHoTen(), ngaySinhStr, ts.getGioiTinh(),
                    ts.getMaTruong(), ts.getMaTinh(), ts.getDoiTuongUt(), ts.getKhuVucUt()
            });
        }

        updateStats();
    }

    private void updateStats() {
        try {
            Map<String, Object> stats = thiSinhService.getThongKeThiSinh();
            lblStatTotal.setText("Tổng thí sinh: " + stats.get("total"));
            
            java.util.List<Object[]> byDT = (java.util.List<Object[]>) stats.get("byDoiTuong");
            StringBuilder sbDT = new StringBuilder("Theo ĐT: ");
            for (Object[] row : byDT) {
                if (row[0] != null && !row[0].toString().isEmpty()) {
                    sbDT.append(row[0]).append("(").append(row[1]).append(") ");
                }
            }
            lblStatDoiTuong.setText(sbDT.length() > 10 ? sbDT.toString() : "Theo ĐT: 0");

            java.util.List<Object[]> byKV = (java.util.List<Object[]>) stats.get("byKhuVuc");
            StringBuilder sbKV = new StringBuilder("Theo KV: ");
            for (Object[] row : byKV) {
                if (row[0] != null && !row[0].toString().isEmpty()) {
                    sbKV.append(row[0]).append("(").append(row[1]).append(") ");
                }
            }
            lblStatKhuVuc.setText(sbKV.length() > 10 ? sbKV.toString() : "Theo KV: 0");
        } catch (Exception e) {
            System.err.println("Lỗi cập nhật thống kê: " + e.getMessage());
        }
    }

    private void showDetail() {
        int selectedRow = tablePanel.getTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thí sinh để xem chi tiết!");
            return;
        }

        String cccd = tablePanel.getTable().getValueAt(selectedRow, 0).toString();
        try {
            com.sgu.tuyensinh.entity.ThiSinh ts = thiSinhService.findByCccd(cccd);
            com.sgu.tuyensinh.entity.DiemThi dt = thiSinhService.findDiemByCccd(cccd);
            
            if (ts == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu thí sinh!");
                return;
            }

            ThiSinhDashboard.CandidateData dashboardData = ThiSinhDashboard.fromEntity(ts, dt);
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết Thí sinh: " + ts.getHoTen(), true);
            dialog.setLayout(new BorderLayout());
            dialog.add(new ThiSinhDashboard(dashboardData));
            dialog.setSize(1100, 800);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải chi tiết: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeSave(boolean isNew) {
        try {
            ThiSinh ts = new ThiSinh();
            ts.setId(txtCCCD.getText().trim());
            ts.setHoTen(txtHoTen.getText().trim());

            // Xử lý Ngày Sinh
            String ngaySinhStr = txtNgaySinh.getText().trim();
            if (!ngaySinhStr.isEmpty()) {
                ts.setNgaySinh(LocalDate.parse(ngaySinhStr, dateFormatter));
            }

            ts.setGioiTinh(cbGioiTinh.getSelectedItem().toString());
            ts.setMaTruong(txtMaTruong.getText().trim());
            ts.setMaTinh(txtMaTinh.getText().trim());

            // Xử lý Ưu tiên
            Object objDoiTuong = cbDoiTuong.getSelectedItem();
            ts.setDoiTuongUt(objDoiTuong != null && !objDoiTuong.toString().isEmpty() ? objDoiTuong.toString() : null);

            Object objKhuVuc = cbKhuVuc.getSelectedItem();
            ts.setKhuVucUt(objKhuVuc != null && !objKhuVuc.toString().isEmpty() ? objKhuVuc.toString() : null);

            // Hàm luuThiSinh cần được định nghĩa trong ThiSinhService
            thiSinhService.luuThiSinh(ts);

            JOptionPane.showMessageDialog(this, (isNew ? "Thêm" : "Cập nhật") + " thành công!");
            clearForm();
            loadData();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Ngày sinh phải theo định dạng dd/MM/yyyy", "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeDelete() {
        int row = tablePanel.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một thí sinh để xóa.");
            return;
        }
        String cccd = tablePanel.getTable().getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa CCCD: " + cccd + "?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                // Hàm xoaThiSinh cần được định nghĩa trong ThiSinhService
                thiSinhService.xoaThiSinh(cccd);

                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                clearForm();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thể xóa do thí sinh đã có điểm thi/nguyện vọng.",
                        "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtCCCD.setText("");
        txtHoTen.setText("");
        txtNgaySinh.setText("");
        txtMaTruong.setText("");
        txtMaTinh.setText("");
        cbGioiTinh.setSelectedIndex(0);
        cbDoiTuong.setSelectedIndex(0);
        cbKhuVuc.setSelectedIndex(0);

        txtCCCD.setEditable(true);
        tablePanel.getTable().clearSelection();
    }
}