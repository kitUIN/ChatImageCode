package io.github.kituin.ChatImageCode;

/**
 * 是否全部为字符串
 */
public class ChatImageBoolean {
    private boolean value;

    public ChatImageBoolean(boolean value) {
        this.value = value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    /**
     *
     * @return 是否全部为字符串，false为有CICode
     */
    public boolean isValue() {
        return value;
    }
}
