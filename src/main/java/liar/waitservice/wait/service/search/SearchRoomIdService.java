package liar.waitservice.wait.service.search;

import liar.waitservice.wait.domain.WaitRoom;
import liar.waitservice.wait.service.search.dto.WaitRoomViewsDto;
import liar.waitservice.wait.service.waitroom.WaitRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchRoomIdService implements SearchService<WaitRoomViewsDto, String> {

    private final WaitRoomService waitRoomService;

    @Override
    public List<WaitRoomViewsDto> searchWaitRoomByCond(String body) {
        WaitRoom waitRoom = waitRoomService.findWaitRoomId(body);
        return Arrays.asList(waitRoom).stream().map(WaitRoomViewsDto::new).collect(Collectors.toList());
    }

    @Override
    public Slice<WaitRoomViewsDto> searchWaitRoomByCond(String body, Pageable pageable) {
        return waitRoomService.findWaitRoomByRoomId(body, pageable).map(WaitRoomViewsDto::new);
    }
}
