package com.sgu.tuyensinh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity mẫu.
 *
 * Giải thích nhanh:
 * - Entity đại diện cho 1 bảng trong database.
 * - Các field là cột.
 *
 * Nhóm có thể xóa class này khi đã có entity thật, hoặc đổi tên/đổi cột theo nghiệp vụ.
 */
@Entity
@Table(name = "example_entity")
public class ExampleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
