package exception;

/** 이미 존재하는 데이터를 중복 등록하려 할 때 발생한다. */
public class DuplicateException extends StudyHubException {
    public DuplicateException(String message) {
        super(message);
    }
}
