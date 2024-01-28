package io.github.kituin.ChatImageCode;


import io.github.kituin.ChatImageCode.exception.InvalidChatImageCodeException;

import java.util.HashMap;
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

    private String name = "codename.chatimage.default";

    private String prefix = "[";
    private String suffix = "]";
    private final boolean isSelf;
    private final long timestamp;
    public static TimeoutHelper timeoutHelper;



    ChatImageCode(boolean isSelf) {
        this.isSelf = isSelf;
        this.timestamp = System.currentTimeMillis();
    }


    public ChatImageCode(String url) {
        this(new ChatImageUrl(url), null,null, null, false);
    }

    public ChatImageCode(String url, boolean isSelf) {
        this(new ChatImageUrl(url), null, null, null, isSelf);
    }

    public ChatImageCode(String url, String name) {
        this(new ChatImageUrl(url), name,null, null, false);
    }

    public ChatImageCode(String url, String name, boolean isSelf) {
        this(new ChatImageUrl(url), name,null, null, isSelf);
    }
    public ChatImageCode(String url, String prefix, String suffix) {
        this(new ChatImageUrl(url), null, prefix, suffix, false);
    }
    public ChatImageCode(String url, String prefix, String suffix, boolean isSelf) {
        this(new ChatImageUrl(url), null, prefix, suffix, isSelf);
    }
    public ChatImageCode(String url, String name, String prefix, String suffix) {
        this(new ChatImageUrl(url), name, prefix, suffix, false);
    }
    public ChatImageCode(String url, String name, String prefix, String suffix, boolean isSelf) {
        this(new ChatImageUrl(url), name, prefix, suffix, isSelf);
    }
    public ChatImageCode(ChatImageUrl url, String name, String prefix, String suffix, boolean isSelf) {
        this.url = url;
        if (name != null) this.name = name;
        if (prefix != null) this.prefix = prefix;
        if (suffix != null) this.suffix = suffix;
        this.timestamp = System.currentTimeMillis();
        this.isSelf = isSelf;
    }

    /**
     * 从字符串 加载 {@link ChatImageCode}
     *
     * @param code 字符串模式的 {@link ChatImageCode}
     * @param self 是否是自己发送的
     * @return {@link ChatImageCode}
     * @throws InvalidChatImageCodeException 识别失败
     */
    public static ChatImageCode fromCode(String code, boolean self) throws InvalidChatImageCodeException {
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
            // return new ChatImageFrame(ChatImageFrame.FrameError.ID_NOT_FOUND);
            return new ChatImageFrame(ChatImageFrame.FrameError.LOADING);
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
                switch (temps[0].trim()) {
                    case "url":
                        this.url = new ChatImageUrl(temps[1].trim());
                        break;
                    case "nsfw":
                        this.nsfw = Boolean.parseBoolean(temps[1].trim());
                        break;
                    case "name":
                        this.name = temps[1].trim();
                        break;
                    case "pre":
                        this.prefix = temps[1];
                        break;
                    case "suf":
                        this.suffix = temps[1];
                        break;
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

    public boolean isNsfw() {
        return nsfw;
    }

    public void setNsfw(boolean nsfw) {
        this.nsfw = nsfw;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[[CICode");
        if (nsfw) {
            sb.append(",nsfw=true");
        }
        if (name != null) {
            sb.append(",name=").append(name);
        }
        sb.append(",url=").append(this.url.getOriginalUrl());
        return sb.append("]]").toString();
    }
    public String getName() {
        return this.name;
    }

    public boolean isSendFromSelf() {
        return isSelf;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
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

}
