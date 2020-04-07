package shamu.company.tests.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.utils.DateUtil;

public class JwtUtil {

  private static PublicKey PUBLIC_KEY;
  private static PrivateKey PRIVATE_KEY;
  // fictitious domain, no such domain
  private static final String issuer = "https://simplyhired-test.auth0.com/";
  private static final String customNamespace = "https://interviewed.com/";

  public static Jwt getJwt() {
    final String token = JWT.create().withIssuer(issuer)
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

  public static String generateRsaToken() throws NoSuchAlgorithmException {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    final KeyPair keyPair = kpg.generateKeyPair();

    final PrivateKey privateKey = keyPair.getPrivate();
    final PublicKey publicKey = keyPair.getPublic();

    PUBLIC_KEY = publicKey;
    PRIVATE_KEY = privateKey;

    final LocalDateTime issuedAt = DateUtil.getLocalUtcTime();
    final LocalDateTime expiredAt = issuedAt.plusDays(3);
    final Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, (RSAPrivateKey) privateKey);

    final DefaultJwtAuthenticationToken authenticationToken =
        (DefaultJwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    final String userId = authenticationToken != null ? authenticationToken.getUserId() : "1";
    return JWT.create().withIssuer(issuer)
        .withIssuedAt(new Date(issuedAt.toEpochSecond(ZoneOffset.UTC) * 1000))
        .withExpiresAt(new Date(expiredAt.toEpochSecond(ZoneOffset.UTC) * 1000))
        .withClaim(customNamespace + "id", userId)
        .withClaim("scope", "openid profile email")
        .withKeyId(RandomStringUtils.randomAlphabetic(10))
        .sign(algorithm);
  }

  public static Jwt decode(final String token) {
    final Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) PUBLIC_KEY, (RSAPrivateKey) PRIVATE_KEY);
    final JWTVerifier jwtVerifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .build();
    final DecodedJWT decodedJwt = jwtVerifier.verify(token);

    final Map<String, Object> jwtHeaders = new HashMap<>();
    jwtHeaders.put("typ", decodedJwt.getHeaderClaim("typ"));
    jwtHeaders.put("alg", decodedJwt.getHeaderClaim("alg"));
    final Map<String, Claim> bodyClaims = decodedJwt.getClaims();

    final Map<String, Object> jwtBodyClaim = new HashMap<>();
    bodyClaims.forEach(((s, claim) -> {
      jwtBodyClaim.put(s, claim.asString());
    }));

    final Date issuedAt = decodedJwt.getIssuedAt();
    final Instant issuedAtDate = DateUtil.toLocalDateTime(issuedAt).toInstant(ZoneOffset.UTC);
    final Date expiredAt = decodedJwt.getExpiresAt();
    final Instant expiredAtDate = DateUtil.toLocalDateTime(expiredAt).toInstant(ZoneOffset.UTC);
    return new Jwt(decodedJwt.getToken(), issuedAtDate, expiredAtDate, jwtHeaders, jwtBodyClaim);
  }
}
