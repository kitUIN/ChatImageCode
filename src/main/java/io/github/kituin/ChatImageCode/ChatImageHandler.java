package io.github.kituin.ChatImageCode;


import com.madgag.gif.fmsware.GifDecoder;
import net.sf.image4j.codec.ico.ICODecoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.github.kituin.ChatImageCode.ChatImageCode.CACHE_MAP;
import static io.github.kituin.ChatImageCode.ChatImageCode.ChatImageType.*;

public class ChatImageHandler {
    /**
     * 添加进入本地图片序列
     *
     * @param frame 图片
     * @param url   url
     */
    public static void AddChatImage(ChatImageFrame frame, String url) {
        CACHE_MAP.put(url, frame);
    }

    public static void AddChatImage(BufferedImage image, String url) {
        AddChatImage(new ChatImageFrame<>(image), url);
    }

    public static void AddChatImage(InputStream image, String url) {
        AddChatImage(new ChatImageFrame<>(image), url);
    }

    /**
     * 添加进入本地图片序列报错
     *
     * @param url   url
     * @param error 报错
     */
    public static void AddChatImageError(String url, ChatImageFrame.FrameError error) {
        CACHE_MAP.put(url, new ChatImageFrame<>(error));
    }

    /**
     * 载入Gif
     * @param is InputStream
     * @param url url
     */
    public static void loadGif(InputStream is, String url) {
        CompletableFuture.supplyAsync(() -> {
            GifDecoder gd = new GifDecoder();
            int status = gd.read(is);
            if (status != GifDecoder.STATUS_OK) {
                AddChatImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
                return null;
            }
            ChatImageFrame frame = new ChatImageFrame<>(gd.getFrame(0));
            for (int i = 1; i < gd.getFrameCount(); i++) {
                frame.append(new ChatImageFrame<>(gd.getFrame(i)));
            }
            // 检查gif所有的帧是否加载成功
            frame.checkLoad();
            CACHE_MAP.put(url, frame);
            return null;
        });
    }

    public static void loadGif(byte[] is, String url) {
        loadGif(new ByteArrayInputStream(is), url);
    }


    /**
     * 载入图片
     *
     * @param input InputStream
     * @param url   url
     */
    public static void loadFile(byte[] input, String url){
        ChatImageCode.ChatImageType t = getPicType(input);
        if (t == GIF) {
            loadGif(input, url);
        } else if (t == ICO) {
            try {
                List<BufferedImage> images = ICODecoder.read(new ByteArrayInputStream(input));
                AddChatImage(images.get(0), url);
            } catch (IOException ex) {
                AddChatImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
            }
        } else if (t == PNG) {
            try {
                AddChatImage(ImageIO.read(new ByteArrayInputStream(input)), url);
            } catch (IOException ex) {
                AddChatImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
            }
        }
        else{
            AddChatImageError(url, ChatImageFrame.FrameError.IMAGE_TYPE_NOT_SUPPORT);
        }
//      else if (t == WEBP) {
//                    ImageReader reader = ImageIO.getImageReadersByMIMEType("image/webp").next();
//                    WebPReadParam readParam = new WebPReadParam();
//                    readParam.setBypassFiltering(true);
//                    reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(input)));
//                    BufferedImage image = reader.read(0, readParam);
//      }
    }

    public static void loadFile(InputStream input, String url) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        try {
            while ((nRead = input.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException ex) {
            AddChatImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
            return;
        }
        byte[] byteArray = buffer.toByteArray();
        loadFile(byteArray, url);
    }

    public static void loadFile(String url) {
        try {
            // 导入url的时候已经检查过了,所以不需要再检查
            loadFile(Files.newInputStream(Paths.get(url)), url);
        } catch (IOException ex) {
            AddChatImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static ChatImageCode.ChatImageType getPicType(byte[] is) {
        byte[] b = new byte[4];
        System.arraycopy(is, 0, b, 0, b.length);
        String type_ = bytesToHex(b).toUpperCase();
        if (type_.startsWith("47494638")) {
            return GIF;
        } else if (type_.startsWith("00000100")) {
            return ChatImageCode.ChatImageType.ICO;
        } else if (type_.startsWith("52494646")) {
            return ChatImageCode.ChatImageType.WEBP;
        } else {
            return PNG;
        }
    }
}
