package io.github.jelilio.jwtauthotp.config.security;

import io.github.jelilio.jwtauthotp.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtil {
    public static final String USER_ID = "uid";
    public static final String ROLE = "role";

    @Value("${jwt-auth-otp.jjwt.secret}")
    private String secret;

    @Value("${jwt-auth-otp.jjwt.expiration}")
    private String expirationTime;

    @Value("${jwt-auth-otp.jjwt.issuer}")
    private String issuer;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public String getUserIdFromToken(String token) {
        return getAllClaimsFromToken(token)
            .get(USER_ID, String.class);
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ROLE, user.getRoles());
        claims.put(USER_ID, user.getId());
        return doGenerateToken(claims, user);
    }

    private String doGenerateToken(Map<String, Object> claims, User user) {
        long expirationTimeLong = Long.parseLong(expirationTime); //in second
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expirationTimeLong * 1000);


        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getUsername())
            .setIssuedAt(createdDate)
            .setExpiration(expirationDate)
            .signWith(key)
            .setIssuer(issuer)
            .compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

}
