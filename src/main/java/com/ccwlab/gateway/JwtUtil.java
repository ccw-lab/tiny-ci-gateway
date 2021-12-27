package com.ccwlab.gateway;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;


@Component
public class JwtUtil {

    private Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    private Key key;
    private Algorithm algorithm;

    @PostConstruct
    public void init(){
        this.algorithm = Algorithm.HMAC256(secret);
//        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public DecodedJWT getDecodedJwt(String tokenOrHeader) {
        try {
            var arr = tokenOrHeader.split("Bearer ");
            JWTVerifier verifier = JWT.require(this.algorithm)
                    .withIssuer("tiny-ci")
                    .build(); //Reusable verifier instance
            return verifier.verify(arr.length == 2 ? arr[1] : arr[0]);
        }catch(JWTVerificationException e){
            logger.debug("wrong jwt: ", e);
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        var decoded = getDecodedJwt(token);
        if(decoded != null){
            return false;
        }return true;
    }

    public boolean isInvalid(String token) {
        return this.isTokenExpired(token);
    }

}
