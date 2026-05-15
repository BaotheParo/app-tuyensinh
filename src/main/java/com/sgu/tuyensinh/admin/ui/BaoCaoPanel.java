package com.sgu.tuyensinh.admin.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.sgu.tuyensinh.admin.ui.common.ExportPanel;
import com.sgu.tuyensinh.service.BaoCaoServiceImpl;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.io.File;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

@Component
public class BaoCaoPanel extends JPanel {

    private final BaoCaoServiceImpl baoCaoService;
    private JComboBox<Integer> cbNamHoc;
    private JPanel pnlContent;
    private boolean dataLoaded = false;

    @Autowired
    public BaoCaoPanel(BaoCaoServiceImpl baoCaoService) {
        this.baoCaoService = baoCaoService;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("HỆ THỐNG BÁO CÁO THỐNG KÊ TUYỂN SINH");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(41, 128, 185));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlFilter.setOpaque(false);

        JLabel lblFilter = new JLabel("Năm tuyển sinh:");
        Integer[] years = new Integer[6];
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i <= 5; i++)
            years[i] = currentYear - i;
        cbNamHoc = new JComboBox<>(years);
        cbNamHoc.setPreferredSize(new Dimension(120, 35));

        JButton btnPrint = new JButton("In báo cáo", new FlatSVGIcon("icon/print.svg", 16, 16));
        btnPrint.setPreferredSize(new Dimension(120, 35));

        pnlFilter.add(lblFilter);
        pnlFilter.add(cbNamHoc);
        pnlFilter.add(btnPrint);
        pnlHeader.add(pnlFilter, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // --- CONTENT Placeholder ---
        pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);
        JLabel lblLoading = new JLabel("Đang chuẩn bị dữ liệu...", SwingConstants.CENTER);
        lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        pnlContent.add(lblLoading, BorderLayout.CENTER);

        add(pnlContent, BorderLayout.CENTER);
    }

    public void loadData() {
        pnlContent.removeAll();
        pnlContent.setLayout(new BorderLayout());
        JLabel lblLoading = new JLabel("Đang xử lý dữ liệu báo cáo (50,000+ hồ sơ)...", SwingConstants.CENTER);
        lblLoading.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblLoading.setForeground(new Color(41, 128, 185));
        pnlContent.add(lblLoading, BorderLayout.CENTER);
        pnlContent.revalidate();
        pnlContent.repaint();

        SwingWorker<ReportData, Void> worker = new SwingWorker<>() {
            @Override
            protected ReportData doInBackground() throws Exception {
                ReportData data = new ReportData();
                data.thongKeNganh = baoCaoService.thongKeDangKyTheoNganh();
                data.dsMon = baoCaoService.getDanhSachMonHoc();
                if (!data.dsMon.isEmpty()) {
                    data.diemMonDau = baoCaoService.getDiemMonHoc(data.dsMon.get(0));
                }
                data.ketQuaTheoNganh = baoCaoService.thongKeKetQuaTheoNganh();
                data.dsTrungTuyen = baoCaoService.getDanhSachTrungTuyen("Tất cả");
                data.thongKePt = baoCaoService.thongKePhuongThucTheoNganh();
                return data;
            }

            @Override
            protected void done() {
                try {
                    ReportData data = get();
                    pnlContent.removeAll();
                    pnlContent.setLayout(new GridLayout(0, 2, 20, 20));
                    
                    pnlContent.add(renderBC01Chart(data.thongKeNganh));
                    pnlContent.add(renderBC02Histogram(data.dsMon, data.diemMonDau));
                    pnlContent.add(renderBC03PieChart(data.ketQuaTheoNganh));
                    pnlContent.add(renderExportCard(data.dsTrungTuyen, data.ketQuaTheoNganh));
                    pnlContent.add(renderBC05BarChart(data.thongKePt));
                    
                    pnlContent.revalidate();
                    pnlContent.repaint();
                    dataLoaded = true;
                } catch (Exception e) {
                    pnlContent.removeAll();
                    pnlContent.add(new JLabel("Lỗi khi tải báo cáo: " + e.getMessage()), BorderLayout.CENTER);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private static class ReportData {
        Map<String, Long> thongKeNganh;
        List<String> dsMon;
        double[] diemMonDau;
        Map<String, BaoCaoServiceImpl.KetQuaTheoNganhDTO> ketQuaTheoNganh;
        Object[][] dsTrungTuyen;
        Map<String, Map<String, Long>> thongKePt;
    }

    // Đổi tên các hàm create... thành render... và truyền data vào để không gọi service trong EDT
    private JPanel renderBC01Chart(Map<String, Long> thongKe) {
        long tong = 0;
        String nganhMax = "";
        long max = 0;

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Long> entry : thongKe.entrySet()) {
            dataset.addValue(entry.getValue(), "Đăng ký", entry.getKey());
            tong += entry.getValue();
            if (entry.getValue() > max) {
                max = entry.getValue();
                nganhMax = entry.getKey();
            }
        }

        StringBuilder sb = new StringBuilder("Tỷ lệ: ");
        for (Map.Entry<String, Long> entry : thongKe.entrySet()) {
            double percent = tong > 0 ? (entry.getValue() * 100.0) / tong : 0;
            sb.append(entry.getKey()).append(" ").append(String.format("%.1f", percent)).append("%, ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2);

        JFreeChart barChart = ChartFactory.createBarChart("", "Ngành", "Số lượng", dataset, PlotOrientation.VERTICAL, false, true, false);
        JPanel card = createStyledCard("Thống kê đăng ký theo Ngành");
        card.add(new ChartPanel(barChart), BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new GridLayout(4, 1));
        pnlBottom.add(new JLabel("Tổng số đăng ký: " + tong, SwingConstants.CENTER));
        pnlBottom.add(new JLabel("Ngành cao nhất: " + nganhMax + " (" + max + ")", SwingConstants.CENTER));
        pnlBottom.add(new JLabel(sb.toString(), SwingConstants.CENTER));
        card.add(pnlBottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel renderBC02Histogram(List<String> dsMon, double[] diem) {
        HistogramDataset dataset = new HistogramDataset();
        if (diem != null && diem.length > 0) {
            dataset.addSeries("Điểm số", diem, 20, 0.0, 10.0);
        }

        JFreeChart histogram = ChartFactory.createHistogram("", "Điểm", "Số lượng", dataset, PlotOrientation.VERTICAL, false, true, false);
        org.jfree.chart.plot.XYPlot plot = (org.jfree.chart.plot.XYPlot) histogram.getPlot();
        org.jfree.chart.axis.NumberAxis domainAxis = (org.jfree.chart.axis.NumberAxis) plot.getDomainAxis();
        domainAxis.setRange(0.0, 10.0);
        domainAxis.setTickUnit(new org.jfree.chart.axis.NumberTickUnit(0.5));
        
        JPanel card = createStyledCard("Phổ điểm tuyển sinh");
        ChartPanel chartPanel = new ChartPanel(histogram);
        card.add(chartPanel, BorderLayout.CENTER);

        JComboBox<String> cbMon = new JComboBox<>(dsMon.toArray(new String[0]));
        cbMon.addActionListener(e -> {
            String mon = (String) cbMon.getSelectedItem();
            double[] diemMon = baoCaoService.getDiemMonHoc(mon);
            HistogramDataset newDataset = new HistogramDataset();
            if (diemMon != null && diemMon.length > 0) newDataset.addSeries("Điểm số", diemMon, 20, 0.0, 10.0);
            
            JFreeChart newChart = ChartFactory.createHistogram("", "Điểm", "Số lượng", newDataset, PlotOrientation.VERTICAL, false, true, false);
            org.jfree.chart.plot.XYPlot newPlot = (org.jfree.chart.plot.XYPlot) newChart.getPlot();
            org.jfree.chart.axis.NumberAxis newDomainAxis = (org.jfree.chart.axis.NumberAxis) newPlot.getDomainAxis();
            newDomainAxis.setRange(0.0, 10.0);
            newDomainAxis.setTickUnit(new org.jfree.chart.axis.NumberTickUnit(0.5));
            
            chartPanel.setChart(newChart);
        });

        JPanel pnlBottom = new JPanel(new FlowLayout());
        pnlBottom.add(new JLabel("Chọn môn:"));
        pnlBottom.add(cbMon);
        card.add(pnlBottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel renderBC03PieChart(Map<String, BaoCaoServiceImpl.KetQuaTheoNganhDTO> ketQuaTheoNganh) {
        JComboBox<String> cbNganh = new JComboBox<>(ketQuaTheoNganh.keySet().toArray(new String[0]));
        String nganhDauTien = (cbNganh.getItemCount() > 0) ? cbNganh.getItemAt(0) : null;
        DefaultPieDataset dataset = new DefaultPieDataset();

        if (nganhDauTien != null) {
            BaoCaoServiceImpl.KetQuaTheoNganhDTO dto = ketQuaTheoNganh.get(nganhDauTien);
            dataset.setValue("Đậu", dto.soDau());
            dataset.setValue("Rớt", dto.soRot());
        }

        JFreeChart pieChart = ChartFactory.createPieChart("", dataset, true, true, false);
        JPanel card = createStyledCard("Tỷ lệ trúng tuyển theo ngành");
        ChartPanel chartPanel = new ChartPanel(pieChart);
        card.add(chartPanel, BorderLayout.CENTER);

        JLabel lblDetail = new JLabel("Chưa có dữ liệu", SwingConstants.CENTER);
        if (nganhDauTien != null) {
            BaoCaoServiceImpl.KetQuaTheoNganhDTO dto = ketQuaTheoNganh.get(nganhDauTien);
            lblDetail.setText("<html><center>Ngành: " + dto.tenNganh() + " | Đậu: " + dto.soDau() + " | Rớt: " + dto.soRot() + "</center></html>");
        }
        card.add(lblDetail, BorderLayout.SOUTH);

        cbNganh.addActionListener(e -> {
            String nganh = (String) cbNganh.getSelectedItem();
            BaoCaoServiceImpl.KetQuaTheoNganhDTO kq = ketQuaTheoNganh.get(nganh);
            DefaultPieDataset newDataset = new DefaultPieDataset();
            newDataset.setValue("Đậu", kq.soDau());
            newDataset.setValue("Rớt", kq.soRot());
            chartPanel.setChart(ChartFactory.createPieChart("", newDataset, true, true, false));
            lblDetail.setText("<html><center>Ngành: " + kq.tenNganh() + " | Đậu: " + kq.soDau() + " | Rớt: " + kq.soRot() + "</center></html>");
        });

        JPanel pnlTop = new JPanel(new FlowLayout());
        pnlTop.add(new JLabel("Chọn ngành:"));
        pnlTop.add(cbNganh);
        card.add(pnlTop, BorderLayout.NORTH);
        return card;
    }

    private JPanel renderExportCard(Object[][] data, Map<String, BaoCaoServiceImpl.KetQuaTheoNganhDTO> ketQuaTheoNganh) {
        JPanel card = createStyledCard("Danh sách trúng tuyển chi tiết (Top 100)");
        String[] cols = { "Mã TS", "Họ tên", "Ngành", "Phương thức", "Điểm", "Trạng thái" };
        DefaultTableModel model = new DefaultTableModel(data, cols);
        JTable table = new JTable(model);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTop.add(new JLabel("Lọc theo Ngành: "));
        JComboBox<String> cbNganh = new JComboBox<>();
        cbNganh.addItem("Tất cả");
        for (String nganh : ketQuaTheoNganh.keySet()) cbNganh.addItem(nganh);
        
        cbNganh.addActionListener(e -> {
            String selectedNganh = (String) cbNganh.getSelectedItem();
            model.setDataVector(baoCaoService.getDanhSachTrungTuyen(selectedNganh), cols);
        });
        
        pnlTop.add(cbNganh);
        card.add(pnlTop, BorderLayout.NORTH);
        return card;
    }

    private JPanel renderBC05BarChart(Map<String, Map<String, Long>> thongKePt) {
        JComboBox<String> cbNganh = new JComboBox<>();
        if (thongKePt.isEmpty()) cbNganh.addItem("Chưa có dữ liệu");
        else {
            for (String nganh : thongKePt.keySet()) cbNganh.addItem(nganh);
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String nganhDauTien = (cbNganh.getItemCount() > 0 && !thongKePt.isEmpty()) ? cbNganh.getItemAt(0) : null;
        if (nganhDauTien != null) {
            Map<String, Long> ptCounts = thongKePt.get(nganhDauTien);
            for (Map.Entry<String, Long> entry : ptCounts.entrySet()) dataset.addValue(entry.getValue(), "Số lượng", entry.getKey());
        }

        JFreeChart barChart = ChartFactory.createBarChart("", "Phương thức", "Số lượng", dataset, PlotOrientation.VERTICAL, false, true, false);
        JPanel card = createStyledCard("Phân bổ phương thức trúng tuyển");
        ChartPanel chartPanel = new ChartPanel(barChart);
        card.add(chartPanel, BorderLayout.CENTER);

        cbNganh.addActionListener(e -> {
            String nganh = (String) cbNganh.getSelectedItem();
            if (nganh == null || nganh.equals("Chưa có dữ liệu")) return;
            Map<String, Long> ptCounts = thongKePt.get(nganh);
            DefaultCategoryDataset newDataset = new DefaultCategoryDataset();
            if (ptCounts != null) {
                for (Map.Entry<String, Long> entry : ptCounts.entrySet()) newDataset.addValue(entry.getValue(), "Số lượng", entry.getKey());
            }
            barChart.getCategoryPlot().setDataset(newDataset);
        });

        JPanel pnlTop = new JPanel(new FlowLayout());
        pnlTop.add(new JLabel("Chọn ngành:"));
        pnlTop.add(cbNganh);
        card.add(pnlTop, BorderLayout.NORTH);
        return card;
    }

    // Helper tạo Card layout chung
    private JPanel createStyledCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new TitledBorder(new LineBorder(new Color(230, 230, 230)), title,
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));
        return card;
    }
}