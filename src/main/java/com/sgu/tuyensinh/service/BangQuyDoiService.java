package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.BangQuyDoi;
import com.sgu.tuyensinh.repository.BangQuyDoiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.Comparator;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BangQuyDoiService {

    private final BangQuyDoiRepository repository;

    // =========================
    // 📌 GET ALL
    // =========================
    public List<BangQuyDoi> getAll() {
        return repository.findAll();
    }

    // =========================
    // 📌 GET BY ID
    // =========================
    public BangQuyDoi getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi"));
    }

    // =========================
    // 📌 SEARCH (Yêu cầu 9)
    // =========================
    public List<BangQuyDoi> search(String phuongThuc, String keyword) {

        if (phuongThuc == null || phuongThuc.isBlank()) {
            throw new IllegalArgumentException("Phương thức không được để trống");
        }

        phuongThuc = phuongThuc.trim().toUpperCase();

        // nếu không nhập keyword → trả tất cả theo phương thức
        if (keyword == null || keyword.isBlank()) {
            return repository.findByPhuongThuc(phuongThuc);
        }

        switch (phuongThuc) {
            case "VSAT":
            case "NGOAINGU":
                return repository.findByPhuongThucAndMonContainingIgnoreCase(
                        phuongThuc, keyword);

            
            case "DGNL":
                return repository.findByPhuongThucAndToHopContainingIgnoreCase(
                        phuongThuc, keyword);

            default:
                throw new IllegalArgumentException("Phương thức không hợp lệ");
        }
    }

    // =========================
    // 📌 CREATE
    // =========================
    public BangQuyDoi create(BangQuyDoi entity) {

        validate(entity);

        Optional<BangQuyDoi> existing = repository
                .findByPhuongThucAndToHopAndMonAndMaQuyDoi(
                        entity.getPhuongThuc(),
                        entity.getToHop(),
                        entity.getMon(),
                        entity.getMaQuyDoi()
                );

        if (existing.isPresent()) {
            throw new IllegalArgumentException("Dữ liệu quy đổi đã tồn tại");
        }

        return repository.save(entity);
    }

    // =========================
    // 📌 UPDATE
    // =========================
    public BangQuyDoi update(Integer id, BangQuyDoi newData) {

        BangQuyDoi old = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi"));

        validate(newData);

        old.setPhuongThuc(newData.getPhuongThuc());
        old.setToHop(newData.getToHop());
        old.setMon(newData.getMon());
        old.setDiemGocA(newData.getDiemGocA());
        old.setDiemGocB(newData.getDiemGocB());
        old.setDiemQuyDoiC(newData.getDiemQuyDoiC());
        old.setDiemQuyDoiD(newData.getDiemQuyDoiD());
        old.setMaQuyDoi(newData.getMaQuyDoi());
        old.setPhanVi(newData.getPhanVi());

        return repository.save(old);
    }

    // =========================
    // 📌 DELETE
    // =========================
    public void delete(Integer id) {

        BangQuyDoi entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi"));

        repository.delete(entity);
    }

   

    // =========================
    // 📌 VALIDATE
    // =========================
    private void validate(BangQuyDoi e) {

        if (e.getPhuongThuc() == null || e.getPhuongThuc().isBlank()) {
            throw new IllegalArgumentException("Phương thức không được trống");
        }

        if (e.getMaQuyDoi() == null || e.getMaQuyDoi().isBlank()) {
            throw new IllegalArgumentException("Mã quy đổi không được trống");
        }

        if (e.getDiemGocA() == null) {
            throw new IllegalArgumentException("Điểm A không được trống");
        }
    }

   /**
 * Hàm nội suy dùng chung cho DGNL (theo tổ hợp) và VSAT (theo môn).
 * Tuân thủ PRD SGU 2026: làm tròn 2 chữ số, có log clamp, có nam_hoc.
 */
public Double calculateInterpolation(String phuongThuc, String identifier, double diemX) {
    if (phuongThuc == null || identifier == null) {
        throw new IllegalArgumentException("Phương thức và mã (tổ hợp/môn) không được để trống");
    }

    // 1. Lấy dữ liệu từ Repository dựa trên loại phương thức 
    List<BangQuyDoi> intervals;
    if ("DGNL".equalsIgnoreCase(phuongThuc)) {
        // DGNL tìm theo tổ hợp (toHop)
        intervals = repository.findByPhuongThucAndToHopContainingIgnoreCase("DGNL", identifier.trim());
    } else {
        // VSAT (hoặc NGOAINGU) tìm theo môn (mon)
        intervals = repository.findByPhuongThucAndMonContainingIgnoreCase(phuongThuc, identifier.trim());
    }

    // 2. Lọc theo năm học 2026 
    intervals = intervals.stream()
            .filter(i -> i.getNamHoc() != null && i.getNamHoc() == 2026)
            .sorted(Comparator.comparingDouble(BangQuyDoi::getDiemGocA))
            .toList();

    if (intervals.isEmpty()) {
        throw new IllegalStateException("Không tìm thấy bảng quy đổi 2026 cho: " + phuongThuc + " - " + identifier);
    }

    // 3. Xử lý Clamp (Bo biên) & Ghi log (PRD trang 5)
    BangQuyDoi minKv = intervals.get(0);
    BangQuyDoi maxKv = intervals.get(intervals.size() - 1);

    if (diemX < minKv.getDiemGocA()) {
        // log.warn("INTERPOLATION_OUT_OF_TABLE: {}", diemX);
        return minKv.getDiemQuyDoiC();
    }
    if (diemX > maxKv.getDiemGocB()) {
        // log.warn("INTERPOLATION_OUT_OF_TABLE: {}", diemX);
        return maxKv.getDiemQuyDoiD();
    }

    // 4. Tính toán nội suy tuyến tính: y = c + ((x - a) / (b - a)) * (d - c)
    for (BangQuyDoi kv : intervals) {
        if (diemX >= kv.getDiemGocA() && diemX <= kv.getDiemGocB()) {
            double a = kv.getDiemGocA();
            double b = kv.getDiemGocB();
            double c = kv.getDiemQuyDoiC();
            double d = kv.getDiemQuyDoiD();

            double result = (Math.abs(b - a) < 1e-9) ? c : c + ((diemX - a) / (b - a)) * (d - c);
            
            // 5. Làm tròn 2 chữ số thập phân (PRD trang 5)
            return Math.round(result * 100.0) / 100.0;
        }
    }
    return 0.0;
}

    /**
     * Lấy danh sách quy đổi có phân trang (Read-only UI)
     */
    public Page<BangQuyDoi> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return repository.findByPhuongThucContainingIgnoreCaseOrMonContainingIgnoreCase(keyword, keyword, pageable);
        }
        return repository.findAll(pageable);
    }
}