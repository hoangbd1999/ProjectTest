package com.elcom.metacen.id.auth.jwt;

import com.elcom.metacen.id.auth.CustomUserDetails;
import com.elcom.metacen.id.service.TokenService;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author anhdv
 */
@Component
public class JwtTokenProvider {

    @Autowired
    private TokenService tokenService;
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final String JWT_SECRET = "elcom@123_2020";
    private final long ACCESS_JWT_EXPIRATION = 604800000L;//7 day

    public String generateToken(CustomUserDetails userDetails) {
        // Lấy thông tin user
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_JWT_EXPIRATION);
        // Tạo chuỗi json web token từ id của user.
        return Jwts.builder()
                   .setSubject(userDetails.getUser().getUuid())
                   .setIssuedAt(now)
                   .setExpiration(expiryDate)
                   .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                   .compact();
    }
    public String generateToken1(String uuid) {
        // Lấy thông tin user
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_JWT_EXPIRATION);
        // Tạo chuỗi json web token từ id của user.
        return Jwts.builder()
                .setSubject(uuid)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                .compact();
    }
//    public TokenParse getUuidFromJWT(String token)  {
//        try {
//            Claims claims = Jwts.parser()
//                            .setSigningKey(JWT_SECRET)
//                            .parseClaimsJws(token)
//                            .getBody();
//            String uuid = claims.getSubject();
//            try {
//                String tokenCache = tokenService.getAccessTokenUpdate(uuid);
//                if(tokenCache!=null){
//                    Claims claims1 = Jwts.parser()
//                            .setSigningKey(JWT_SECRET)
//                            .parseClaimsJws(tokenCache)
//                            .getBody();
//                    long timeAuthToken = claims.getExpiration().getTime();
//                    long timeToken = claims1.getExpiration().getTime();
//                    if(timeAuthToken<timeToken){
//                    LOGGER.error("Expired JWT token(Password just changed)");
//                    return new TokenParse(425,"Expired JWT accessToken(Password just changed)",null);
//                }else
//                    return new TokenParse(HttpStatus.OK.value(),HttpStatus.OK.toString(),claims.getSubject());
//                }else
//                     return new TokenParse(HttpStatus.OK.value(),HttpStatus.OK.toString(),claims.getSubject());
//
//            }catch (ExpiredJwtException ex) {
//                tokenService.removeAccessTokenUpdate(uuid);
//                return new TokenParse(HttpStatus.OK.value(), HttpStatus.OK.toString(), claims.getSubject());
//            } catch (Exception ex){
//                return new TokenParse(HttpStatus.OK.value(),HttpStatus.OK.toString(),claims.getSubject());
//        }
//        }  catch (MalformedJwtException ex) {
//            LOGGER.error("Invalid JWT token");
//            return new TokenParse(420,"Invalid JWT accessToken",null);
//        } catch (ExpiredJwtException ex) {
//            return new TokenParse(421,"Expired JWT accessToken",null);
//        } catch (UnsupportedJwtException ex) {
//            LOGGER.error("Unsupported JWT accessToken");
//            return new TokenParse(422,"Unsupported JWT accessToken",null);
//        } catch (IllegalArgumentException ex) {
//            LOGGER.error("JWT claims string is empty.");
//            return new TokenParse(423,"JWT claims string is empty",null);
//        }
//    }
    public Boolean checkTokenExpired(String token){
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
            return false;
        } catch (Exception ex){
            return true;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken);
            
            return true;
        } catch (MalformedJwtException ex) {
            LOGGER.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            LOGGER.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            LOGGER.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            LOGGER.error("JWT claims string is empty.");
        }
        return false;
    }
    public static void main(String[] args) {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJkZDYwOWE0ZC0zYWZmLTRlZWYtYTUzYy05ZmE3NmQ2NjlhMDMiLCJpYXQiOjE2MDkxMjk0MzMsImV4cCI6MTYwOTczNDIzM30.v_NtMslWCT6RgDl6t8qX4JBu058pkBve96OqW6pdjKGTeLK4H0ASpj4lA13uhH_DSOf0wGAHVUiXNiHMnvFSjw";
    }

    public String getUuidFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
