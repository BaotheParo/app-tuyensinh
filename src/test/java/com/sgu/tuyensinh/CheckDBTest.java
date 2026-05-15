package com.sgu.tuyensinh;

import com.sgu.tuyensinh.repository.DiemThiRepository;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CheckDBTest {

    @Autowired
    private DiemThiRepository diemThiRepository;

    @Autowired
    private NguyenVongRepository nguyenVongRepository;

    @Test
    public void testData() {
        long diemThiCount = diemThiRepository.count();
        long nguyenVongCount = nguyenVongRepository.count();
        System.out.println("====== DB CHECK ======");
        System.out.println("DiemThi count: " + diemThiCount);
        System.out.println("NguyenVong count: " + nguyenVongCount);
        System.out.println("======================");
    }
}
