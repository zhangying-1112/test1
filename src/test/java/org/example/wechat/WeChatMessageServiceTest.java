package org.example.wechat;

import org.example.command.CommandHandler;
import org.example.service.QwenAiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeChatMessageServiceTest {

    @Mock
    private QwenAiService qwenAiService;

    @Mock
    private CommandHandler commandHandler;

    @InjectMocks
    private WeChatMessageService weChatMessageService;

    // ==================== 命令优先级：命令命中后不进AI ====================

    @Test
    @DisplayName("help - 命中命令，不进AI")
    void helpCommand() {
        when(commandHandler.handleCommand("u", "help")).thenReturn("帮助");
        String r = weChatMessageService.handleTextMessage("u", "gh", "help");
        assertTrue(r.contains("帮助"));
        verifyNoInteractions(qwenAiService);
    }

    @Test
    @DisplayName("天气杭州 - 命中天气命令，不进AI")
    void weatherCommand() {
        when(commandHandler.handleCommand("u", "天气杭州")).thenReturn("天气数据");
        String r = weChatMessageService.handleTextMessage("u", "gh", "天气杭州");
        assertTrue(r.contains("天气数据"));
        verifyNoInteractions(qwenAiService);
    }

    @Test
    @DisplayName("天气 杭州 - 命中天气命令，不进AI")
    void weatherWithSpaceCommand() {
        when(commandHandler.handleCommand("u", "天气 杭州")).thenReturn("天气数据");
        String r = weChatMessageService.handleTextMessage("u", "gh", "天气 杭州");
        assertTrue(r.contains("天气数据"));
        verifyNoInteractions(qwenAiService);
    }

    // ==================== AI对话：无命令时走AI ====================

    @Test
    @DisplayName("普通文字 - 走AI")
    void normalText() {
        when(commandHandler.handleCommand("u", "你好")).thenReturn(null);
        when(qwenAiService.singleChat("你好")).thenReturn("AI回复");
        String r = weChatMessageService.handleTextMessage("u", "gh", "你好");
        assertTrue(r.contains("AI回复"));
        verify(qwenAiService).singleChat("你好");
    }

    @Test
    @DisplayName("AI异常 - 返回友好提示")
    void aiException() {
        when(commandHandler.handleCommand("u", "x")).thenReturn(null);
        when(qwenAiService.singleChat("x")).thenThrow(new RuntimeException("err"));
        String r = weChatMessageService.handleTextMessage("u", "gh", "x");
        assertTrue(r.contains("故障"));
        assertFalse(r.contains("RuntimeException"));
    }

    // ==================== XML结构 ====================

    @Test
    @DisplayName("XML结构完整")
    void xmlStructure() {
        when(commandHandler.handleCommand("u", "t")).thenReturn(null);
        when(qwenAiService.singleChat("t")).thenReturn("r");
        String r = weChatMessageService.handleTextMessage("u", "gh", "t");
        assertTrue(r.startsWith("<xml>"));
        assertTrue(r.endsWith("</xml>"));
        assertTrue(r.contains("<ToUserName><![CDATA[gh]]></ToUserName>"));
        assertTrue(r.contains("<FromUserName><![CDATA[u]]></FromUserName>"));
    }

    // ==================== 非文本消息 ====================

    @Test
    @DisplayName("图片消息 - 默认回复")
    void imageMessage() {
        String r = weChatMessageService.handleNonTextMessage("u", "gh", "image");
        assertTrue(r.contains("仅支持文字消息"));
        verifyNoInteractions(qwenAiService);
    }
}
