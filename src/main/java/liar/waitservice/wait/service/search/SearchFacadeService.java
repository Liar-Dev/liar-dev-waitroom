package liar.waitservice.wait.service.search;

import jakarta.ws.rs.NotFoundException;
import liar.waitservice.wait.controller.dto.FindAllWaitRoomRequest;
import liar.waitservice.wait.controller.dto.SearchWaitRoomRequest;
import liar.waitservice.wait.controller.dto.SearchWaitRoomSliceRequest;
import liar.waitservice.wait.service.search.dto.WaitRoomViewsDto;
import liar.waitservice.wait.service.waitroom.WaitRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchFacadeService {

    private final SearchRoomIdService searchRoomIdService;
    private final SearchRoomNameService searchRoomNameService;
    private final SearchHostNameService searchHostNameService;

    private final WaitRoomService waitRoomService;

    public List<WaitRoomViewsDto> searchWaitRoomCondition(SearchWaitRoomRequest dto) {
        return matchSearchService(dto).searchWaitRoomByCond(dto.getBody());
    }

    public Slice<WaitRoomViewsDto> searchWaitRoomSliceCondition(SearchWaitRoomSliceRequest dto) {
        return matchSearchService(dto).searchWaitRoomByCond(dto.getBody(), getPageable(dto));
    }

    public List<WaitRoomViewsDto> searchAllWaitRooms(FindAllWaitRoomRequest dto) {
        return waitRoomService.findAllWaitRooms(PageRequest.of(dto.getPage(), dto.getLimit()));
    }

    private SearchService matchSearchService(SearchWaitRoomRequest dto) {
        switch (dto.upperSearchType()) {

            case "WAITROOMID":
                return searchRoomIdService;

            case "WAITROOMNAME":
                return searchRoomNameService;

            case "HOSTNAME":
                return searchHostNameService;

            default:
                throw new NotFoundException();
        }
    }

    private Pageable getPageable(SearchWaitRoomSliceRequest dto) {
        return PageRequest.of(dto.getPage(), dto.getLimit());
    }

}
