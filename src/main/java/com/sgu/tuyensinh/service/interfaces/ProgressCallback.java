package com.sgu.tuyensinh.service.interfaces;

/**
 * Interface callback tiến trình import dùng chung
 * cho cả tầng service và UI (ImportWorker).
 */
public interface ProgressCallback {
    void onProgress(int current, int total);
}
