package com.example.demo.security.auth;

import com.example.demo.security.entity.Role;
import com.example.demo.security.entity.UserEntity;
import com.example.demo.security.jwt.JwtService;
import com.example.demo.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username da ton tai: " + req.username());
        }

        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email da ton tai: " + req.email());
        }

        // Mật khẩu bắt buộc băm bằng BCrypt (không bao giờ lưu mật khẩu dạng thô)
        UserEntity user = UserEntity.builder()
                .username(req.username().trim())
                .password(passwordEncoder.encode(req.password()))
                .email(req.email().trim().toLowerCase())
                .phone(req.phone() != null ? req.phone().trim() : null)
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getRole().name(), jwtService.getAccessExpirationMs());
    }

    public AuthResponse login(AuthRequest req) {
        // AuthenticationManager gọi DaoAuthenticationProvider và BCryptPasswordEncoder để xác thực
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        UserEntity user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay user: " + req.username()));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, user.getUsername(), user.getRole().name(), jwtService.getAccessExpirationMs());
    }

    /**
     * Endpoint cấp lại Token mới khi Access Token hết hạn
     */
    public AuthResponse refreshToken(RefreshTokenRequest req) {
        String refreshToken = req.refreshToken();
        String username;
        try {
            username = jwtService.extractUsernameFromRefreshToken(refreshToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Refresh Token khong hop le hoac sai chu ky");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay user tu Refresh Token: " + username));

        if (!jwtService.isRefreshTokenValid(refreshToken, user)) {
            throw new IllegalArgumentException("Refresh Token khong hop le, da het han hoac sai token_type");
        }

        // Cấp Access Token mới (15 phút) và Refresh Token mới xoay vòng (Token Rotation)
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                user.getUsername(),
                user.getRole().name(),
                jwtService.getAccessExpirationMs()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay user: " + username));

        return new UserProfileResponse(
                user.getUsername(),
                com.example.demo.security.util.MaskingUtils.maskEmail(user.getEmail()),
                com.example.demo.security.util.MaskingUtils.maskPhone(user.getPhone()),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
