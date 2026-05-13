package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.DiemThi;
import com.sgu.tuyensinh.entity.ThiSinh;
import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.repository.ThiSinhRepository;
import com.sgu.tuyensinh.service.dto.DiemCongDTO;
import com.sgu.tuyensinh.service.dto.DiemThiDTO;
import com.sgu.tuyensinh.service.dto.ThiSinhDetailDTO;
import com.sgu.tuyensinh.repository.custom.ThiSinhCustomRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xá»­ lÃ½ logic vÃ  cung cáº¥p API ná»™i bá»™ cho UI (Desktop Admin: Java
 * Swing).
 * KhÃ´ng sá»­ dá»¥ng Controller REST vÃ¬ giao diá»‡n gá»i trá»±c tiáº¿p hÃ m (MÃ´ hÃ¬nh
 * Monolith).
 */
@Service
public class ThiSinhService {

    @Autowired
    private ThiSinhRepository thiSinhRepository;

    @Autowired
    private DiemThiRepository diemThiRepository;

    @Autowired
    private ThiSinhCustomRepository repo;

    /**
     * 1. Láº¥y danh sÃ¡ch phÃ¢n trang vÃ  ngÄƒn cháº·n N+1 query.
     * Sá»­ dá»¥ng PageRequest Ä‘á»ƒ láº¥y Ä‘Ãºng lÆ°á»£ng dá»¯ liá»‡u (VD: 20 dÃ²ng/trang).
     */
    public Page<ThiSinh> getDanhSachThiSinhPhanTrang(int pageNumber, int pageSize) {
        return thiSinhRepository.findAllWithDiemThi(PageRequest.of(pageNumber, pageSize));
    }

    /**
     * 2. Cáº­p nháº­t Ä‘Ã¨ Ä‘iá»ƒm sá»‘ cho má»™t thÃ­ sinh.
     * DÃ¹ng Annotation @Transactional Ä‘á»ƒ báº£o Ä‘áº£m tÃ­nh toÃ n váº¹n,
     * náº¿u cÃ³ lá»—i vÄƒng ra há»‡ thá»‘ng sáº½ rollback dá»¯ liá»‡u.
     */
    @Transactional
    public DiemThi capNhatDiemThi(String cccd, DiemThi diemMoi) {
        // Kiá»ƒm tra thi sinh cÃ³ thá»±c sá»± tá»“n táº¡i trong DB khÃ´ng
        thiSinhRepository.findById(cccd)
                .orElseThrow(() -> new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y thÃ­ sinh vá»›i CCCD: " + cccd));

        // Láº¥y DiemThi hiá»‡n táº¡i, náº¿u chÆ°a nháº­p bao giá» (null) thÃ¬ táº¡o record tráº¯ng
        DiemThi diemHienTai = diemThiRepository.findByCccd(cccd)
                .orElse(new DiemThi());

        if (diemHienTai.getCccd() == null) {
            diemHienTai.setCccd(cccd);
        }

        // Cáº­p nháº­t cÃ¡c Ä‘iá»ƒm mÃ´n vÄƒn hÃ³a cÆ¡ báº£n
        diemHienTai.setToan(diemMoi.getToan());
        diemHienTai.setVan(diemMoi.getVan());
        diemHienTai.setLy(diemMoi.getLy());
        diemHienTai.setHoa(diemMoi.getHoa());
        diemHienTai.setSinh(diemMoi.getSinh());
        diemHienTai.setSu(diemMoi.getSu());
        diemHienTai.setDia(diemMoi.getDia());
        diemHienTai.setAnh(diemMoi.getAnh());

        // Cáº­p nháº­t cÃ¡c Ä‘iá»ƒm nÄƒng khiáº¿u (náº¿u mÃ´n trá»‘ng, null váº«n Ä‘Æ°á»£c lÆ°u há»£p lá»‡ theo
        // PRD)
        diemHienTai.setNk1(diemMoi.getNk1());
        diemHienTai.setNk2(diemMoi.getNk2());
        diemHienTai.setNk3(diemMoi.getNk3());
        diemHienTai.setNk4(diemMoi.getNk4());
        diemHienTai.setNk5(diemMoi.getNk5());
        diemHienTai.setNk6(diemMoi.getNk6());
        diemHienTai.setNk7(diemMoi.getNk7());
        diemHienTai.setNk8(diemMoi.getNk8());

        // LÆ°u vÃ o JPA repository
        return diemThiRepository.save(diemHienTai);
    }

    // 3. Láº¥y chi tiáº¿t thÃ­ sinh kÃ¨m Ä‘iá»ƒm thi & Ä‘iá»ƒm cá»™ng

    public List<ThiSinhDetailDTO> getThiSinhDetailsForScoring() {

        List<Object[]> tsRows = repo.fetchThiSinhWithDiemThi();
        List<Object[]> dcRows = repo.fetchDiemCong();

        // ===== Map DiemCong theo CCCD =====
        Map<String, List<DiemCongDTO>> diemCongMap = new HashMap<>();

        for (Object[] row : dcRows) {
            DiemCongDTO dc = new DiemCongDTO(
                    (String) row[0],
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (Double) row[4],
                    (Double) row[5],
                    (Double) row[6],
                    (String) row[7]);

            diemCongMap
                    .computeIfAbsent(dc.getTsCccd(), k -> new ArrayList<>())
                    .add(dc);
        }

        // ===== Build result =====
        List<ThiSinhDetailDTO> result = new ArrayList<>();

        for (Object[] row : tsRows) {

            String cccd = (String) row[0];

            // map DiemThi
            DiemThiDTO diemThi = new DiemThiDTO(
                    cccd,
                    (Double) row[7], (Double) row[8], (Double) row[9],
                    (Double) row[10], (Double) row[11],
                    (Double) row[12], (Double) row[13], (Double) row[14],
                    (Double) row[15], (Double) row[16], (Double) row[17],
                    (Double) row[18], (Double) row[19], (Double) row[20],
                    (Double) row[21], (Double) row[22]);

            ThiSinhDetailDTO dto = new ThiSinhDetailDTO(
                    cccd,
                    (String) row[1],
                    (String) row[2],
                    (String) row[3],
                    (String) row[4],
                    (String) row[5],
                    (String) row[6],
                    diemThi,
                    diemCongMap.getOrDefault(cccd, new ArrayList<>()));

            result.add(dto);
        }

        return result;
    }

    public Page<ThiSinh> searchThiSinh(String keyword, Pageable pageable) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return thiSinhRepository.findAll(pageable);
        }

        String keywordTrim = keyword.trim();

        return thiSinhRepository.searchThiSinh(keywordTrim, pageable);
    }
    //4. TÃ¬m kiáº¿m thÃ­ sinh theo CCCD hoáº·c Há» tÃªn (cÃ³ phÃ¢n trang)
    public Page<ThiSinh> findByIdContainingIgnoreCaseOrHoTenContainingIgnoreCase (String keyword, Pageable pageable) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return thiSinhRepository.findAll(pageable);
        }

        String keywordTrim = keyword.trim();

        return thiSinhRepository.findByIdContainingIgnoreCaseOrHoTenContainingIgnoreCase(
                keywordTrim, keywordTrim, pageable);
    }
    public Page<ThiSinh> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return thiSinhRepository.findByIdContainingIgnoreCaseOrHoTenContainingIgnoreCase(keyword.trim(), keyword.trim(), pageable);
        }
        return thiSinhRepository.findAll(pageable);
    }

    @Transactional
    public ThiSinh luuThiSinh(ThiSinh thiSinh) {
        if (thiSinh.getId() == null || thiSinh.getId().trim().length() < 9) {
            throw new IllegalArgumentException("CCCD khong hop le (phai tu 9-12 so)!");
        }
        if (thiSinh.getHoTen() == null || thiSinh.getHoTen().trim().isEmpty()) {
            throw new IllegalArgumentException("Ho ten khong duoc de trong!");
        }
        thiSinh.setId(thiSinh.getId().trim());
        thiSinh.setHoTen(thiSinh.getHoTen().trim());
        if (thiSinh.getMaTruong() != null) thiSinh.setMaTruong(thiSinh.getMaTruong().trim());
        if (thiSinh.getMaTinh() != null) thiSinh.setMaTinh(thiSinh.getMaTinh().trim());
        return thiSinhRepository.save(thiSinh);
    }

    @Transactional
    public void xoaThiSinh(String cccd) {
        String idClean = cccd.trim();
        if (!thiSinhRepository.existsById(idClean)) {
            throw new IllegalArgumentException("Khong tim thay thi sinh voi CCCD: " + idClean);
        }
        thiSinhRepository.deleteById(idClean);
    }

    public java.util.Map<String, Object> getThongKeThiSinh() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", thiSinhRepository.countTotal());
        stats.put("byDoiTuong", thiSinhRepository.countByDoiTuong());
        stats.put("byKhuVuc", thiSinhRepository.countByKhuVuc());
        return stats;
    }

    public com.sgu.tuyensinh.entity.ThiSinh findByCccd(String cccd) {
        return thiSinhRepository.findById(cccd.trim()).orElse(null);
    }

    public com.sgu.tuyensinh.entity.DiemThi findDiemByCccd(String cccd) {
        return diemThiRepository.findByCccd(cccd.trim()).orElse(null);
    }
}
