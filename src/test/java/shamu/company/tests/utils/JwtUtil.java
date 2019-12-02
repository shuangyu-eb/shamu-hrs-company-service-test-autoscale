package shamu.company.tests.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtUtil {

  public static Jwt getJwt() {
    final String token = JWT.create().withIssuer("http://www.google.com/")
        .sign(Algorithm.HMAC256("secret"));
    final DecodedJWT decodedJwt = JWT.decode(token);
    final Map<String, Object> jwtHeaders = new HashMap<>();
    jwtHeaders.put("typ", decodedJwt.getHeaderClaim("typ"));
    jwtHeaders.put("alg", decodedJwt.getHeaderClaim("alg"));

    final Map<String, Object> bodyClaims = new HashMap<>();
    bodyClaims.put("iss", decodedJwt.getClaim("iss"));

    final Instant jwtIssuedAt = LocalDateTime.now().toInstant(ZoneOffset.UTC);
    final Instant jwtExpiredAt = LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC);
    return new Jwt(token, jwtIssuedAt, jwtExpiredAt,  jwtHeaders, bodyClaims);
  }
}
