package com.example.spring_boot_3_security_6_jwt_amigos;

import com.example.spring_boot_3_security_6_jwt_amigos.config.EncryptionConfiguration;
import com.example.spring_boot_3_security_6_jwt_amigos.user.AppUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@SpringBootApplication
public class SpringBoot3Security6JwtAmigosApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBoot3Security6JwtAmigosApplication.class, args);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(AppUserService appUserService, EncryptionConfiguration encryptionConfiguration){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(appUserService);
        authProvider.setPasswordEncoder(encryptionConfiguration.passwordEncoder());
        return authProvider;
    }

}
