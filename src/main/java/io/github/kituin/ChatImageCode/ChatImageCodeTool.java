package io.github.kituin.ChatImageCode;

import com.google.common.collect.Lists;
import io.github.kituin.ChatImageCode.exception.InvalidChatImageCodeException;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatImageCodeTool {
    public static final Pattern cicodesPattern = Pattern.compile("(\\[\\[CICode,(.*?)\\]\\])");
    public static final Pattern cqPattern = Pattern.compile("\\[CQ:image,(.*?)\\]");
    public static final Pattern uriPattern = Pattern.compile("(https?:\\/\\/|file:\\/\\/\\/)([^:<>\\\"]*\\/)([^:<>\\\"]*)\\.(png!thumbnail|bmp|jpe?g|gif)");

    /**
     *  构建消息
     * @param texts 切分后的文本列表
     * @param appendString 添加普通文本方法
     * @param appendCode 添加CICODE方法
     */
    public static void buildMsg(List<Object> texts,
                                Consumer<String> appendString,
                                Consumer<ChatImageCode> appendCode)
    {
        texts.forEach((obj)->{
            if(obj instanceof String){
                appendString.accept((String)obj);
            }else if(obj instanceof ChatImageCode){
                appendCode.accept((ChatImageCode)obj);
            }
        });
    }
    /**
     * 切分文本中的CICODE
     * @param checkedText 检测的文本
     * @param isSelf 是否为自身发送
     * @param allString 是否检测到CICODE
     * @param logger 日志
     * @return 切分后的文本
     */
    public static List<Object> sliceMsg (String checkedText,
                                         boolean isSelf,
                                         ChatImageBoolean allString,
                                         Consumer<InvalidChatImageCodeException> logger) {
        Matcher m = cicodesPattern.matcher(checkedText);
        List<Object> res = Lists.newArrayList();
        int lastPosition = 0;
        allString.setValue(true);
        while (m.find()) {
            try {
                ChatImageCode image = ChatImageCode.of(m.group(), isSelf);
                if(m.start() != 0) res.add(checkedText.substring(lastPosition,m.start()));
                lastPosition = m.end();
                res.add(image);
                allString.setValue(false);
            } catch (InvalidChatImageCodeException e) {
                logger.accept(e);
            }
        }
        if(lastPosition != checkedText.length()) res.add(checkedText.substring(lastPosition));
        return res;
    }
    /**
     * 检测文本中存在的CQCode,若存在则转为CICODE
     * @param checkedText 检测的文本
     * @return 新文本
     */
    public static String checkCQCode(String checkedText) {
        Matcher cqm = cqPattern.matcher(checkedText);
        while (cqm.find()) {
            String[] cqArgs = cqm.group(1).split(",");
            String cq_Url = "";
            for(int i=0; i<cqArgs.length; i++){
                String[] cqParams = cqArgs[i].split("=");
                if("url".equals(cqParams[0])){
                    cq_Url = cqParams[1];
                    break;
                }
            }
            if(!cq_Url.isEmpty()){
                checkedText = checkedText.substring(0,cqm.start()) + String.format("[[CICode,url=%s]]", cq_Url) + checkedText.substring(cqm.end());
            }
        }
        return checkedText;
    }

    /**
     * 检测文本中存在的图片链接,若存在则转为CICODE
     * @param checkedText 检测的文本
     * @return 新文本
     */
    public static String checkImageUri(String checkedText) {
        Matcher matcher = uriPattern.matcher(checkedText);
        while (matcher.find()) {
            String url = matcher.group();
            if(!url.isEmpty()){
                checkedText = checkedText.substring(0,matcher.start()) + String.format("[[CICode,url=%s]]", url) + checkedText.substring(matcher.end());
            }
        }
        return checkedText;
    }
}
