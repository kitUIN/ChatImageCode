package io.github.kituin.ChatImageCode.exception;

import java.io.IOException;

public class FileTooLargeException extends IOException {


    public FileTooLargeException() {
        super();
    }


    public FileTooLargeException(String message) {
        super(message);
    }


    public FileTooLargeException(String message, Throwable cause) {
        super(message, cause);
    }


    public FileTooLargeException(Throwable cause) {
        super(cause);
    }
}
