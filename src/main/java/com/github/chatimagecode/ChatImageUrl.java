package com.github.chatimagecode;

import com.github.chatimagecode.exception.InvalidChatImageUrlException;

import java.io.File;
import java.io.IOException;

import static com.github.chatimagecode.ChatImageCode.CACHE_MAP;
import static com.github.chatimagecode.ChatImageHandler.loadFile;

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

    public ChatImageUrl(String url) throws InvalidChatImageUrlException {
        this.originalUrl = url;
        cachePathHelper.check();
        if (this.originalUrl.startsWith("http://") || this.originalUrl.startsWith("https://")) {
            this.urlMethod = UrlMethod.HTTP;
            this.httpUrl = this.originalUrl;
            if (!CACHE_MAP.containsKey(this.httpUrl)) {
                boolean f = ChatImageHttpHandler.getInputStream(this.httpUrl);
                if (!f) {
                    throw new InvalidChatImageUrlException("Invalid HTTP URL",
                            InvalidChatImageUrlException.InvalidUrlMode.HttpNotFound);
                }
            }

        } else if (this.originalUrl.startsWith("file:///")) {
            this.urlMethod = UrlMethod.FILE;
            this.fileUrl = this.originalUrl
                    .replace("\\", "\\\\")
                    .replace("file:///", "");
            File file = new File(this.fileUrl);
            if (!CACHE_MAP.containsKey(this.fileUrl)) {
                if (file.exists()) {
                    try{
                        loadFile(this.fileUrl);
                        networkHelper.send(this.fileUrl, file, true);
                    }catch (IOException e){
                        throw new InvalidChatImageUrlException(originalUrl + "<- IOException",
                                InvalidChatImageUrlException.InvalidUrlMode.NotFound);
                    }

                } else {
                    networkHelper.send(this.fileUrl, file, false);
                }
            }
        } else {
            throw new InvalidChatImageUrlException(originalUrl + "<- this url is invalid, Please Check again",
                    InvalidChatImageUrlException.InvalidUrlMode.NotFound);
        }
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
     * Url的类型
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
