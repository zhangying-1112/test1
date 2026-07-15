package org.example.wechat;

import org.example.service.QwenAiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeChatMessageServiceTest {

    @Mock
    private QwenAiService qwenAiService;

    @InjectMocks
    private WeChatMessageService weChatMessageService;

    // ==================== 文本消息测试 ====================

    @Test
    @DisplayName("TC-23: 正常文本消息 - 调用AI并返回正确XML")
    void handleTextMessage_normalCase() {
        when(qwenAiService.singleChat("你好")).thenReturn("你好！有什么可以帮你的？");

        String result = weChatMessageService.handleTextMessage("oUser123", "gh_test", "你好");

        assertTrue(result.contains("<ToUserName><![CDATA[oUser123]]></ToUserName>"));
        assertTrue(result.contains("<FromUserName><![CDATA[gh_test]]></FromUserName>"));
        assertTrue(result.contains("<MsgType><![CDATA[text]]></MsgType>"));
        assertTrue(result.contains("<Content><![CDATA[你好！有什么可以帮你的？]]></Content>"));
        verify(qwenAiService).singleChat("你好");
    }

    @Test
    @DisplayName("TC-24: AI返回长回复 - XML格式正确")
    void handleTextMessage_longReply() {
        String longReply = "这是一段很长的AI回复".repeat(20);
        when(qwenAiService.singleChat("详细解释")).thenReturn(longReply);

        String result = weChatMessageService.handleTextMessage("oUser123", "gh_test", "详细解释");

        assertTrue(result.contains("<Content><![CDATA[" + longReply + "]]></Content>"));
    }

    @Test
    @DisplayName("TC-25: AI返回空字符串 - XML格式正确")
    void handleTextMessage_emptyReply() {
        when(qwenAiService.singleChat("测试")).thenReturn("");

        String result = weChatMessageService.handleTextMessage("oUser123", "gh_test", "测试");

        assertTrue(result.contains("<Content><![CDATA[]]></Content>"));
    }

    @Test
    @DisplayName("TC-26: XML结构完整性验证")
    void handleTextMessage_xmlStructure() {
        when(qwenAiService.singleChat("测试")).thenReturn("回复内容");

        String result = weChatMessageService.handleTextMessage("oUser123", "gh_test", "测试");

        assertTrue(result.startsWith("<xml>"));
        assertTrue(result.endsWith("</xml>"));
        assertTrue(result.contains("<CreateTime>"));
        assertTrue(result.contains("<MsgType><![CDATA[text]]></MsgType>"));
    }

    @Test
    @DisplayName("TC-27: 用户ID映射正确 - ToUserName和FromUserName互换")
    void handleTextMessage_userMapping() {
        when(qwenAiService.singleChat("测试")).thenReturn("回复");

        String result = weChatMessageService.handleTextMessage("oUser123", "gh_test", "测试");

        assertTrue(result.contains("<ToUserName><![CDATA[gh_test]]></ToUserName>"));
        assertTrue(result.contains("<FromUserName><![CDATA[oUser123]]></FromUserName>"));
    }

    @Test
    @DisplayName("TC-28: 特殊字符输入 - CDATA转义正确")
    void handleTextMessage_specialCharsInput() {
        when(qwenAiService.singleChat("<script>")).thenReturn("回复含<>&符号");

        String result = weChatMessageService.handleTextMessage("oUser123", "gh_test", "<script>");

        assertTrue(result.contains("<Content><![CDATA[回复含<>&符号]]></Content>"));
    }

    @Test
    @DisplayName("TC-29: Unicode字符输入 - 正常处理")
    void handleTextMessage_unicodeInput() {
        when(qwenAiService.singleChat("你好世界🌍")).thenReturn("Hello World 🌍");

        String result = weChatMessageService.handleTextMessage("oUser123", "gh_test", "你好世界🌍");

        assertTrue(result.contains("Hello World 🌍"));
    }

    // ==================== 非文本消息测试 ====================

    @Test
    @DisplayName("TC-30: 图片消息 - 返回默认回复话术")
    void handleNonTextMessage_image() {
        String result = weChatMessageService.handleNonTextMessage("oUser123", "gh_test", "image");

        assertTrue(result.contains("<Content><![CDATA[目前仅支持文字消息哦，快跟我说点什么吧~]]></Content>"));
        assertTrue(result.contains("<ToUserName><![CDATA[gh_test]]></ToUserName>"));
        assertTrue(result.contains("<FromUserName><![CDATA[oUser123]]></FromUserName>"));
        verifyNoInteractions(qwenAiService);
    }

    @Test
    @DisplayName("TC-31: 语音消息 - 返回默认回复话术")
    void handleNonTextMessage_voice() {
        String result = weChatMessageService.handleNonTextMessage("oUser123", "gh_test", "voice");

        assertTrue(result.contains("目前仅支持文字消息哦"));
        verifyNoInteractions(qwenAiService);
    }

    @Test
    @DisplayName("TC-32: 视频消息 - 返回默认回复话术")
    void handleNonTextMessage_video() {
        String result = weChatMessageService.handleNonTextMessage("oUser123", "gh_test", "video");

        assertTrue(result.contains("目前仅支持文字消息哦"));
        verifyNoInteractions(qwenAiService);
    }

    @Test
    @DisplayName("TC-33: 非文本消息 - XML结构完整")
    void handleNonTextMessage_xmlStructure() {
        String result = weChatMessageService.handleNonTextMessage("oUser123", "gh_test", "image");

        assertTrue(result.startsWith("<xml>"));
        assertTrue(result.endsWith("</xml>"));
        assertTrue(result.contains("<CreateTime>"));
        assertTrue(result.contains("<MsgType><![CDATA[text]]></MsgType>"));
    }
}
