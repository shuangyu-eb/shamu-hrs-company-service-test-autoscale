package com.tardisone.companyservice.config;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tardisone.companyservice.entity.User;
import com.tardisone.companyservice.exception.UnAuthenticatedException;
import com.tardisone.companyservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

@Component
public class JwtTokenProvider {

    @Value("${auth0.jwks}")
    private String jwks;

    @Value("${auth0.algorithm}")
    private String algorithm;

    @Value("${auth0.authDomain}")
    private String authDomain;

    @Autowired
    UserService userService;

    private boolean isRightAlgorithm(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return this.algorithm.equals(jwt.getAlgorithm());
    }

    private DecodedJWT verifySignatureAndGetDecodedJWT(String token) {
        DecodedJWT jwt = JWT.decode(token);
        JwkProvider provider = new UrlJwkProvider(jwks);
        Jwk jwk = null;
        try {
            jwk = provider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(authDomain).build();
            return verifier.verify(token);
        } catch (JwkException | TokenExpiredException e) {
            throw new UnAuthenticatedException(e.getMessage());
        }
    }

    public Authentication authenticate(String token) {
        if (!this.isRightAlgorithm(token)) {
            return null;
        }

        DecodedJWT decodedJWT = this.verifySignatureAndGetDecodedJWT(token);
        if (decodedJWT == null) {
            return null;
        }
        String email = decodedJWT.getClaim("email").asString();
        User user = userService.findUserByEmail(email);
        return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<SimpleGrantedAuthority>());
    }

}
