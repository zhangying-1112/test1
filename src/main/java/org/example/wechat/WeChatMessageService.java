package org.example.wechat;

import org.example.service.QwenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WeChatMessageService {

    private static final Logger log = LoggerFactory.getLogger(WeChatMessageService.class);

    private static final String DEFAULT_TEXT_REPLY = "目前仅支持文字消息哦，快跟我说点什么吧~";
    private static final String DEFAULT_ERROR_REPLY = "抱歉，AI助手暂时出现故障，请稍后再试！";

    private final QwenAiService qwenAiService;

    public WeChatMessageService(QwenAiService qwenAiService) {
        this.qwenAiService = qwenAiService;
    }

    /**
     * 处理微信用户文本消息，调用AI生成回复
     */
    public String handleTextMessage(String fromUser, String toUser, String content) {
        log.info("收到微信文本消息 | 用户:{} | 内容:{}", fromUser, content);

        String aiReply = qwenAiService.singleChat(content);

        return buildTextXmlReply(fromUser, toUser, aiReply);
    }

    /**
     * 处理非文本消息（图片、语音、表情包等），返回默认回复
     */
    public String handleNonTextMessage(String fromUser, String toUser, String msgType) {
        log.info("收到非文本消息 | 用户:{} | 类型:{}", fromUser, msgType);

        return buildTextXmlReply(fromUser, toUser, DEFAULT_TEXT_REPLY);
    }

    /**
     * 构建微信文本消息XML回复
     */
    private String buildTextXmlReply(String toUser, String fromUser, String content) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        return "<xml>" +
                "<ToUserName><![CDATA[" + toUser + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + fromUser + "]]></FromUserName>" +
                "<CreateTime>" + timestamp + "</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[" + content + "]]></Content>" +
                "</xml>";
    }
}
