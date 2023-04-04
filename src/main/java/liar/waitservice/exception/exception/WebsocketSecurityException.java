package liar.waitservice.exception.exception;

import liar.waitservice.exception.type.ExceptionCode;
import liar.waitservice.exception.type.ExceptionMessage;

public class WebsocketSecurityException extends CommonException {

    public WebsocketSecurityException() {
        super(ExceptionCode.BAD_REQUEST, ExceptionMessage.BAD_REQUEST);
    }
}
