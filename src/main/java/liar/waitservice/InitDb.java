package liar.waitservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import liar.waitservice.common.other.domain.Authorities;
import liar.waitservice.common.other.domain.Authority;
import liar.waitservice.common.other.domain.Member;
import liar.waitservice.wait.controller.dto.CreateWaitRoomRequest;
import liar.waitservice.wait.service.WaitRoomFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Component
@Profile({"default", "local"})
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.dbInit();
    }

    @Slf4j
    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;
        private final WaitRoomFacadeService waitRoomFacadeService;
        private Key key;
        private @Value("${jwt.secret}") String secretKey;
        private @Value("${jwt.access-expiration-time}") long accessTokenExpirationTime;
        private @Value("${jwt.refresh-expiration-time}") long refreshTokenExpirationTime;

        public void dbInit() {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            this.key = Keys.hmacShaKeyFor(keyBytes);

            String userId = "c87afd49-956f-4e4c-9829-f2f24a193695";

            Member member = new Member(userId, "Kose");
            List<Authority> authorities = new ArrayList<>();
            authorities.add(new Authority(member, Authorities.ROLE_USER));

            em.persist(member);
            authorities.stream().forEach(em::persist);

            String accessToken = createToken(member.getUserId(), authorities, accessTokenExpirationTime);
            String refreshToken = createToken(member.getUserId(), authorities, refreshTokenExpirationTime);

            String waitRoomId = waitRoomFacadeService.saveWaitRoomByHost(CreateWaitRoomRequest.builder()
                    .userId(userId).roomName("즐겜").limitMembers(5).build());

            log.info("AccessToken = {}", accessToken);
            log.info("RefreshToken = {}", refreshToken);
            log.info("WaitRoomId = {}", waitRoomId);
        }

        public String createToken(String userId, List<Authority> authorities, long tokenTime) {

            Claims claims = Jwts.claims().setSubject(userId);
            claims.put("auth", getRoles(authorities));

            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + tokenTime + new Random().nextInt(100000)))
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        }

        private List<String> getRoles (List<Authority> authorities) {

            List<String> roles = new ArrayList<>();
            authorities.forEach(role -> roles.add(role.getAuthorities().getAuthoritiesName()));
            return roles;
        }
    }
}
