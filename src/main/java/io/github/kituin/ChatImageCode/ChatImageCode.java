package io.github.kituin.ChatImageCode;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.kituin.ChatImageCode.enums.UrlMethod;
import io.github.kituin.ChatImageCode.exception.InvalidChatImageCodeException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.kituin.ChatImageCode.ChatImageCodeInstance.ADAPTER;


/**
 * @author kitUIN
 */
public class ChatImageCode {
    public static final Pattern pattern = Pattern.compile("\\[\\[CICode,(.+)\\]\\]");

    private String url = "";
    private boolean nsfw = false;
    public static final String DEFAULT_NAME = "codename.chatimage.default";
    public static final String DEFAULT_PREFIX = "[";
    public static final String DEFAULT_SUFFIX = "]";

    private String name = DEFAULT_NAME;

    private String prefix = DEFAULT_PREFIX;
    private String suffix = DEFAULT_SUFFIX;
    private boolean isSelf = false;
    private final long timestamp;
    private String httpUrl;
    private UrlMethod urlMethod = UrlMethod.UNKNOWN;
    private String fileUrl;
    private
    ChatImageCode() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Load Texture from cache
     *
     * @return Identifier
     */
    public ChatImageFrame getFrame() {
        if(this.url.isEmpty()) return new ChatImageFrame(ChatImageFrame.FrameError.ILLEGAL_CICODE_ERROR);
        ChatImageFrame frame = ClientStorage.getImage(this.getUrl());
        if (frame == null) return new ChatImageFrame(ChatImageFrame.FrameError.LOADING);
        return frame;
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
                        checkUrl(temps[1].trim());
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
        if (this.url.isEmpty()) {
            throw new InvalidChatImageCodeException("not match url in ChatImageCode, Please Recheck");
        }
    }



    public String getUrl() {
        switch (urlMethod) {
            case HTTP:
                return httpUrl;
            case FILE:
                return fileUrl;
            default:
                return url;
        }
    }

    /**
     * 检查Url
     * @param url
     */
    public void checkUrl(String url) {
        if(url == null || url.isEmpty()) {
            ClientStorage.AddImageError(url, ChatImageFrame.FrameError.INVALID_URL);
            return;
        }
        this.url = url.replace("\\", "/");
        URI uri;
        try {
            uri = new URI(this.url);
        } catch (URISyntaxException e) {
            ClientStorage.AddImageError(this.url, ChatImageFrame.FrameError.INVALID_URL);
            return;
        }
        ADAPTER.checkCachePath();
        if (Objects.equals(uri.getScheme(), "https") ||
                Objects.equals(uri.getScheme(), "http")) {
            this.urlMethod = UrlMethod.HTTP;
            this.httpUrl = uri.toString();
            if (!ClientStorage.ContainImageAndCheck(this.httpUrl)) {
                boolean f = HttpImageHandler.request(this.httpUrl);
                if (!f) {
                    ClientStorage.AddImageError(this.httpUrl, ChatImageFrame.FrameError.INVALID_URL);
                }
            }
        } else if (Objects.equals(uri.getScheme(), "file")) {
            this.urlMethod = UrlMethod.FILE;
            this.fileUrl = uri.toString().replace("file:///","");
            File file = new File(this.fileUrl);
            if (!ClientStorage.ContainImageAndCheck(this.fileUrl)) {
                boolean fileExist = file.exists();
                if (fileExist) {
                    FileImageHandler.loadFile(this.fileUrl);
                }
                ADAPTER.sendPacket(this.fileUrl, file, fileExist);
            }
        } else {
            ClientStorage.AddImageError(this.url, ChatImageFrame.FrameError.INVALID_URL);
        }
    }
    public void retry() {
        checkUrl(this.url);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[[CICode");
        if (nsfw) {
            sb.append(",nsfw=true");
        }
        if (name != null && !name.equals(DEFAULT_NAME)) {
            sb.append(",name=").append(name);
        }
        if (!Objects.equals(prefix, DEFAULT_PREFIX)) {
            sb.append(",nsfw=").append(suffix);
        }
        if (!Objects.equals(suffix, DEFAULT_SUFFIX)) {
            sb.append(",suffix=").append(suffix);
        }
        sb.append(",url=").append(this.url);
        return sb.append("]]").toString();
    }
    public String getName() {
        return this.name;
    }
    public boolean isNsfw() {
        return nsfw;
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
        return System.currentTimeMillis() > this.timestamp + 1000L * ADAPTER.getTimeOut();
    }


    /**
     * 构造悬浮图片样式的抽象类文本
     * @param newText 字符串文本的新建
     * @param newTranslatableText 翻译文本的新建
     * @param appendText 抽象类文本的添加
     * @return 抽象类文本
     * @param <Mutable> 抽象类文本
     */
    public<Mutable> Mutable messageFromCode(Function<String, Mutable> newText,
                                            Function<String, Mutable> newTranslatableText,
                                            BiFunction<Mutable, Mutable, Mutable> appendText) {
        Mutable t = newText.apply(prefix);
        if (DEFAULT_NAME.equals(name)) {
            appendText.apply(t, newTranslatableText.apply(name));
        } else {
            appendText.apply(t, newText.apply(name));
        }
        appendText.apply(t,newText.apply(suffix));
        return t;
    }


    /**
     * 反序列化CICode
     * @param json CICode JSON模式
     * @return CICode
     */
    public static ChatImageCode fromJson(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        ChatImageCode.Builder builder = ChatImageCodeInstance.createBuilder();
        if (jsonObject.has("url")) {
            builder.setUrl(jsonObject.get("url").getAsString());
        }
        if (jsonObject.has("name")) {
            builder.setName(jsonObject.get("name").getAsString());
        }
        if (jsonObject.has("prefix")) {
            builder.setPrefix(jsonObject.get("prefix").getAsString());
        }
        if (jsonObject.has("suffix")) {
            builder.setSuffix(jsonObject.get("suffix").getAsString());
        }
        if (jsonObject.has("nsfw")) {
            builder.setNsfw(jsonObject.get("nsfw").getAsBoolean());
        }
        return builder.build();
    }

    /**
     * 序列化CICode
     * @param code CICode
     * @return CICode JSON模式
     */
    public static JsonElement toJson(ChatImageCode code) {
        JsonObject jsonObject = new JsonObject();
        if(!code.getName().equals(DEFAULT_NAME))  jsonObject.addProperty("name", code.getName());
        if(!code.getPrefix().equals(DEFAULT_PREFIX))  jsonObject.addProperty("prefix", code.getPrefix());
        if(!code.getSuffix().equals(DEFAULT_SUFFIX))  jsonObject.addProperty("suffix", code.getSuffix());
        jsonObject.addProperty("url", code.getUrl());
        if(code.isNsfw()) jsonObject.addProperty("nsfw", code.isNsfw());
        return jsonObject;
    }
    public static class Builder {
        private final ChatImageCode code;
        public Builder(){
            this.code = new ChatImageCode();
        }

        public ChatImageCode build() {
            return code;
        }

        /**
         * 从字符串 加载 {@link ChatImageCode}
         *
         * @param ciCode 字符串模式的 {@link ChatImageCode}
         * @return {@link ChatImageCode}
         * @throws InvalidChatImageCodeException 识别失败
         */
        public Builder fromCode(String ciCode) throws InvalidChatImageCodeException {
            code.match(ciCode);
            return this;
        }
        /**
         * 设置name
         * @param name name
         * @return  {@link Builder}
         */
        public Builder setName(String name) {
            if(name != null) code.name = name;
            return this;
        }
        /**
         * 设置url
         * @param url url
         * @return  {@link Builder}
         */
        public Builder setUrl(String url) {
            code.checkUrl(url);
            return this;
        }

        /**
         * 设置nsfw
         * @param nsfw 是否时nsfw
         * @return  {@link Builder}
         */
        public Builder setNsfw(boolean nsfw) {
            code.nsfw = nsfw;
            return this;
        }

        /**
         * 设置是否自己发送
         * @param isSelf 是否自己发送
         * @return {@link Builder}
         */
        public Builder setIsSelf(boolean isSelf) {
            code.isSelf = isSelf;
            return this;
        }
        /**
         * 设置前缀
         * @param prefix 前缀
         * @return {@link Builder}
         */
        public Builder setPrefix(String prefix) {
            if(prefix != null) code.prefix = prefix;
            return this;
        }

        /**
         * 设置后缀
         * @param suffix 后缀
         * @return {@link Builder}
         */
        public Builder setSuffix(String suffix) {
            if(suffix != null) code.suffix = suffix;
            return this;
        }
    }
}
