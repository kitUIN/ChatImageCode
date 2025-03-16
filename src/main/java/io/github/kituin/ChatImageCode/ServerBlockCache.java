package io.github.kituin.ChatImageCode;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.kituin.ChatImageCode.NetworkHelper.gson;


public class ServerBlockCache {

    /**
     * 广播列表 URL2List(UUID)
     */
    public HashMap<String, List<String>> userCache = new HashMap<>();

    /**
     * 服务器文件分块缓存时间 Time
     */
    public HashMap<String, Long> blockCacheTime = new HashMap<>();

    /**
     * 服务器文件分块缓存URL2MAP(序号,数据)
     */
    public HashMap<String, HashMap<Integer, String>> blockCache = new HashMap<>();

    /**
     * 文件分块总数记录 URL2Total
     */
    public HashMap<String, Integer> fileCount = new HashMap<>();

    /**
     * Creates or updates a block of image data in the cache for the given title.
     * If the block for the specified title does not exist, a new block is created
     * and the current time is recorded. Then, the image bytes are added to the block
     * with respect to the given index.
     *
     * @param title    An instance of ChatImageIndex containing the index, total count,
     *                 URL, and bytes of the image block.
     * @param imgBytes The image data bytes to be added to the block for the specified index.
     * @return A HashMap representing the blocks of image data for the given title,
     * indexed by their respective indexes.
     */
    public HashMap<Integer, String> createBlock(ChatImageIndex title, String imgBytes) {
        HashMap<Integer, String> blocks = blockCache.containsKey(title.url) ? blockCache.get(title.url) : new HashMap<>();
        if (blocks.isEmpty()) {
            blockCacheTime.put(title.url, System.currentTimeMillis());
        }
        blocks.put(title.index, imgBytes);
        blockCache.put(title.url, blocks);
        fileCount.put(title.url, title.total);
        return blocks;
    }

    /**
     * Retrieves a block of data associated with the specified URL from the cache.
     * The method checks if the complete set of blocks is available for the given URL in the cache.
     * If the cache contains all blocks for the URL, the method returns the full data block map.
     * If any block is missing or the URL does not exist in the cache, the method returns null.
     *
     * @param url the URL for which the data block map is requested
     * @return the complete map of data blocks associated with the specified URL,
     * or null if the URL is not found or the data blocks are incomplete in the cache
     */
    public HashMap<Integer, String> getBlock(String url) {
        if (blockCache.containsKey(url) && fileCount.containsKey(url)) {
            HashMap<Integer, String> list = blockCache.get(url);
            Integer total = fileCount.get(url);
            if (total == list.size()) return list;
        }
        return null;
    }

    /**
     * Retrieves the base64-encoded image data associated with the specified URL.
     * This method collects all blocks of image data from the cache, concatenates them,
     * and returns the complete image as a base64 string.
     *
     * @param url the URL for which the base64-encoded image data is requested
     * @return the concatenated base64 string of the image if all blocks are available,
     * or null if the blocks are incomplete or the URL is not found in the cache
     */
    public String getImage(String url) {
        HashMap<Integer, String> blocks = getBlock(url);
        StringBuilder base64Img = new StringBuilder();
        if (blocks != null) {
            for (int i = 1; i <= blocks.size(); i++) {
                base64Img.append(gson.fromJson(blocks.get(i), ChatImageIndex.class).bytes);
            }
            return base64Img.toString();
        }
        return null;
    }


    /**
     * Attempts to add a user identifier (UUID) to the cache associated with a specified URL.
     * If the URL already exists in the cache, the UUID is added to the list of identifiers.
     * If the URL does not exist in the cache, a new entry with the URL and UUID is created.
     *
     * @param url  the unique identifier for which the user is associated
     * @param uuid the user identifier to be added to the cache
     */
    public void tryAddUser(String url, String uuid) {
        List<String> names = userCache.containsKey(url) ? userCache.get(url) : Lists.newArrayList();
        names.add(uuid);
        userCache.put(url, names);
    }

    /**
     * Retrieves a list of user identifiers associated with the given URL from the cache.
     * If the URL exists in the cache, the list of user identifiers is cleared from the cache
     * and returned. If the URL does not exist in the cache, null is returned.
     *
     * @param url the URL for which the user identifiers are requested
     * @return a list of user identifiers associated with the specified URL, or null if the URL is not found in the cache
     */
    public List<String> getUsers(String url) {
        if (userCache.containsKey(url)) {
            List<String> names = userCache.get(url);
            userCache.put(url, Lists.newArrayList());
            return names;
        }
        return null;
    }

    /**
     * Clears cache entries older than the specified timestamp. This method iterates through the
     * block cache time entries, identifies keys with timestamps less than the provided value,
     * and removes associated entries from various caches.
     *
     * @param timestamp the cutoff timestamp indicating which entries should be considered
     *                  outdated and therefore cleared from the caches
     */
    public void clear(long timestamp) {
        List<String> keys = Lists.newArrayList();
        for (Map.Entry<String, Long> entry : blockCacheTime.entrySet()) {
            if (entry.getValue() < timestamp) {
                keys.add(entry.getKey());
            }
        }
        for (String key : keys) {
            blockCache.remove(key);
            blockCacheTime.remove(key);
            fileCount.remove(key);
            userCache.remove(key);
        }
    }

    /**
     * Clears all entries from the userCache, blockCache, blockCacheTime, and fileCount.
     * This operation will remove all cached data related to users, image blocks,
     * and associated metadata, resetting the state of the ServerBlockCache instance.
     */
    public void clear() {
        userCache.clear();
        blockCache.clear();
        blockCacheTime.clear();
        fileCount.clear();
    }
}
