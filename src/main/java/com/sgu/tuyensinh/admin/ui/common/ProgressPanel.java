package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ProgressPanel extends JPanel {

    private static final Color BG         = new Color(0xF3F2EE);
    private static final Color TRACK      = new Color(0xDDDBD4);
    private static final Color FILL       = new Color(0x1874D2);
    private static final Color TEXT_MAIN  = new Color(0x1A1A1A);
    private static final Color TEXT_MUTED = new Color(0x6B6B68);

    private final JLabel lblStatus;
    private final JLabel lblPercent;
    private final JLabel lblDetail;
    private final SmoothBar bar;

    public ProgressPanel() {
        setBackground(BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
            new ImportPanel.RoundedBorder(8, BG),
            new EmptyBorder(12, 14, 12, 14)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        setAlignmentX(LEFT_ALIGNMENT);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        lblStatus = new JLabel("Chờ import...");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setForeground(TEXT_MUTED);

        lblPercent = new JLabel("0%");
        lblPercent.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblPercent.setForeground(TEXT_MAIN);
        lblPercent.setHorizontalAlignment(SwingConstants.RIGHT);

        topRow.add(lblStatus,  BorderLayout.WEST);
        topRow.add(lblPercent, BorderLayout.EAST);

        bar = new SmoothBar();
        bar.setAlignmentX(LEFT_ALIGNMENT);

        lblDetail = new JLabel(" ");
        lblDetail.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblDetail.setForeground(TEXT_MUTED);
        lblDetail.setAlignmentX(LEFT_ALIGNMENT);

        add(topRow);
        add(Box.createVerticalStrut(8));
        add(bar);
        add(Box.createVerticalStrut(6));
        add(lblDetail);
    }

    public void updateProgress(int current, int total) {
        int pct = total > 0 ? (int)((current * 100.0) / total) : 0;
        SwingUtilities.invokeLater(() -> {
            bar.setValue(pct);
            lblPercent.setText(pct + "%");
            lblStatus.setText("Đang nhập...");
            lblDetail.setText(current + " / " + total + " dòng");
        });
    }

    public void finish(int total) {
        SwingUtilities.invokeLater(() -> {
            bar.setValue(100);
            lblPercent.setText("100%");
            lblStatus.setText("Hoàn tất");
            lblDetail.setText(total + " dòng đã xử lý");
        });
    }

    public void reset() {
        bar.setValue(0);
        lblPercent.setText("0%");
        lblStatus.setText("Đang xử lý...");
        lblDetail.setText(" ");
    }

    // ── Smooth progress bar ──────────────────────────────────
    static class SmoothBar extends JComponent {
        private int value = 0;

        SmoothBar() {
            setPreferredSize(new Dimension(100, 6));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        }

        void setValue(int v) {
            this.value = Math.max(0, Math.min(100, v));
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            // track
            g2.setColor(new Color(0xDDDBD4));
            g2.fillRoundRect(0, 0, w, h, h, h);
            // fill
            if (value > 0) {
                int filled = (int)(w * (value / 100.0));
                g2.setColor(new Color(0x1874D2));
                g2.fillRoundRect(0, 0, filled, h, h, h);
            }
            g2.dispose();
        }
    }
}
