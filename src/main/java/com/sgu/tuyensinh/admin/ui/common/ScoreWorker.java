package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import java.util.List;

public class ScoreWorker extends SwingWorker<Void, Integer> {
    private ProgressPanel progressPanel;
    private int total;

    public ScoreWorker(ProgressPanel progressPanel, int total) {
        this.progressPanel = progressPanel;
        this.total = total;
    }

    @Override
    protected Void doInBackground() throws Exception {
        for (int i = 1; i <= total; i++) {
            // giả lập xử lý từng thí sinh
            Thread.sleep(200); 
            publish(i); // gửi tiến độ cho process()
        }
        return null;
    }

    @Override
    protected void process(List<Integer> chunks) {
        int current = chunks.get(chunks.size() - 1);
        progressPanel.updateProgress(current, total);
    }

    @Override
    protected void done() {
        progressPanel.finish(total);
    }
}






// MainFrame

// ProgressPanel progressPanel = new ProgressPanel(500);
// add(progressPanel, BorderLayout.SOUTH);

// JButton startButton = new JButton("Bắt đầu tính điểm");
// startButton.addActionListener(e -> {
//     ScoreWorker worker = new ScoreWorker(progressPanel, 500);
//     worker.execute();
// });
// add(startButton, BorderLayout.NORTH);