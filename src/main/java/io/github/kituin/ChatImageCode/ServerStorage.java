package io.github.kituin.ChatImageCode;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerStorage {

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

}
