package liar.waitservice.exception.exception;


import liar.waitservice.exception.type.ExceptionCode;
import liar.waitservice.exception.type.ExceptionMessage;

public class BadJwtRequestException extends CommonException {

    public BadJwtRequestException() {super(ExceptionCode.BAD_REQUEST, ExceptionMessage.BAD_REQUEST);}
}
