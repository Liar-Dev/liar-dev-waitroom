package liar.waitservice.wait.controller.dto;

import lombok.*;

@Getter
@NoArgsConstructor
public class CreateWaitRoomRequest {

    private String userId;
    private String roomName;
    private int limitMembers;

    @Builder
    public CreateWaitRoomRequest(String userId, String roomName, int limitMembers) {
        this.userId = userId;
        this.roomName = roomName;
        this.limitMembers = limitMembers;
    }
}
