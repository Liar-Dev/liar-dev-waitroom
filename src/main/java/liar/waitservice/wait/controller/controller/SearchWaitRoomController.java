package liar.waitservice.wait.controller.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import liar.waitservice.wait.controller.dto.FindAllWaitRoomRequest;
import liar.waitservice.wait.controller.dto.SearchWaitRoomRequest;
import liar.waitservice.wait.controller.dto.SearchWaitRoomSliceRequest;
import liar.waitservice.wait.controller.dto.message.SendSuccessBody;
import liar.waitservice.wait.service.search.SearchFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wait-service")
@RequiredArgsConstructor
public class SearchWaitRoomController {

    private final SearchFacadeService searchFacadeService;

    @GetMapping("/waitrooms")
    public ResponseEntity searchAllWaitRooms(@Valid @RequestBody FindAllWaitRoomRequest request) {
        return ResponseEntity.ok().body(SendSuccessBody.of(searchFacadeService
                .searchAllWaitRooms(request)));
    }

    @GetMapping("/waitroom/search")
    public ResponseEntity searchWaitRooms(@Valid @RequestBody SearchWaitRoomRequest dto) {
        return ResponseEntity.ok().body(SendSuccessBody.of(searchFacadeService.searchWaitRoomCondition(dto)));
    }

    @GetMapping("/waitroom-slice/search")
    public ResponseEntity searchWaitRoomsSlice(@Valid @RequestBody SearchWaitRoomSliceRequest dto) {
        return ResponseEntity.ok().body(SendSuccessBody.of(searchFacadeService.searchWaitRoomSliceCondition(dto)));
    }


}
