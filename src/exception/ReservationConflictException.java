package exception;

/** 같은 좌석(또는 같은 회원)의 예약 시간이 겹칠 때 발생한다. */
public class ReservationConflictException extends StudyHubException {
    public ReservationConflictException(String message) {
        super(message);
    }
}
