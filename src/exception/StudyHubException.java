package exception;

/** StudyHub 사용자 정의 예외의 공통 부모. UI에서 이 타입 하나로 묶어 처리할 수 있다. */
public class StudyHubException extends RuntimeException {
    public StudyHubException(String message) {
        super(message);
    }
}
