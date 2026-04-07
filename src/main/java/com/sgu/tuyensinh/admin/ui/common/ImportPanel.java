package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * TUẦN 1 — ImportPanel  (đã cập nhật package com.sgu.tuyensinh)
 *
 * Nằm cùng folder với: ImportWorker, MessageDialog, BaseTablePanel
 * Package: com.sgu.tuyensinh.admin.ui.common
 *
 * Sử dụng ImportWorker sẵn có của team để chạy nền.
 * Thêm: ComboBox loại dữ liệu, Spinner năm học, style SGU.
 */
public class ImportPanel extends JPanel {

    // ── Constants ─────────────────────────────────────────────────
    private static final Color SGU_BLUE      = new Color(0, 82, 155);
    private static final Color SGU_BLUE_DARK = new Color(0, 55, 110);
    private static final Color BG_GRAY       = new Color(245, 246, 250);
    private static final Color BORDER_COLOR  = new Color(210, 215, 225);
    private static final Color SUCCESS_GREEN = new Color(34, 139, 34);
    private static final Color ERROR_RED     = new Color(196, 43, 28);
    private static final Font  FONT_HEADER   = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font  FONT_LABEL    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_LABEL_B  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font  FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Danh sách loại dữ liệu — khớp đúng với tên file thực tế trong Phụ lục B.
     * Index 0 là placeholder, không import được.
     */
    private static final String[] DATA_TYPES = {
            "-- Chọn loại dữ liệu --",
            "Thí sinh (Ds_thi_sinh.xlsx)            → xt_thisinhxettuyen25 + xt_diemthixettuyen",
            "Chỉ tiêu ngành (Chi_tieu_2025.xlsx)    → xt_nganh.n_chitieu",
            "Ngưỡng đầu vào (Nguong_dau_vao_2025.xlsx) → xt_nganh.n_diemsan",
            "Tổ hợp môn (tohopmon.xlsx)             → xt_tohop_monthi + xt_nganh_tohop",
            "Quy đổi Tiếng Anh (Ds_quy_doi_tieng_Anh.xlsx) → xt_bangquydoi",
            "Ưu tiên xét tuyển (Uu_tien_xet_tuyen.xlsx) → xt_diemcongxetuyen",
            "Nguyện vọng (Nguyenvong.xlsx)          → xt_nguyenvongxettuyen"
    };

    // Mapping index → API endpoint (dùng bởi ImportWorker)
    private static final String[] API_ENDPOINTS = {
            null,
            "http://localhost:8080/api/import/thisinh",
            "http://localhost:8080/api/import/chitieu",
            "http://localhost:8080/api/import/nguong",
            "http://localhost:8080/api/import/tohop",
            "http://localhost:8080/api/import/quydoi",
            "http://localhost:8080/api/import/uutien",
            "http://localhost:8080/api/import/nguyenvong"
    };

    // ── Widgets ───────────────────────────────────────────────────
    private JTextField   filePathField;   // tên giống ImportPanel gốc của team
    private JButton      browseButton;
    private JButton      importButton;
    private JProgressBar progressBar;
    private JComboBox<String> cboDataType;
    private JSpinner     spnNamHoc;
    private JLabel       lblStatus;
    private JLabel       lblFileInfo;

    // ── State ─────────────────────────────────────────────────────
    private File selectedFile = null;

    // ─────────────────────────────────────────────────────────────
    public ImportPanel() {
        initUI();
        wireEvents();
    }

    // ─────────────────────────────────────────────────────────────
    private void initUI() {
        setBackground(BG_GRAY);
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(),    BorderLayout.NORTH);
        add(buildFormCard(),  BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SGU_BLUE);
        p.setBorder(new EmptyBorder(14, 22, 14, 22));

        JLabel lbl = new JLabel("Import Dữ Liệu");
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Nhập dữ liệu từ file Excel vào hệ thống tuyển sinh SGU 2026");
        sub.setFont(FONT_SMALL);
        sub.setForeground(new Color(180, 210, 255));

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(lbl);
        txt.add(Box.createVerticalStrut(3));
        txt.add(sub);
        p.add(txt, BorderLayout.WEST);
        return p;
    }

    // ── Main form card ────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_GRAY);
        outer.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(24, 28, 24, 28)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(sectionLabel("1. Chọn file Excel"));
        card.add(Box.createVerticalStrut(8));
        card.add(buildFileRow());
        card.add(buildFileInfoLabel());
        card.add(Box.createVerticalStrut(18));

        card.add(sectionLabel("2. Cấu hình import"));
        card.add(Box.createVerticalStrut(8));
        card.add(buildConfigRow());
        card.add(Box.createVerticalStrut(24));

        card.add(sectionLabel("3. Tiến trình"));
        card.add(Box.createVerticalStrut(8));
        card.add(buildProgressSection());
        card.add(Box.createVerticalStrut(20));

        card.add(buildButtonRow());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        outer.add(card, gbc);
        return outer;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL_B);
        l.setForeground(SGU_BLUE_DARK);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // ── File row — giữ nguyên tên biến filePathField, browseButton ──
    private JPanel buildFileRow() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        filePathField = new JTextField();
        filePathField.setFont(FONT_LABEL);
        filePathField.setEditable(false);
        filePathField.setText("Chưa chọn file...");
        filePathField.setForeground(Color.GRAY);
        filePathField.setBackground(new Color(250, 251, 253));
        filePathField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));

        browseButton = new JButton("Browse...");
        browseButton.setFont(FONT_LABEL_B);
        browseButton.setBackground(SGU_BLUE);
        browseButton.setForeground(Color.WHITE);
        browseButton.setFocusPainted(false);
        browseButton.setBorderPainted(false);
        browseButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        browseButton.setPreferredSize(new Dimension(100, 36));

        row.add(filePathField, BorderLayout.CENTER);
        row.add(browseButton, BorderLayout.EAST);
        return row;
    }

    private JPanel buildFileInfoLabel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        p.setOpaque(false); p.setAlignmentX(LEFT_ALIGNMENT);
        lblFileInfo = new JLabel("Hỗ trợ: .xlsx, .xls  |  Tối đa 50 MB");
        lblFileInfo.setFont(FONT_SMALL);
        lblFileInfo.setForeground(Color.GRAY);
        p.add(lblFileInfo);
        return p;
    }

    // ── Config row: ComboBox + Spinner năm học ─────────────────────
    private JPanel buildConfigRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);

        JPanel pType = new JPanel(new BorderLayout(0, 4));
        pType.setOpaque(false);
        JLabel lblType = new JLabel("Loại dữ liệu:");
        lblType.setFont(FONT_LABEL);
        cboDataType = new JComboBox<>(DATA_TYPES);
        cboDataType.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboDataType.setPreferredSize(new Dimension(380, 34));
        cboDataType.setBackground(Color.WHITE);
        pType.add(lblType, BorderLayout.NORTH);
        pType.add(cboDataType, BorderLayout.CENTER);

        JPanel pNam = new JPanel(new BorderLayout(0, 4));
        pNam.setOpaque(false);
        pNam.setBorder(new EmptyBorder(0, 20, 0, 0));
        JLabel lblNam = new JLabel("Năm học:");
        lblNam.setFont(FONT_LABEL);
        spnNamHoc = new JSpinner(new SpinnerNumberModel(2026, 2020, 2030, 1));
        spnNamHoc.setFont(FONT_LABEL);
        spnNamHoc.setPreferredSize(new Dimension(86, 34));

        // Tắt định dạng hàng nghìn (dấu phẩy) bằng pattern "#"
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spnNamHoc, "#");
        editor.getTextField().setHorizontalAlignment(JTextField.CENTER); 
        spnNamHoc.setEditor(editor);
        pNam.add(lblNam, BorderLayout.NORTH);
        pNam.add(spnNamHoc, BorderLayout.CENTER);

        row.add(pType);
        row.add(pNam);
        return row;
    }

    // ── Progress bar — giữ nguyên tên biến progressBar ────────────
    private JPanel buildProgressSection() {
        JPanel p = new JPanel();
        p.setOpaque(false); p.setAlignmentX(LEFT_ALIGNMENT);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        progressBar = new JProgressBar(0, 100);    // tên giữ nguyên
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("Chờ import...");
        progressBar.setFont(FONT_SMALL);
        progressBar.setForeground(SGU_BLUE);
        progressBar.setBackground(new Color(230, 235, 245));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel hint = new JLabel("Thanh tiến trình sẽ chạy khi ImportWorker hoạt động.");
        hint.setFont(FONT_SMALL);
        hint.setForeground(Color.GRAY);

        p.add(progressBar);
        p.add(Box.createVerticalStrut(5));
        p.add(hint);
        return p;
    }

    // ── Button row — giữ nguyên tên importButton ─────────────────
    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnReset = new JButton("Làm mới");
        btnReset.setFont(FONT_LABEL);
        btnReset.setPreferredSize(new Dimension(100, 36));
        btnReset.setBackground(Color.WHITE);
        btnReset.setForeground(SGU_BLUE_DARK);
        btnReset.setBorder(new LineBorder(BORDER_COLOR, 1));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> resetForm());

        importButton = new JButton("Import");         // tên giữ nguyên
        importButton.setFont(FONT_LABEL_B);
        importButton.setPreferredSize(new Dimension(110, 36));
        importButton.setBackground(new Color(160, 160, 160));
        importButton.setForeground(Color.WHITE);
        importButton.setBorderPainted(false);
        importButton.setFocusPainted(false);
        importButton.setEnabled(false);
        importButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        row.add(btnReset);
        row.add(importButton);
        return row;
    }

    // ── Status bar ─────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(235, 238, 245));
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(5, 22, 5, 22)
        ));
        lblStatus = new JLabel("Sẵn sàng. Chọn file và loại dữ liệu để bắt đầu.");
        lblStatus.setFont(FONT_SMALL);
        lblStatus.setForeground(new Color(80, 90, 110));
        bar.add(lblStatus, BorderLayout.WEST);

        JLabel ver = new JLabel("SGU Tuyển Sinh 2026  •  v3.0");
        ver.setFont(FONT_SMALL);
        ver.setForeground(new Color(160, 170, 190));
        bar.add(ver, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────
    //  Events — giữ logic tương tự ImportPanel gốc của team
    // ─────────────────────────────────────────────────────────────
    private void wireEvents() {
        // Browse: giữ nguyên logic JFileChooser của ImportPanel gốc
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Chọn file Excel");
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Excel Files (*.xlsx, *.xls)", "xlsx", "xls"));
            chooser.setAcceptAllFileFilterUsed(false);

            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath()); // giống gốc
                filePathField.setForeground(Color.BLACK);

                long sizeKB = selectedFile.length() / 1024;
                String sizeStr = sizeKB > 1024 ? (sizeKB / 1024) + " MB" : sizeKB + " KB";
                lblFileInfo.setText("File: " + selectedFile.getName() + "  |  " + sizeStr);
                lblFileInfo.setForeground(new Color(34, 100, 34));
                refreshImportButton();
            }
        });

        cboDataType.addActionListener(e -> refreshImportButton());

        // Import: gọi ImportWorker của team, truyền thêm endpoint
        importButton.addActionListener(e -> {
            String path = filePathField.getText();
            if (path == null || path.isEmpty()) {
                MessageDialog.showError("Vui lòng chọn file Excel!");
                return;
            }
            if (cboDataType.getSelectedIndex() == 0) {
                MessageDialog.showWarning("Vui lòng chọn loại dữ liệu!");
                return;
            }

            progressBar.setValue(0);
            progressBar.setString("Đang import...");
            progressBar.setForeground(SGU_BLUE);
            importButton.setEnabled(false);
            browseButton.setEnabled(false);
            cboDataType.setEnabled(false);
            setStatus("Đang xử lý: " + selectedFile.getName());

            // Dùng ImportWorker của team — chỉ cần truyền path + progressBar
            ImportWorker worker = new ImportWorker(path, progressBar) {
                @Override
                protected void done() {
                    super.done(); // gọi done() gốc (hiện dialog hoàn tất)
                    progressBar.setString("Hoàn thành!");
                    progressBar.setForeground(SUCCESS_GREEN);
                    importButton.setEnabled(true);
                    browseButton.setEnabled(true);
                    cboDataType.setEnabled(true);
                    setStatus("✅  Import hoàn tất: " + selectedFile.getName());
                }
            };
            worker.execute();
        });
    }

    // ─────────────────────────────────────────────────────────────
    private void refreshImportButton() {
        boolean ok = selectedFile != null && selectedFile.exists()
                && cboDataType.getSelectedIndex() > 0;
        importButton.setEnabled(ok);
        importButton.setBackground(ok ? SGU_BLUE : new Color(160, 160, 160));
        if (ok) setStatus("✔  Sẵn sàng import. Nhấn 'Import' để tiếp tục.");
    }

    private void resetForm() {
        selectedFile = null;
        filePathField.setText("Chưa chọn file...");
        filePathField.setForeground(Color.GRAY);
        cboDataType.setSelectedIndex(0);
        spnNamHoc.setValue(2026);
        progressBar.setValue(0);
        progressBar.setString("Chờ import...");
        progressBar.setForeground(SGU_BLUE);
        lblFileInfo.setText("Hỗ trợ: .xlsx, .xls  |  Tối đa 50 MB");
        lblFileInfo.setForeground(Color.GRAY);
        importButton.setEnabled(false);
        importButton.setBackground(new Color(160, 160, 160));
        setStatus("Form đã được làm mới.");
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg);
    }

    // ── Getters cho controller / worker dùng ──────────────────────
    public File getSelectedFile()  { return selectedFile; }
    public String getDataType()    { return (String) cboDataType.getSelectedItem(); }
    public int    getNamHoc()      { return (Integer) spnNamHoc.getValue(); }
    public String getApiEndpoint() {
        int idx = cboDataType.getSelectedIndex();
        return (idx > 0 && idx < API_ENDPOINTS.length) ? API_ENDPOINTS[idx] : null;
    }

    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            JFrame f = new JFrame("SGU Import Panel");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(740, 490);
            f.setMinimumSize(new Dimension(600, 420));
            f.setLocationRelativeTo(null);
            f.add(new ImportPanel());
            f.setVisible(true);
        });
    }
}