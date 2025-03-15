package io.github.kituin.ChatImageCode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 配置项
 * @author kitUIN
 */
public class ChatImageConfig {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping().setLenient().setPrettyPrinting()
            .create();

    public String cachePath = "ChatImageCache";
    /**
     * 最大文件大小(KB) 默认 10MB
     */
    public int MaxFileSize = 10 * 1024;
    /**
     * 显示图片限制宽度
     */
    public int limitWidth = 125;
    /**
     * 显示图片限制长度
     */
    public int limitHeight = 125;
    /**
     * 外框偏移-左
     */
    public int paddingLeft = 1;
    /**
     * 外框偏移-右
     */
    public int paddingRight = 1;
    /**
     * 外框偏移-上
     */
    public int paddingTop = 1;
    /**
     * 外框偏移-下
     */
    public int paddingBottom = 1;
    /**
     * gif播放速度
     */
    public int gifSpeed = 3;
    /**
     * NSFW模式
     */
    public boolean nsfw = false;
    /**
     * 兼容识别cqCode
     */
    public boolean cqCode = true;
    /**
     * 兼容识别链接(如果是图片)
     */
    public boolean checkImageUri = true;
    /**
     * 聊天栏拖入图片功能,关闭则拖入图片无效
     */
    public boolean dragImage = true;
    /**
     * 聊天栏拖入图片 开启时使用CICode,关闭时使用FileUrl
     */
    public boolean dragUseCicode = true;
    /**
     * 聊天栏粘贴图片 自动转换为CICODE
     */
    public boolean pasteImageUseCicode = true;
    /**
     * 实验性文本组件兼容,用于兼容一些特殊情况下的文本组件,如果无法识别CICode,请尝试关闭该功能
     */
    public boolean experimentalTextComponentCompatibility = true;
    /**
     * 网络图片请求超时
     */
    public int timeout = 60;

    public static File configFile;

    public ChatImageConfig() {
    }

    public void save() {
        ChatImageConfig.saveConfig(this);
    }

    public static ChatImageConfig loadConfig() {
        try {
            ChatImageConfig config;
            if (configFile.exists()) {
                String json = IOUtils.toString(new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8));
                config = GSON.fromJson(json, ChatImageConfig.class);
            } else {
                config = new ChatImageConfig();
            }
            saveConfig(config);
            return config;
        } catch (IOException e) {
            // e.printStackTrace();
            return new ChatImageConfig();
        }
    }

    public static void saveConfig(ChatImageConfig config) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8));
            writer.write(GSON.toJson(config));
            writer.close();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
}
