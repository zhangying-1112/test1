package org.example.controller;

import org.example.service.QwenAiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AiChatController.class)
class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QwenAiService qwenAiService;

    // ==================== 正常场景测试 ====================

    @Test
    @DisplayName("TC-11: 正常请求 - 返回AI回复")
    void chat_normalRequest() throws Exception {
        when(qwenAiService.singleChat("你好")).thenReturn("你好！有什么可以帮你的？");

        mockMvc.perform(get("/api/ai/chat").param("msg", "你好"))
                .andExpect(status().isOk())
                .andExpect(content().string("你好！有什么可以帮你的？"));
    }

    @Test
    @DisplayName("TC-12: 中文消息 - 正常返回")
    void chat_chineseMessage() throws Exception {
        when(qwenAiService.singleChat("今天天气怎么样")).thenReturn("今天天气不错哦");

        mockMvc.perform(get("/api/ai/chat").param("msg", "今天天气怎么样"))
                .andExpect(status().isOk())
                .andExpect(content().string("今天天气不错哦"));
    }

    @Test
    @DisplayName("TC-13: 英文消息 - 正常返回")
    void chat_englishMessage() throws Exception {
        when(qwenAiService.singleChat("Hello")).thenReturn("Hello! How can I help?");

        mockMvc.perform(get("/api/ai/chat").param("msg", "Hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello! How can I help?"));
    }

    // ==================== 异常场景测试 ====================

    @Test
    @DisplayName("TC-14: AI服务异常 - 返回友好提示")
    void chat_serviceException() throws Exception {
        when(qwenAiService.singleChat(anyString())).thenReturn("抱歉，AI助手暂时出现故障，请稍后再试！");

        mockMvc.perform(get("/api/ai/chat").param("msg", "测试"))
                .andExpect(status().isOk())
                .andExpect(content().string("抱歉，AI助手暂时出现故障，请稍后再试！"));
    }

    // ==================== 参数测试 ====================

    @Test
    @DisplayName("TC-15: 缺少msg参数 - 返回400错误")
    void chat_missingParam() throws Exception {
        mockMvc.perform(get("/api/ai/chat"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-16: 特殊字符参数 - 正常处理")
    void chat_specialCharsParam() throws Exception {
        when(qwenAiService.singleChat("!@#$%")).thenReturn("收到特殊字符");

        mockMvc.perform(get("/api/ai/chat").param("msg", "!@#$%"))
                .andExpect(status().isOk())
                .andExpect(content().string("收到特殊字符"));
    }
}
