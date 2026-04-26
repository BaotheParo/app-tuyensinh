package com.sgu.tuyensinh.admin.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.sgu.tuyensinh.admin.ui.common.ExportPanel;
import com.sgu.tuyensinh.service.impl.BaoCaoServiceImpl;
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

                // --- CONTENT ---
                JPanel pnlContent = new JPanel(new GridLayout(2, 2, 20, 20));
                pnlContent.setOpaque(false);
                pnlContent.setBorder(new EmptyBorder(20, 0, 0, 0));

                pnlContent.add(createBC01Chart());
                pnlContent.add(createBC02Histogram());
                pnlContent.add(createBC03PieChart());
                pnlContent.add(createExportCard());

                add(pnlContent, BorderLayout.CENTER);
        }

        // BC-01: Biểu đồ cột từ Service
        private JPanel createBC01Chart() {
                Map<String, Long> thongKe = baoCaoService.thongKeDangKyTheoNganh();

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

                // Tính tỷ lệ
                StringBuilder sb = new StringBuilder("Tỷ lệ: ");
                for (Map.Entry<String, Long> entry : thongKe.entrySet()) {
                        double percent = (entry.getValue() * 100.0) / tong;
                        sb.append(entry.getKey())
                                        .append(" ")
                                        .append(String.format("%.1f", percent))
                                        .append("%, ");
                }
                if (sb.length() > 2) {
                        sb.setLength(sb.length() - 2); // bỏ dấu ", " cuối
                }

                JFreeChart barChart = ChartFactory.createBarChart(
                                "", "Ngành", "Số lượng",
                                dataset, PlotOrientation.VERTICAL, false, true, false);

                JPanel card = createStyledCard("Thống kê đăng ký theo Ngành");
                card.add(new ChartPanel(barChart), BorderLayout.CENTER);

                // Info & Buttons
                JPanel pnlBottom = new JPanel(new GridLayout(4, 1));
                JLabel lblInfo = new JLabel("Tổng số đăng ký: " + tong, SwingConstants.CENTER);
                JLabel lblMax = new JLabel("Ngành cao nhất: " + nganhMax + " (" + max + ")", SwingConstants.CENTER);
                JLabel lblPercent = new JLabel(sb.toString(), SwingConstants.CENTER);

                JPanel pnlBtns = new JPanel(new FlowLayout());
                JButton btnDetail = new JButton("Xem chi tiết");
                btnDetail.addActionListener(e -> JOptionPane.showMessageDialog(this,
                                "Có " + thongKe.size() + " ngành đang mở tuyển."));
                JButton btnExport = new JButton("Xuất báo cáo ngành");
                btnExport.addActionListener(e -> {
                        // Tạo dialog chứa ExportPanel
                        JDialog dialog = new JDialog((Frame) null, "Xuất báo cáo ngành", true);

                        ExportPanel exportPanel = new ExportPanel(folder -> {
                                try {
                                        File file = new File(folder, "bao_cao_nganh.csv");
                                        PrintWriter writer = new PrintWriter(file);

                                        writer.println("Ngành, Số lượng đăng ký");
                                        for (Map.Entry<String, Long> entry : thongKe.entrySet()) {
                                                writer.println(entry.getKey() + "," + entry.getValue());
                                        }

                                        writer.close();
                                        JOptionPane.showMessageDialog(dialog, "Xuất báo cáo ngành thành công!");
                                } catch (Exception ex) {
                                        JOptionPane.showMessageDialog(dialog, "Lỗi khi xuất: " + ex.getMessage());
                                }
                        });

                        dialog.add(exportPanel);
                        dialog.pack();
                        dialog.setLocationRelativeTo(this);
                        dialog.setVisible(true);
                });

                pnlBtns.add(btnDetail);
                pnlBtns.add(btnExport);

                pnlBottom.add(lblInfo);
                pnlBottom.add(lblMax);
                pnlBottom.add(lblPercent);
                pnlBottom.add(pnlBtns);

                card.add(pnlBottom, BorderLayout.SOUTH);
                return card;
        }

        // BC-02: Biểu đồ phổ điểm từ Service
        private JPanel createBC02Histogram() {
                // Lấy danh sách môn từ service
                List<String> dsMon = baoCaoService.getDanhSachMonHoc();
                if (dsMon.isEmpty()) {
                        // Nếu chưa có dữ liệu môn thì trả về panel rỗng
                        return new JPanel(new BorderLayout());
                }

                // Dataset ban đầu cho môn đầu tiên
                String monDauTien = dsMon.get(0);
                HistogramDataset dataset = new HistogramDataset();
                double[] diem = baoCaoService.getDiemMonHoc(monDauTien);
                dataset.addSeries("Điểm số", diem, 10);

                JFreeChart histogram = ChartFactory.createHistogram(
                                "", "Điểm", "Số lượng",
                                dataset, PlotOrientation.VERTICAL, false, true, false);

                JPanel card = createStyledCard("Phổ điểm tuyển sinh");
                ChartPanel chartPanel = new ChartPanel(histogram);
                card.add(chartPanel, BorderLayout.CENTER);

                // Combobox lấy từ service
                JComboBox<String> cbMon = new JComboBox<>(dsMon.toArray(new String[0]));
                cbMon.addActionListener(e -> {
                        String mon = (String) cbMon.getSelectedItem();
                        double[] diemMon = baoCaoService.getDiemMonHoc(mon);

                        HistogramDataset newDataset = new HistogramDataset();
                        newDataset.addSeries("Điểm số", diemMon, 10);

                        JFreeChart newHistogram = ChartFactory.createHistogram(
                                        "", "Điểm", "Số lượng",
                                        newDataset, PlotOrientation.VERTICAL, false, true, false);

                        chartPanel.setChart(newHistogram);
                });

                JPanel pnlBottom = new JPanel(new FlowLayout());
                pnlBottom.add(new JLabel("Chọn môn:"));
                pnlBottom.add(cbMon);
                card.add(pnlBottom, BorderLayout.SOUTH);

                return card;
        }

        // BC-03: Tỷ lệ Đậu/Rớt theo ngành
        private JPanel createBC03PieChart() {
                // Lấy thống kê theo ngành từ service
                Map<String, BaoCaoServiceImpl.KetQuaTheoNganhDTO> ketQuaTheoNganh = baoCaoService.thongKeKetQuaTheoNganh();

                // Tạo combobox chọn ngành
                JComboBox<String> cbNganh = new JComboBox<>(ketQuaTheoNganh.keySet().toArray(new String[0]));

                // Dataset ban đầu cho ngành đầu tiên
                String nganhDauTien = cbNganh.getItemAt(0);
                BaoCaoServiceImpl.KetQuaTheoNganhDTO dto = ketQuaTheoNganh.get(nganhDauTien);

                DefaultPieDataset dataset = new DefaultPieDataset();
                dataset.setValue("Đậu", dto.soDau());
                dataset.setValue("Rớt", dto.soRot());

                JFreeChart pieChart = ChartFactory.createPieChart(
                                "", dataset, true, true, false);

                JPanel card = createStyledCard("Tỷ lệ trúng tuyển theo ngành");
                ChartPanel chartPanel = new ChartPanel(pieChart);
                card.add(chartPanel, BorderLayout.CENTER);

                // Label chi tiết
                JLabel lblDetail = new JLabel(
                                "<html><center>Ngành: " + dto.tenNganh() +
                                                " | Đậu: " + dto.soDau() +
                                                " | Rớt: " + dto.soRot() + "</center></html>",
                                SwingConstants.CENTER);
                card.add(lblDetail, BorderLayout.SOUTH);

                // Sự kiện chọn ngành
                cbNganh.addActionListener(e -> {
                        String nganh = (String) cbNganh.getSelectedItem();
                        BaoCaoServiceImpl.KetQuaTheoNganhDTO kq = ketQuaTheoNganh.get(nganh);

                        DefaultPieDataset newDataset = new DefaultPieDataset();
                        newDataset.setValue("Đậu", kq.soDau());
                        newDataset.setValue("Rớt", kq.soRot());

                        JFreeChart newPie = ChartFactory.createPieChart(
                                        "", newDataset, true, true, false);
                        chartPanel.setChart(newPie);

                        lblDetail.setText("<html><center>Ngành: " + kq.tenNganh() +
                                        " | Đậu: " + kq.soDau() +
                                        " | Rớt: " + kq.soRot() + "</center></html>");
                });

                // Panel chọn ngành
                JPanel pnlTop = new JPanel(new FlowLayout());
                pnlTop.add(new JLabel("Chọn ngành:"));
                pnlTop.add(cbNganh);
                card.add(pnlTop, BorderLayout.NORTH);

                return card;
        }

        // BC-04: Bảng kết quả trúng tuyển
        private JPanel createExportCard() {
        JPanel card = createStyledCard("Danh sách trúng tuyển");
        card.setBorder(new TitledBorder(new LineBorder(new Color(46, 204, 113), 2),
        "Kết quả trúng tuyển"));

        String[] cols = { "Mã TS", "Họ tên", "Ngành", "Điểm", "Trạng thái" };
        JTable table = new JTable(baoCaoService.getDanhSachTrungTuyen(), cols);
        card.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnExcel = new JButton("Xuất Excel", new
        FlatSVGIcon("icon/excel.svg", 16, 16));
        btnExcel.addActionListener(e -> JOptionPane.showMessageDialog(this,
        "Đã xuất danh sách " + table.getRowCount() + " thí sinh."));

        JPanel pnlBottom = new JPanel(new FlowLayout());
        pnlBottom.add(btnExcel);
        card.add(pnlBottom, BorderLayout.SOUTH);

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