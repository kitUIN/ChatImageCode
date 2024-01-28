package io.github.kituin.ChatImageCode.exception;


/**
 * @author kituin
 */
public class InvalidChatImageCodeException extends Exception
{
    private final String message;
    private final InvalidCodeMode codeMode;
    public InvalidChatImageCodeException(String message)
    {
        this.message = message;
        this.codeMode = InvalidCodeMode.CodeInvalid;
    }

    public String getMessage()
    {
        return this.message;
    }
    public InvalidCodeMode getCodeMode()
    {
        return this.codeMode;
    }
    public enum InvalidCodeMode
    {
        /**
         * URL 出错
         */
        URLInvalid,
        /**
         * Code 出错
         */
        CodeInvalid,

    }
}
