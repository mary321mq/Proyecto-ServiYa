package com.upeu.auth.service;

import com.upeu.auth.client.MsUserClient;
import com.upeu.auth.client.MsNotificationClient;
import com.upeu.auth.dto.AuthResponse;
import com.upeu.auth.dto.LoginRequest;
import com.upeu.auth.dto.RegisterRequest;
import com.upeu.auth.entity.AuthUser;
import com.upeu.auth.repository.AuthUserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Map;

@Service
public class AuthService {
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final MsUserClient msUserClient;
    private final MsNotificationClient msNotificationClient;
    private final String issuer;
    private final long ttlSeconds;

    public AuthService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            MsUserClient msUserClient,
            MsNotificationClient msNotificationClient,
            @Value("${security.jwt.issuer:${JWT_ISSUER:serviya}}") String issuer,
            @Value("${security.jwt.ttl-seconds:${JWT_TTL_SECONDS:3600}}") long ttlSeconds
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.msUserClient = msUserClient;
        this.msNotificationClient = msNotificationClient;
        this.issuer = issuer;
        this.ttlSeconds = ttlSeconds;
    }

    @Transactional
    public AuthUser register(@Valid RegisterRequest request) {
        if (authUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
        }
        AuthUser user = AuthUser.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .role("USER")
                .enabled(true)
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();
        
        AuthUser savedUser = authUserRepository.save(user);

        // Crear perfil en MS2 de forma automática
        try {
            System.out.println("Enviando creación de cliente a MS2 para: " + request.getEmail());
            msUserClient.crearCliente(Map.of(
                "nombre", request.getNombre(),
                "email", request.getEmail(),
                "telefono", "", 
                "direccion", "" 
            ));
            System.out.println("MS2 respondió correctamente para: " + request.getEmail());
        } catch (Exception e) {
            System.err.println("Error creando perfil en MS2 para " + request.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Enviar notificación de bienvenida en MS8
        try {
            msNotificationClient.crearNotificacion(Map.of(
                    "destinatario", request.getEmail(),
                    "canal", "EMAIL",
                    "titulo", "Bienvenido a ServiYa",
                    "mensaje", "Tu cuenta fue creada correctamente."
            ));
        } catch (Exception e) {
            System.err.println("Error enviando notificación de bienvenida para " + request.getEmail() + ": " + e.getMessage());
        }

        return savedUser;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(@Valid LoginRequest request) {
        AuthUser user = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario deshabilitado");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlSeconds);
        
        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("role", user.getRole())
                .build();
        
        String token = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .expiresAt(exp)
                .build();
    }
}
