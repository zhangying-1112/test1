package org.example.command;

import org.example.config.WeChatConfig;
import org.example.service.QwenAiService;
import org.example.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandHandlerTest {

    @Mock
    private WeChatConfig config;

    @Mock
    private QwenAiService qwenAiService;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private CommandHandler commandHandler;

    @BeforeEach
    void setUp() {
        lenient().when(config.getVersion()).thenReturn("1.0.0");
    }

    // ==================== help ====================

    @Test
    @DisplayName("help - 命中")
    void help() {
        assertNotNull(commandHandler.handleCommand("u", "help"));
    }

    @Test
    @DisplayName("HELP - 不区分大小写命中")
    void helpUpperCase() {
        assertNotNull(commandHandler.handleCommand("u", "HELP"));
    }

    // ==================== version ====================

    @Test
    @DisplayName("version - 命中")
    void version() {
        String r = commandHandler.handleCommand("u", "version");
        assertTrue(r.contains("v1.0.0"));
    }

    // ==================== status ====================

    @Test
    @DisplayName("status - 命中")
    void status() {
        when(qwenAiService.checkConnection()).thenReturn(true);
        when(weatherService.checkConnection()).thenReturn(true);
        String r = commandHandler.handleCommand("u", "status");
        assertTrue(r.contains("服务运行状态"));
    }

    // ==================== 天气指令（重点：两种格式都必须命中） ====================

    @Test
    @DisplayName("天气 杭州 - 带空格格式命中天气命令")
    void weatherWithSpace() {
        when(weatherService.queryWeather("杭州")).thenReturn("杭州天气数据");
        assertEquals("杭州天气数据", commandHandler.handleCommand("u", "天气 杭州"));
    }

    @Test
    @DisplayName("天气杭州 - 无空格格式命中天气命令")
    void weatherWithoutSpace() {
        when(weatherService.queryWeather("杭州")).thenReturn("杭州天气数据");
        assertEquals("杭州天气数据", commandHandler.handleCommand("u", "天气杭州"));
    }

    @Test
    @DisplayName("天气 北京 - 带空格")
    void weatherBeijing() {
        when(weatherService.queryWeather("北京")).thenReturn("北京天气");
        assertEquals("北京天气", commandHandler.handleCommand("u", "天气 北京"));
    }

    @Test
    @DisplayName("天气北京 - 无空格")
    void weatherBeijingNoSpace() {
        when(weatherService.queryWeather("北京")).thenReturn("北京天气");
        assertEquals("北京天气", commandHandler.handleCommand("u", "天气北京"));
    }

    @Test
    @DisplayName("天气 上海 - 多个空格也命中")
    void weatherMultipleSpaces() {
        when(weatherService.queryWeather("上海")).thenReturn("上海天气");
        assertEquals("上海天气", commandHandler.handleCommand("u", "天气  上海"));
    }

    @Test
    @DisplayName("天气 哈尔滨 - 三字城市无空格")
    void weatherHarbinNoSpace() {
        when(weatherService.queryWeather("哈尔滨")).thenReturn("哈尔滨天气");
        assertEquals("哈尔滨天气", commandHandler.handleCommand("u", "天气哈尔滨"));
    }

    @Test
    @DisplayName("天气 - 无城市名返回提示")
    void weatherNoCity() {
        assertEquals("请输入城市名称，格式：天气 城市名", commandHandler.handleCommand("u", "天气"));
    }

    @Test
    @DisplayName("天气  - 仅空格无城市返回提示")
    void weatherOnlySpace() {
        assertEquals("请输入城市名称，格式：天气 城市名", commandHandler.handleCommand("u", "天气 "));
    }

    @Test
    @DisplayName("天气 - API失败返回错误提示")
    void weatherApiFail() {
        when(weatherService.queryWeather("北京")).thenReturn(null);
        String r = commandHandler.handleCommand("u", "天气 北京");
        assertTrue(r.contains("查询天气失败"));
    }

    // ==================== 非命令（必须返回null交给AI） ====================

    @Test
    @DisplayName("普通文字 - 返回null")
    void normalText() {
        assertNull(commandHandler.handleCommand("u", "你好"));
    }

    @Test
    @DisplayName("今天天气怎么样 - 不是天气命令，返回null")
    void weatherQuestion() {
        assertNull(commandHandler.handleCommand("u", "今天天气怎么样"));
    }

    @Test
    @DisplayName("null - 返回null")
    void nullContent() {
        assertNull(commandHandler.handleCommand("u", null));
    }

    @Test
    @DisplayName("空白 - 返回null")
    void blankContent() {
        assertNull(commandHandler.handleCommand("u", "   "));
    }

    @Test
    @DisplayName("helpme - 不匹配help")
    void helpme() {
        assertNull(commandHandler.handleCommand("u", "helpme"));
    }
}
