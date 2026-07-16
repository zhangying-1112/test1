package org.example.wechat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestMessageController {

    private static final Logger log = LoggerFactory.getLogger(TestMessageController.class);

    private static final String TEST_USER = "test_browser";
    private static final String TEST_TO = "gh_test";

    private final WeChatMessageService weChatMessageService;

    public TestMessageController(WeChatMessageService weChatMessageService) {
        this.weChatMessageService = weChatMessageService;
    }

    /**
     * 浏览器简易测试接口，直接返回纯文本回复（方便浏览器查看）
     * 用法：http://localhost:8080/test/msg?content=help
     */
    @GetMapping("/test/msg")
    public String testMessage(@RequestParam("content") String content) {
        log.info("[测试接口] content:{}", content);

        try {
            String xmlReply = weChatMessageService.handleTextMessage(TEST_USER, TEST_TO, content);
            String plainText = extractContentFromXml(xmlReply);
            return plainText;
        } catch (Exception e) {
            log.error("[测试接口异常] content:{} | 异常:{}", content, e.getMessage(), e);
            return "服务器异常: " + e.getMessage();
        }
    }

    private String extractContentFromXml(String xml) {
        String cdataStart = "<Content><![CDATA[";
        String cdataEnd = "]]></Content>";
        int start = xml.indexOf(cdataStart);
        if (start == -1) return xml;
        start += cdataStart.length();
        int end = xml.indexOf(cdataEnd, start);
        return end != -1 ? xml.substring(start, end) : xml.substring(start);
    }
}
