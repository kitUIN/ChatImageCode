package io.github.kituin.ChatImageCode;


import com.google.common.collect.Lists;
import io.github.kituin.ChatImageCode.exception.InvalidChatImageCodeException;
import io.github.kituin.ChatImageCode.exception.InvalidChatImageUrlException;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @author kitUIN
 */
public class ChatImageCode {
    public static final Pattern pattern = Pattern.compile("\\[\\[CICode,(.+)\\]\\]");

    /**
     * 图片缓存
     */
    public static HashMap<String, ChatImageFrame> CACHE_MAP = new HashMap<>();
    /**
     * NSFW列表
     */
    public static HashMap<String, Integer> NSFW_MAP = new HashMap<>();
    private ChatImageUrl url = null;
    private boolean nsfw = false;
    private final boolean isSelf;
    private final long timestamp;
    private String name = "codename.chatimage.default";
    public static TimeoutHelper timeoutHelper;



    ChatImageCode(boolean isSelf) {
        this.isSelf = isSelf;
        this.timestamp = System.currentTimeMillis();
    }

    public ChatImageCode(String url) throws InvalidChatImageUrlException {
        this(new ChatImageUrl(url), null, false);
    }

    public ChatImageCode(String url, String name) throws InvalidChatImageUrlException {
        this(new ChatImageUrl(url), name, false);
    }


    public ChatImageCode(ChatImageUrl url, String name, boolean isSelf) {
        this.url = url;
        if (name != null) {
            this.name = name;
        }
        this.timestamp = System.currentTimeMillis();
        this.isSelf = isSelf;
    }

    /**
     * 从字符串 加载 {@link ChatImageCode}
     *
     * @param code 字符串模式的 {@link ChatImageCode}
     * @return {@link ChatImageCode}
     * @throws InvalidChatImageCodeException 加载失败
     */
    public static ChatImageCode of(String code) throws InvalidChatImageCodeException {
        return ChatImageCode.of(code, false);
    }

    public static ChatImageCode of(String code, boolean self) throws InvalidChatImageCodeException {
        ChatImageCode chatImageCode = new ChatImageCode(self);
        chatImageCode.match(code);
        return chatImageCode;
    }

    /**
     * Load Texture from cache
     *
     * @return Identifier
     */
    public ChatImageFrame getFrame() {
        if(this.url == null) return new ChatImageFrame(ChatImageFrame.FrameError.ILLEGAL_CICODE_ERROR);
        String useUrl = this.url.getUrl();
        if (CACHE_MAP.containsKey(useUrl)) {
            return CACHE_MAP.get(useUrl);
        } else {
            return new ChatImageFrame(ChatImageFrame.FrameError.ID_NOT_FOUND);
        }

    }


    /**
     * 匹配 {@link ChatImageCode}
     *
     * @param originalCode 字符串模式的 {@link ChatImageCode}
     * @throws InvalidChatImageCodeException 匹配失败
     */
    private void match(String originalCode) throws InvalidChatImageCodeException {
        Matcher matcher = pattern.matcher(originalCode);
        if (matcher.find()) {
            slice(matcher.group(1));
        } else {
            throw new InvalidChatImageCodeException(originalCode + "<-can not find any String to ChatImageCode, Please Recheck");
        }
    }

    /**
     * slice each code variable
     *
     */
    private void slice(String rawCode) throws InvalidChatImageCodeException {
        String[] raws = rawCode.split(",");
        for (String raw : raws) {
            String[] temps = raw.split("=", 2);
            if (temps.length == 2) {
                String value = temps[0].trim();
                if(value.equals("url")){
                    try {
                        this.url = new ChatImageUrl(temps[1].trim());
                    } catch (InvalidChatImageUrlException e ) {
                        throw new InvalidChatImageCodeException(e.getMessage(), e.getMode());
                    }
                }else if(value.equals("nsfw")){
                    this.nsfw = Boolean.parseBoolean(temps[1].trim());
                }else if(value.equals("name")){
                    this.name = temps[1].trim();
                }
            } else {
                throw new InvalidChatImageCodeException(raw + "<-can not match the value of ChatImageCode, Please Recheck");
            }
        }
        if (this.url == null) {
            throw new InvalidChatImageCodeException("not match url in ChatImageCode, Please Recheck");
        }
    }


    public String getOriginalUrl() {
        return this.url.getOriginalUrl();
    }

    public ChatImageUrl getChatImageUrl() {
        return this.url;
    }

    public boolean getNsfw() {
        return this.nsfw;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[[CICode,url=").append(this.url.getOriginalUrl());
        if (nsfw) {
            sb.append(",nsfw=true");
        }
        if (name != null) {
            sb.append(",name=").append(name);
        }
        return sb.append("]]").toString();
    }

    public String getName() {
        return this.name;
    }

    public boolean isSendFromSelf() {
        return isSelf;
    }


    public boolean isTimeout() {
        return System.currentTimeMillis() > this.timestamp + 1000L * timeoutHelper.getTimeOut();
    }

    public enum ChatImageType {
        GIF, PNG, ICO, WEBP
    }

    @FunctionalInterface
    public interface TimeoutHelper {
        /**
         * 获取配置的超时时间
         * @return 超时时间
         */
        int getTimeOut();
    }
    @FunctionalInterface
    public interface LogHelper {
        void Log();
    }

}
