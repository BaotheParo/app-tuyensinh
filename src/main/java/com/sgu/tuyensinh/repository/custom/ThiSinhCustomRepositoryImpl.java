package com.sgu.tuyensinh.repository.custom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;  
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public class ThiSinhCustomRepositoryImpl implements ThiSinhCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Object[]> fetchThiSinhWithDiemThi() {
        String sql = """
                    SELECT ts.cccd, ts.ho_ten, ts.gioi_tinh, ts.ma_truong, ts.ma_tinh,
                        ts.doi_tuong_ut, ts.khu_vuc_ut,
                        dt.toan, dt.van, dt.ly, dt.hoa, dt.sinh,
                        dt.su, dt.dia, dt.anh,
                        dt.nk1, dt.nk2, dt.nk3, dt.nk4,
                        dt.nk5, dt.nk6, dt.nk7, dt.nk8
                    FROM thi_sinh ts
                    LEFT JOIN diem_thi dt ON ts.cccd = dt.cccd
                """;

        return em.createNativeQuery(sql).getResultList();
    }

    @Override
    public List<Object[]> fetchDiemCong() {
        String sql = """
                    SELECT ts_cccd, manganh, matohop, phuongthuc,
                        diemcc, diem_utxt, diem_tong, ngay_cap
                    FROM xt_diemcongxetuyen
                """;

        return em.createNativeQuery(sql).getResultList();
    }
}
