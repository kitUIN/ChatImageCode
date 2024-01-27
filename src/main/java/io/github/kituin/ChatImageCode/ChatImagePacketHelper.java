package io.github.kituin.ChatImageCode;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.kituin.ChatImageCode.ChatImageHandler.loadFile;

public class ChatImagePacketHelper {
    /**
     * 单个包限制
     */
    public static int Limit = 10000;
    /**
     * 服务器文件分块缓存URL2MAP(序号,数据)
     */
    public static HashMap<String, HashMap<Integer, String>> SERVER_BLOCK_CACHE = new HashMap<>();

    /**
     * 文件分块总数记录 URL2Total
     */
    public static HashMap<String, Integer> FILE_COUNT_MAP = new HashMap<>();
    /**
     * 广播列表 URL2List(UUID)
     */
    public static HashMap<String, List<String>> USER_CACHE_MAP = new HashMap<>();
    /**
     * 用户本地分块缓存
     */
    public static HashMap<String, HashMap<Integer, ChatImageIndex>> CLIENT_CACHE_MAP = new HashMap<>();
    public static Gson gson = new Gson();


    /**
     * 创建分包
     * @param url url
     * @param file 本地文件
     */
    public static List<String> createFilePacket(String url, File file) {
        try (InputStream input = Files.newInputStream(file.toPath())) {
            List<String> stringList = Lists.newArrayList();
            byte[] byt = new byte[input.available()];
            int status = input.read(byt);
            int index = 0;
            int indexC = 1;
            String base64 = Base64.getEncoder().encodeToString(byt);
            int total = base64.length();
            int count = total / Limit;
            int totalC;
            if (total % Limit == 0) {
                totalC = count;
            } else {
                totalC = count + 1;
            }
            while (index <= total) {
                stringList.add(gson.toJson(new ChatImageIndex(indexC, totalC, url, base64.substring(index, Math.min(index + Limit, total)))));
                index += Limit;
                indexC ++;
            }
            return stringList;
        } catch (IOException e) {
            return Lists.newArrayList();
        }

    }

    /**
     * 合并文件分块
     * @param url url
     * @param blocks blocks
     */
    public static void mergeFileBlocks(String url, Map<Integer,ChatImageIndex> blocks) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= blocks.size(); i++) {
            builder.append(blocks.get(i).bytes);
        }
        ChatImageHandler.loadFile(Base64.getDecoder().decode(builder.toString()), url);
    }
}
