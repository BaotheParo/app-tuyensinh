package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TUẦN 3 — ThiSinhDashboard (hoàn chỉnh)
 * Package: com.sgu.tuyensinh.admin.ui.tracuu
 *
 * Dashboard điểm chi tiết 1 thí sinh.
 * Admin dùng để verify logic tính điểm trước khi phát hành kết quả.
 *
 * 5 sections:
 *   1. Thông tin cơ bản       (xt_thisinhxettuyen25)
 *   2. Điểm thi               (xt_diemthixettuyen) — tab THPT / ĐGNL / V-SAT
 *   3. Điểm cộng & ưu tiên   (xt_diemcongxetuyen)
 *   4. Công thức tính ĐXT     (hiển thị từng bước 6 bước theo PRD)
 *   5. Kết quả nguyện vọng    (xt_nguyenvongxettuyen)
 */
public class ThiSinhDashboard extends JPanel {

    // ── Colors / Fonts ────────────────────────────────────────────
    private static final Color SGU_BLUE       = new Color(0, 82, 155);
    private static final Color SGU_BLUE_DARK  = new Color(0, 55, 110);
    private static final Color BG_GRAY        = new Color(245, 246, 250);
    private static final Color BORDER_COLOR   = new Color(210, 215, 225);
    private static final Color SECTION_BG     = new Color(237, 242, 252);
    private static final Color SUCCESS_GREEN  = new Color(0, 140, 0);
    private static final Color ERROR_RED      = new Color(196, 43, 28);
    private static final Color WARN_ORANGE    = new Color(200, 120, 0);
    private static final Color TRUNG_TUYEN_BG = new Color(218, 255, 218);
    private static final Color TRUOT_BG       = new Color(255, 228, 228);
    private static final Color FORMULA_BG     = new Color(255, 252, 235);
    private static final Color FORMULA_BORDER = new Color(220, 200, 100);
    private static final Font  FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font  FONT_LABEL_B   = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font  FONT_VALUE_B   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font  FONT_VALUE     = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_MONO      = new Font("Consolas", Font.BOLD, 13);
    private static final Font  FONT_SCORE     = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font  FONT_TABLE     = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font  FONT_TABLE_H   = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font  FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font  FONT_FORMULA   = new Font("Consolas", Font.PLAIN, 12);

    private final CandidateData data;

    public ThiSinhDashboard(CandidateData data) {
        this.data = data;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_GRAY);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_GRAY);
        content.setBorder(new EmptyBorder(12, 0, 12, 0));

        content.add(buildInfoCard());
        content.add(vgap(12));
        content.add(buildScoreCard());
        content.add(vgap(12));
        content.add(buildBonusCard());
        content.add(vgap(12));
        content.add(buildFormulaCard());
        content.add(vgap(12));
        content.add(buildWishCard());
        content.add(vgap(8));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG_GRAY);
        scroll.getViewport().setBackground(BG_GRAY);
        add(scroll, BorderLayout.CENTER);
    }

    // ═════════════════════════════════════════════════════════════
    //  SECTION 1 — Thông tin cơ bản
    // ═════════════════════════════════════════════════════════════
    private JPanel buildInfoCard() {
        JPanel card = card("👤  Thông tin Thí sinh");

        JPanel grid = new JPanel(new GridLayout(2, 4, 20, 10));
        grid.setOpaque(false);

        String dtLabel = (data.doiTuongUt == null || data.doiTuongUt.isEmpty() || data.doiTuongUt.equals("—"))
                ? "—" : data.doiTuongUt + (data.doiTuongLabel == null || data.doiTuongLabel.isEmpty()
                                           ? "" : "  (" + data.doiTuongLabel + ")");
        String kvLabel = (data.khuVucUt == null || data.khuVucUt.isEmpty() || data.khuVucUt.equals("—"))
                ? "—" : data.khuVucUt + (data.khuVucLabel == null || data.khuVucLabel.isEmpty()
                                         ? "" : "  (" + data.khuVucLabel + ")");

        grid.add(field("CCCD",             data.cccd,        FONT_MONO));
        grid.add(field("Họ và tên",        data.hoTen,       FONT_VALUE_B));
        grid.add(field("Ngày sinh",        data.ngaySinh,    FONT_VALUE));
        grid.add(field("Giới tính",        data.gioiTinh,    FONT_VALUE));
        grid.add(field("Đối tượng ưu tiên",dtLabel,          FONT_VALUE));
        grid.add(field("Khu vực ưu tiên",  kvLabel,          FONT_VALUE));
        grid.add(field("Nơi sinh / Tỉnh",  data.maTinh,      FONT_VALUE));
        grid.add(field("Năm học",          String.valueOf(data.namHoc), FONT_VALUE));

        card.add(grid, BorderLayout.CENTER);
        return wrap(card);
    }

    // ═════════════════════════════════════════════════════════════
    //  SECTION 2 — Điểm thi
    // ═════════════════════════════════════════════════════════════
    private JPanel buildScoreCard() {
        JPanel card = card("📊  Điểm Thi");

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(FONT_LABEL_B);
        tabs.addTab("THPT",  buildThptTab());
        tabs.addTab("ĐGNL",  buildDgnlVsatTab(true));
        tabs.addTab("V-SAT", buildDgnlVsatTab(false));

        card.add(tabs, BorderLayout.CENTER);
        return wrap(card);
    }

    private JPanel buildThptTab() {
        // DiemThiImportDTO dùng Double nullable: null = không thi, 0.0 = điểm liệt
        String anhNote = data.getAnhCC() > data.getAnh()
                ? "Dùng CC: " + fmt(data.certBest != null ? data.certBest : 0.0)
                  + " > thi: " + fmt(data.getAnh())
                : data.anh == null ? "Không thi" : "Dùng điểm thi";

        String[][] rows = {
                { "Toán  (toan)", fmt(data.getToan()), nullNote(data.toan) },
                { "Văn   (van)",  fmt(data.getVan()),  nullNote(data.van)  },
                { "Lý    (ly)",   fmt(data.getLy()),   nullNote(data.ly)   },
                { "Hóa   (hoa)",  fmt(data.getHoa()),  nullNote(data.hoa)  },
                { "Sinh  (sinh)", fmt(data.getSinh()), nullNote(data.sinh) },
                { "Sử    (su)",   fmt(data.getSu()),   nullNote(data.su)   },
                { "Địa   (dia)",  fmt(data.getDia()),  nullNote(data.dia)  },
                { "TA thi (anh)", fmt(data.getAnh()),  nullNote(data.anh)  },
                { "TA_CC = max(anh, certBest)", fmt(data.getAnhCC()), anhNote },
                { "NK1   (nk1)",  fmt(data.getNk1()),  nullNote(data.nk1)  },
                { "NK2   (nk2)",  fmt(data.getNk2()),  nullNote(data.nk2)  },
                { "NK3   (nk3)",  fmt(data.getNk3()),  nullNote(data.nk3)  },
                { "NK4   (nk4)",  fmt(data.getNk4()),  nullNote(data.nk4)  },
                { "NK5   (nk5)",  fmt(data.getNk5()),  nullNote(data.nk5)  },
                { "NK6   (nk6)",  fmt(data.getNk6()),  nullNote(data.nk6)  },
                { "NK7   (nk7)",  fmt(data.getNk7()),  nullNote(data.nk7)  },
                { "NK8   (nk8)",  fmt(data.getNk8()),  nullNote(data.nk8)  },
        };
        return scoreTable(new String[]{ "Môn / Field DTO", "Điểm", "Ghi chú" }, rows);
    }

    /** null = không thi (DiemThiImportDTO.Double); 0.0 = điểm liệt */
    private static String nullNote(Double v) {
        return v == null ? "Không thi (null)" : "";
    }

    private JPanel buildDgnlVsatTab(boolean isDgnl) {
        double raw       = isDgnl ? data.dDGNL  : data.dVSAT;
        double converted = isDgnl ? data.dDGNLConverted : data.dVSATConverted;
        String name      = isDgnl ? "ĐGNL" : "V-SAT";
        String thang     = isDgnl ? "1200" : "450";
        String phuongThuc= isDgnl ? "DGNL" : "VSAT";

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));

        if (raw <= 0) {
            JLabel none = new JLabel("Thí sinh không thi " + name, SwingConstants.CENTER);
            none.setFont(FONT_VALUE_B);
            none.setForeground(Color.GRAY);
            p.add(none, BorderLayout.CENTER);
            return p;
        }

        // Điểm gốc
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        top.setOpaque(false);
        JLabel lblRaw = new JLabel("Điểm " + name + " gốc (thang " + thang + "):  ");
        lblRaw.setFont(FONT_LABEL_B);
        JLabel lblScore = new JLabel(String.valueOf((int) raw));
        lblScore.setFont(FONT_SCORE);
        lblScore.setForeground(SGU_BLUE);
        top.add(lblRaw);
        top.add(lblScore);

        // Quy đổi
        JPanel mid = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        mid.setOpaque(false);
        JLabel arrow = new JLabel("→  Quy đổi về thang THPT:  ");
        arrow.setFont(FONT_VALUE);
        JLabel conv = new JLabel(fmt(converted) + " điểm");
        conv.setFont(FONT_VALUE_B);
        conv.setForeground(SUCCESS_GREEN);
        mid.add(arrow);
        mid.add(conv);

        JLabel note = new JLabel(
                "Bảng quy đổi: xt_bangquydoi  WHERE d_phuongthuc = '" + phuongThuc
                        + "'  AND nam_hoc = " + data.namHoc);
        note.setFont(FONT_SMALL);
        note.setForeground(Color.GRAY);

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(top);
        inner.add(Box.createVerticalStrut(10));
        inner.add(mid);
        inner.add(Box.createVerticalStrut(6));
        inner.add(note);

        p.add(inner, BorderLayout.NORTH);
        return p;
    }

    // ═════════════════════════════════════════════════════════════
    //  SECTION 3 — Điểm cộng & ưu tiên
    // ═════════════════════════════════════════════════════════════
    private JPanel buildBonusCard() {
        JPanel card = card("➕  Điểm Cộng & Ưu tiên");

        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        row.add(miniCard("Điểm Cộng (ĐC)",
                fmt(data.dc),
                "HSG: " + fmt(data.dcHSG) + "   +   CC-TA: " + fmt(data.dcCC),
                data.dc > 0 ? SUCCESS_GREEN : Color.GRAY,
                "cap max 3.0 điểm  →  diemCC"));

        row.add(miniCard("Điểm Ưu tiên (ĐƯT)",
                fmt(data.dut),
                "Đối tượng: " + fmt(data.dutDT) + "   +   Khu vực: " + fmt(data.dutKV),
                data.dut > 0 ? new Color(0, 100, 180) : Color.GRAY,
                "giảm tuyến tính khi base ≥ 22.5  →  diemUtxt"));

        row.add(miniCard("TA_CC (anh + certBest)",
                fmt(data.getAnhCC()),
                "Thi (anh): " + fmt(data.getAnh()) + "   vs   CC tốt nhất: "
                        + fmt(data.certBest != null ? data.certBest : 0.0),
                data.getAnhCC() > data.getAnh() ? new Color(110, 0, 160) : new Color(0, 80, 130),
                data.getAnhCC() > data.getAnh() ? "Lấy điểm chứng chỉ" : "Lấy điểm thi"));

        card.add(row, BorderLayout.CENTER);
        return wrap(card);
    }

    private JPanel miniCard(String title, String score, String detail,
                            Color scoreColor, String hint) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(SECTION_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel t = new JLabel(title);    t.setFont(FONT_LABEL_B);  t.setForeground(SGU_BLUE_DARK);
        JLabel s = new JLabel(score);    s.setFont(FONT_SCORE);    s.setForeground(scoreColor);
        JLabel d = new JLabel(detail);   d.setFont(FONT_SMALL);    d.setForeground(new Color(80, 90, 110));
        JLabel h = new JLabel(hint);     h.setFont(FONT_SMALL);    h.setForeground(Color.GRAY);

        JPanel sub = new JPanel();
        sub.setOpaque(false);
        sub.setLayout(new BoxLayout(sub, BoxLayout.Y_AXIS));
        sub.add(d);
        sub.add(Box.createVerticalStrut(2));
        sub.add(h);

        p.add(t,   BorderLayout.NORTH);
        p.add(s,   BorderLayout.CENTER);
        p.add(sub, BorderLayout.SOUTH);
        return p;
    }

    // ═════════════════════════════════════════════════════════════
    //  SECTION 4 — Công thức tính ĐXT (6 bước theo PRD)
    //  Admin dùng để verify logic — mỗi bước hiện giá trị thực tế
    // ═════════════════════════════════════════════════════════════
    private JPanel buildFormulaCard() {
        JPanel card = card("🧮  Công thức tính Điểm Xét Tuyển (ĐXT) — 6 bước");
        card.setBackground(FORMULA_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(FORMULA_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Tính lại từng bước theo đúng PRD section 2.9
        double dthgxt   = data.dthgxt;
        double dcCapped = Math.min(data.dcHSG + data.dcCC, 3.0);
        double base     = Math.min(dthgxt + dcCapped, 30.0);
        double dut      = data.dut;
        double dxt      = Math.min(base + dut, 30.0);

        String[][] steps = {
                { "Bước 1", "ĐTHGXT",
                        fmt(dthgxt),
                        "Điểm Tổ Hợp Gốc Xét Tuyển  (ĐTHXT − dolech)" },
                { "Bước 2", "ĐC_raw = ĐC_HSG + ĐC_CC",
                        fmt(data.dcHSG) + " + " + fmt(data.dcCC) + " = " + fmt(data.dcHSG + data.dcCC),
                        "Điểm cộng gốc chưa cap" },
                { "Bước 3", "ĐC = min(ĐC_raw, 3.0)",
                        fmt(dcCapped),
                        "Cap ĐC tối đa 3.0 điểm" },
                { "Bước 4", "base = min(ĐTHGXT + ĐC, 30.0)",
                        "min(" + fmt(dthgxt) + " + " + fmt(dcCapped) + ", 30) = " + fmt(base),
                        "Cap trung gian — tránh ĐƯT ra số âm" },
                { "Bước 5", "ĐƯT  (đối tượng + khu vực)",
                        fmt(data.dutDT) + " + " + fmt(data.dutKV) + " = " + fmt(dut),
                        base >= 22.5
                                ? "Giảm tuyến tính vì base = " + fmt(base) + " ≥ 22.5"
                                : "Lấy toàn bộ vì base = " + fmt(base) + " < 22.5" },
                { "Bước 6", "ĐXT = min(base + ĐƯT, 30.0)",
                        "min(" + fmt(base) + " + " + fmt(dut) + ", 30) = " + fmt(dxt),
                        "Điểm Xét Tuyển cuối cùng dùng để xét trúng tuyển" },
        };

        JPanel stepsPanel = new JPanel(new GridLayout(steps.length, 1, 0, 4));
        stepsPanel.setOpaque(false);

        for (int i = 0; i < steps.length; i++) {
            stepsPanel.add(buildFormulaRow(
                    steps[i][0], steps[i][1], steps[i][2], steps[i][3],
                    i == steps.length - 1 // highlight bước cuối
            ));
        }

        card.add(stepsPanel, BorderLayout.CENTER);
        return wrap(card);
    }

    private JPanel buildFormulaRow(String step, String formula,
                                   String value, String note,
                                   boolean highlight) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(highlight ? new Color(230, 255, 230) : Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(highlight ? new Color(100, 200, 100) : BORDER_COLOR, 1, true),
                new EmptyBorder(7, 12, 7, 12)
        ));

        // Step badge
        JLabel stepLbl = new JLabel(step);
        stepLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        stepLbl.setForeground(Color.WHITE);
        stepLbl.setBackground(highlight ? SUCCESS_GREEN : new Color(120, 130, 160));
        stepLbl.setOpaque(true);
        stepLbl.setBorder(new EmptyBorder(2, 6, 2, 6));
        stepLbl.setPreferredSize(new Dimension(52, 0));
        stepLbl.setHorizontalAlignment(SwingConstants.CENTER);

        // Formula
        JLabel fLbl = new JLabel(formula);
        fLbl.setFont(FONT_FORMULA);
        fLbl.setForeground(new Color(40, 60, 120));

        // Value
        JLabel vLbl = new JLabel(value);
        vLbl.setFont(highlight
                ? new Font("Consolas", Font.BOLD, 14)
                : new Font("Consolas", Font.BOLD, 12));
        vLbl.setForeground(highlight ? SUCCESS_GREEN : SGU_BLUE);
        vLbl.setPreferredSize(new Dimension(200, 0));
        vLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        // Note
        JLabel nLbl = new JLabel(note);
        nLbl.setFont(FONT_SMALL);
        nLbl.setForeground(new Color(100, 110, 130));

        JPanel left = new JPanel(new BorderLayout(0, 2));
        left.setOpaque(false);
        left.add(fLbl, BorderLayout.NORTH);
        left.add(nLbl, BorderLayout.CENTER);

        row.add(stepLbl, BorderLayout.WEST);
        row.add(left,    BorderLayout.CENTER);
        row.add(vLbl,    BorderLayout.EAST);
        return row;
    }

    // ═════════════════════════════════════════════════════════════
    //  SECTION 5 — Kết quả nguyện vọng
    // ═════════════════════════════════════════════════════════════
    private JPanel buildWishCard() {
        JPanel card = card("🎓  Kết quả Xét tuyển theo Nguyện vọng");

        String[] cols  = { "NV", "Mã ngành", "Tên ngành", "Phương thức", "Tổ hợp", "ĐXT", "Ngưỡng", "Kết quả" };
        int[]    widths = {  40,     90,         220,          120,          70,      70,    70,        130 };

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (WishResult w : data.wishes) {
            model.addRow(new Object[]{
                    w.thuTu, w.maNganh, w.tenNganh,
                    w.phuongThuc, w.toHopMon,
                    fmt(w.getDiemXetTuyen()), fmt(w.nguong),
                    w.ketQua
            });
        }

        JTable table = new JTable(model);
        table.setFont(FONT_TABLE);
        table.setRowHeight(30);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(225, 228, 235));
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(FONT_TABLE_H);
        hdr.setBackground(new Color(230, 235, 248));
        hdr.setForeground(SGU_BLUE);
        hdr.setPreferredSize(new Dimension(0, 32));
        hdr.setReorderingAllowed(false);

        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < widths.length; i++) {
            cm.getColumn(i).setPreferredWidth(widths[i]);
        }
        cm.getColumn(5).setCellRenderer(new CenterBoldRenderer(SGU_BLUE));
        cm.getColumn(6).setCellRenderer(new CenterBoldRenderer(new Color(80, 90, 110)));
        cm.getColumn(7).setCellRenderer(new KetQuaRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.setPreferredSize(new Dimension(0, data.wishes.size() * 30 + 60));

        card.add(scroll, BorderLayout.CENTER);
        return wrap(card);
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────
    private JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(SGU_BLUE_DARK);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(210, 220, 240)),
                new EmptyBorder(0, 0, 8, 0)
        ));
        p.add(lbl, BorderLayout.NORTH);
        return p;
    }

    private JPanel wrap(JPanel card) {
        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.setAlignmentX(LEFT_ALIGNMENT);
        w.add(card, BorderLayout.CENTER);
        return w;
    }

    private Component vgap(int h) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        p.setPreferredSize(new Dimension(0, h));
        return p;
    }

    private JPanel field(String label, String value, Font valueFont) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(FONT_SMALL);
        l.setForeground(Color.GRAY);
        JLabel v = new JLabel(value != null && !value.isEmpty() ? value : "—");
        v.setFont(valueFont);
        v.setForeground(new Color(25, 35, 65));
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JPanel scoreTable(String[] cols, String[][] rows) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(8, 0, 8, 0));

        DefaultTableModel model = new DefaultTableModel(rows, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(model);
        t.setFont(FONT_TABLE);
        t.setRowHeight(24);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(235, 238, 245));
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setFont(FONT_TABLE_H);
        h.setBackground(SECTION_BG);
        h.setForeground(SGU_BLUE_DARK);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object val,
                                                           boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, val, sel, focus, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 252));
                if (col == 1) {
                    boolean zero = "0.00".equals(val == null ? "" : val.toString());
                    c.setForeground(zero ? new Color(190, 195, 210) : SGU_BLUE);
                    c.setFont(zero ? FONT_TABLE : FONT_TABLE_H);
                } else {
                    c.setForeground(new Color(55, 65, 85));
                    c.setFont(FONT_TABLE);
                }
                ((JLabel) c).setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(t);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));
        scroll.setPreferredSize(new Dimension(0, rows.length * 24 + 36));
        p.add(scroll);
        return p;
    }

    private static String fmt(double v) {
        return v == 0 ? "0.00" : String.format("%.2f", v);
    }

    // ── Renderers ─────────────────────────────────────────────────
    private static class KetQuaRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object val,
                                                       boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            String v = val == null ? "" : val.toString();
            if (!sel) switch (v) {
                case "TRUNG_TUYEN"  -> { c.setBackground(TRUNG_TUYEN_BG); c.setForeground(new Color(0, 120, 0)); }
                case "TRUOT"        -> { c.setBackground(TRUOT_BG);        c.setForeground(ERROR_RED); }
                case "ERROR"        -> { c.setBackground(new Color(255, 245, 200)); c.setForeground(WARN_ORANGE); }
                case "KHONG_HOP_LE" -> { c.setBackground(new Color(240, 240, 255)); c.setForeground(new Color(80, 80, 180)); }
                default             -> { c.setBackground(new Color(245, 246, 250)); c.setForeground(Color.GRAY); }
            }
            ((JLabel) c).setFont(FONT_TABLE_H);
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    private static class CenterBoldRenderer extends DefaultTableCellRenderer {
        private final Color color;
        CenterBoldRenderer(Color color) { this.color = color; }
        public Component getTableCellRendererComponent(JTable t, Object val,
                                                       boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            ((JLabel) c).setFont(FONT_TABLE_H);
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            if (!sel) { c.setForeground(color); c.setBackground(Color.WHITE); }
            return c;
        }
    }

    // ═════════════════════════════════════════════════════════════
    //  DTOs — tên field khớp với DTO của team
    // ═════════════════════════════════════════════════════════════

    /**
     * Dữ liệu hiển thị dashboard 1 thí sinh.
     * Tên field điểm thi khớp với DiemThiImportDTO:
     *   toan, van, ly, hoa, sinh, su, dia, anh, nk1..nk8
     * Tên field thí sinh khớp với ThiSinhImportDTO:
     *   cccd, hoTen, ngaySinh, gioiTinh, doiTuongUt, khuVucUt
     */
    public static class CandidateData {
        // ThiSinhImportDTO
        public String cccd, hoTen, ngaySinh, gioiTinh;
        public String maTinh;           // nơi sinh / tỉnh
        public String doiTuongUt;       // đối tượng ưu tiên (01..07)
        public String doiTuongLabel;    // mô tả hiển thị (FE only)
        public String khuVucUt;         // khu vực ưu tiên (1/2NT/2/3)
        public String khuVucLabel;      // mô tả hiển thị (FE only)
        public int    namHoc = 2026;

        // DiemThiImportDTO — dùng Double để null = không thi (khác 0 = điểm liệt)
        public Double toan, van, ly, hoa, sinh, su, dia;
        public Double anh;              // N1_THI
        public Double anhCC;            // N1_CC = max(anh, certBest)
        public Double certBest;         // điểm chứng chỉ TA tốt nhất
        public Double nk1, nk2, nk3, nk4, nk5, nk6, nk7, nk8;

        // Điểm kỳ thi khác + quy đổi (FE only — không có trong DTO)
        public double dDGNL, dDGNLConverted;
        public double dVSAT, dVSATConverted;

        // xt_diemcongxetuyen (FE only)
        public double dc, dcHSG, dcCC;
        public double dut, dutDT, dutKV;

        // Trung gian cho section công thức (FE only)
        public double dthgxt;

        // NguyenVongImportDTO
        public List<WishResult> wishes = new ArrayList<>();

        // ── Helper: đọc điểm an toàn (null → 0.0) ────────────────
        public double getToan()  { return toan  != null ? toan  : 0.0; }
        public double getVan()   { return van   != null ? van   : 0.0; }
        public double getLy()    { return ly    != null ? ly    : 0.0; }
        public double getHoa()   { return hoa   != null ? hoa   : 0.0; }
        public double getSinh()  { return sinh  != null ? sinh  : 0.0; }
        public double getSu()    { return su    != null ? su    : 0.0; }
        public double getDia()   { return dia   != null ? dia   : 0.0; }
        public double getAnh()   { return anh   != null ? anh   : 0.0; }
        public double getAnhCC() { return anhCC != null ? anhCC : 0.0; }
        public double getNk1()   { return nk1   != null ? nk1   : 0.0; }
        public double getNk2()   { return nk2   != null ? nk2   : 0.0; }
        public double getNk3()   { return nk3   != null ? nk3   : 0.0; }
        public double getNk4()   { return nk4   != null ? nk4   : 0.0; }
        public double getNk5()   { return nk5   != null ? nk5   : 0.0; }
        public double getNk6()   { return nk6   != null ? nk6   : 0.0; }
        public double getNk7()   { return nk7   != null ? nk7   : 0.0; }
        public double getNk8()   { return nk8   != null ? nk8   : 0.0; }
    }

    /**
     * Kết quả 1 nguyện vọng — khớp với NguyenVongImportDTO:
     *   cccd, maNganh, thuTu, phuongThuc, toHopMon,
     *   diemThxt, diemUtqd, diemCong, diemXetTuyen, ketQua
     */
    public static class WishResult {
        public int    thuTu;            // NguyenVongImportDTO.thuTu  (= nv_tt)
        public String maNganh;          // NguyenVongImportDTO.maNganh
        public String tenNganh;         // FE only — join từ xt_nganh
        public String phuongThuc;       // NguyenVongImportDTO.phuongThuc
        public String toHopMon;         // NguyenVongImportDTO.toHopMon
        public Double diemXetTuyen;     // NguyenVongImportDTO.diemXetTuyen
        public double nguong;           // FE only — lấy từ NganhImportDTO.diemSan
        public String ketQua;           // NguyenVongImportDTO.ketQua

        // Helper: đọc an toàn
        public double getDiemXetTuyen() {
            return diemXetTuyen != null ? diemXetTuyen : 0.0;
        }
    }

    // ═════════════════════════════════════════════════════════════
    //  Sample data — xóa khi tích hợp Service thật (tuần 4)
    //
    //  Trả về null → TraCuuPanel show NOT_FOUND
    //  Dùng CCCD thật từ Ds_thi_sinh.xlsx để test demo:
    //    "012345678901" → TS_0001 (An Giang, điểm thấp)
    //    "024680135790" → TS_0002 (Trà Vinh, có NK1/NK2)
    //    "111111111111" → NOT_FOUND
    // ═════════════════════════════════════════════════════════════
    public static CandidateData createSampleData(String cccd) {
        if (cccd == null || cccd.length() != 12) return null;

        // Mô phỏng NOT_FOUND cho CCCD toàn số 1 hoặc không bắt đầu bằng 0
        char first = cccd.charAt(0);
        if (first != '0') return null;

        // Chọn dataset theo ký tự thứ 2
        if (cccd.charAt(1) == '2') return buildSample2(cccd); // thí sinh có NK + ĐGNL

        return buildSample1(cccd); // thí sinh cơ bản
    }

    private static CandidateData buildSample1(String cccd) {
        CandidateData d = new CandidateData();
        // ThiSinhImportDTO fields
        d.cccd        = cccd;
        d.hoTen       = "Nguyễn Thị Bảo Châu";
        d.ngaySinh    = "25/07/2007";
        d.gioiTinh    = "Nữ";
        d.maTinh      = "An Giang";
        d.doiTuongUt  = "";             // không có đối tượng ưu tiên
        d.doiTuongLabel = "";
        d.khuVucUt    = "1";
        d.khuVucLabel = "Xã đặc biệt khó khăn (+0.75)";
        d.namHoc      = 2026;

        // DiemThiImportDTO fields — null = không thi môn đó
        d.toan = 3.35; d.van = 5.25; d.ly = 5.75;
        d.hoa  = null; d.sinh = null; d.su = null; d.dia = null;
        d.anh  = 4.0;  d.certBest = null; d.anhCC = 4.0;
        // nk1..nk8 = null (không thi năng khiếu)

        d.dDGNL = 0; d.dVSAT = 0;
        d.dcHSG = 0; d.dcCC = 0; d.dc = 0;
        d.dutKV = 0.75; d.dutDT = 0; d.dut = 0.75;
        d.dthgxt = 14.35;

        // NguyenVongImportDTO fields
        WishResult w1 = new WishResult();
        w1.thuTu = 1; w1.maNganh = "7140222"; w1.tenNganh = "Sư phạm Mỹ thuật";
        w1.phuongThuc = "PT4"; w1.toHopMon = "H00";
        w1.diemXetTuyen = 15.10; w1.nguong = 12.0; w1.ketQua = "TRUNG_TUYEN";

        WishResult w2 = new WishResult();
        w2.thuTu = 2; w2.maNganh = "7140201"; w2.tenNganh = "Giáo dục Mầm non";
        w2.phuongThuc = "PT4"; w2.toHopMon = "M00";
        w2.diemXetTuyen = 15.10; w2.nguong = 20.0; w2.ketQua = "TRUOT";

        WishResult w3 = new WishResult();
        w3.thuTu = 3; w3.maNganh = "7220201"; w3.tenNganh = "Ngôn ngữ Anh";
        w3.phuongThuc = "PT4"; w3.toHopMon = "D01";
        w3.diemXetTuyen = null; w3.nguong = 22.0; w3.ketQua = "KHONG_HOP_LE";

        d.wishes.add(w1); d.wishes.add(w2); d.wishes.add(w3);
        return d;
    }

    private static CandidateData buildSample2(String cccd) {
        CandidateData d = new CandidateData();
        // ThiSinhImportDTO fields
        d.cccd        = cccd;
        d.hoTen       = "Trần Minh Khoa";
        d.ngaySinh    = "08/09/2007";
        d.gioiTinh    = "Nam";
        d.maTinh      = "Trà Vinh";
        d.doiTuongUt  = "02";
        d.doiTuongLabel = "Con thương binh (+1.5)";
        d.khuVucUt    = "2NT";
        d.khuVucLabel = "Nông thôn (+0.5)";
        d.namHoc      = 2026;

        // DiemThiImportDTO fields
        d.toan = 4.6; d.van = 7.75; d.su = 7.6; d.dia = 9.0;
        d.ly = null; d.hoa = null; d.sinh = null;
        d.anh = null; d.certBest = 9.0; d.anhCC = 9.0; // IELTS 7.0 → quy đổi 9.0
        d.nk1 = 8.5; d.nk2 = 7.5;
        // nk3..nk8 = null

        d.dDGNL = 820; d.dDGNLConverted = 19.30;
        d.dVSAT = 0;

        d.dcHSG = 1.5;  // Giải Ba Quốc gia môn su có trong tổ hợp
        d.dcCC  = 0.0;  // tổ hợp có anh → không cộng CC vào ĐC
        d.dc    = Math.min(d.dcHSG + d.dcCC, 3.0);
        d.dutDT = 1.5; d.dutKV = 0.5;
        d.dut   = d.dutDT + d.dutKV;
        d.dthgxt = 24.80;

        // NguyenVongImportDTO fields
        WishResult w1 = new WishResult();
        w1.thuTu = 1; w1.maNganh = "7140217"; w1.tenNganh = "Sư phạm Ngữ văn";
        w1.phuongThuc = "PT4"; w1.toHopMon = "C00";
        w1.diemXetTuyen = 28.30; w1.nguong = 25.0; w1.ketQua = "TRUNG_TUYEN";

        WishResult w2 = new WishResult();
        w2.thuTu = 2; w2.maNganh = "7480201"; w2.tenNganh = "Công nghệ thông tin";
        w2.phuongThuc = "PT2"; w2.toHopMon = "A01";
        w2.diemXetTuyen = 21.80; w2.nguong = 23.0; w2.ketQua = "TRUOT";

        d.wishes.add(w1); d.wishes.add(w2);
        return d;
    }

    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            JFrame f = new JFrame("SGU — Dashboard Thí sinh");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1100, 780);
            f.setMinimumSize(new Dimension(900, 600));
            f.setLocationRelativeTo(null);
            // Test 2 mẫu dữ liệu khác nhau:
            // "012345678901" → TS cơ bản (KV1, không NK, không ĐGNL)
            // "024680135790" → TS có NK, ĐGNL, đối tượng ưu tiên
            f.add(new ThiSinhDashboard(createSampleData("024680135790")));
            f.setVisible(true);
        });
    }
}