package exception;

/** 회원의 잔여 이용권 시간이 부족할 때 발생한다. */
public class InsufficientTimeException extends StudyHubException {
    public InsufficientTimeException(String message) {
        super(message);
    }
}
