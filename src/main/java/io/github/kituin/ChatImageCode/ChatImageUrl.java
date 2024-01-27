package io.github.kituin.ChatImageCode;

import io.github.kituin.ChatImageCode.exception.InvalidChatImageUrlException;
import java.net.URI;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

import static io.github.kituin.ChatImageCode.ChatImageHandler.loadFile;

public class ChatImageUrl {
    private final String originalUrl;
    private String httpUrl;
    private final UrlMethod urlMethod;
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
    private static URI loadUri(String url) throws InvalidChatImageUrlException {
        try {
            return new URI(url.replace("\\", "/"));
        } catch (URISyntaxException e) {
            throw new InvalidChatImageUrlException(url + "<- this url is invalid, Please Check again",
                    InvalidChatImageUrlException.InvalidUrlMode.NotFound);
        }
    }
    public ChatImageUrl(URI uri) throws InvalidChatImageUrlException {
        this.originalUrl = uri.toString();
        cachePathHelper.check();
        if (Objects.equals(uri.getScheme(), "https") ||
                Objects.equals(uri.getScheme(), "http")) {
            this.urlMethod = UrlMethod.HTTP;
            this.httpUrl = uri.toString();
            if (!ChatImageCode.CACHE_MAP.containsKey(this.httpUrl)) {
                boolean f = ChatImageHttpHandler.getInputStream(this.httpUrl);
                if (!f) {
                    throw new InvalidChatImageUrlException("Invalid HTTP URL",
                            InvalidChatImageUrlException.InvalidUrlMode.HttpNotFound);
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
            throw new InvalidChatImageUrlException(originalUrl + "<- this url is invalid, Please Check again",
                    InvalidChatImageUrlException.InvalidUrlMode.NotFound);
        }
    }
    public ChatImageUrl(String url) throws InvalidChatImageUrlException {
        this(loadUri(url));
    }

    public String getOriginalUrl() {
        return this.originalUrl;
    }

    public UrlMethod getUrlMethod() {
        return this.urlMethod;
    }

    public String getUrl() {
        if(urlMethod == UrlMethod.FILE){
            return fileUrl;
        }
        else {
            return httpUrl;
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
    }

}
