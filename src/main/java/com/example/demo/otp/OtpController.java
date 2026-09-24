package com.example.demo.otp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/practice/otp")
public class OtpController {

    private final OtpService service;

    public OtpController(OtpService service) {
        this.service = service;
    }

    @PostMapping("/send")
    public ResponseEntity<String> send(
            @RequestParam("phone") String phone) {

        // Bài thực hành chỉ nhận số Việt Nam dạng 0xxxxxxxxx.
        String normalizedPhone = phone.trim();

        if (!normalizedPhone.matches("0[0-9]{9}")) {
            return ResponseEntity.badRequest()
                    .body("So dien thoai phai gom 10 chu so, bat dau bang 0");
        }

        if (!service.allowSend(normalizedPhone)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Qua 3 lan. Hay doi het thoi han roi thu lai.");
        }

        return ResponseEntity.ok("Da chap nhan gui OTP gia lap");
    }
}