package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TUẦN 2 — ErrorLogPanel
 * Package: com.sgu.tuyensinh.admin.ui.common
 *
 * Bảng hiển thị lỗi sau khi ImportWorker chạy xong.
 * Gắn với ImportPanel: sau khi done() → gọi loadErrors()
 *
 * Error codes khớp với dữ liệu thực tế từ các file Excel:
 *   Ds_thi_sinh.xlsx, Ds_quy_doi_tieng_Anh.xlsx, Uu_tien_xet_tuyen.xlsx...
 */
public class ErrorLogPanel extends JPanel {

    // ── Màu sắc / Font ────────────────────────────────────────────
    private static final Color SGU_BLUE     = new Color(0, 82, 155);
    private static final Color ERROR_BG     = new Color(255, 235, 235);
    private static final Color ERROR_FG     = new Color(180, 30, 20);
    private static final Color WARN_BG      = new Color(255, 250, 220);
    private static final Color WARN_FG      = new Color(160, 110, 0);
    private static final Color SUCCESS_BG   = new Color(235, 255, 235);
    private static final Color BORDER_COLOR = new Color(210, 215, 225);
    private static final Color BG_GRAY      = new Color(245, 246, 250);
    private static final Font  FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font  FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font  FONT_LABEL_B = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font  FONT_TABLE   = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font  FONT_TABLE_H = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font  FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font  FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);

    // ── Cột bảng — khớp với cấu trúc file Excel thực tế ─────────
    private static final String[] COLUMNS = {
            "#", "Dòng Excel", "CCCD", "Loại lỗi", "Mô tả", "Gợi ý sửa"
    };
    private static final int[] COL_WIDTHS = { 40, 80, 130, 140, 280, 220 };

    // ── Widgets ───────────────────────────────────────────────────
    private ErrorTableModel tableModel;
    private JTable          table;
    private JLabel          lblFileName;
    private JLabel          lblSummaryText;
    private JButton         btnDetail;
    private JButton         btnExport;
    private JPanel          pnlCards;

    private final List<ErrorRow> rows = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    public ErrorLogPanel() {
        initUI();
        wireEvents();
    }

    // ─────────────────────────────────────────────────────────────
    private void initUI() {
        setBackground(BG_GRAY);
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(),    BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel outer = new JPanel(new BorderLayout(0, 10));
        outer.setBackground(BG_GRAY);
        outer.setBorder(new EmptyBorder(16, 22, 0, 22));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = new JLabel("Log Lỗi Import");
        title.setFont(FONT_TITLE);
        title.setForeground(new Color(30, 40, 70));

        lblFileName = new JLabel("Nguồn: —");
        lblFileName.setFont(FONT_SMALL);
        lblFileName.setForeground(Color.GRAY);

        titleRow.add(title,      BorderLayout.WEST);
        titleRow.add(lblFileName, BorderLayout.EAST);

        pnlCards = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        pnlCards.setOpaque(false);

        outer.add(titleRow, BorderLayout.NORTH);
        outer.add(pnlCards, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildCard(String count, String label, Color bg, Color fg) {
        JPanel c = new JPanel(new BorderLayout(0, 2));
        c.setBackground(bg);
        c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(fg.darker(), 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        JLabel n = new JLabel(count, SwingConstants.CENTER);
        n.setFont(new Font("Segoe UI", Font.BOLD, 20));
        n.setForeground(fg);
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        l.setFont(FONT_SMALL);
        l.setForeground(fg.darker());
        c.add(n, BorderLayout.CENTER);
        c.add(l, BorderLayout.SOUTH);
        return c;
    }

    // ── Table ─────────────────────────────────────────────────────
    private JPanel buildTableArea() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_GRAY);
        p.setBorder(new EmptyBorder(10, 22, 10, 22));

        // Filter bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filterBar.setBackground(new Color(238, 241, 250));
        filterBar.setBorder(new LineBorder(BORDER_COLOR, 1));

        JLabel lblFilter = new JLabel("Lọc:");
        lblFilter.setFont(FONT_LABEL);

        // Các mã lỗi thực tế từ dữ liệu SGU
        String[] filterOpts = {
                "Tất cả",
                "CCCD_DUPLICATE", "CCCD_FORMAT_INVALID",
                "DATE_FORMAT_INVALID", "SCORE_OUT_OF_RANGE",
                "CERT_EXPIRED", "MISSING_REQUIRED_FIELD",
                "INVALID_DOITUONG", "INVALID_KHUVUC"
        };
        JComboBox<String> cboFilter = new JComboBox<>(filterOpts);
        cboFilter.setFont(FONT_SMALL);
        cboFilter.setPreferredSize(new Dimension(185, 24));

        JTextField txtSearch = new JTextField(14);
        txtSearch.setFont(FONT_SMALL);
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(2, 5, 2, 5)
        ));

        lblSummaryText = new JLabel();
        lblSummaryText.setFont(FONT_SMALL);
        lblSummaryText.setForeground(Color.GRAY);

        filterBar.add(lblFilter);
        filterBar.add(cboFilter);
        filterBar.add(new JLabel("  Tìm:"));
        filterBar.add(txtSearch);
        filterBar.add(Box.createHorizontalStrut(12));
        filterBar.add(lblSummaryText);

        // Table
        tableModel = new ErrorTableModel();
        table = new JTable(tableModel);
        table.setFont(FONT_TABLE);
        table.setRowHeight(27);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(225, 228, 235));
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultRenderer(Object.class, new ErrorRowRenderer());

        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(FONT_TABLE_H);
        hdr.setBackground(new Color(230, 235, 248));
        hdr.setForeground(SGU_BLUE);
        hdr.setPreferredSize(new Dimension(0, 32));
        hdr.setReorderingAllowed(false);

        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < COL_WIDTHS.length; i++) {
            cm.getColumn(i).setPreferredWidth(COL_WIDTHS[i]);
        }
        cm.getColumn(2).setCellRenderer(new MonoRenderer()); // CCCD monospace

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));

        p.add(filterBar, BorderLayout.NORTH);
        p.add(scroll,    BorderLayout.CENTER);
        return p;
    }

    // ── Button bar ────────────────────────────────────────────────
    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(238, 241, 250));
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(8, 22, 8, 22)
        ));

        JLabel hint = new JLabel("  Các dòng lỗi KHÔNG được import vào DB. Sửa file rồi import lại.");
        hint.setFont(FONT_SMALL);
        hint.setForeground(new Color(150, 80, 0));
        bar.add(hint, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        // Dùng SguButton — tự vẽ, màu text luôn rõ dù enabled hay disabled
        btnDetail = new SguButton("Xem chi tiết",
                new Color(50, 80, 150),   // bg enabled
                new Color(80, 110, 185),  // bg disabled (nhạt hơn)
                Color.WHITE,              // text enabled
                new Color(200, 210, 230)  // text disabled (xám nhạt, vẫn đọc được)
        );
        btnDetail.setEnabled(false);

        btnExport = new SguButton("Xuất log Excel",
                new Color(30, 110, 50),
                new Color(30, 110, 50),
                Color.WHITE,
                Color.WHITE
        );

        SguButton btnClose = new SguButton("Đóng",
                new Color(90, 100, 120),
                new Color(90, 100, 120),
                Color.WHITE,
                Color.WHITE
        );
        btnClose.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JDialog) ((JDialog) w).dispose();
        });

        btns.add(btnDetail);
        btns.add(btnExport);
        btns.add(btnClose);
        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    /**
     * Custom button — tự vẽ bằng paintComponent để màu text/background
     * KHÔNG bị Swing L&F override khi enabled/disabled.
     *
     * Lý do cần: System L&F (Windows/macOS) tự đổi màu text thành xám
     * khi setEnabled(false), bỏ qua setForeground() hoàn toàn.
     */
    private static class SguButton extends JButton {
        private final Color bgEnabled, bgDisabled, fgEnabled, fgDisabled;

        SguButton(String text, Color bgEnabled, Color bgDisabled,
                  Color fgEnabled, Color fgDisabled) {
            super(text);
            this.bgEnabled  = bgEnabled;
            this.bgDisabled = bgDisabled;
            this.fgEnabled  = fgEnabled;
            this.fgDisabled = fgDisabled;

            setFont(FONT_LABEL);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false); // tắt fill mặc định, tự vẽ
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(130, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean enabled = isEnabled();
            Color bg = enabled ? bgEnabled : bgDisabled;

            // Hover: làm sáng nhẹ khi chuột đè
            ButtonModel model = getModel();
            if (enabled && model.isPressed()) {
                bg = bg.darker();
            } else if (enabled && model.isRollover()) {
                bg = bg.brighter();
            }

            // Vẽ nền bo góc
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

            // Vẽ text — luôn dùng màu chúng ta chỉ định, không để L&F can thiệp
            g2.setColor(enabled ? fgEnabled : fgDisabled);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth()  - fm.stringWidth(getText())) / 2;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Events
    // ─────────────────────────────────────────────────────────────
    private void wireEvents() {
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                btnDetail.setEnabled(table.getSelectedRow() >= 0);
        });

        btnDetail.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                ErrorRow er = tableModel.getRow(row);
                new ErrorDetailDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this), er
                ).setVisible(true);
            }
        });

        btnExport.addActionListener(e ->
                MessageDialog.showInfo(
                        "Tính năng xuất log Excel sẽ được hoàn thiện sau.\n" +
                                "File sẽ được xuất ra: error_log_<timestamp>.xlsx"
                )
        );
    }

    // ─────────────────────────────────────────────────────────────
    //  Public API — gọi từ ImportWorker.done()
    // ─────────────────────────────────────────────────────────────
    /**
     * @param errors       list lỗi từ ImportResultDTO
     * @param fileName     tên file đã import (hiển thị header)
     * @param successCount số dòng import thành công vào DB
     * @param totalCount   tổng dòng dữ liệu trong file (trừ header)
     */
    public void loadErrors(List<ErrorRow> errors, String fileName,
                           int successCount, int totalCount) {
        rows.clear();
        rows.addAll(errors);
        tableModel.fireTableDataChanged();

        lblFileName.setText("Nguồn: " + fileName);
        lblSummaryText.setText("Hiển thị " + errors.size() + " dòng");

        long errCnt  = errors.stream().filter(r -> "ERROR".equals(r.severity)).count();
        long warnCnt = errors.stream().filter(r -> "WARNING".equals(r.severity)).count();

        pnlCards.removeAll();
        pnlCards.add(buildCard(String.valueOf(successCount), "Thành công",
                SUCCESS_BG, new Color(34, 120, 34)));
        pnlCards.add(buildCard(String.valueOf(errCnt), "Lỗi (ERROR)",
                ERROR_BG, ERROR_FG));
        pnlCards.add(buildCard(String.valueOf(warnCnt), "Cảnh báo",
                WARN_BG, WARN_FG));
        pnlCards.add(buildCard(String.valueOf(totalCount), "Tổng dòng",
                new Color(230, 235, 250), SGU_BLUE));
        pnlCards.revalidate();
        pnlCards.repaint();
    }

    // ─────────────────────────────────────────────────────────────
    //  DTO — ErrorRow (sau này move sang service/dto/)
    // ─────────────────────────────────────────────────────────────
    /**
     * Đại diện 1 dòng lỗi.
     * severity: "ERROR" hoặc "WARNING"
     *
     * errorCode — các giá trị thực tế từ dữ liệu SGU:
     *   CCCD_DUPLICATE         (Ds_thi_sinh.xlsx: CCCD trùng)
     *   CCCD_FORMAT_INVALID    (CCCD không đủ 12 số)
     *   DATE_FORMAT_INVALID    (cột Ngày sinh sai format)
     *   SCORE_OUT_OF_RANGE     (điểm ngoài [0, 10])
     *   CERT_EXPIRED           (Ds_quy_doi_tieng_Anh.xlsx: hết hạn)
     *   MISSING_REQUIRED_FIELD (thiếu CCCD hoặc field bắt buộc)
     *   INVALID_DOITUONG       (cột ĐTƯT giá trị lạ)
     *   INVALID_KHUVUC         (cột KVƯT giá trị lạ)
     */
    public static class ErrorRow {
        public final int    excelRow;
        public final String cccd;
        public final String severity;
        public final String errorCode;
        public final String description;
        public final String suggestion;

        public ErrorRow(int excelRow, String cccd, String severity,
                        String errorCode, String description, String suggestion) {
            this.excelRow    = excelRow;
            this.cccd        = cccd;
            this.severity    = severity;
            this.errorCode   = errorCode;
            this.description = description;
            this.suggestion  = suggestion;
        }
    }

    // ── Table model ───────────────────────────────────────────────
    private class ErrorTableModel extends AbstractTableModel {
        public int    getRowCount()    { return rows.size(); }
        public int    getColumnCount() { return COLUMNS.length; }
        public String getColumnName(int c) { return COLUMNS[c]; }

        public Object getValueAt(int r, int c) {
            ErrorRow row = rows.get(r);
            return switch (c) {
                case 0 -> r + 1;
                case 1 -> row.excelRow;
                case 2 -> row.cccd;
                case 3 -> row.errorCode;
                case 4 -> row.description;
                case 5 -> row.suggestion;
                default -> "";
            };
        }
        public ErrorRow getRow(int r) { return rows.get(r); }
    }

    // ── Renderer tô màu ───────────────────────────────────────────
    private class ErrorRowRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object val,
                                                       boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            if (!sel) {
                ErrorRow er = tableModel.getRow(row);
                c.setBackground("ERROR".equals(er.severity) ? ERROR_BG : WARN_BG);
                c.setForeground("ERROR".equals(er.severity) ? ERROR_FG : WARN_FG);
            }
            ((JLabel) c).setBorder(new EmptyBorder(0, 6, 0, 6));
            return c;
        }
    }

    private static class MonoRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object val,
                                                       boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            c.setFont(FONT_MONO);
            return c;
        }
    }

    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}

            // Demo với dữ liệu mẫu khớp cấu trúc file thực tế
            ErrorLogPanel panel = new ErrorLogPanel();
            List<ErrorRow> demo = new ArrayList<>();
            demo.add(new ErrorRow(2,  "TS_0042", "ERROR",   "CCCD_DUPLICATE",
                    "CCCD đã tồn tại trong bảng xt_thisinhxettuyen25",
                    "Xóa dòng trùng trong Ds_thi_sinh.xlsx hoặc dùng upsert"));
            demo.add(new ErrorRow(7,  "TS_0197", "ERROR",   "DATE_FORMAT_INVALID",
                    "Ngày sinh '31/02/2007' không hợp lệ (tháng 2 không có ngày 31)",
                    "Sửa lại: dd/MM/yyyy — VD: 28/02/2007"));
            demo.add(new ErrorRow(12, "TS_12077","ERROR",   "CERT_EXPIRED",
                    "IELTS 7.5 cấp 10/01/2023 — hết hạn trước 30/06/2026",
                    "Chứng chỉ phải cấp sau 30/06/2024 (ngay_cap + 2 năm ≥ 30/06/2026)"));
            demo.add(new ErrorRow(18, "TS_8814", "WARNING", "MISSING_REQUIRED_FIELD",
                    "Cột ĐTƯT bỏ trống — mặc định null (không có ưu tiên đối tượng)",
                    "Bỏ qua nếu thí sinh không có đối tượng ưu tiên"));
            demo.add(new ErrorRow(25, "TS_0311", "ERROR",   "SCORE_OUT_OF_RANGE",
                    "Điểm TO = 11.0 vượt thang [0.00, 10.00]",
                    "Kiểm tra lại điểm Toán trong Ds_thi_sinh.xlsx cột TO"));

            panel.loadErrors(demo, "Ds_thi_sinh.xlsx", 4309, 4314);

            JFrame f = new JFrame("SGU — Log Lỗi Import");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(980, 540);
            f.setMinimumSize(new Dimension(800, 440));
            f.setLocationRelativeTo(null);
            f.add(panel);
            f.setVisible(true);
        });
    }
}