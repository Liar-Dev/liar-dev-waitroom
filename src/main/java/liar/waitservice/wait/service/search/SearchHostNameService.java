package liar.waitservice.wait.service.search;

import liar.waitservice.wait.service.search.dto.WaitRoomViewsDto;
import liar.waitservice.wait.service.waitroom.WaitRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SearchHostNameService implements SearchService<WaitRoomViewsDto, String> {

    private final WaitRoomService waitRoomService;

    @Override
    public List<WaitRoomViewsDto> searchWaitRoomByCond(String body) {
        return waitRoomService.findWaitRoomByHostName(body)
                .stream()
                .map(WaitRoomViewsDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public Slice<WaitRoomViewsDto> searchWaitRoomByCond(String body, Pageable pageable) {
        return waitRoomService.findWaitRoomByHostName(body, pageable).map(WaitRoomViewsDto::new);
    }
}
