package com.sgu.tuyensinh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestScores {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/sgu_tuyensinh_2026?useSSL=false&allowPublicKeyRetrieval=true", "root", "123456");
            Statement stmt = conn.createStatement();
            
            String[] cccds = {"TS_2798", "TS_6945", "TS_33417"};
            
            for (String cccd : cccds) {
                System.out.println("Checking CCCD: " + cccd);
                ResultSet rs = stmt.executeQuery("SELECT * FROM diem_thi WHERE cccd = '" + cccd + "'");
                if (rs.next()) {
                    System.out.println("  Found in diem_thi!");
                    System.out.println("  Toan: " + rs.getDouble("toan") + 
                                     ", Van: " + rs.getDouble("van") + 
                                     ", Anh: " + rs.getDouble("anh") + 
                                     ", Ly: " + rs.getDouble("ly") + 
                                     ", Hoa: " + rs.getDouble("hoa") + 
                                     ", Sinh: " + rs.getDouble("sinh"));
                } else {
                    System.out.println("  NOT FOUND in diem_thi!");
                }
                
                rs = stmt.executeQuery("SELECT * FROM xt_thisinh WHERE cccd = '" + cccd + "'");
                if (rs.next()) {
                    System.out.println("  Found in xt_thisinh.");
                } else {
                    System.out.println("  NOT FOUND in xt_thisinh!");
                }
                
                System.out.println("  --- Nguyen Vong ---");
                rs = stmt.executeQuery("SELECT nv_manganh, diem_thxt, diem_utqd, diem_xet_tuyen FROM xt_nguyenvong WHERE nn_cccd = '" + cccd + "'");
                while (rs.next()) {
                    System.out.println("  " + rs.getString("nv_manganh") + " | THXT: " + rs.getDouble("diem_thxt") + " | UT: " + rs.getDouble("diem_utqd") + " | XT: " + rs.getDouble("diem_xet_tuyen"));
                }
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
