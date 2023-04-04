package liar.waitservice.common.other.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member implements Serializable {

    @Id
    @Column(name = "member_id")
    private String id;
    private String userId;
    private String username;


    /**
     * 테스트를 위한 생성자 최대한 사용하지 말것
     */
    public Member(String userId, String username) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.username = username;
    }
}
