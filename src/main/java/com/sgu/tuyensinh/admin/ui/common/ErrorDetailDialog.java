package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * TUẦN 2 — ErrorDetailDialog
 * Package: com.sgu.tuyensinh.admin.ui.common
 *
 * Dialog chi tiết lỗi — mở từ ErrorLogPanel khi nhấn "Xem chi tiết".
 * Dùng MessageDialog (đã có sẵn trong team) khi cần popup đơn giản.
 */
public class ErrorDetailDialog extends JDialog {

    private static final Color SGU_BLUE     = new Color(0, 82, 155);
    private static final Color ERROR_RED    = new Color(196, 43, 28);
    private static final Color WARN_ORANGE  = new Color(190, 120, 0);
    private static final Color ERROR_BG     = new Color(255, 235, 235);
    private static final Color WARN_BG      = new Color(255, 250, 220);
    private static final Color BG_LIGHT     = new Color(247, 248, 252);
    private static final Color BORDER_COLOR = new Color(210, 215, 225);
    private static final Font  FONT_LABEL   = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font  FONT_VALUE   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_VALUE_B = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font  FONT_MONO    = new Font("Consolas", Font.BOLD, 13);
    private static final Font  FONT_CODE    = new Font("Consolas", Font.PLAIN, 13);
    private static final Font  FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);

    private final ErrorLogPanel.ErrorRow err;

    public ErrorDetailDialog(Frame parent, ErrorLogPanel.ErrorRow err) {
        super(parent, "Chi tiết lỗi — Dòng " + err.excelRow, true);
        this.err = err;
        initUI();
        pack();
        setMinimumSize(new Dimension(540, 400));
        setResizable(true);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_LIGHT);
        root.add(buildBanner(),  BorderLayout.NORTH);
        root.add(buildDetail(),  BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ── Banner ────────────────────────────────────────────────────
    private JPanel buildBanner() {
        boolean isErr = "ERROR".equals(err.severity);
        Color bg = isErr ? ERROR_BG : WARN_BG;
        Color fg = isErr ? ERROR_RED : WARN_ORANGE;
        String label = isErr
                ? "  LỖI — Dòng này KHÔNG được import vào DB"
                : "  CẢNH BÁO — Dữ liệu có vấn đề, cần kiểm tra";

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 2, 0, fg.darker()),
                new EmptyBorder(12, 18, 12, 18)
        ));

        JLabel badge = new JLabel(err.severity);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 17));
        badge.setForeground(fg);

        JLabel desc = new JLabel(label);
        desc.setFont(FONT_SMALL);
        desc.setForeground(fg.darker());

        JPanel txt = new JPanel();
        txt.setOpaque(false);
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.add(badge);
        txt.add(Box.createVerticalStrut(3));
        txt.add(desc);
        p.add(txt, BorderLayout.WEST);
        return p;
    }

    // ── Detail card ───────────────────────────────────────────────
    private JPanel buildDetail() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_LIGHT);
        outer.setBorder(new EmptyBorder(14, 18, 0, 18));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.NORTHWEST;
        lc.insets = new Insets(5, 0, 5, 14);
        lc.weightx = 0; lc.gridx = 0;

        GridBagConstraints vc = new GridBagConstraints();
        vc.anchor = GridBagConstraints.NORTHWEST;
        vc.insets = new Insets(5, 0, 5, 0);
        vc.fill = GridBagConstraints.HORIZONTAL;
        vc.weightx = 1; vc.gridx = 1;

        int r = 0;
        addRow(card, lc, vc, r++, "Dòng Excel:",  mono(String.valueOf(err.excelRow)));
        addRow(card, lc, vc, r++, "CCCD:",         mono(err.cccd));
        addRow(card, lc, vc, r++, "Mã lỗi:",       buildCodeBadge());
        addRow(card, lc, vc, r++, "Mô tả:",        textArea(err.description, ERROR_RED));
        addRow(card, lc, vc, r++, "Gợi ý sửa:",   textArea(err.suggestion, new Color(30, 100, 30)));
        addRow(card, lc, vc, r,   "Tài liệu:",     docHint());

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    private void addRow(JPanel p, GridBagConstraints lc, GridBagConstraints vc,
                        int row, String label, Component comp) {
        lc.gridy = row; vc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(FONT_LABEL);
        l.setForeground(new Color(80, 90, 110));
        l.setPreferredSize(new Dimension(100, 0));
        p.add(l, lc);
        p.add(comp, vc);
    }

    private JLabel mono(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_MONO);
        l.setForeground(SGU_BLUE);
        return l;
    }

    private JPanel buildCodeBadge() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        JLabel b = new JLabel("  " + err.errorCode + "  ");
        b.setFont(FONT_CODE);
        b.setForeground(ERROR_RED);
        b.setBackground(ERROR_BG);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ERROR_RED, 1, true),
                new EmptyBorder(2, 4, 2, 4)
        ));
        p.add(b);
        return p;
    }

    private JTextArea textArea(String text, Color fg) {
        JTextArea ta = new JTextArea(text);
        ta.setFont(FONT_VALUE);
        ta.setForeground(fg);
        ta.setBackground(Color.WHITE);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(null);
        ta.setOpaque(false);
        ta.setColumns(38);
        return ta;
    }

    private JLabel docHint() {
        // Gợi ý tài liệu dựa vào cột thực tế từ Ds_thi_sinh.xlsx, Ds_quy_doi_tieng_Anh.xlsx,...
        String hint = switch (err.errorCode) {
            case "CCCD_DUPLICATE"          -> "Cột CCCD phải duy nhất trong file và DB (xt_thisinhxettuyen25.cccd UNIQUE)";
            case "CCCD_FORMAT_INVALID"     -> "Cột CCCD: đúng 12 chữ số, không chứa ký tự đặc biệt";
            case "DATE_FORMAT_INVALID"     -> "Cột Ngày sinh: dd/MM/yyyy — VD: 25/07/2007";
            case "SCORE_OUT_OF_RANGE"      -> "Các cột TO,VA,LI,HO,SI,SU,DI,NN,KTPL,TI... ∈ [0.00, 10.00]";
            case "CERT_EXPIRED"            -> "Ds_quy_doi_tieng_Anh.xlsx: ngay_cap + 2 năm ≥ 30/06/2026";
            case "MISSING_REQUIRED_FIELD"  -> "Kiểm tra các cột bắt buộc: CCCD, Họ Tên, Ngày sinh, KVƯT";
            case "INVALID_DOITUONG"        -> "Cột ĐTƯT hợp lệ: 01, 02, 03, 04, 05, 06a, 07 hoặc để trống";
            case "INVALID_KHUVUC"          -> "Cột KVƯT hợp lệ: 1, 2NT, 2, 3 hoặc để trống (xem Sheet3 Ds_thi_sinh.xlsx)";
            default                        -> "Mã lỗi: " + err.errorCode;
        };
        JLabel l = new JLabel(hint);
        l.setFont(FONT_SMALL);
        l.setForeground(new Color(50, 80, 160));
        return l;
    }

    // ── Footer ────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bar.setBackground(BG_LIGHT);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton btnCopy = new JButton("Sao chép mã lỗi");
        btnCopy.setFont(FONT_SMALL);
        btnCopy.setPreferredSize(new Dimension(140, 28));
        btnCopy.setFocusPainted(false);
        btnCopy.addActionListener(e -> {
            java.awt.datatransfer.StringSelection sel =
                    new java.awt.datatransfer.StringSelection(
                            err.errorCode + ": " + err.description);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            btnCopy.setText("✔ Đã sao chép!");
            Timer t = new Timer(1500, ev -> btnCopy.setText("Sao chép mã lỗi"));
            t.setRepeats(false);
            t.start();
        });

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(FONT_LABEL);
        btnClose.setPreferredSize(new Dimension(80, 28));
        btnClose.setBackground(SGU_BLUE);
        btnClose.setForeground(Color.WHITE);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        bar.add(btnCopy);
        bar.add(btnClose);
        return bar;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new ErrorDetailDialog(null,
                    new ErrorLogPanel.ErrorRow(12, "TS_12077", "ERROR", "CERT_EXPIRED",
                            "IELTS 7.5 cấp 10/01/2023 — hết hạn trước 30/06/2026",
                            "Chứng chỉ phải cấp sau 30/06/2024. Kiểm tra lại cột ngay_cap trong Ds_quy_doi_tieng_Anh.xlsx")
            ).setVisible(true);
        });
    }
}