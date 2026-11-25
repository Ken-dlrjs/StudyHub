package ui;

/**
 * 입력 도중 사용자가 취소 키워드(q)를 입력했을 때 발생한다.
 * ConsoleMenu가 잡아서 아무것도 바꾸지 않고 메뉴로 돌아간다.
 */
public class InputCancelledException extends RuntimeException {
    public InputCancelledException() {
        super("입력을 취소했습니다.");
    }
}