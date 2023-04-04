package liar.waitservice.wait.controller.dto.message.message;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ChatMessageResponse<T> {

    private String userId;
    private JoinStatus joinStatus;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;
    private T body;

    public ChatMessageResponse(String userId, JoinStatus joinStatus) {
        this.userId = userId;
        this.joinStatus = joinStatus;
        this.createdAt = LocalDateTime.now();
    }

    public ChatMessageResponse(String userId, JoinStatus joinStatus, T body) {
        this.userId = userId;
        this.joinStatus = joinStatus;
        this.createdAt = LocalDateTime.now();
        this.body = body;
    }

    public static <T> ChatMessageResponse of(String userId, JoinStatus joinStatus, T body) {
        return new ChatMessageResponse(userId, joinStatus, body);
    }

    public static <T> ChatMessageResponse of(String userId, JoinStatus joinStatus) {
        return new ChatMessageResponse(userId, joinStatus);
    }
}
