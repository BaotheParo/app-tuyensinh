package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.ImportPanel;
import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.service.ChiTieuImportService;
import com.sgu.tuyensinh.service.NganhImportService;
import com.sgu.tuyensinh.service.NguongDauVaoImportService;
import com.sgu.tuyensinh.service.ToHopImportService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

@Component
public class NganhPanel extends JPanel {

    private final NganhImportService nganhService;
    private final ChiTieuImportService chiTieuImportService;
    private final NguongDauVaoImportService nguongDauVaoImportService;
    private final ToHopImportService toHopImportService;

    // Components
    private BaseTablePanel tablePanel;
    private JTextField txtMaNganh, txtTenNganh, txtToHopGoc, txtChiTieu, txtDiemSan;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnPrev, btnNext, btnRefresh, btnViewToHop;
    private JLabel lblPage;
    private JButton btnImport;

    // Phân trang
    private int currentPage = 0;
    private final int pageSize = 15;
    private int totalPages = 1;

    public NganhPanel(NganhImportService nganhService,
                      ChiTieuImportService chiTieuImportService,
                      NguongDauVaoImportService nguongDauVaoImportService,
                      ToHopImportService toHopImportService) {
        this.nganhService = nganhService;
        this.chiTieuImportService = chiTieuImportService;
        this.nguongDauVaoImportService = nguongDauVaoImportService;
        this.toHopImportService = toHopImportService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        // loadData(); // Defer loading to speed up startup
    }

    private void initComponents() {
        String[] columns = { "Mã Ngành", "Tên Ngành", "Chỉ Tiêu", "Điểm Sàn", "Điểm Chuẩn", "Phương Thức", "SL NV", "Tổ Hợp Gốc" };
        tablePanel = new BaseTablePanel(columns);
        tablePanel.getTable().setRowHeight(30);

        txtMaNganh = new JTextField(15);
        txtTenNganh = new JTextField(20);
        txtToHopGoc = new JTextField(10);
        txtChiTieu = new JTextField(10);
        txtDiemSan = new JTextField(10);

        btnAdd = new JButton("Thêm Mới");
        btnUpdate = new JButton("Cập Nhật");
        btnDelete = new JButton("Xóa");
        btnClear = new JButton("Làm Mới Form");

        btnViewToHop = new JButton("Xem Tổ Hợp");
        btnViewToHop.setBackground(new Color(155, 89, 182));
        btnViewToHop.setForeground(Color.WHITE);

        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        btnRefresh = new JButton("Làm mới");
        lblPage = new JLabel("Trang: 1/1");

        btnImport = new JButton("Import");
        btnImport.setBackground(new Color(30, 144, 255));
        btnImport.setForeground(Color.WHITE);
    }

    private void layoutComponents() {
        // ── Form Input ──────────────────────────────────────────
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Ngành"));

        formPanel.add(new JLabel("Mã Ngành:"));
        formPanel.add(txtMaNganh);
        formPanel.add(new JLabel("Tên Ngành:"));
        formPanel.add(txtTenNganh);
        formPanel.add(new JLabel("Tổ Hợp Gốc:"));
        formPanel.add(txtToHopGoc);
        formPanel.add(new JLabel("Chỉ Tiêu:"));
        formPanel.add(txtChiTieu);
        formPanel.add(new JLabel("Điểm Sàn:"));
        formPanel.add(txtDiemSan);

        // ── Action Buttons ───────────────────────────────────────
        JPanel actionPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
        actionPanel.add(btnViewToHop);
        actionPanel.add(btnClear);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(actionPanel, BorderLayout.EAST);

        // ── Bottom: Phân trang + Import ──────────────────────────
        // FIX: chỉ add btnPrev / lblPage / btnNext MỘT LẦN duy nhất
        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pagingPanel.add(btnPrev);
        pagingPanel.add(lblPage);
        pagingPanel.add(btnNext);
        pagingPanel.add(btnRefresh);

        JPanel importWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        importWrap.add(btnImport);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(pagingPanel, BorderLayout.CENTER);
        bottomPanel.add(importWrap, BorderLayout.EAST);

        // ── Assemble ─────────────────────────────────────────────
        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addEventHandlers() {
        // Đổ data từ Table lên Form
        JTable table = tablePanel.getTable();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtMaNganh.setText(table.getValueAt(row, 0).toString());
                txtTenNganh.setText(table.getValueAt(row, 1).toString());
                txtChiTieu.setText(table.getValueAt(row, 2).toString());
                txtDiemSan.setText(table.getValueAt(row, 3) != null ? table.getValueAt(row, 3).toString() : "0");
                txtToHopGoc.setText(table.getValueAt(row, 7) != null ? table.getValueAt(row, 7).toString() : "");
                txtMaNganh.setEditable(false);
            }
        });

        btnAdd.addActionListener(e -> executeSave(true));
        btnUpdate.addActionListener(e -> executeSave(false));
        btnDelete.addActionListener(e -> executeDelete());
        btnViewToHop.addActionListener(e -> showToHopDetail());
        btnClear.addActionListener(e -> clearForm());

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

        btnRefresh.addActionListener(e -> loadData());

        // Nút Import → Dialog với 3 tab: Chi tiêu / Tổ hợp gốc / Ngưỡng đầu vào
        btnImport.addActionListener(e -> showSmartImportDialog());
    }

    /**
     * Dialog Import thông minh với 3 phần:
     *  1. Chi tiêu tuyển sinh (Chi tieu 2025.xlsx)
     *  2. Tổ hợp môn gốc (tohopmon.xlsx) — cũng tự động update Tổ hợp gốc của Ngành
     *  3. Ngưỡng đầu vào (Nguong dau vao 2025.xlsx)
     */
    private void showSmartImportDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Import dữ liệu Ngành", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // ── Tab 1: Chỉ tiêu ────────────────────────────────
        JPanel tab1 = new JPanel(new BorderLayout());
        tab1.add(buildInfoLabel(
            "<html><b>File:</b> Chi tieu 2025.xlsx<br><br>" +
            "<b>Mục đích:</b> Nạp số chỉ tiêu tuyển sinh cho từng ngành.<br>" +
            "<b>Lưu ý:</b> File có 2 dòng đầu là tiêu đề/header, dữ liệu bắt đầu từ dòng 3.<br>" +
            "Nếu ngành đã tồn tại → cập nhật chỉ tiêu. Nếu chưa → tạo mới.</html>"
        ), BorderLayout.NORTH);
        tab1.add(new ImportPanel(
            (is, cb) -> chiTieuImportService.importFromExcel(is, cb)
        ), BorderLayout.CENTER);
        tabs.addTab("📊 Chỉ tiêu", tab1);

        // ── Tab 2: Tổ hợp môn ──────────────────────────────
        JPanel tab2 = new JPanel(new BorderLayout());
        tab2.add(buildInfoLabel(
            "<html><b>File:</b> tohopmon.xlsx<br><br>" +
            "<b>Mục đích:</b> Nạp danh sách tổ hợp môn và tự động gán Tổ hợp gốc cho ngành.<br>" +
            "<b>Cấu trúc cột:</b> STT | MANGANH | TÊN NGÀNH | MÃ TỔ HỢP | tb_keys | TÊN TH | <u>Gốc</u> | Độ lệch<br>" +
            "Dòng nào có cột 'Gốc' = 'Gốc' → tự động set Tổ hợp gốc của ngành đó.</html>"
        ), BorderLayout.NORTH);
        tab2.add(new ImportPanel(
            (is, cb) -> toHopImportService.importFromExcel(is, cb)
        ), BorderLayout.CENTER);
        tabs.addTab("📚 Tổ hợp môn", tab2);

        // ── Tab 3: Ngưỡng đầu vào ──────────────────────────
        JPanel tab3 = new JPanel(new BorderLayout());
        tab3.add(buildInfoLabel(
            "<html><b>File:</b> Nguong dau vao 2025.xlsx<br><br>" +
            "<b>Mục đích:</b> Cập nhật ngưỡng điểm sàn (Điểm sàn) cho từng ngành.<br>" +
            "<b>Cấu trúc cột:</b> STT | Mã xét tuyển | Tên ngành | Ngưỡng đầu vào<br>" +
            "Nên import <u>sau</u> khi đã import Chỉ tiêu để ngành đã tồn tại sẵn.</html>"
        ), BorderLayout.NORTH);
        tab3.add(new ImportPanel(
            (is, cb) -> nguongDauVaoImportService.importFromExcel(is, cb)
        ), BorderLayout.CENTER);
        tabs.addTab("🎯 Ngưỡng đầu vào", tab3);

        dialog.add(tabs);
        dialog.setSize(580, 460);
        dialog.setMinimumSize(new Dimension(520, 400));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Sau khi dialog đóng → reload lại bảng
        loadData();
    }

    private void showToHopDetail() {
        int row = tablePanel.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một ngành để xem tổ hợp!");
            return;
        }
        String maNganh = tablePanel.getTable().getValueAt(row, 0).toString();
        String tenNganh = tablePanel.getTable().getValueAt(row, 1).toString();

        java.util.List<com.sgu.tuyensinh.entity.NganhToHop> list = nganhService.getToHopsByMaNganh(maNganh);
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ngành này chưa được gán tổ hợp môn nào.");
            return;
        }

        JDialog dialog = new JDialog((Window) SwingUtilities.getWindowAncestor(this), "Tổ hợp môn: " + tenNganh, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());

        String[] cols = { "Mã TH", "Môn 1", "HS1", "Môn 2", "HS2", "Môn 3", "HS3", "Lệch (dolech)" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (com.sgu.tuyensinh.entity.NganhToHop nth : list) {
            model.addRow(new Object[]{
                nth.getMaToHop(),
                nth.getThMon1(), nth.getHsMon1(),
                nth.getThMon2(), nth.getHsMon2(),
                nth.getThMon3(), nth.getHsMon3(),
                nth.getDoLech()
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);
        
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Label hiển thị hướng dẫn bên trên mỗi tab import */
    private JPanel buildInfoLabel(String html) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(0xFFF8E1));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xFFD54F)),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        JLabel lbl = new JLabel(html);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(0x5D4037));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    public void loadData() {
        Page<Nganh> pageData = nganhService.layDanhSachPhanTrang(currentPage, pageSize, "");
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        java.util.Map<String, Long> candidateCounts = nganhService.getCandidateCounts();

        for (Nganh n : pageData.getContent()) {
            tablePanel.addRow(new Object[] {
                    n.getMaNganh(), 
                    n.getTenNganh(), 
                    n.getChiTieu(), 
                    n.getDiemSan(),
                    n.getDiemTrungTuyen() != null ? n.getDiemTrungTuyen() : "—",
                    nganhService.formatPhuongThuc(n),
                    candidateCounts.getOrDefault(n.getMaNganh(), 0L),
                    n.getToHopGoc()
            });
        }
    }

    private void executeSave(boolean isNew) {
        try {
            Nganh nganh = new Nganh();
            nganh.setMaNganh(txtMaNganh.getText().trim());
            nganh.setTenNganh(txtTenNganh.getText().trim());
            nganh.setToHopGoc(txtToHopGoc.getText().trim());
            nganh.setChiTieu(Integer.parseInt(txtChiTieu.getText().trim()));
            nganh.setDiemSan(new BigDecimal(txtDiemSan.getText().trim()));
            nganh.setTuyenThang("0");
            nganh.setDgnl("0");
            nganh.setThpt("0");
            nganh.setVsat("0");

            nganhService.luuNganh(nganh);
            JOptionPane.showMessageDialog(this, (isNew ? "Thêm" : "Cập nhật") + " thành công!");
            clearForm();
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Chỉ tiêu và Điểm sàn phải là số!",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(),
                    "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeDelete() {
        int row = tablePanel.getTable().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một ngành để xóa.");
            return;
        }
        String maNganh = tablePanel.getTable().getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa ngành: " + maNganh + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                nganhService.deleteNganh(maNganh);
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                clearForm();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Không thể xóa do dữ liệu đang được liên kết ở Tổ Hợp/Thí Sinh.",
                        "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        txtMaNganh.setText("");
        txtTenNganh.setText("");
        txtToHopGoc.setText("");
        txtChiTieu.setText("");
        txtDiemSan.setText("");
        txtMaNganh.setEditable(true);
        tablePanel.getTable().clearSelection();
    }
}
