package com.sgu.tuyensinh.service;

import com.sgu.tuyensinh.entity.Nganh;
import com.sgu.tuyensinh.repository.NganhRepository;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NganhService {

    private final NganhRepository nganhRepository;
    private final NguyenVongRepository nguyenVongRepository;
    private final NganhToHopRepository nganhToHopRepository;

    public List<Nganh> getAllNganh() {
        return nganhRepository.findAll();
    }

    public Nganh getNganhById(String maNganh) {
        return nganhRepository.findById(maNganh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ngành với mã: " + maNganh));
    }

    public Nganh createNganh(Nganh nganh) {
        if (nganh.getMaNganh() == null || nganh.getMaNganh().isBlank())
            throw new IllegalArgumentException("Mã ngành không được để trống");
        if (nganhRepository.existsById(nganh.getMaNganh()))
            throw new IllegalArgumentException("Mã ngành đã tồn tại");
        return nganhRepository.save(nganh);
    }

    public Nganh updateNganh(String maNganh, Nganh newData) {
        Nganh old = nganhRepository.findById(maNganh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ngành với mã: " + maNganh));

        if (!old.getMaNganh().equals(newData.getMaNganh()))
            throw new IllegalStateException("Không được thay đổi mã ngành");

        old.setTenNganh(newData.getTenNganh());
        old.setDiemSan(newData.getDiemSan());
        old.setChiTieu(newData.getChiTieu());
        old.setToHopGoc(newData.getToHopGoc());

        return nganhRepository.save(old);
    }

    public void deleteNganh(String maNganh) {
        Nganh nganh = nganhRepository.findById(maNganh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ngành với mã: " + maNganh));

        if (nguyenVongRepository.existsByNvManganh(maNganh))
            throw new IllegalStateException("Không thể xóa vì có nguyện vọng liên quan");

        if (nganhToHopRepository.existsByMaNganh(maNganh))
            throw new IllegalStateException("Không thể xóa vì có tổ hợp liên quan");

        try {
            nganhRepository.delete(nganh);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Không thể xóa do ràng buộc dữ liệu trong DB");
        }
    }
}
