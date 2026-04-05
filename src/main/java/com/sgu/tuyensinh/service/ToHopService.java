package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.ToHop;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import com.sgu.tuyensinh.repository.ToHopRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ToHopService {

    private final ToHopRepository toHopRepository;
    private final NganhToHopRepository nganhToHopRepository;
    private final NguyenVongRepository nguyenVongRepository;

    public List<ToHop> getAllToHop() {
        return toHopRepository.findAll();
    }

    public ToHop getToHopById(Integer id) {
        return toHopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id));
    }

    public ToHop createToHop(ToHop toHop) {
        if (toHop.getMaToHop() == null || toHop.getMaToHop().isBlank()) {
            throw new IllegalArgumentException("Mã tổ hợp không được để trống");
        }

        // check duplicate theo business key
        if (toHopRepository.existsByMaToHop(toHop.getMaToHop())) {
            throw new IllegalArgumentException("Mã tổ hợp đã tồn tại");
        }

        return toHopRepository.save(toHop);
    }

    public ToHop updateToHop(Integer id, ToHop newData) {
        ToHop old = toHopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id));

        // ❌ Không cho đổi mã (tránh vỡ FK)
        if (!old.getMaToHop().equals(newData.getMaToHop())) {
            throw new IllegalStateException("Không được thay đổi mã tổ hợp");
        }

        old.setTenToHop(newData.getTenToHop());
        old.setMon1(newData.getMon1());
        old.setMon2(newData.getMon2());
        old.setMon3(newData.getMon3());

        return toHopRepository.save(old);
    }

    public void deleteToHop(Integer id) {
        ToHop toHop = toHopRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tổ hợp với ID: " + id));

        // check logic trước
        if (nguyenVongRepository.existsByTtThm(toHop.getMaToHop())) {
            throw new IllegalStateException("Không thể xóa vì có nguyện vọng liên quan");
        }

        if (nganhToHopRepository.existsByMaToHop(toHop.getMaToHop())) {
            throw new IllegalStateException("Không thể xóa vì có ngành liên quan");
        }

        try {
            toHopRepository.delete(toHop);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Không thể xóa do ràng buộc dữ liệu trong DB");
        }
    }
}