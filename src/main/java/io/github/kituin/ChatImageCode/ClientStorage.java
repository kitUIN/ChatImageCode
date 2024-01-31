package io.github.kituin.ChatImageCode;

import java.util.HashMap;

import static io.github.kituin.ChatImageCode.ChatImageCodeInstance.LOGGER;
/**
 * 客户端存储
 *
 * @author kitUIN
 */
public class ClientStorage {
    /**
     * 下载进度
     */
    public static HashMap<String, Integer> URL_PROGRESS = new HashMap<>();
    /**
     * 图片缓存
     */
    private static final HashMap<String, ChatImageFrame> images = new HashMap<>();
    public static void AddImage(String url, ChatImageFrame frame) {
        images.put(url, frame);
    }
    public static boolean ContainImage(String url) {
        return images.containsKey(url);
    }

    /**
     * 图片存在并且已经加载入材质中
     * @param url 图片Url
     * @return 是否存在
     */
    public static boolean ContainImageAndCheck(String url) {
        if(images.containsKey(url)) return images.get(url).getId() != null;
        return false;
    }
    public static ChatImageFrame getImage(String url) {
        if(ContainImage(url))
            return images.get(url);
        else
            return null;
    }

    /**
     * 添加进入本地图片序列报错
     *
     * @param url   url
     * @param error 报错
     */
    public static void AddImageError(String url, ChatImageFrame.FrameError error) {
        LOGGER.debug("AddImageError: {} -> {}",error,url);
        AddImage(url, new ChatImageFrame<>(error));
    }
    /**
     * NSFW列表
     */
    private static final HashMap<String, Integer> nsfws = new HashMap<>();
    public static void AddNsfw(String url, Integer value) {
        nsfws.put(url, value);
    }
    public static boolean ContainNsfw(String url) {
        return nsfws.containsKey(url);
    }
    public static Integer getNsfw(String url) {
        if(ContainNsfw(url))
            return nsfws.get(url);
        else
            return null;
    }
    /**
     * 用户本地分块缓存
     */
    public static HashMap<String, HashMap<Integer, ChatImageIndex>> CLIENT_CACHE_MAP = new HashMap<>();

}
