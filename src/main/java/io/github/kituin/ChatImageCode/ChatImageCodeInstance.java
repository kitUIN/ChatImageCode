package io.github.kituin.ChatImageCode;

public class ChatImageCodeInstance {
    public static IChatImageAdapter ADAPTER;
    public static IChatImageCodeLogger LOGGER;

    public static ChatImageCode.Builder createBuilder() {
            return new ChatImageCode.Builder();
    }

}

