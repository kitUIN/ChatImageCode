package io.github.kituin.ChatImageCode;


import io.github.kituin.ChatImageCode.enums.UrlMethod;
import io.github.kituin.ChatImageCode.exception.InvalidChatImageCodeException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.kituin.ChatImageCode.ChatImageCodeInstance.ADAPTER;


/**
 * @author kitUIN
 */
public class ChatImageCode {
    public static final Pattern pattern = Pattern.compile("\\[\\[CICode,(.+)\\]\\]");

    private String url = null;
    private boolean nsfw = false;

    private String name = "codename.chatimage.default";

    private String prefix = "[";
    private String suffix = "]";
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
        if(this.url == null) return new ChatImageFrame(ChatImageFrame.FrameError.ILLEGAL_CICODE_ERROR);
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
        if (this.url == null) {
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
        if(url == null) {
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
            if (!ClientStorage.ContainImage(this.httpUrl)) {
                boolean f = HttpImageHandler.request(this.httpUrl);
                if (!f) {
                    ClientStorage.AddImageError(this.httpUrl, ChatImageFrame.FrameError.INVALID_HTTP_URL);
                }
            }
        } else if (Objects.equals(uri.getScheme(), "file")) {
            this.urlMethod = UrlMethod.FILE;
            this.fileUrl = uri.toString().replace("file:///","");
            File file = new File(this.fileUrl);
            if (!ClientStorage.ContainImage(this.fileUrl)) {
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
        if (name != null) {
            sb.append(",name=").append(name);
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
            code.name = name;
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
            code.prefix = prefix;
            return this;
        }

        /**
         * 设置后缀
         * @param suffix 后缀
         * @return {@link Builder}
         */
        public Builder setSuffix(String suffix) {
            code.suffix = suffix;
            return this;
        }
    }
}
