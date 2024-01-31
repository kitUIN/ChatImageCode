package io.github.kituin.ChatImageCode.Http;

public interface IProgressListener {

    /**
     * @param bytesRead 已经读取的字节数
     * @param contentLength 响应总长度
     * @param done 是否读取完毕
     */
    void update(long bytesRead,long contentLength,boolean done);
}