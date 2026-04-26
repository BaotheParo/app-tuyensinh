package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.admin.ui.common.BaseTablePanel;
import com.sgu.tuyensinh.admin.ui.common.ImportPanel;
import com.sgu.tuyensinh.admin.ui.common.MessageDialog;
import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.service.impl.DiemThiServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

/**
 * Giao diện Quản lý Điểm Thi (Task 6).
 */
@Component
public class DiemThiPanel extends JPanel {

    private final DiemThiServiceImpl diemThiService;

    private JTextField txtSearch;
    private JButton btnSearch;
    private BaseTablePanel tablePanel;
    private JLabel lblPagination;
    private JButton btnPrev, btnNext, btnEdit, btnClear;

    private JButton btnImport;

    private int currentPage = 0;
    private final int pageSize = 20;
    private int totalPages = 1;

    @Autowired
    public DiemThiPanel(DiemThiServiceImpl  diemThiService) {
        this.diemThiService = diemThiService;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- TOP PANEL: Search ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtSearch = new JTextField(30);
        btnSearch = new JButton("Tìm kiếm");

        topPanel.add(new JLabel("CCCD / Họ Tên: "));
        topPanel.add(txtSearch);
        topPanel.add(btnSearch);

        add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: Table ---
        String[] columns = { "CCCD", "Họ Tên", "Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh", "V-SAT", "ĐGNL" };
        tablePanel = new BaseTablePanel(columns);
        add(tablePanel, BorderLayout.CENTER);

        // --- BOTTOM PANEL: Pagination & Actions ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Action Buttons (Right)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);

        btnEdit = new JButton("Sửa Điểm");
        btnClear = new JButton("Xóa Điểm");

        btnEdit.setBackground(new Color(41, 128, 185));
        btnEdit.setForeground(Color.WHITE);
        btnClear.setBackground(new Color(192, 57, 43));
        btnClear.setForeground(Color.WHITE);

        actionPanel.add(btnEdit);
        actionPanel.add(btnClear);

        // Pagination Buttons (Center)
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setBackground(Color.WHITE);

        btnPrev = new JButton("|< Trước");
        btnNext = new JButton("Sau >|");
        lblPagination = new JLabel("Trang 1 / 1");

        btnImport = new JButton("Import");
        btnImport.setBackground(new Color(30, 144, 255));
        btnImport.setForeground(Color.WHITE);

        paginationPanel.add(btnPrev);
        paginationPanel.add(lblPagination);
        paginationPanel.add(btnNext);

        bottomPanel.add(paginationPanel, BorderLayout.CENTER);
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        JPanel importWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        importWrap.add(btnImport);
        actionPanel.add(importWrap);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- EVENT LISTENERS ---
        btnSearch.addActionListener(e -> {
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

        btnEdit.addActionListener(e -> openEditDialog());
        btnClear.addActionListener(e -> handleClearAction());

        btnImport.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parentWindow, "Import Điểm Thi", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            ImportPanel importPanel = new ImportPanel(
                    (inputStream, callback) -> diemThiService.importFromExcel(inputStream, callback));

            dialog.add(importPanel);
            dialog.pack();
            dialog.setMinimumSize(new Dimension(500, 280));
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            loadData();
        });
    }

    /**
     * Dùng SwingWorker gọi API bất đồng bộ để tránh đơ giao diện UI (Yêu cầu Clean
     * Code / Threading).
     */
    public void loadData() {
        String keyword = txtSearch.getText().trim();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Page<DiemThi>, Void> worker = new SwingWorker<>() {
            @Override
            protected Page<DiemThi> doInBackground() throws Exception {
                return diemThiService.getDanhSachDiemThi(keyword, currentPage, pageSize);
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    Page<DiemThi> page = get();
                    totalPages = page.getTotalPages() == 0 ? 1 : page.getTotalPages();
                    lblPagination.setText("Trang " + (currentPage + 1) + " / " + totalPages);

                    // Reset table
                    DefaultTableModel model = (DefaultTableModel) tablePanel.getTable().getModel();
                    model.setRowCount(0);

                    for (DiemThi dt : page.getContent()) {
                        String hoTen = (dt.getThiSinh() != null) ? dt.getThiSinh().getHoTen() : "Không tìm thấy";
                        // Cột: CCCD, Họ Tên, Toán, Văn, Anh, Lý, Hóa, Sinh, V-SAT (nk1), ĐGNL (nk2)
                        model.addRow(new Object[] {
                                dt.getCccd(), hoTen,
                                dt.getToan(), dt.getVan(), dt.getAnh(),
                                dt.getLy(), dt.getHoa(), dt.getSinh(),
                                dt.getNk1(), dt.getNk2()
                        });
                    }

                    btnPrev.setEnabled(currentPage > 0);
                    btnNext.setEnabled(currentPage < totalPages - 1);

                } catch (InterruptedException | ExecutionException ex) {
                    MessageDialog.showError("Lỗi kết nối CSDL: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void openEditDialog() {
        int selectedRow = tablePanel.getTable().getSelectedRow();
        if (selectedRow < 0) {
            MessageDialog.showWarning("Vui lòng chọn 1 dòng thí sinh để sửa điểm.");
            return;
        }

        JTable table = tablePanel.getTable();
        String cccd = table.getValueAt(selectedRow, 0).toString();
        String hoTen = table.getValueAt(selectedRow, 1).toString();

        // Các field nhập liệu
        JTextField txtToan = createNumberField(table.getValueAt(selectedRow, 2));
        JTextField txtVan = createNumberField(table.getValueAt(selectedRow, 3));
        JTextField txtAnh = createNumberField(table.getValueAt(selectedRow, 4));
        JTextField txtLy = createNumberField(table.getValueAt(selectedRow, 5));
        JTextField txtHoa = createNumberField(table.getValueAt(selectedRow, 6));
        JTextField txtSinh = createNumberField(table.getValueAt(selectedRow, 7));
        JTextField txtVsat = createNumberField(table.getValueAt(selectedRow, 8));
        JTextField txtDgnl = createNumberField(table.getValueAt(selectedRow, 9));

        JPanel panel = new JPanel(new GridLayout(8, 2, 5, 5));
        panel.add(new JLabel("Toán:"));
        panel.add(txtToan);
        panel.add(new JLabel("Văn:"));
        panel.add(txtVan);
        panel.add(new JLabel("Ngoại Ngữ:"));
        panel.add(txtAnh);
        panel.add(new JLabel("Lý:"));
        panel.add(txtLy);
        panel.add(new JLabel("Hóa:"));
        panel.add(txtHoa);
        panel.add(new JLabel("Sinh:"));
        panel.add(txtSinh);
        panel.add(new JLabel("V-SAT:"));
        panel.add(txtVsat);
        panel.add(new JLabel("ĐGNL:"));
        panel.add(txtDgnl);

        int confirm = JOptionPane.showConfirmDialog(this, panel,
                "Phạm vi chỉnh sửa điểm - " + hoTen + " (" + cccd + ")", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (confirm == JOptionPane.OK_OPTION) {
            try {
                DiemThi diemMoi = new DiemThi();
                // Bắt lỗi convert String to Double
                diemMoi.setToan(parseDouble(txtToan.getText()));
                diemMoi.setVan(parseDouble(txtVan.getText()));
                diemMoi.setAnh(parseDouble(txtAnh.getText()));
                diemMoi.setLy(parseDouble(txtLy.getText()));
                diemMoi.setHoa(parseDouble(txtHoa.getText()));
                diemMoi.setSinh(parseDouble(txtSinh.getText()));
                diemMoi.setNk1(parseDouble(txtVsat.getText()));
                diemMoi.setNk2(parseDouble(txtDgnl.getText()));

                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                // Bọc lệnh update bằng SwingWorker để chạy background
                SwingWorker<DiemThi, Void> worker = new SwingWorker<>() {
                    @Override
                    protected DiemThi doInBackground() throws Exception {
                        return diemThiService.updateDiemThi(cccd, diemMoi);
                    }

                    @Override
                    protected void done() {
                        setCursor(Cursor.getDefaultCursor());
                        try {
                            get();
                            MessageDialog.showInfo("Cập nhật điểm thành công cho thí sinh " + hoTen);
                            loadData(); // Tải lại bảng để phản chiếu thay đổi
                        } catch (Exception ex) {
                            MessageDialog.showError("Cập nhật điểm thất bại: " + ex.getMessage());
                        }
                    }
                };
                worker.execute();

            } catch (NumberFormatException ex) {
                MessageDialog.showError("Vui lòng nhập đúng định dạng số (VD: 8.5) hoặc để trống.");
            }
        }
    }

    private void handleClearAction() {
        int selectedRow = tablePanel.getTable().getSelectedRow();
        if (selectedRow < 0) {
            MessageDialog.showWarning("Vui lòng chọn 1 dòng thí sinh để xóa điểm.");
            return;
        }

        String cccd = tablePanel.getTable().getValueAt(selectedRow, 0).toString();
        String hoTen = tablePanel.getTable().getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn làm rỗng toàn bộ điểm của thí sinh " + hoTen
                        + "\nChú ý: Hành động này thao tác trực tiếp vào hệ thống!",
                "Xác nhận xóa điểm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    diemThiService.clearDiemThi(cccd);
                    return null;
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    try {
                        get();
                        MessageDialog.showInfo("Đã xóa rỗng toàn bộ điểm thi.");
                        loadData();
                    } catch (Exception ex) {
                        MessageDialog.showError("Lỗi hệ thống: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    // Helper functions
    private JTextField createNumberField(Object value) {
        String strVal = (value != null) ? value.toString() : "";
        return new JTextField(strVal);
    }

    private Double parseDouble(String text) throws NumberFormatException {
        if (text == null || text.trim().isEmpty() || text.trim().equalsIgnoreCase("null")) {
            return null;
        }
        return Double.parseDouble(text.trim().replace(",", "."));
    }
}
