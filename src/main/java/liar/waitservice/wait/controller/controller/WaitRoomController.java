package liar.waitservice.wait.controller.controller;

import jakarta.validation.Valid;
import liar.waitservice.wait.controller.dto.CommonWaitRoomRequest;
import liar.waitservice.wait.controller.dto.CreateWaitRoomRequest;
import liar.waitservice.wait.controller.dto.message.SendSuccessBody;
import liar.waitservice.wait.service.WaitRoomFacadeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/wait-service")
@RequiredArgsConstructor
public class WaitRoomController {

    private final WaitRoomFacadeService waitRoomFacadeService;

    @PostMapping("/waitroom/create")
    public ResponseEntity createWaitRoom(@Valid @RequestBody CreateWaitRoomRequest dto) {
        String waitRoomId = waitRoomFacadeService.saveWaitRoomByHost(dto);
        return ResponseEntity.ok().body(SendSuccessBody.of(waitRoomId));
    }


    @GetMapping("/waitroom/info")
    public ResponseEntity fetchJoinedWaitRoomInfo(@Valid @RequestBody CommonWaitRoomRequest request) {
        waitRoomFacadeService.isJoinedMemberThenThrow(request.getRoomId(), request.getUserId());
        return ResponseEntity.ok()
                .body(SendSuccessBody.of(waitRoomFacadeService.fetchWaitRoomDetails(request.getRoomId())));
    }

}
