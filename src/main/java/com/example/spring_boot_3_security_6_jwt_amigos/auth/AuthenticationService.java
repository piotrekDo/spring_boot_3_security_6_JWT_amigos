package com.example.spring_boot_3_security_6_jwt_amigos.auth;

import com.example.spring_boot_3_security_6_jwt_amigos.config.EncryptionConfiguration;
import com.example.spring_boot_3_security_6_jwt_amigos.config.JwtService;
import com.example.spring_boot_3_security_6_jwt_amigos.user.AppUser;
import com.example.spring_boot_3_security_6_jwt_amigos.user.AppUserRepository;
import com.example.spring_boot_3_security_6_jwt_amigos.user.Role;
import com.example.spring_boot_3_security_6_jwt_amigos.user.UserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AppUserRepository appUserRepository;
    private final EncryptionConfiguration encryptionConfiguration;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthenticationResponse register(RegisterRequest request) {
        AppUser appUser = AppUser.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(encryptionConfiguration.passwordEncoder().encode(request.getPassword()))
                .role(Role.USER)
                .build();
        AppUser save = appUserRepository.save(appUser);
        String token = jwtService.generateToken(new UserDetailsAdapter(appUser));
        return AuthenticationResponse.builder().token(token).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authenticate = null;
        try {
            authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (Exception e) {
            return AuthenticationResponse.builder().token(e.getLocalizedMessage()).build();
        }

        AppUser appUser = appUserRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generateToken(new UserDetailsAdapter(appUser));
        return AuthenticationResponse.builder().token(token).build();
    }
}
