package io.github.kituin.ChatImageCode;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static io.github.kituin.ChatImageCode.ChatImageHandler.AddChatImageError;

public class ChatImageUrl {
    private String originalUrl = "";
    private String httpUrl;
    private UrlMethod urlMethod = UrlMethod.UNKNOWN;
    private String fileUrl;
    public static NetworkHelper networkHelper;
    public static CachePathHelper cachePathHelper;
    @FunctionalInterface
    public interface NetworkHelper {
        void send(String url, File file, boolean isServer);
    }
    @FunctionalInterface
    public interface CachePathHelper {
        void check();
    }

    public ChatImageUrl(String url) {
        if(url == null) {
            AddChatImageError(url, ChatImageFrame.FrameError.INVALID_URL);
            return;
        }
        this.originalUrl = url.replace("\\", "/");
        URI uri;
        try {
            uri = new URI(this.originalUrl);
        } catch (URISyntaxException e) {
            AddChatImageError(this.originalUrl, ChatImageFrame.FrameError.INVALID_URL);
            return;
        }
        cachePathHelper.check();
        if (Objects.equals(uri.getScheme(), "https") ||
                Objects.equals(uri.getScheme(), "http")) {
            this.urlMethod = UrlMethod.HTTP;
            this.httpUrl = uri.toString();
            if (!ChatImageCode.CACHE_MAP.containsKey(this.httpUrl)) {
                boolean f = ChatImageHttpHandler.getInputStream(this.httpUrl);
                if (!f) {
                    AddChatImageError(this.httpUrl, ChatImageFrame.FrameError.INVALID_HTTP_URL);
                }
            }
        } else if (Objects.equals(uri.getScheme(), "file")) {
            this.urlMethod = UrlMethod.FILE;
            this.fileUrl = uri.toString().replace("file:///","");
            File file = new File(this.fileUrl);
            if (!ChatImageCode.CACHE_MAP.containsKey(this.fileUrl)) {
                boolean fileExist = file.exists();
                if (fileExist) {
                    ChatImageHandler.loadFile(this.fileUrl);
                }
                networkHelper.send(this.fileUrl, file, fileExist);
            }
        } else {
            AddChatImageError(this.originalUrl, ChatImageFrame.FrameError.INVALID_URL);
        }
    }

    public String getOriginalUrl() {
        return this.originalUrl;
    }

    public UrlMethod getUrlMethod() {
        return this.urlMethod;
    }

    public String getUrl() {
        switch (urlMethod) {
            case HTTP:
                return httpUrl;
            case FILE:
                return fileUrl;
            default:
                return originalUrl;
        }
    }

    @Override
    public String toString() {
        return this.originalUrl;
    }


    /**
     * Url method
     */
    public enum UrlMethod {
        /**
         * 本地文件 格式
         */
        FILE,
        /**
         * http(s)格式
         */
        HTTP,
        UNKNOWN,
    }

}
