package com.sgu.tuyensinh.admin.ui.common;

import com.sgu.tuyensinh.admin.ui.common.MessageDialog;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * TUẦN 3 — TraCuuPanel (hoàn chỉnh)
 * Package: com.sgu.tuyensinh.admin.ui.tracuu
 *
 * Màn hình tra cứu hồ sơ thí sinh theo CCCD.
 * 4 trạng thái: EMPTY → LOADING → DASHBOARD / NOT_FOUND
 *
 * Tích hợp tuần 4: thay createSampleData() bằng gọi Service thật.
 */
public class TraCuuPanel extends JPanel {

    // ── Colors / Fonts ────────────────────────────────────────────
    private static final Color SGU_BLUE      = new Color(0, 82, 155);
    private static final Color SGU_BLUE_DARK = new Color(0, 55, 110);
    private static final Color BG_GRAY       = new Color(245, 246, 250);
    private static final Color BORDER_COLOR  = new Color(210, 215, 225);
    private static final Color SUCCESS_GREEN = new Color(34, 120, 34);
    private static final Color ERROR_RED     = new Color(196, 43, 28);
    private static final Font  FONT_HEADER   = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font  FONT_LABEL_B  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font  FONT_MONO     = new Font("Consolas", Font.BOLD, 22);
    private static final Font  FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Widgets ───────────────────────────────────────────────────
    private JTextField txtCCCD;
    private JLabel     lblValidation;
    private JButton    btnSearch;
    private JButton    btnClear;
    private JPanel     pnlResult;
    private CardLayout cardResult;
    private JPanel     pnlHistory;

    // Dashboard hiện tại — lưu để remove() khi tra cứu mới
    private ThiSinhDashboard currentDashboard = null;

    private final Deque<String> history = new ArrayDeque<>(5);

    // ─────────────────────────────────────────────────────────────
    public TraCuuPanel() {
        initUI();
        wireEvents();
    }

    private void initUI() {
        setBackground(BG_GRAY);
        setLayout(new BorderLayout());
        add(buildHeader(),   BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SGU_BLUE);
        p.setBorder(new EmptyBorder(14, 22, 14, 22));

        JLabel lbl = new JLabel("Tra cứu Hồ sơ Thí sinh");
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Nhập CCCD để xem bảng điểm và kết quả xét tuyển");
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

    // ── Main area ─────────────────────────────────────────────────
    private JPanel buildMainArea() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(BG_GRAY);
        p.setBorder(new EmptyBorder(18, 26, 18, 26));
        p.add(buildSearchCard(), BorderLayout.NORTH);
        p.add(buildResultArea(), BorderLayout.CENTER);
        return p;
    }

    // ── Search card ───────────────────────────────────────────────
    private JPanel buildSearchCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JLabel lbl = new JLabel("Số Căn cước công dân (CCCD):");
        lbl.setFont(FONT_LABEL_B);
        lbl.setForeground(SGU_BLUE_DARK);

        // Input + buttons
        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);

        txtCCCD = new JTextField();
        txtCCCD.setFont(FONT_MONO);
        txtCCCD.setBackground(new Color(250, 251, 253));
        txtCCCD.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 2),
                new EmptyBorder(6, 12, 6, 12)
        ));
        txtCCCD.setPreferredSize(new Dimension(0, 50));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);

        btnClear = new JButton("Xóa");
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClear.setPreferredSize(new Dimension(70, 50));
        btnClear.setBackground(new Color(240, 240, 245));
        btnClear.setForeground(new Color(100, 100, 120));
        btnClear.setBorderPainted(false);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClear.setToolTipText("Xóa");

        btnSearch = new SearchButton();

        btnRow.add(btnClear);
        btnRow.add(btnSearch);
        inputRow.add(txtCCCD, BorderLayout.CENTER);
        inputRow.add(btnRow,  BorderLayout.EAST);

        // Dòng validation + history
        lblValidation = new JLabel("Nhập đúng 12 chữ số CCCD.");
        lblValidation.setFont(FONT_SMALL);
        lblValidation.setForeground(Color.GRAY);

        pnlHistory = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlHistory.setOpaque(false);

        JPanel bottom = new JPanel(new BorderLayout(0, 4));
        bottom.setOpaque(false);
        bottom.add(lblValidation, BorderLayout.NORTH);
        bottom.add(pnlHistory,    BorderLayout.CENTER);

        card.add(lbl,      BorderLayout.NORTH);
        card.add(inputRow, BorderLayout.CENTER);
        card.add(bottom,   BorderLayout.SOUTH);
        return card;
    }

    // ── Result area — CardLayout ───────────────────────────────────
    private JPanel buildResultArea() {
        cardResult = new CardLayout();
        pnlResult  = new JPanel(cardResult);
        pnlResult.setOpaque(false);

        pnlResult.add(buildStatePanel("", 52,
                "Nhập CCCD để tra cứu", new Color(100, 110, 140),
                "Hệ thống hiển thị bảng điểm và kết quả xét tuyển."), "EMPTY");

        pnlResult.add(buildStatePanel(" X", 44,
                "Không tìm thấy thí sinh", ERROR_RED,
                "CCCD không tồn tại trong hệ thống. Kiểm tra lại số CCCD."), "NOT_FOUND");

        pnlResult.add(buildStatePanel("⏳", 44,
                "Đang tải...", new Color(100, 110, 140), ""), "LOADING");

        // DASHBOARD slot trống — sẽ add/replace động
        pnlResult.add(new JPanel(), "DASHBOARD");

        cardResult.show(pnlResult, "EMPTY");
        return pnlResult;
    }

    private JPanel buildStatePanel(String emoji, int emojiSize,
                                   String title, Color titleColor,
                                   String subtitle) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, emojiSize));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel line1 = new JLabel(title, SwingConstants.CENTER);
        line1.setFont(new Font("Segoe UI", Font.BOLD, 15));
        line1.setForeground(titleColor);
        line1.setAlignmentX(CENTER_ALIGNMENT);

        inner.add(icon);
        inner.add(Box.createVerticalStrut(14));
        inner.add(line1);

        if (!subtitle.isEmpty()) {
            JLabel line2 = new JLabel(subtitle, SwingConstants.CENTER);
            line2.setFont(FONT_SMALL);
            line2.setForeground(Color.GRAY);
            line2.setAlignmentX(CENTER_ALIGNMENT);
            inner.add(Box.createVerticalStrut(6));
            inner.add(line2);
        }

        p.add(inner);
        return p;
    }

    // ─────────────────────────────────────────────────────────────
    //  Events
    // ─────────────────────────────────────────────────────────────
    private void wireEvents() {
        txtCCCD.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { onInput(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { onInput(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        txtCCCD.addActionListener(e -> doSearch());
        btnSearch.addActionListener(e -> doSearch());
        btnClear.addActionListener(e -> clear());
    }

    // ── Validate CCCD real-time ───────────────────────────────────
    private void onInput() {
        String raw    = txtCCCD.getText();
        String digits = raw.replaceAll("[^0-9]", "");

        if (!raw.equals(digits)) {
            int caret = txtCCCD.getCaretPosition();
            txtCCCD.setText(digits);
            txtCCCD.setCaretPosition(Math.min(caret, digits.length()));
            return;
        }

        int len = digits.length();
        if (len == 0) {
            setValidation("Nhập đúng 12 chữ số CCCD.", Color.GRAY, BORDER_COLOR);
            btnSearch.setEnabled(false);
            btnSearch.repaint();
        } else if (len < 12) {
            setValidation("Còn thiếu " + (12 - len) + " chữ số...",
                    new Color(180, 120, 0), new Color(220, 160, 0));
            btnSearch.setEnabled(false);
            btnSearch.repaint();
        } else if (len == 12) {
            setValidation("✔  CCCD hợp lệ — nhấn Tra cứu hoặc Enter",
                    SUCCESS_GREEN, SUCCESS_GREEN);
            btnSearch.setEnabled(true);
            btnSearch.repaint();
        } else {
            txtCCCD.setText(digits.substring(0, 12));
        }
    }

    private void setValidation(String msg, Color fg, Color borderColor) {
        lblValidation.setText(msg);
        lblValidation.setForeground(fg);
        txtCCCD.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 2),
                new EmptyBorder(6, 12, 6, 12)
        ));
    }

    // ── Tra cứu ───────────────────────────────────────────────────
    private void doSearch() {
        String cccd = txtCCCD.getText().trim();
        if (cccd.length() != 12) return;

        // Cập nhật history (dedup, max 5)
        history.remove(cccd);
        history.addFirst(cccd);
        if (history.size() > 5) {
            String last = null;
            for (String s : history) last = s;
            history.remove(last);
        }
        refreshHistory();

        // Hiện loading ngay
        cardResult.show(pnlResult, "LOADING");
        btnSearch.setEnabled(false);
        btnSearch.repaint();

        // TODO tuần 4 — thay bằng SwingWorker gọi Service thật:
        // new SwingWorker<ThiSinhDashboard.CandidateData, Void>() {
        //     protected CandidateData doInBackground() throws Exception {
        //         return thiSinhService.getCandidateDetail(cccd);
        //     }
        //     protected void done() {
        //         try { renderResult(cccd, get()); }
        //         catch (Exception ex) { renderResult(cccd, null); }
        //         btnSearch.setEnabled(true); btnSearch.repaint();
        //     }
        // }.execute();

        // Hiện tại: Timer 300ms giả lập latency rồi render
        final String finalCccd = cccd;
        Timer timer = new Timer(300, e -> {
            renderResult(finalCccd, ThiSinhDashboard.createSampleData(finalCccd));
            btnSearch.setEnabled(true);
            btnSearch.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    /** Nhận CandidateData và render đúng card */
    private void renderResult(String cccd, ThiSinhDashboard.CandidateData data) {
        if (data == null) {
            cardResult.show(pnlResult, "NOT_FOUND");
            return;
        }

        // Xóa dashboard cũ khỏi panel trước khi tạo mới
        if (currentDashboard != null) {
            pnlResult.remove(currentDashboard);
            currentDashboard = null;
        }

        currentDashboard = new ThiSinhDashboard(data);
        pnlResult.add(currentDashboard, "DASHBOARD");
        cardResult.show(pnlResult, "DASHBOARD");
        pnlResult.revalidate();
        pnlResult.repaint();
    }

    private void clear() {
        txtCCCD.setText("");
        txtCCCD.requestFocus();
        cardResult.show(pnlResult, "EMPTY");
        setValidation("Nhập đúng 12 chữ số CCCD.", Color.GRAY, BORDER_COLOR);
    }

    // ── History chips ─────────────────────────────────────────────
    private void refreshHistory() {
        pnlHistory.removeAll();

        if (history.isEmpty()) {
            pnlHistory.revalidate();
            pnlHistory.repaint();
            return;
        }

        JLabel lbl = new JLabel("Gần đây:");
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(Color.GRAY);
        pnlHistory.add(lbl);

        for (String cccd : history) {
            // Hiển thị dạng masked: 6 số đầu + ··· + 3 số cuối
            String display = cccd.length() >= 9
                    ? cccd.substring(0, 6) + "···" + cccd.substring(9)
                    : cccd;
            JButton chip = new JButton(display);
            chip.setFont(FONT_SMALL);
            chip.setToolTipText("Tra cứu lại: " + cccd);
            chip.setPreferredSize(new Dimension(112, 22));
            chip.setBackground(new Color(225, 235, 255));
            chip.setForeground(SGU_BLUE_DARK);
            chip.setBorder(new LineBorder(new Color(180, 200, 240), 1, true));
            chip.setFocusPainted(false);
            chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final String finalCccd = cccd;
            chip.addActionListener(e -> {
                txtCCCD.setText(finalCccd);
                doSearch();
            });
            pnlHistory.add(chip);
        }

        pnlHistory.revalidate();
        pnlHistory.repaint();
    }

    // ═════════════════════════════════════════════════════════════
    //  SearchButton — tự vẽ để màu text không bị L&F override
    // ═════════════════════════════════════════════════════════════
    private static class SearchButton extends JButton {
        SearchButton() {
            super("Tra cứu");
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setPreferredSize(new Dimension(120, 50));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            boolean en = isEnabled();
            Color bg;
            Color fg;

            if (!en) {
                bg = new Color(185, 192, 204);
                fg = new Color(230, 233, 238);
            } else {
                ButtonModel m = getModel();
                if      (m.isPressed())  { bg = new Color(0, 60, 120); }
                else if (m.isRollover()) { bg = new Color(0, 100, 185); }
                else                     { bg = new Color(0, 82, 155); }
                fg = Color.WHITE;
            }

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            g2.setColor(fg);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth()  - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            JFrame f = new JFrame("SGU — Tra cứu Hồ sơ");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1100, 720);
            f.setMinimumSize(new Dimension(900, 580));
            f.setLocationRelativeTo(null);
            f.add(new TraCuuPanel());
            f.setVisible(true);
        });
    }
}