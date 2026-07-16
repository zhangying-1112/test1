package org.example.command;

import org.example.config.WeChatConfig;
import org.example.service.QwenAiService;
import org.example.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommandHandler {

    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private static final String HELP_TEXT = """
            你好！我是微信智能AI助手，支持以下命令：

            基础命令：
            help     - 显示本帮助信息
            version  - 查看当前程序版本号
            status   - 查看服务运行状态

            功能命令：
            天气 城市名  - 查询指定城市天气（如：天气 北京）

            其他消息将由AI智能回复，快来和我聊天吧！""";

    private final WeChatConfig config;
    private final QwenAiService qwenAiService;
    private final WeatherService weatherService;
    private final long startTime;

    public CommandHandler(WeChatConfig config, QwenAiService qwenAiService, WeatherService weatherService) {
        this.config = config;
        this.qwenAiService = qwenAiService;
        this.weatherService = weatherService;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 处理命令消息，命中命令返回回复内容，未命中返回null
     *
     * 优先级：help > version > status > 天气 > null(交给AI)
     */
    public String handleCommand(String fromUser, String content) {
        if (content == null || content.isBlank()) {
            log.info("[命令分发] 内容为空，跳过 | 用户:{}", fromUser);
            return null;
        }

        String trimmed = content.trim();

        // ====== 1. help 精确匹配（不区分大小写） ======
        if ("help".equalsIgnoreCase(trimmed)) {
            log.info("[命令命中] help | 用户:{}", fromUser);
            return HELP_TEXT;
        }

        // ====== 2. version 精确匹配（不区分大小写） ======
        if ("version".equalsIgnoreCase(trimmed)) {
            log.info("[命令命中] version | 用户:{}", fromUser);
            return "微信AI助手 v" + config.getVersion();
        }

        // ====== 3. status 精确匹配（不区分大小写） ======
        if ("status".equalsIgnoreCase(trimmed)) {
            log.info("[命令命中] status | 用户:{}", fromUser);
            return handleStatusCommand(fromUser);
        }

        // ====== 4. 天气指令匹配（兼容"天气 城市"和"天气城市"两种格式） ======
        if (trimmed.startsWith("天气")) {
            String city = trimmed.substring(2).trim(); // "天气"是2个字符，取后面的部分
            if (city.isEmpty()) {
                log.info("[命令命中] 天气(无城市) | 用户:{}", fromUser);
                return "请输入城市名称，格式：天气 城市名";
            }
            log.info("[命令命中] 天气 | 用户:{} | 城市:{}", fromUser, city);
            return handleWeatherCommand(fromUser, city);
        }

        // ====== 未命中任何命令，返回null交给AI ======
        log.info("[命令分发] 无匹配，交由AI | 用户:{} | 内容:{}", fromUser, trimmed);
        return null;
    }

    /**
     * 处理status命令
     */
    private String handleStatusCommand(String fromUser) {
        log.info("[status] 检测各组件状态 | 用户:{}", fromUser);

        long uptimeMs = System.currentTimeMillis() - startTime;
        String uptime = formatUptime(uptimeMs);

        boolean aiStatus = qwenAiService.checkConnection();
        boolean weatherStatus = weatherService.checkConnection();

        StringBuilder sb = new StringBuilder();
        sb.append("服务运行状态\n");
        sb.append("-------------------\n");
        sb.append("程序版本: v").append(config.getVersion()).append("\n");
        sb.append("大模型连接: ").append(aiStatus ? "正常" : "异常").append("\n");
        sb.append("天气API连接: ").append(weatherStatus ? "正常" : "异常").append("\n");
        sb.append("服务运行时间: ").append(uptime);

        log.info("[status] 完成 | 用户:{} | AI:{} | 天气:{} | 运行:{}",
                fromUser, aiStatus ? "正常" : "异常", weatherStatus ? "正常" : "异常", uptime);
        return sb.toString();
    }

    /**
     * 处理天气命令 - 调用心知天气API
     */
    private String handleWeatherCommand(String fromUser, String city) {
        log.info("[天气查询] 开始 | 用户:{} | 城市:{}", fromUser, city);

        String weatherInfo = weatherService.queryWeather(city);

        if (weatherInfo == null) {
            log.warn("[天气查询] 失败 | 用户:{} | 城市:{}", fromUser, city);
            return "抱歉，查询天气失败，请稍后再试或检查城市名是否正确。";
        }

        log.info("[天气查询] 成功 | 用户:{} | 城市:{}", fromUser, city);
        return weatherInfo;
    }

    private String formatUptime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "天" + (hours % 24) + "小时" + (minutes % 60) + "分钟";
        if (hours > 0) return hours + "小时" + (minutes % 60) + "分钟";
        if (minutes > 0) return minutes + "分钟" + (seconds % 60) + "秒";
        return seconds + "秒";
    }
}
