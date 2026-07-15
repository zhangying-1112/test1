package org.example.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QwenAiServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private QwenAiService qwenAiService;

    private QwenAiService buildService() {
        return new QwenAiService(chatClientBuilder);
    }

    // ==================== 正常场景测试 ====================

    @Test
    @DisplayName("TC-01: 正常对话 - 返回AI回复内容")
    void singleChat_normalCase() {
        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        ChatClient.CallResponseSpec mockCallResponse = mock(ChatClient.CallResponseSpec.class);

        lenient().when(chatClient.prompt("你好")).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenReturn(mockCallResponse);
        lenient().when(mockCallResponse.content()).thenReturn("你好！有什么可以帮助你的吗？");

        QwenAiService service = buildService();
        String result = service.singleChat("你好");

        assertEquals("你好！有什么可以帮助你的吗？", result);
        verify(chatClient).prompt("你好");
    }

    @Test
    @DisplayName("TC-02: 长文本输入 - 正常返回")
    void singleChat_longInput() {
        String longInput = "这是一段很长的用户输入内容".repeat(50);

        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        ChatClient.CallResponseSpec mockCallResponse = mock(ChatClient.CallResponseSpec.class);

        lenient().when(chatClient.prompt(longInput)).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenReturn(mockCallResponse);
        lenient().when(mockCallResponse.content()).thenReturn("这是AI的回复");

        QwenAiService service = buildService();
        String result = service.singleChat(longInput);

        assertEquals("这是AI的回复", result);
        verify(chatClient).prompt(longInput);
    }

    @Test
    @DisplayName("TC-03: 英文输入 - 正常返回英文回复")
    void singleChat_englishInput() {
        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        ChatClient.CallResponseSpec mockCallResponse = mock(ChatClient.CallResponseSpec.class);

        lenient().when(chatClient.prompt("Hello")).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenReturn(mockCallResponse);
        lenient().when(mockCallResponse.content()).thenReturn("Hello! How can I help you?");

        QwenAiService service = buildService();
        String result = service.singleChat("Hello");

        assertEquals("Hello! How can I help you?", result);
    }

    // ==================== 异常场景测试 ====================

    @Test
    @DisplayName("TC-04: API密钥错误 - 返回默认友好提示")
    void singleChat_apiKeyError() {
        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        lenient().when(chatClient.prompt("测试")).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenThrow(new RuntimeException("Invalid API key"));

        QwenAiService service = buildService();
        String result = service.singleChat("测试");

        assertEquals("抱歉，AI助手暂时出现故障，请稍后再试！", result);
    }

    @Test
    @DisplayName("TC-05: 限流异常 - 返回默认友好提示")
    void singleChat_rateLimit() {
        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        lenient().when(chatClient.prompt("测试")).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenThrow(new RuntimeException("Rate limit exceeded"));

        QwenAiService service = buildService();
        String result = service.singleChat("测试");

        assertEquals("抱歉，AI助手暂时出现故障，请稍后再试！", result);
    }

    @Test
    @DisplayName("TC-06: 超时异常 - 返回默认友好提示")
    void singleChat_timeout() {
        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        lenient().when(chatClient.prompt("测试")).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenThrow(new RuntimeException("Connection timed out"));

        QwenAiService service = buildService();
        String result = service.singleChat("测试");

        assertEquals("抱歉，AI助手暂时出现故障，请稍后再试！", result);
    }

    @Test
    @DisplayName("TC-07: 网络异常 - 返回默认友好提示")
    void singleChat_networkError() {
        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        lenient().when(chatClient.prompt("测试")).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenThrow(new RuntimeException("Connection refused"));

        QwenAiService service = buildService();
        String result = service.singleChat("测试");

        assertEquals("抱歉，AI助手暂时出现故障，请稍后再试！", result);
    }

    @Test
    @DisplayName("TC-08: 空指针异常 - 返回默认友好提示")
    void singleChat_nullPointer() {
        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        lenient().when(chatClient.prompt("测试")).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenThrow(new NullPointerException("Unexpected null"));

        QwenAiService service = buildService();
        String result = service.singleChat("测试");

        assertEquals("抱歉，AI助手暂时出现故障，请稍后再试！", result);
    }

    // ==================== 边界值测试 ====================

    @Test
    @DisplayName("TC-09: 空字符串输入 - 正常处理")
    void singleChat_emptyInput() {
        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        ChatClient.CallResponseSpec mockCallResponse = mock(ChatClient.CallResponseSpec.class);

        lenient().when(chatClient.prompt("")).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenReturn(mockCallResponse);
        lenient().when(mockCallResponse.content()).thenReturn("请输入一些内容");

        QwenAiService service = buildService();
        String result = service.singleChat("");

        assertEquals("请输入一些内容", result);
    }

    @Test
    @DisplayName("TC-10: 特殊字符输入 - 正常处理")
    void singleChat_specialChars() {
        String specialInput = "!@#$%^&*()_+{}|:\"<>?";

        ChatClient.PromptSpec mockPromptSpec = mock(ChatClient.PromptSpec.class);
        ChatClient.CallResponseSpec mockCallResponse = mock(ChatClient.CallResponseSpec.class);

        lenient().when(chatClient.prompt(specialInput)).thenReturn(mockPromptSpec);
        lenient().when(mockPromptSpec.call()).thenReturn(mockCallResponse);
        lenient().when(mockCallResponse.content()).thenReturn("收到特殊字符");

        QwenAiService service = buildService();
        String result = service.singleChat(specialInput);

        assertEquals("收到特殊字符", result);
    }
}
