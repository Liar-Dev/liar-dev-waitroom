package liar.waitservice.common.other.repository;

import liar.waitservice.common.other.domain.Member;
import liar.waitservice.common.other.dao.MemberIdOnly;
import liar.waitservice.common.other.dao.MemberNameOnly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    MemberNameOnly findProjectionByUserId(String userId);

    List<MemberIdOnly> findProjectionByUsername(String userName);
}
