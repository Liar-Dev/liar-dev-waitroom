package liar.waitservice.wait.controller.socket;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import liar.waitservice.common.other.domain.Authorities;
import liar.waitservice.common.other.domain.Authority;
import liar.waitservice.common.other.domain.Member;
import liar.waitservice.common.other.repository.MemberRepository;
import liar.waitservice.wait.controller.controller.WaitRoomController;
import liar.waitservice.wait.controller.dto.CreateWaitRoomRequest;
import liar.waitservice.wait.repository.redis.JoinMemberRedisRepository;
import liar.waitservice.wait.repository.redis.WaitRoomRedisRepository;
import liar.waitservice.wait.service.WaitRoomFacadeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.security.Key;
import java.util.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SocketMockMvcController {

    private Key key;
    private static final String AUTHORITIES_KEY = "auth";
    private @Value("${jwt.secret}") String secretKey;
    private @Value("${jwt.access-expiration-time}") long accessTokenExpirationTime;
    private @Value("${jwt.refresh-expiration-time}") long refreshTokenExpirationTime;

    protected MockMvc mockMvc;

    protected String accessToken;
    protected String refreshToken;
    protected String userId = UUID.randomUUID().toString();
    protected String waitRoomId;

    @Autowired
    WaitRoomRedisRepository waitRoomRedisRepository;
    @Autowired
    JoinMemberRedisRepository joinMemberRedisRepository;

    @Autowired
    WaitRoomFacadeService waitRoomFacadeService;

    @Autowired
    WaitRoomController waitRoomController;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    public void init(WebApplicationContext webApplicationContext) throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        Member member = new Member(userId, "KOSE");
        memberRepository.save(member);

        List<Authority> authorities = new ArrayList<>();
        authorities.add(new Authority(member, Authorities.ROLE_USER));

        accessToken = createToken(member.getUserId(), authorities, accessTokenExpirationTime);
        refreshToken = createToken(member.getUserId(), authorities, refreshTokenExpirationTime);

        waitRoomId = waitRoomFacadeService.saveWaitRoomByHost(CreateWaitRoomRequest.builder()
                .userId(userId).roomName("즐겜").limitMembers(5).build());

    }

    @AfterEach
    public void tearDown() {
        waitRoomRedisRepository.deleteAll();
        joinMemberRedisRepository.deleteAll();
        memberRepository.deleteAll();
    }


    public String createToken(String userId, List<Authority> authorities, long tokenTime) {

        Claims claims = Jwts.claims().setSubject(userId);
        claims.put(AUTHORITIES_KEY, getRoles(authorities));

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
