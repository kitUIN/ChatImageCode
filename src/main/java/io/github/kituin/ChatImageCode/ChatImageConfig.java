package io.github.kituin.ChatImageCode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
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
    public int limitWidth = 125;
    public int limitHeight = 125;
    public int paddingLeft = 1;
    public int paddingRight = 1;
    public int paddingTop = 1;
    public int paddingBottom = 1;
    public int gifSpeed = 3;
    public boolean nsfw = false;
    public boolean cqCode = true;
    public boolean checkImageUri = true;
    public boolean dragUseCicode = true;
    public int timeout = 60;
    public static File configFile;
    public ChatImageConfig() { }
    public void save()
    {
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
        }
        catch(IOException e) {
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
            //e.printStackTrace();
        }
    }
}
