package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.service.dto.NguyenVongResultDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ cho Nguyện vọng (Read-only UI).
 */
@Service
@RequiredArgsConstructor
public class NguyenVongService {

    private final NguyenVongRepository repository;

    public Page<NguyenVong> layDanhSachPhanTrang(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return repository.findByNnCccdContainingIgnoreCaseOrNvManganhContainingIgnoreCase(keyword, keyword,
                    pageable);
        }
        return repository.findAll(pageable);
    }

    public List<NguyenVong> getByCccd(String cccd) {
        // Có thể bổ sung findByNnCccd vào Repository nếu cần
        return repository.findAll().stream()
                .filter(nv -> nv.getNnCccd().equals(cccd))
                .toList();
    }

    @Transactional
    public void capNhatKetQuaTuLead(List<NguyenVongResultDTO> inputData) {

        if (inputData == null || inputData.isEmpty()) {
            return;
        }

        List<Integer> ids = inputData.stream()
                .map(NguyenVongResultDTO::getIdnv)
                .toList();

        List<NguyenVong> listNV = repository.findAllById(ids);

        // Map để lookup nhanh
        Map<Integer, String> mapKetQua = inputData.stream()
                .collect(Collectors.toMap(
                        NguyenVongResultDTO::getIdnv,
                        NguyenVongResultDTO::getKetQua));

        int success = 0;
        int fail = 0;

        for (NguyenVong nv : listNV) {
            String ketQua = mapKetQua.get(nv.getIdnv());

            if (ketQua != null) {
                nv.setNvKetQua(ketQua);
                success++;
            } else {
                fail++;
            }
        }

        // SAVE ALL 1 lần
        repository.saveAll(listNV);

        System.out.println("Hoan thanh cap nhat:");
        System.out.println("   - Thanh cong: " + success);
        System.out.println("   - LLoi: " + fail);
    }

    @Transactional(readOnly = true)
    public List<NguyenVong> getDanhSachTrungTuyen(String maNganh) {
        return repository.findByNvManganhAndNvKetQua(maNganh, "TRUNG_TUYEN");
    }
}
