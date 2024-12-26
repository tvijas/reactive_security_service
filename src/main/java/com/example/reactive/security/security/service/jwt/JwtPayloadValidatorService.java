//package com.example.reactive.security.security.service.jwt;
//
//import com.auth0.jwt.interfaces.DecodedJWT;
//import com.example.kuby.security.blacklist.BlacklistService;
//import com.example.kuby.security.models.enums.TokenActionType;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//@Service
//@RequiredArgsConstructor
//public class JwtPayloadValidatorService {
//    private final BlacklistService blacklistService;
//    private final JwtGeneratorService jwtGeneratorService;
//    public boolean isTokenClaimValid(String jwtId, DecodedJWT decodedAccessToken, HttpServletResponse response) {
//        if (blacklistService.isBlacklisted(jwtId)) {
//            Map<String, TokenActionType> actions = blacklistService.getBlacklistActions(jwtId);
//            for (Map.Entry<String, TokenActionType> entry : actions.entrySet()) {
//                String key = entry.getKey();
//                TokenActionType action = entry.getValue();
//                //TODO
//                //???????????????????????????????????????
//                //???????????????????????????????????????
//                //???????????????????????????????????????
//                //???????????????????????????????????????
//                //???????????????????????????????????????
//                //???????????????????????????????????????
//            }
//            blacklistService.removeFromBlacklist(jwtId);
//            return false;
//        } else {
//            return true;
//        }
//    }
//    private String updateClaimsOfDecodedJwtToken(Map<String, Object> newClaims, DecodedJWT decodedJWT) {
//        Map<String, Object> claims = new HashMap<>(decodedJWT.getClaims().size() + newClaims.size());
//        decodedJWT.getClaims().forEach((key, value) -> claims.put(key, value.as(Object.class)));
//
//        claims.putAll(newClaims);
//
//        return jwtGeneratorService.generateTokenWithNewClaims(claims,decodedJWT);
//    }
//    private String deleteClaimsFromDecodedJwtToken(List<String> claimsToBeDeleted, DecodedJWT decodedJWT) {
//        Map<String, Object> claims = new HashMap<>(decodedJWT.getClaims().size());
//        decodedJWT.getClaims().forEach((key, value) -> claims.put(key, value.as(Object.class)));
//
//        claimsToBeDeleted.forEach(claims::remove);
//
//        return jwtGeneratorService.generateTokenWithNewClaims(claims,decodedJWT);
//    }
//}
