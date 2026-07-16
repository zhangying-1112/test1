package org.example.wechat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wechat")
public class WeChatMessageController {

    private static final Logger log = LoggerFactory.getLogger(WeChatMessageController.class);

    private final WeChatMessageService weChatMessageService;

    public WeChatMessageController(WeChatMessageService weChatMessageService) {
        this.weChatMessageService = weChatMessageService;
    }

    /**
     * 微信公众号服务器验证接口（GET）
     */
    @GetMapping
    public String verify(@RequestParam(value = "signature", required = false) String signature,
                         @RequestParam(value = "timestamp", required = false) String timestamp,
                         @RequestParam(value = "nonce", required = false) String nonce,
                         @RequestParam(value = "echostr", required = false) String echostr) {
        if (signature == null || timestamp == null || nonce == null || echostr == null) {
            return "微信AI助手服务运行中。请通过微信公众号发送消息进行交互。";
        }
        log.info("微信服务器验证请求 | timestamp:{} | nonce:{}", timestamp, nonce);
        // TODO: 实际部署时需校验签名，这里直接返回echostr用于开发测试
        return echostr;
    }

    /**
     * 微信公众号消息接收接口（POST）
     */
    @PostMapping
    public String handleMessage(@RequestBody String xmlBody) {
        log.info("收到微信消息报文: {}", xmlBody);

        try {
            String fromUser = extractXmlValue(xmlBody, "FromUserName");
            String toUser = extractXmlValue(xmlBody, "ToUserName");
            String msgType = extractXmlValue(xmlBody, "MsgType");
            String content = extractXmlValue(xmlBody, "Content");

            if ("text".equals(msgType)) {
                return weChatMessageService.handleTextMessage(fromUser, toUser, content);
            }

            return weChatMessageService.handleNonTextMessage(fromUser, toUser, msgType);
        } catch (Exception e) {
            log.error("[消息处理异常] 异常类型:{} | 异常信息:{}", e.getClass().getSimpleName(), e.getMessage(), e);
            String fromUser = extractXmlValue(xmlBody, "FromUserName");
            String toUser = extractXmlValue(xmlBody, "ToUserName");
            return buildErrorReply(fromUser, toUser);
        }
    }

    /**
     * 构建错误回复XML（异常时不暴露系统细节）
     */
    private String buildErrorReply(String fromUser, String toUser) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        return "<xml>" +
                "<ToUserName><![CDATA[" + toUser + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + fromUser + "]]></FromUserName>" +
                "<CreateTime>" + timestamp + "</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[抱歉，服务器出现异常，请稍后再试。]]></Content>" +
                "</xml>";
    }

    /**
     * 简易XML字段提取（兼容CDATA和普通标签）
     */
    private String extractXmlValue(String xml, String tagName) {
        String cdataStart = "<" + tagName + "><![CDATA[";
        String cdataEnd = "]]></" + tagName + ">";
        int start = xml.indexOf(cdataStart);
        if (start != -1) {
            start += cdataStart.length();
            int end = xml.indexOf(cdataEnd, start);
            return end != -1 ? xml.substring(start, end) : "";
        }
        String tagStart = "<" + tagName + ">";
        String tagEnd = "</" + tagName + ">";
        start = xml.indexOf(tagStart);
        if (start == -1) return "";
        start += tagStart.length();
        int end = xml.indexOf(tagEnd, start);
        return end != -1 ? xml.substring(start, end) : "";
    }
}
