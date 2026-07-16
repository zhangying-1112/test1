package org.example.wechat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeChatMessageController.class)
class WeChatMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeChatMessageService weChatMessageService;

    // ==================== 微信服务器验证测试 ====================

    @Test
    @DisplayName("TC-17: 微信服务器验证 - 返回echostr")
    void verify_success() throws Exception {
        mockMvc.perform(get("/wechat")
                        .param("signature", "test-signature")
                        .param("timestamp", "1234567890")
                        .param("nonce", "test-nonce")
                        .param("echostr", "echo-test-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("echo-test-123"));
    }

    @Test
    @DisplayName("TC-18: 缺少验证参数 - 返回友好提示而非400")
    void verify_missingParams() throws Exception {
        mockMvc.perform(get("/wechat"))
                .andExpect(status().isOk())
                .andExpect(content().string("微信AI助手服务运行中。请通过微信公众号发送消息进行交互。"));
    }

    // ==================== 文本消息处理测试 ====================

    @Test
    @DisplayName("TC-19: 正常文本消息 - 调用handleTextMessage并返回AI回复XML")
    void handleMessage_textMessage() throws Exception {
        String xmlBody = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[你好]]></Content>" +
                "<MsgId>1234567890123456</MsgId>" +
                "</xml>";

        String expectedReply = "<xml>" +
                "<ToUserName><![CDATA[oUser123]]></ToUserName>" +
                "<FromUserName><![CDATA[gh_test]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[你好！有什么可以帮你的？]]></Content>" +
                "</xml>";

        when(weChatMessageService.handleTextMessage("oUser123", "gh_test", "你好"))
                .thenReturn(expectedReply);

        mockMvc.perform(post("/wechat")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xmlBody))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedReply));
    }

    // ==================== 非文本消息处理测试 ====================

    @Test
    @DisplayName("TC-20: 图片消息 - 调用handleNonTextMessage返回默认回复")
    void handleMessage_imageMessage() throws Exception {
        String xmlBody = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[image]]></MsgType>" +
                "<PicUrl><![CDATA[http://example.com/pic.jpg]]></PicUrl>" +
                "<MediaId><![CDATA[media_id_123]]></MediaId>" +
                "<MsgId>1234567890123456</MsgId>" +
                "</xml>";

        String expectedReply = "<xml>" +
                "<ToUserName><![CDATA[oUser123]]></ToUserName>" +
                "<FromUserName><![CDATA[gh_test]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[目前仅支持文字消息哦，快跟我说点什么吧~]]></Content>" +
                "</xml>";

        when(weChatMessageService.handleNonTextMessage("oUser123", "gh_test", "image"))
                .thenReturn(expectedReply);

        mockMvc.perform(post("/wechat")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xmlBody))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedReply));
    }

    @Test
    @DisplayName("TC-21: 语音消息 - 调用handleNonTextMessage")
    void handleMessage_voiceMessage() throws Exception {
        String xmlBody = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[voice]]></MsgType>" +
                "<MediaId><![CDATA[voice_media_id]]></MediaId>" +
                "<Format><![CDATA[amr]]></Format>" +
                "<MsgId>1234567890123456</MsgId>" +
                "</xml>";

        when(weChatMessageService.handleNonTextMessage("oUser123", "gh_test", "voice"))
                .thenReturn("default-reply");

        mockMvc.perform(post("/wechat")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xmlBody))
                .andExpect(status().isOk())
                .andExpect(content().string("default-reply"));
    }

    @Test
    @DisplayName("TC-22: 空消息体 - 正常处理不报错")
    void handleMessage_emptyBody() throws Exception {
        mockMvc.perform(post("/wechat")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-23: 特殊字符文本消息 - 正常调用handleTextMessage")
    void handleMessage_specialChars() throws Exception {
        String xmlBody = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[!@#$%^&*()]]></Content>" +
                "<MsgId>1234567890123456</MsgId>" +
                "</xml>";

        String expectedReply = "<xml>" +
                "<ToUserName><![CDATA[oUser123]]></ToUserName>" +
                "<FromUserName><![CDATA[gh_test]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[收到特殊字符]]></Content>" +
                "</xml>";

        when(weChatMessageService.handleTextMessage("oUser123", "gh_test", "!@#$%^&*()"))
                .thenReturn(expectedReply);

        mockMvc.perform(post("/wechat")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xmlBody))
                .andExpect(status().isOk());
    }

    // ==================== 异常处理测试 ====================

    @Test
    @DisplayName("TC-24: 消息处理异常 - 返回友好错误提示而非系统异常")
    void handleMessage_exception() throws Exception {
        String xmlBody = "<xml>" +
                "<ToUserName><![CDATA[gh_test]]></ToUserName>" +
                "<FromUserName><![CDATA[oUser123]]></FromUserName>" +
                "<CreateTime>1234567890</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[测试异常]]></Content>" +
                "<MsgId>1234567890123456</MsgId>" +
                "</xml>";

        when(weChatMessageService.handleTextMessage(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("模拟系统异常"));

        mockMvc.perform(post("/wechat")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xmlBody))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("抱歉，服务器出现异常")));
    }
}
