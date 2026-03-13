package com.anthony.tfg.tfg.Modulos.Seguridad.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    @Value("${jwt.secret:${JWT_SECRET:}}")
    private String SECRET_KEY;

    /**
     * Gestiona operaciones relacionadas con tokens JWT.
     * @param user parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public String getToken(User user) {
        return getToken(new HashMap<>(), user);
    }

    /**
     * Gestiona operaciones relacionadas con tokens JWT.
     * @param extraClaims parametro de entrada de la operacion.
     * @param user parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private String getToken(HashMap<String, Object> extraClaims, User user) {
        String nombreCompleto = null;
        Long idEmpleado = null;
        if (user.getEmpleado() != null) {
            String nombre = user.getEmpleado().getNombre();
            String primerApellido = user.getEmpleado().getPrimerApellido();
            String segundoApellido = user.getEmpleado().getSegundoApellido();
            nombreCompleto = nombre + " " + primerApellido + 
                           (segundoApellido != null ? " " + segundoApellido : "");
            idEmpleado = user.getEmpleado().getId();
        }
        
        return Jwts.builder()
                .claims(extraClaims)
                .claim("userId", user.getId())
                .claim("idEmpleado", idEmpleado)
                .claim("role", user.getRole())
                .claim("nombreCompleto", nombreCompleto)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // 24 horas
                .signWith(getKey())
                .compact();
    }

    /**
     * Gestiona operaciones relacionadas con tokens JWT.
     * @return resultado de la operacion.
     */
    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gestiona operaciones relacionadas con tokens JWT.
     */
    @PostConstruct
    private void checkSecret() {
        if (SECRET_KEY == null || SECRET_KEY.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret not configured. Set 'jwt.secret' in application.properties or 'JWT_SECRET' env var.");
        }
    }

    /**
     * Gestiona operaciones relacionadas con tokens JWT.
     * @param token parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Gestiona operaciones relacionadas con tokens JWT.
     * @param token parametro de entrada de la operacion.
     * @param userDetails parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Gestiona operaciones relacionadas con tokens JWT.
     * @param token parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private Claims getAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Gestiona operaciones relacionadas con tokens JWT.
     * @param token parametro de entrada de la operacion.
     * @param claimsResolver parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Ejecuta la logica principal de getExpirationDate.
     * @param token parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private Date getExpirationDate(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Gestiona operaciones relacionadas con tokens JWT.
     * @param token parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }
}