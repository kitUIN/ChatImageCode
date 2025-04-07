package io.github.kituin.ChatImageCode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface IClientAdapter  {

    /**
     * 不同版本的处理材质方法,请自己实现
     *
     * @param image 图片的InputStream
     * @param <T>   不同版本的材质ID类
     * @return 注册好的材质Texture
     * @throws IOException 读取错误
     */
    <T> ChatImageFrame.TextureReader<T> loadTexture(InputStream image) throws IOException;

    /**
     * 从服务器获取文件
     *
     * @param url        url
     */
    void tryGetFileFromServer(String url);

    /**
     * 发送文件到服务器
     *
     * @param url url
     * @param file 文件
     */
    void sendFileToServer(String url, File file);

    /**
     * 获取下载进度
     *
     * @param process 进度
     * @param <T>     抽象类文本
     * @return 进度文本
     */
    <T> T getProcessMessage(int process);
}
