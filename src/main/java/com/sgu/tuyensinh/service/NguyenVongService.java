package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.NguyenVong;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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
            return repository.findByNnCccdContainingIgnoreCaseOrNvManganhContainingIgnoreCase(keyword, keyword, pageable);
        }
        return repository.findAll(pageable);
    }
    
    public List<NguyenVong> getByCccd(String cccd) {
        // Có thể bổ sung findByNnCccd vào Repository nếu cần
        return repository.findAll().stream()
                .filter(nv -> nv.getNnCccd().equals(cccd))
                .toList();
    }
}
