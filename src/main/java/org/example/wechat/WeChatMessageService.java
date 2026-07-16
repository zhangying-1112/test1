package org.example.wechat;

import org.example.command.CommandHandler;
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
    private final CommandHandler commandHandler;

    public WeChatMessageService(QwenAiService qwenAiService, CommandHandler commandHandler) {
        this.qwenAiService = qwenAiService;
        this.commandHandler = commandHandler;
    }

    /**
     * 处理微信用户文本消息
     * 优先级：固定命令 > AI大模型对话
     */
    public String handleTextMessage(String fromUser, String toUser, String content) {
        log.info("收到微信文本消息 | 用户:{} | 内容:{}", fromUser, content);

        // ====== 第一步：优先判断固定命令，命中则直接return，绝不进入AI ======
        String commandReply = commandHandler.handleCommand(fromUser, content);
        if (commandReply != null) {
            log.info("命令命中，直接回复 | 用户:{}", fromUser);
            return buildTextXmlReply(fromUser, toUser, commandReply);
        }

        // ====== 第二步：未命中任何命令，才走AI大模型对话 ======
        log.info("无命令命中，进入AI对话 | 用户:{} | 内容:{}", fromUser, content);
        try {
            String aiReply = qwenAiService.singleChat(content);
            return buildTextXmlReply(fromUser, toUser, aiReply);
        } catch (Exception e) {
            log.error("[AI回复异常] 用户:{} | 内容:{} | 异常:{}", fromUser, content, e.getMessage(), e);
            return buildTextXmlReply(fromUser, toUser, DEFAULT_ERROR_REPLY);
        }
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
