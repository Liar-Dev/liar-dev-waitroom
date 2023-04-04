package liar.waitservice.wait.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchWaitRoomRequest {

    @NotNull
    private String body;

    @NotNull
    private String searchType;

    public String upperSearchType() {
        return searchType.toUpperCase();
    }

}
