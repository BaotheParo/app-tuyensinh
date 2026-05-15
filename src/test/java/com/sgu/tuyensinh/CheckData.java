package com.sgu.tuyensinh;

import com.sgu.tuyensinh.entity.NganhToHop;
import com.sgu.tuyensinh.repository.NganhToHopRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CheckData {
    @Autowired
    private NganhToHopRepository repo;

    @Test
    public void checkNganhToHop() {
        List<NganhToHop> all = repo.findAll();
        System.out.println("TOTAL NganhToHop: " + all.size());
        for (NganhToHop nth : all) {
            System.out.println("Nganh: " + nth.getMaNganh() + ", ToHop: " + nth.getMaToHop());
        }
    }
}
