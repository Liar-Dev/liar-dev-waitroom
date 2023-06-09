package liar.waitservice.exception.advice;

import liar.waitservice.exception.exception.BadRequestException;
import liar.waitservice.exception.exception.BindingInvalidException;
import liar.waitservice.exception.exception.NotExistsRefreshTokenException;
import liar.waitservice.exception.exception.NotFoundUserException;
import liar.waitservice.exception.type.ExceptionCode;
import liar.waitservice.exception.type.ExceptionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ExceptionControllerAdvice {

    /**
     *  바인딩 에러
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> bindingInvalidHandler(BindingInvalidException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), BAD_REQUEST);
    }


    /**
     *  권한 없는 사용자 접근
     */
    @ResponseStatus(UNAUTHORIZED)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> notExistsTokenHandler(NotExistsRefreshTokenException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), UNAUTHORIZED);
    }


    /**
     *  등록되지 않은 유저 접근
     */
    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> userNotFoundHandler(NotFoundUserException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), NOT_FOUND);
    }

    /**
     * 유효하지 않은 BadRequest 요청시 발생
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> badRequestExceptionHandler(BadRequestException e) {
        return new ResponseEntity<>(new ErrorDto(e.getErrorCode(), e.getMessage()), BAD_REQUEST);
    }

    /**
     *  공통 4XX 에러
     */
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> illegalHandler(IllegalArgumentException e) {
        return new ResponseEntity<>(new ErrorDto(ExceptionCode.BAD_REQUEST, ExceptionMessage.BAD_REQUEST), BAD_REQUEST);
    }


    /**
     *  공통 5XX 에러
     */
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ResponseEntity<ErrorDto> internalServerErrorHandler(Exception e) {

        return new ResponseEntity<>(new ErrorDto(ExceptionCode.INTERNAL_SERVER_ERROR, ExceptionMessage.INTERNAL_SERVER_ERROR), INTERNAL_SERVER_ERROR);
    }

}
