package cuong.com.bai_1.service;

import cuong.com.bai_1.dto.LoginRequest;
import cuong.com.bai_1.dto.RegisterRequest;
import cuong.com.bai_1.entity.User;
import cuong.com.bai_1.repository.UserRepository;
import cuong.com.bai_1.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtProvider jwtProvider;

    public String register(RegisterRequest request) {
        // Kiểm tra xem email đã có ai dùng chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // Tạo mới User và mã hóa mật khẩu trước khi lưu DB
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole("ROLE_USER"); // Gán quyền mặc định

        userRepository.save(user);
        return "Đăng ký thành công!";
    }

    public Map<String, String> login(LoginRequest request) {
        // Giao việc kiểm tra email/password cho Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Nếu qua được bước trên nghĩa là đúng mật khẩu, tiến hành tạo Token
        String email = authentication.getName();
        String accessToken = jwtProvider.generateAccessToken(email);
        String refreshToken = jwtProvider.generateRefreshToken(email);

        // Đóng gói cả 2 token trả về cho client
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);

        return tokens;
    }
}