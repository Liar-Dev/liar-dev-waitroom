package liar.waitservice.wait.controller.dto.message;

import liar.waitservice.wait.controller.dto.message.code.SuccessCode;
import liar.waitservice.wait.controller.dto.message.message.SendMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendSuccessJoin<T> extends SendSuccess {

    private T body;

    public SendSuccessJoin(String code, String message, T body) {
        super(code, message);
        this.body = body;
    }

    public static <T> SendSuccessJoin of(String code, String message, T body) {
        return new SendSuccessJoin(code, message, body);
    }

    public static <T> SendSuccessJoin of(T body) {
        return new SendSuccessJoin(SuccessCode.OK, SendMessage.OK, body);
    }
}
