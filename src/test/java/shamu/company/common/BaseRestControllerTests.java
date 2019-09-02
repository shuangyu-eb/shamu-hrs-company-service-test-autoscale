package shamu.company.common;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import shamu.company.common.config.DefaultJwtAuthenticationToken;
import shamu.company.common.config.SecurityHolder;
import shamu.company.company.entity.Company;
import shamu.company.user.entity.User;

class BaseRestControllerTests {

  @BeforeAll
  static void setUp() {

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
    final Jwt jwt = new Jwt(token, jwtIssuedAt, jwtExpiredAt,  jwtHeaders, bodyClaims);

    final String userId = "1";
    final Authentication authentication = new DefaultJwtAuthenticationToken(jwt, userId,
        Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    final User user = new User();
    user.setId(1L);
    user.setUserId(userId);
    final Company company = new Company();
    company.setId(1L);
    user.setCompany(company);
    SecurityHolder.getCurrentUser().set(user);
  }

  @BeforeEach
  void init() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testGetUser() {
    final BaseRestController baseRestController = PowerMockito.spy(new BaseRestController());
    final User user = baseRestController.getUser();
    Assertions.assertNotNull(user);
  }

  @Test
  void testGetCompany() {
    final BaseRestController baseRestController = PowerMockito.spy(new BaseRestController());
    final Company company = baseRestController.getCompany();
    Assertions.assertNotNull(company);
  }

  @Test
  void testGetUserId() {
    final BaseRestController baseRestController = PowerMockito.spy(new BaseRestController());
    final String userId = baseRestController.getUserId();
    Assertions.assertNotNull(userId);
  }
}
