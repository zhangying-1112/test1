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
    public String verify(@RequestParam("signature") String signature,
                         @RequestParam("timestamp") String timestamp,
                         @RequestParam("nonce") String nonce,
                         @RequestParam("echostr") String echostr) {
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

        String fromUser = extractXmlValue(xmlBody, "FromUserName");
        String toUser = extractXmlValue(xmlBody, "ToUserName");
        String msgType = extractXmlValue(xmlBody, "MsgType");
        String content = extractXmlValue(xmlBody, "Content");

        if ("text".equals(msgType)) {
            return weChatMessageService.handleTextMessage(fromUser, toUser, content);
        }

        return weChatMessageService.handleNonTextMessage(fromUser, toUser, msgType);
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
