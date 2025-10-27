package patientmanagment.authservice.service;


import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import patientmanagment.authservice.dto.LoginRequestDTO;
import patientmanagment.authservice.util.JwtUtil;

import java.util.Optional;

@Service
public class authService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public authService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;

        this.jwtUtil = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO){
        return userService
                .findByEmail(loginRequestDTO.getEmail())
                .filter(u->passwordEncoder.matches(loginRequestDTO.getPassword(),
                        u.getPassword())).map(u->jwtUtil.generateToken(u.getEmail(),u.getRole()));
    }

    public Boolean validateToken(String token){
        try {
            jwtUtil.validateToken(token);
            return true;
        }catch (JwtException e){

            return false;
        }
    }
}
