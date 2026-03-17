package com.sgu.tuyensinh.repository;

import com.sgu.tuyensinh.model.ExampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository mẫu.
 *
 * Gợi ý:
 * - JpaRepository đã có sẵn các hàm CRUD cơ bản (save, findById, findAll, deleteById...).
 * - Khi cần query thêm, nhóm có thể tạo method theo quy tắc đặt tên của Spring Data JPA.
 */
public interface ExampleRepository extends JpaRepository<ExampleEntity, Long> {
}
