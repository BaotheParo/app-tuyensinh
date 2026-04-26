// package com.sgu.tuyensinh.service;

// import com.sgu.tuyensinh.service.dto.ThiSinhDetailDTO;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.data.domain.PageRequest;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// @ActiveProfiles("test")
// class ThiSinhServiceTest {

//     @Autowired
//     private ThiSinhService thiSinhService;

//     /**
//      * Test lấy danh sách thí sinh + điểm thi + điểm cộng (REAL DB)
//      */
//     @Test
//     void testGetThiSinhDetailsForScoring() {

//         List<ThiSinhDetailDTO> result = thiSinhService.getThiSinhDetailsForScoring();

//         assertNotNull(result, "Danh sách không được null");

//         if (!result.isEmpty()) {
//             for (ThiSinhDetailDTO ts : result) {

//                 assertNotNull(ts.getCccd());
//                 assertNotNull(ts.getHoTen());

//                 if (ts.getDiemThi() != null) {
//                     assertEquals(ts.getCccd(), ts.getDiemThi().getCccd());
//                 }

//                 assertNotNull(ts.getDiemCongs());

//                 ts.getDiemCongs().forEach(dc -> {
//                     assertNotNull(dc.getTsCccd());
//                 });
//             }
//         }

//         System.out.println("===== SAMPLE DATA =====");
//         result.stream().limit(3).forEach(System.out::println);

//         long start = System.currentTimeMillis();
//         thiSinhService.getThiSinhDetailsForScoring();
//         long end = System.currentTimeMillis();

//         System.out.println("Execution time: " + (end - start) + " ms");
//     }

//     /**
//      * Test search có keyword
//      */
//     @Test
//     void testSearchThiSinh_WithKeyword() {

//         String keyword = "nguyen"; // phải tồn tại trong DB test
//         var pageable = PageRequest.of(0, 10);

//         var result = thiSinhService.findByIdContainingIgnoreCaseOrHoTenContainingIgnoreCase(keyword, pageable);

//         assertNotNull(result);

//         result.getContent().forEach(ts -> {
//             assertNotNull(ts.getId());
//             assertNotNull(ts.getHoTen());

//             boolean match =
//                     ts.getId().toLowerCase().contains(keyword.toLowerCase()) ||
//                     ts.getHoTen().toLowerCase().contains(keyword.toLowerCase());

//             assertTrue(match, "Kết quả phải đúng keyword");
//         });

//         System.out.println("===== SEARCH RESULT =====");
//         result.getContent().forEach(System.out::println);
//     }

//     /**
//      * Test search keyword rỗng
//      */
//     @Test
//     void testSearchThiSinh_EmptyKeyword() {

//         var pageable = PageRequest.of(0, 10);

//         var result = thiSinhService.findByIdContainingIgnoreCaseOrHoTenContainingIgnoreCase("", pageable);

//         assertNotNull(result);
//         assertTrue(result.getContent().size() >= 0);
//     }
// }