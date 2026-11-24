package exception;

/** CSV 파일의 한 줄이 형식에 맞지 않거나, 파일을 읽고 쓸 수 없을 때 발생한다. */
public class DataFileException extends StudyHubException {
    public DataFileException(String message) {
        super(message);
    }
}
