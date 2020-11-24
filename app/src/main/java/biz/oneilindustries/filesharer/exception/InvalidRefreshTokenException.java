package biz.oneilindustries.filesharer.exception;

import java.io.IOException;

public class InvalidRefreshTokenException extends IOException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
