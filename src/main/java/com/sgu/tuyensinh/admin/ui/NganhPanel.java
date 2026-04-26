package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.ImportPanel;
import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.service.impl.NganhServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

@Component
public class NganhPanel extends JPanel {

    private final NganhServiceImpl nganhService;

    // Components
    private BaseTablePanel tablePanel;
    private JTextField txtMaNganh, txtTenNganh, txtToHopGoc, txtChiTieu, txtDiemSan;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnPrev, btnNext;
    private JLabel lblPage;
    private JButton btnImport;

    // Phân trang
    private int currentPage = 0;
    private final int pageSize = 15;
    private int totalPages = 1;

    public NganhPanel(NganhServiceImpl nganhService) {
        this.nganhService = nganhService;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();
        loadData();
    }

    private void initComponents() {
        String[] columns = { "Mã Ngành", "Tên Ngành", "Tổ Hợp Gốc", "Chỉ Tiêu", "Điểm Sàn" };
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

        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
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
        JPanel actionPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);
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
                txtToHopGoc.setText(table.getValueAt(row, 2) != null ? table.getValueAt(row, 2).toString() : "");
                txtChiTieu.setText(table.getValueAt(row, 3).toString());
                txtDiemSan.setText(table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString() : "0");
                txtMaNganh.setEditable(false);
            }
        });

        btnAdd.addActionListener(e -> executeSave(true));
        btnUpdate.addActionListener(e -> executeSave(false));
        btnDelete.addActionListener(e -> executeDelete());
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

        // FIX: truyền parent window vào JDialog để định vị đúng
        btnImport.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parentWindow, "Import Ngành", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            ImportPanel importPanel = new ImportPanel(
                    (inputStream, callback) -> nganhService.importFromExcel(inputStream, callback));

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
        Page<Nganh> pageData = nganhService.layDanhSachPhanTrang(currentPage, pageSize, "");
        totalPages = pageData.getTotalPages() == 0 ? 1 : pageData.getTotalPages();
        lblPage.setText("Trang: " + (currentPage + 1) + " / " + totalPages);

        DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
        model.setRowCount(0);

        for (Nganh n : pageData.getContent()) {
            tablePanel.addRow(new Object[] {
                    n.getMaNganh(), n.getTenNganh(), n.getToHopGoc(),
                    n.getChiTieu(), n.getDiemSan()
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
