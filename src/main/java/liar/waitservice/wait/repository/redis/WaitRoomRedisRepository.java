package liar.waitservice.wait.repository.redis;

import liar.waitservice.wait.domain.WaitRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitRoomRedisRepository extends CrudRepository<WaitRoom, String> {
    List<WaitRoom> findAllByHostName(String hostName);
    List<WaitRoom> findAllByRoomName(String roomName);

    Optional<WaitRoom> findWaitRoomByHostId(String hostId);
    WaitRoom findByHostId(String hostId);

    Slice<WaitRoom> findWaitRoomByRoomName(String roomName, Pageable pageable);
    Slice<WaitRoom> findWaitRoomByHostName(String hostName, Pageable pageable);
    Slice<WaitRoom> findWaitRoomById(String hostName, Pageable pageable);

    Slice<WaitRoom> findAllBy(Pageable pageable);
}
