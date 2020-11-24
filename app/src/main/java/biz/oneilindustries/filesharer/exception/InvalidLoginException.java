package biz.oneilindustries.filesharer.exception;

import java.io.IOException;

public class InvalidLoginException extends IOException {
    public InvalidLoginException(String message) {
        super(message);
    }
}
