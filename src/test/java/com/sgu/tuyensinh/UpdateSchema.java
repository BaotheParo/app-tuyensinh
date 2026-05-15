package com.sgu.tuyensinh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateSchema {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sgu_tuyensinh_2026?useSSL=false&allowPublicKeyRetrieval=true", "root", "123456");
            Statement stmt = conn.createStatement();
            
            try {
                stmt.execute("ALTER TABLE xt_nguyenvongxettuyen ADD COLUMN is_tuyen_thang BOOLEAN DEFAULT FALSE");
                System.out.println("Column is_tuyen_thang added successfully.");
            } catch (Exception e) {
                System.out.println("Column might already exist: " + e.getMessage());
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
