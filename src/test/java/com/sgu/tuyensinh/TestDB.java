package com.sgu.tuyensinh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sgu_tuyensinh_2026?useSSL=false&allowPublicKeyRetrieval=true", "root", "123456");
            Statement stmt = conn.createStatement();
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM diem_thi");
            if (rs.next()) System.out.println("DiemThi count: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM xt_thisinh");
            if (rs.next()) System.out.println("ThiSinh count: " + rs.getInt(1));
            
            rs = stmt.executeQuery("SELECT nv_ketqua, COUNT(*) FROM xt_nguyenvong GROUP BY nv_ketqua");
            while (rs.next()) {
                System.out.println("KetQua: " + rs.getString(1) + " -> " + rs.getInt(2));
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM xt_nguyenvong WHERE diem_thxt > 0");
            if (rs.next()) System.out.println("NguyenVong diem_thxt > 0: " + rs.getInt(1));

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
