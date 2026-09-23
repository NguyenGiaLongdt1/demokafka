package com.example.demo.product;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    @Cacheable(cacheNames = "practiceProducts", key = "#p0")
    public String getProduct(Long id) throws InterruptedException {
        System.out.println("Đang đọc nguồn dữ liệu: " + id);
        Thread.sleep(2000); // Chỉ giả lập chậm trong bài thực hành
        return "Sản phẩm " + id;
    }
}