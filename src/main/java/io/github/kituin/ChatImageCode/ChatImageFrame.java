package io.github.kituin.ChatImageCode;

import com.google.common.collect.Lists;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ChatImageFrame<T> {
    private int width, height;
    /**
     * 原始高度
     */
    private int originalHeight;
    /**
     * 原始宽度
     */
    private int originalWidth;
    private T id;
    private final List<ChatImageFrame<T>> siblings = Lists.newArrayList();
    public static TextureHelper<?> textureHelper;
    private FrameError error = FrameError.LOADING;
    private int index = 0;
    private int butter = 0;

    public ChatImageFrame(InputStream image) {
        try {
            TextureReader<T> temp = (TextureReader<T>) textureHelper.loadTexture(image);
            this.id = temp.getId();
            this.originalWidth = temp.getWidth();
            this.originalHeight = temp.getHeight();
        } catch (IOException e) {
            this.error = FrameError.FILE_LOAD_ERROR;
        }

    }

    public ChatImageFrame(BufferedImage image) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            TextureReader<T> temp = (TextureReader<T>) textureHelper.loadTexture(new ByteArrayInputStream(os.toByteArray()));
            this.id = temp.getId();
            this.originalWidth = temp.getWidth();
            this.originalHeight = temp.getHeight();
        } catch (IOException e) {
            this.error = FrameError.FILE_LOAD_ERROR;
        }

    }
    public ChatImageFrame<T> append(ChatImageFrame<T> frame) {
        this.siblings.add(frame);
        return this;
    }

    public ChatImageFrame(FrameError error) {
        this.error = error;
    }

    /**
     * 检查所有帧导入完毕
     * @return 是否完毕
     */
    public boolean checkLoad()
    {
        if(id==null){
            return false;
        }
        for (int i = 0; i < this.siblings.size(); i++) {
            if(this.siblings.get(i).getId()==null){
                return false;
            }
        }
        return true;
    }

    /**
     * 载入图片
     * @param limitWidth limitWidth
     * @param limitHeight limitHeight
     * @return 载入成功返回true, 失败则为false
     */
    public boolean loadImage(int limitWidth, int limitHeight) {
        if (id == null) {
            return false;
        }
        if (index == 0) {
            limitSize(limitWidth, limitHeight);
        } else {
            this.siblings.get(index - 1).limitSize(limitWidth, limitHeight);
        }
        return true;
    }

    /**
     * limit display image width and height
     *
     * @param limitWidth limitWidth
     * @param limitHeight limitHeight
     */
    public void limitSize(int limitWidth, int limitHeight) {
        this.width = originalWidth;
        this.height = originalHeight;
        BigDecimal b = new BigDecimal((float) originalHeight / originalWidth);
        double hx = b.setScale(2, RoundingMode.HALF_UP).doubleValue();
        if (width > limitWidth) {
            width = limitWidth;
            height = (int) (limitWidth * hx);
        }
        if (height > limitHeight) {
            height = limitHeight;
            width = (int) (limitHeight / hx);
        }
    }

    /**
     * 如果Id==null,再使用该方法
     * @return {@link FrameError}
     */
    public FrameError getError() {
        return error;
    }

    public T getId() {
        if (index == 0) {
            return id;
        } else {
            return this.siblings.get(index - 1).getId();
        }
    }

    public int getHeight() {
        return height;
    }

    public List<ChatImageFrame<T>> getSiblings() {
        return siblings;
    }

    public int getWidth() {
        return width;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getButter() {
        return butter;
    }

    public void setButter(int butter) {
        this.butter = butter;
    }

    public enum FrameError {
        /**
         * 找不到该文件
         */
        FILE_NOT_FOUND,
        /**
         * 找不到图片材质ID
         */
        ID_NOT_FOUND,
        /**
         * 文件加载错误
         */
        FILE_LOAD_ERROR,
        /**
         * 未知错误
         */
        OTHER_ERROR,
        /**
         * 加载中
         */
        LOADING,
        /**
         * 服务器加载图片失败
         */
        SERVER_FILE_LOAD_ERROR,
        /**
         * 无效的CICode格式
         */
        ILLEGAL_CICODE_ERROR,
        /**
         * 不受支持的图片格式
         */
        IMAGE_TYPE_NOT_SUPPORT,
        /**
         * 图片过大
         */
        FILE_TOO_LARGE;
        /**
         * 快速转换为翻译键
         * @return "{小写name}.chatimage.exception"
         */
        public String toTranslationKey()
        {
            String name = name();
            return name.toLowerCase() + ".chatimage.exception";
        }
    }

    public static class TextureReader<T> {
        public T id;
        public int width;
        public int height;

        /**
         * 材质读取器,临时类
         * @param id 不同版本的材质ID
         * @param width 宽度
         * @param height 高度
         */
        public TextureReader(T id, int width, int height) {
            this.id = id;
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public T getId() {
            return id;
        }
    }

    @FunctionalInterface
    public interface TextureHelper<T> {
        /**
         * 不同版本的处理材质方法
         * @param image 图片的InputStream
         * @return 注册好的材质Texture
         * @throws IOException 读取错误
         */
        TextureReader<T> loadTexture(InputStream image) throws IOException;
    }
}
