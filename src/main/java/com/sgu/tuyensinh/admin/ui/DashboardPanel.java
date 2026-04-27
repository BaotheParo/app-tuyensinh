package com.sgu.tuyensinh.admin.ui;

import com.sgu.tuyensinh.service.BaoCaoService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;

/**
 * TUẦN 4 — DashboardPanel (Nhiệm vụ của Sô)
 * Package: com.sgu.tuyensinh.admin.ui
 *
 * Gắn vào MainFrame qua Spring DI, hiển thị khi nhấn nút "Báo Cáo"
 * (hoặc tách thành màn hình riêng).
 *
 * Gồm 3 phần:
 *   1. Stat cards: Tổng nộp / Tổng đậu / Ngành lấy điểm cao nhất
 *   2. Biểu đồ cột: Thống kê lượng thí sinh theo loại điểm (THPT/VSAT/ĐGNL)
 *   3. Histogram: Phổ điểm môn Toán / Văn / Anh
 *
 * Dùng dữ liệu từ BaoCaoService (đã có sẵn trong project).
 * Khi BE chưa xong → dùng mock data tự build bên trong.
 */
@Component
public class DashboardPanel extends JPanel {

    // ── Colors ────────────────────────────────────────────────────
    private static final Color BG             = new Color(245, 246, 250);
    private static final Color CARD_BG        = Color.WHITE;
    private static final Color BLUE           = new Color(41, 128, 185);
    private static final Color GREEN          = new Color(39, 174, 96);
    private static final Color ORANGE         = new Color(243, 156, 18);
    private static final Color RED            = new Color(231, 76, 60);
    private static final Color PURPLE         = new Color(142, 68, 173);
    private static final Color TEXT_DARK      = new Color(44, 62, 80);
    private static final Color TEXT_GRAY      = new Color(127, 140, 141);
    private static final Color BORDER_COLOR   = new Color(220, 220, 220);

    // ── Fonts ─────────────────────────────────────────────────────
    private static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SUBTITLE   = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_CARD_VAL   = new Font("Segoe UI", Font.BOLD, 34);
    private static final Font FONT_CARD_LBL   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SECTION    = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);

    private final BaoCaoService baoCaoService;

    // ── Stat card labels — để refresh được ───────────────────────
    private JLabel lblTongNop;
    private JLabel lblTongDau;
    private JLabel lblNganhMax;
    private JLabel lblNganhMaxDiem;

    // ── Chart panels — để refresh được ───────────────────────────
    private JPanel pnlChartLoaiDiem;
    private JPanel pnlChartPhoToan;
    private JPanel pnlChartPhoVan;
    private JPanel pnlChartPhoAnh;

    @Autowired
    public DashboardPanel(BaoCaoService baoCaoService) {
        this.baoCaoService = baoCaoService;
        initUI();
        loadData();
    }

    // ─────────────────────────────────────────────────────────────
    private void initUI() {
        setBackground(BG);
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.setBorder(new EmptyBorder(16, 24, 12, 24));

        JLabel title = new JLabel("Dashboard Thống Kê Tuyển Sinh 2026");
        title.setFont(FONT_TITLE);
        title.setForeground(BLUE);

        JLabel sub = new JLabel("Tổng quan kết quả xét tuyển — Trường Đại học Sài Gòn");
        sub.setFont(FONT_SUBTITLE);
        sub.setForeground(TEXT_GRAY);

        JButton btnRefresh = new JButton("↻ Làm mới");
        btnRefresh.setFont(FONT_SMALL);
        btnRefresh.setBackground(BLUE);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.setPreferredSize(new Dimension(100, 30));
        btnRefresh.addActionListener(e -> loadData());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        p.add(left,        BorderLayout.WEST);
        p.add(btnRefresh,  BorderLayout.EAST);
        return p;
    }

    // ── Content: stat cards + charts ──────────────────────────────
    private JScrollPane buildContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Row 1: Stat cards
        content.add(buildSectionTitle("Thống kê nhanh"));
        content.add(Box.createVerticalStrut(10));
        content.add(buildStatCardsRow());
        content.add(Box.createVerticalStrut(20));

        // Row 2: Biểu đồ loại điểm (THPT / VSAT / ĐGNL)
        content.add(buildSectionTitle("Thống kê thí sinh theo loại điểm thi"));
        content.add(Box.createVerticalStrut(10));
        pnlChartLoaiDiem = new JPanel(new BorderLayout());
        pnlChartLoaiDiem.setBackground(CARD_BG);
        pnlChartLoaiDiem.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        pnlChartLoaiDiem.setPreferredSize(new Dimension(0, 280));
        pnlChartLoaiDiem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        pnlChartLoaiDiem.setAlignmentX(LEFT_ALIGNMENT);
        content.add(pnlChartLoaiDiem);
        content.add(Box.createVerticalStrut(20));

        // Row 3: Phổ điểm 3 môn
        content.add(buildSectionTitle("Phổ điểm môn Toán / Văn / Anh"));
        content.add(Box.createVerticalStrut(10));
        JPanel row3 = new JPanel(new GridLayout(1, 3, 14, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        row3.setAlignmentX(LEFT_ALIGNMENT);

        pnlChartPhoToan = chartPlaceholder("Toán");
        pnlChartPhoVan  = chartPlaceholder("Văn");
        pnlChartPhoAnh  = chartPlaceholder("Anh");
        row3.add(pnlChartPhoToan);
        row3.add(pnlChartPhoVan);
        row3.add(pnlChartPhoAnh);
        content.add(row3);
        content.add(Box.createVerticalStrut(16));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── Stat cards row ────────────────────────────────────────────
    private JPanel buildStatCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        row.setAlignmentX(LEFT_ALIGNMENT);

        lblTongNop        = new JLabel("...");
        lblTongDau        = new JLabel("...");
        lblNganhMax       = new JLabel("...");
        lblNganhMaxDiem   = new JLabel("...");

        row.add(statCard("Tổng hồ sơ nộp",       lblTongNop,      BLUE));
        row.add(statCard("Tổng trúng tuyển",       lblTongDau,      GREEN));
        row.add(statCard("Ngành lấy điểm cao nhất",lblNganhMax,     ORANGE));
        row.add(statCard("Điểm chuẩn cao nhất",    lblNganhMaxDiem, PURPLE));
        return row;
    }

    private JPanel statCard(String label, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));
        // Accent bar trên đỉnh card
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
                new EmptyBorder(14, 20, 14, 20)
        ));

        valueLabel.setFont(FONT_CARD_VAL);
        valueLabel.setForeground(accentColor);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_CARD_LBL);
        lbl.setForeground(TEXT_GRAY);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lbl,        BorderLayout.SOUTH);
        return card;
    }

    // ── Section title ─────────────────────────────────────────────
    private JLabel buildSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(TEXT_DARK);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel chartPlaceholder(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
        JLabel lbl = new JLabel("Đang tải " + title + "...", SwingConstants.CENTER);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_GRAY);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ═════════════════════════════════════════════════════════════
    //  Load data — dùng SwingWorker để không đơ UI
    // ═════════════════════════════════════════════════════════════
    public void loadData() {
        // Reset về trạng thái loading
        lblTongNop.setText("...");
        lblTongDau.setText("...");
        lblNganhMax.setText("...");
        lblNganhMaxDiem.setText("...");

        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() {
                return fetchData();
            }

            @Override
            protected void done() {
                try {
                    DashboardData d = get();
                    renderData(d);
                } catch (Exception ex) {
                    lblTongNop.setText("Lỗi");
                    System.err.println("[DashboardPanel] loadData error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────
    //  fetchData — gọi BaoCaoService, fallback sang mock nếu lỗi
    // ─────────────────────────────────────────────────────────────
    private DashboardData fetchData() {
        DashboardData d = new DashboardData();
        try {
            // ── 1. Stat cards ──────────────────────────────────
            // Tổng hồ sơ nộp = tổng tất cả nguyện vọng
            Map<String, Long> dangKy = baoCaoService.thongKeDangKyTheoNganh();
            d.tongNop = dangKy.values().stream().mapToLong(Long::longValue).sum();

            // Tổng trúng tuyển
            Map<String, BaoCaoService.KetQuaTheoNganhDTO> ketQua = baoCaoService.thongKeKetQuaTheoNganh();
            d.tongDau = ketQua.values().stream().mapToLong(BaoCaoService.KetQuaTheoNganhDTO::soDau).sum();

            // Ngành lấy điểm cao nhất — lấy từ getDanhSachTrungTuyen data
            // Dùng dangKy map để tìm ngành có số lượng lớn nhất làm proxy
            String nganhMaxKey = "";
            double diemMaxVal  = 0;
            for (Map.Entry<String, BaoCaoService.KetQuaTheoNganhDTO> e : ketQua.entrySet()) {
                // Giả sử tenNganh có dấu ":" kèm điểm — nếu không thì lấy tên
                // Thực tế cần thêm hàm getDiemChuanCaoNhat() vào BaoCaoService
                if (e.getValue().soDau() > 0) {
                    nganhMaxKey = e.getValue().tenNganh();
                    break;
                }
            }
            d.nganhMax      = nganhMaxKey.isEmpty() ? "Chưa có dữ liệu" : nganhMaxKey;
            d.diemChuanMax  = diemMaxVal;

            // ── 2. Biểu đồ loại điểm (THPT/VSAT/ĐGNL) ─────────
            // BaoCaoService.getDiemMonHoc trả về double[]
            // Đếm số thí sinh có điểm > 0 theo từng loại
            double[] dsToan = baoCaoService.getDiemMonHoc("toan");
            double[] dsVsat = baoCaoService.getDiemMonHoc("nk1");  // nk1 = V-SAT theo entity
            double[] dsDgnl = baoCaoService.getDiemMonHoc("nk2");  // nk2 = ĐGNL theo entity

            d.soThiSinhThpt = countNonZero(dsToan);
            d.soThiSinhVsat = countNonZero(dsVsat);
            d.soThiSinhDgnl = countNonZero(dsDgnl);

            // ── 3. Phổ điểm 3 môn ──────────────────────────────
            d.diemToan = dsToan;
            d.diemVan  = baoCaoService.getDiemMonHoc("van");
            d.diemAnh  = baoCaoService.getDiemMonHoc("anh");

        } catch (Exception ex) {
            // BE chưa xong — dùng mock data để UI vẫn chạy được
            System.err.println("[DashboardPanel] Service error, dùng mock data: " + ex.getMessage());
            d = buildMockData();
        }
        return d;
    }

    /** Mock data từ DatabaseSeeder: 25 thí sinh, 4 ngành, điểm random 4.0–9.5 */
    private DashboardData buildMockData() {
        DashboardData d   = new DashboardData();
        d.tongNop         = 75;   // 25 thí sinh × 3 NV
        d.tongDau         = 48;
        d.nganhMax        = "Ngôn ngữ Anh (7220201)";
        d.diemChuanMax    = 24.5;
        d.soThiSinhThpt   = 25;
        d.soThiSinhVsat   = 12;
        d.soThiSinhDgnl   = 8;

        // Sinh điểm giả lập phân phối chuẩn cho histogram
        d.diemToan = generateMockScores(25, 6.0, 1.5);
        d.diemVan  = generateMockScores(25, 6.5, 1.2);
        d.diemAnh  = generateMockScores(25, 7.0, 1.0);
        return d;
    }

    private double[] generateMockScores(int n, double mean, double std) {
        double[] arr = new double[n];
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < n; i++) {
            double v = mean + rnd.nextGaussian() * std;
            arr[i] = Math.max(0, Math.min(10, Math.round(v * 10.0) / 10.0));
        }
        return arr;
    }

    private long countNonZero(double[] arr) {
        if (arr == null) return 0;
        long c = 0;
        for (double v : arr) if (v > 0) c++;
        return c;
    }

    // ─────────────────────────────────────────────────────────────
    //  renderData — chạy trên EDT
    // ─────────────────────────────────────────────────────────────
    private void renderData(DashboardData d) {
        // 1. Stat cards
        lblTongNop.setText(String.format("%,d", d.tongNop));
        lblTongDau.setText(String.format("%,d", d.tongDau));
        lblNganhMax.setText(shortenName(d.nganhMax, 18));
        lblNganhMax.setToolTipText(d.nganhMax);
        lblNganhMaxDiem.setText(d.diemChuanMax > 0
                ? String.format("%.2f", d.diemChuanMax)
                : "Chưa xét");

        // 2. Biểu đồ loại điểm
        pnlChartLoaiDiem.removeAll();
        pnlChartLoaiDiem.add(buildLoaiDiemChart(d), BorderLayout.CENTER);
        pnlChartLoaiDiem.revalidate();
        pnlChartLoaiDiem.repaint();

        // 3. Histogram phổ điểm
        replaceChart(pnlChartPhoToan, buildHistogram(d.diemToan, "Toán",  new Color(52, 152, 219)));
        replaceChart(pnlChartPhoVan,  buildHistogram(d.diemVan,  "Văn",   new Color(46, 204, 113)));
        replaceChart(pnlChartPhoAnh,  buildHistogram(d.diemAnh,  "Anh",   new Color(155, 89, 182)));
    }

    // ── Biểu đồ cột: số thí sinh theo loại điểm thi ──────────────
    private ChartPanel buildLoaiDiemChart(DashboardData d) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(d.soThiSinhThpt, "Thí sinh", "THPT");
        dataset.addValue(d.soThiSinhVsat, "Thí sinh", "V-SAT");
        dataset.addValue(d.soThiSinhDgnl, "Thí sinh", "ĐGNL");

        JFreeChart chart = ChartFactory.createBarChart(
                "Số lượng thí sinh theo loại điểm thi",
                "Loại kỳ thi", "Số thí sinh",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        // Style
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 13));

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, BLUE);
        renderer.setMaximumBarWidth(0.15);
        renderer.setItemMargin(0.1);

        // Hiện số trên đầu cột
        renderer.setDefaultItemLabelGenerator(
                new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 12));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((NumberAxis) plot.getRangeAxis()).setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 11));

        ChartPanel cp = new ChartPanel(chart);
        cp.setPreferredSize(new Dimension(0, 260));
        cp.setMouseWheelEnabled(false);
        return cp;
    }

    // ── Histogram phổ điểm 1 môn ─────────────────────────────────
    private ChartPanel buildHistogram(double[] scores, String monName, Color barColor) {
        HistogramDataset dataset = new HistogramDataset();
        if (scores != null && scores.length > 0) {
            dataset.addSeries(monName, scores, 10, 0.0, 10.0);
        }

        JFreeChart chart = ChartFactory.createHistogram(
                "Phổ điểm môn " + monName,
                "Điểm", "Số thí sinh",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 12));

        CategoryPlot dummyPlot = null;
        org.jfree.chart.plot.XYPlot xyPlot = (org.jfree.chart.plot.XYPlot) chart.getPlot();
        xyPlot.setBackgroundPaint(Color.WHITE);
        xyPlot.setRangeGridlinePaint(new Color(220, 220, 220));
        xyPlot.setOutlineVisible(false);

        org.jfree.chart.renderer.xy.XYBarRenderer renderer =
                (org.jfree.chart.renderer.xy.XYBarRenderer) xyPlot.getRenderer();
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter());
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, barColor);

        ChartPanel cp = new ChartPanel(chart);
        cp.setMouseWheelEnabled(false);
        return cp;
    }

    private void replaceChart(JPanel container, ChartPanel newChart) {
        container.removeAll();
        container.add(newChart, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
    }

    private String shortenName(String s, int maxLen) {
        if (s == null) return "—";
        return s.length() > maxLen ? s.substring(0, maxLen) + "…" : s;
    }

    // ── Data holder ───────────────────────────────────────────────
    private static class DashboardData {
        long   tongNop, tongDau;
        String nganhMax = "";
        double diemChuanMax;
        long   soThiSinhThpt, soThiSinhVsat, soThiSinhDgnl;
        double[] diemToan, diemVan, diemAnh;
    }
}