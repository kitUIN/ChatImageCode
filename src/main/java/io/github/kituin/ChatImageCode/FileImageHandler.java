package io.github.kituin.ChatImageCode;


import com.madgag.gif.fmsware.GifDecoder;
import com.sun.imageio.plugins.gif.GIFImageWriter;
import io.github.kituin.ChatImageCode.enums.ChatImageType;
import org.apache.commons.imaging.*;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.gif.GifImageMetadata;
import org.apache.commons.imaging.formats.gif.GifImageMetadataItem;
import org.apache.commons.imaging.formats.gif.GifImageParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.github.kituin.ChatImageCode.ChatImageCodeInstance.LOGGER;
import static io.github.kituin.ChatImageCode.enums.ChatImageType.*;

public class FileImageHandler {


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

    public static ChatImageType getPicType(byte[] is) {
        byte[] b = new byte[4];
        System.arraycopy(is, 0, b, 0, b.length);
        String type_ = bytesToHex(b).toUpperCase();
        if (type_.startsWith("47494638")) {
            return GIF;
        } else if (type_.startsWith("00000100")) {
            return ChatImageType.ICO;
        } else if (type_.startsWith("52494646")) {
            return ChatImageType.WEBP;
        } else {
            return PNG;
        }
    }

    /**
     * 载入Gif
     *
     * @param is  InputStream
     * @param url url
     */
    public static void loadGif(InputStream is, String url) {
        CompletableFuture.supplyAsync(() -> {
            GifDecoder gd = new GifDecoder();
            int status = gd.read(is);
            if (status != GifDecoder.STATUS_OK) {
                ClientStorage.AddImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
                return null;
            }
            ChatImageFrame<?> frame = new ChatImageFrame<>(gd.getFrame(0));
            for (int i = 1; i < gd.getFrameCount(); i++) {
                frame.append(new ChatImageFrame<>(gd.getFrame(i)));
            }
            // 检查gif所有的帧是否加载成功
            if (frame.checkLoad()) {
                ClientStorage.AddImage(url, frame);
            } else {
                ClientStorage.AddImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
            }
            return null;
        });
    }

    public static void loadGif(byte[] is, String url) {
        loadGif(new ByteArrayInputStream(is), url);
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
            ClientStorage.AddImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
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
            ClientStorage.AddImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
        }
    }

    public static void loadFile(byte[] input, String url) {
        loadFile(input, url, false);
    }

    /**
     * 载入图片
     *
     * @param input InputStream
     * @param url   url
     */
    public static void loadFile(byte[] input, String url, Boolean save) {
        try {

            ImageInfo imageInfo = Imaging.getImageInfo(input);
            ImageFormat format = imageInfo.getFormat();
            LOGGER.info("[FileImageHandler][{}]Image Type: {}", url, format);
            if (format == ImageFormats.GIF) {
                loadGif(input, url);
            } else {
                BufferedImage image = Imaging.getBufferedImage(input);
                if (image == null) {
                    ClientStorage.AddImageError(url, ChatImageFrame.FrameError.INVALID_URL);
                    return;
                }
                ClientStorage.AddImage(url, new ChatImageFrame<>(image));
                if (save) {
                    File file = new File(ChatImageConfig.CONFIG.cachePath + "/" + url);
                    if (!file.exists()) {
                        Imaging.writeImage(image, file, format);
                    }
                }
            }

        } catch (IOException ex) {
            ClientStorage.AddImageError(url, ChatImageFrame.FrameError.FILE_LOAD_ERROR);
        }
    }

}
