package exception;

/** 존재하지 않는 회원·좌석·예약을 찾으려 할 때 발생한다. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
