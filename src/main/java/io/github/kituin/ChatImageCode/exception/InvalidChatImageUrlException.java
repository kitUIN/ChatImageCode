package io.github.kituin.ChatImageCode.exception;

public class InvalidChatImageUrlException extends Exception{
    private final String message;
    private final InvalidUrlMode mode;
    public InvalidChatImageUrlException(String message, InvalidUrlMode mode)
    {
        this.message = message;
        this.mode = mode;
    }

    public String getMessage()
    {
        return this.message;
    }
    public InvalidUrlMode getMode()
    {
        return this.mode;
    }
    public enum InvalidUrlMode
    {
        /**
         * file not found
         */
        FileNotFound,
        /**
         * http can't connection
         */
        HttpNotFound,
        /**
         * others
         */
        NotFound
    }
}