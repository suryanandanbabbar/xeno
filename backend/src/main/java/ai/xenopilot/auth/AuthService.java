package ai.xenopilot.auth;

import ai.xenopilot.security.JwtService;
import ai.xenopilot.user.User;
import ai.xenopilot.user.UserProfileResponse;
import ai.xenopilot.user.UserRepository;
import ai.xenopilot.user.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        UserRole role = userRepository.count() == 0 ? UserRole.ADMIN : UserRole.MEMBER;
        User user = userRepository.save(new User(request.name(), email, passwordEncoder.encode(request.password()), role));
        return new AuthResponse(jwtService.generateToken(user), UserProfileResponse.from(user));
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        User user = userRepository.findByEmail(request.email().toLowerCase()).orElseThrow();
        return new AuthResponse(jwtService.generateToken(user), UserProfileResponse.from(user));
    }
}
