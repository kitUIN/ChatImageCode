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
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.github.kituin.ChatImageCode.ChatImageCodeInstance.CLIENT_ADAPTER;
import static io.github.kituin.ChatImageCode.ChatImageFrame.FrameError.LOADING_FROM_SERVER;
import static io.github.kituin.ChatImageCode.ChatImageFrame.FrameError.TIMEOUT;
import static io.github.kituin.ChatImageCode.ClientStorage.URL_PROGRESS;

public class ChatImageFrame<T> {
    /**
     * 设定宽度
     */
    private int width;
    /**
     * 设定高度
     */
    private int height;
    /**
     * 原始高度
     */
    private int originalHeight;
    /**
     * 原始宽度
     */
    private int originalWidth;
    /**
     * 不同版本mc的材质ID
     */
    private T id;
    /**
     * 若是gif,则包含除第一帧外的剩余
     */
    private final List<ChatImageFrame<T>> siblings = Lists.newArrayList();
    /**
     * 报错
     */
    private FrameError error = FrameError.LOADING;
    /**
     * gif的帧序号
     */
    private int index = 0;
    /**
     * 若是gif,表示当前帧已经停留序号,等于gifSpeed则下一帧
     */
    private int butter = 0;

    public ChatImageFrame(InputStream image) {
        try {
            TextureReader<T> temp = CLIENT_ADAPTER.loadTexture(image);
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
            TextureReader<T> temp = CLIENT_ADAPTER.loadTexture(new ByteArrayInputStream(os.toByteArray()));
            this.id = temp.getId();
            this.originalWidth = temp.getWidth();
            this.originalHeight = temp.getHeight();
        } catch (IOException e) {
            this.error = FrameError.FILE_LOAD_ERROR;
        }
    }
    public ChatImageFrame(FrameError error) {
        this.error = error;
    }

    public ChatImageFrame<T> append(ChatImageFrame<T> frame) {
        this.siblings.add(frame);
        return this;
    }


    /**
     * 检查所有帧导入完毕
     * @return 是否完毕
     */
    public boolean checkLoad()
    {
        if(id == null){
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
        if (id == null) return false;
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

    /**
     * GIF动图循环
     * @param gifSpeed gif速率
     * @return this
     */
    public ChatImageFrame gifLoop(int gifSpeed) {
        int gifLength = this.siblings.size();
        if (gifLength != 0) {
            if (this.butter == gifSpeed) {
                this.index = ((this.index + 1) % ( gifLength + 1));
                this.butter = 0;
            } else {
                this.butter = ((this.butter + 1) % (gifSpeed + 1));
            }
        }
        return this;
    }

    /**
     * 获取错误信息
     * @param newText 字符串文本的新建
     * @param newTranslatableText 翻译文本的新建
     * @param appendText 抽象类文本的添加
     * @param code CICODE
     * @return 抽象类文本
     * @param <Mutable> 抽象类文本
     */
    public<Mutable>  Mutable getErrorMessage(Function<String, Mutable> newText,
                                              Function<String, Mutable> newTranslatableText,
                                              BiFunction<Mutable, Mutable, Mutable> appendText,
                                              ChatImageCode code) {
        switch (error) {
            case FILE_NOT_FOUND:
                if (code.isSendFromSelf()) {
                    return appendText.apply(newText.apply(code.getUrl()+"\n↑"), newTranslatableText.apply(error.toTranslationKey()));
                } else {
                    return newTranslatableText.apply(code.isTimeout() ? TIMEOUT.toTranslationKey() : LOADING_FROM_SERVER.toTranslationKey());
                }
            case INVALID_IMAGE_URL: case INVALID_URL:
                return appendText.apply(newText.apply(code.getUrl()+"\n↑"), newTranslatableText.apply(error.toTranslationKey()));
            case LOADING:
                if (URL_PROGRESS.containsKey(code.getUrl())) return CLIENT_ADAPTER.getProcessMessage(URL_PROGRESS.get(code.getUrl()));
                if (code.isTimeout()) return newTranslatableText.apply( TIMEOUT.toTranslationKey() );
        }
        return newTranslatableText.apply(error.toTranslationKey());
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
         * 从服务器加载中
         */
        LOADING_FROM_SERVER,
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
         * 无效的图片链接
         */
        INVALID_IMAGE_URL,
        /**
         * 超时
         */
        TIMEOUT,
        /**
         * 无效的链接
         */
        INVALID_URL,
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

    /**
     * 材质读取器,临时类
     * @param <T> 材质ID类
     */
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

}
