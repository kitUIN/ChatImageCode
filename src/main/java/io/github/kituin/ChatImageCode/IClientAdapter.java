package io.github.kituin.ChatImageCode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface IClientAdapter {

    /**
     * 获取配置的超时时间
     * @return 超时时间
     */
    int getTimeOut();

    /**
     * 不同版本的处理材质方法,请自己实现
     * @param image 图片的InputStream
     * @return 注册好的材质Texture
     * @param <T> 不同版本的材质ID类
     * @throws IOException 读取错误
     */
    <T> ChatImageFrame.TextureReader<T> loadTexture(InputStream image) throws IOException;

    /**
     * 发包
     * @param url url
     * @param file 文件
     * @param isToServer 发送到服务器
     */
    void sendToServer(String url, File file, boolean isToServer);

    /**
     * 检查Cache Path是否存在,若不存在则新建
     */
    void checkCachePath();

    /**
     * 最大文件大小(KB)
     * @return 最大文件大小(KB)
     */
    int getMaxFileSize();
    /**
     * 获取下载进度
     * @param process 进度
     * @return 进度文本
     * @param <T> 抽象类文本
     */
    <T> T getProcessMessage(int process);
}
