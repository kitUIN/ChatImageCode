package io.github.kituin.ChatImageCode;

public class ChatImageCodeInstance {
    public static IClientAdapter CLIENT_ADAPTER;
    public static IServerAdapter SERVER_ADAPTER;
    public static IChatImageCodeLogger LOGGER;

    public static ChatImageCode.Builder createBuilder() {
            return new ChatImageCode.Builder();
    }

}

