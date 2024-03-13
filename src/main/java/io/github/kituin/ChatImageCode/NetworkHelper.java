package io.github.kituin.ChatImageCode;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class NetworkHelper {
    /**
     * 单个包限制
     */
    public static int PacketLimit = 500000; // 500K default:32767;
    public static int MAX_STRING = 532767;
    public static Gson gson = new Gson();


    /**
     * 创建分包
     * @param url url
     * @param file 本地文件
     * @return 返回分包列表
     */
    public static List<String> createFilePacket(String url, File file) {
        try (InputStream input = Files.newInputStream(file.toPath())) {
            List<String> stringList = Lists.newArrayList();
            byte[] byt = new byte[input.available()];
            int status = input.read(byt);
            int index = 0;
            int indexC = 1;
            ChatImageCodeInstance.LOGGER.debug("{} File Size: {}",url,byt.length);
            String base64 = Base64.getEncoder().encodeToString(byt);
            int total = base64.length();
            int count = total / PacketLimit;
            int totalC;
            if (total % PacketLimit == 0) {
                totalC = count;
            } else {
                totalC = count + 1;
            }
            while (index <= total) {
                stringList.add(gson.toJson(new ChatImageIndex(indexC, totalC, url, base64.substring(index, Math.min(index + PacketLimit, total)))));
                index += PacketLimit;
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
        FileImageHandler.loadFile(Base64.getDecoder().decode(builder.toString()), url);
    }

}
