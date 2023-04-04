package liar.waitservice.wait.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FindAllWaitRoomRequest {

    @Range(min = 0, max = 1000)
    private int page = 0;

    @Range(min = 0, max = 20)
    private int limit = 10;

}
