package com.furniro.AuthService.config;

import com.furniro.AuthService.service.other.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

@RequiredArgsConstructor
public class JwtBlacklistValidator implements OAuth2TokenValidator<Jwt> {

    private final RedisService redisService;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        String tokenId = jwt.getId();

        if (tokenId != null && redisService.isCaching("BLACKLISTED_TOKEN:" + tokenId)) {

            OAuth2Error error = new OAuth2Error("invalid_token", 
            "The token has been blacklisted, please login again",null);
            
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }
}
