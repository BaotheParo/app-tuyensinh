package com.sgu.tuyensinh;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Commit;

@SpringBootTest
public class UpdateSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Commit
    public void testUpdateSchema() {
        try {
            jdbcTemplate.execute("ALTER TABLE xt_nguyenvongxettuyen ADD COLUMN is_tuyen_thang BOOLEAN DEFAULT FALSE");
            System.out.println("✅ Thêm cột is_tuyen_thang thành công!");
        } catch (Exception e) {
            System.out.println("⚠️ Có thể cột đã tồn tại: " + e.getMessage());
        }
    }
}
