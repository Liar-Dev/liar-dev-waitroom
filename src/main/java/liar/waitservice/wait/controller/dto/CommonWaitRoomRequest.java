package liar.waitservice.wait.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonWaitRoomRequest {

    private String userId;
    private String roomId;

}
