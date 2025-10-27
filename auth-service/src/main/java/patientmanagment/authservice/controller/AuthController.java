package patientmanagment.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import patientmanagment.authservice.dto.LoginRequestDTO;
import patientmanagment.authservice.dto.LoginResponseDTO;
import patientmanagment.authservice.service.authService;

import java.util.Optional;

@RestController
public class AuthController {

    private final patientmanagment.authservice.service.authService authService;

    public AuthController(authService authService) {
        this.authService = authService;
    }

    @PostMapping("login")
    public ResponseEntity<LoginResponseDTO> login(
            @RequestBody LoginRequestDTO loginRequestDTO){
        Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

        if(tokenOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = tokenOptional.get();
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(
            @RequestHeader("Authorization") String authHeader){
            if(authHeader == null || !authHeader.startsWith("Bearer ")){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return authService.validateToken(authHeader.substring(7))
                    ? ResponseEntity.ok().build()
                    :ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
