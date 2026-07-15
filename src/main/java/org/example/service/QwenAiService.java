package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class QwenAiService {

    private static final Logger log = LoggerFactory.getLogger(QwenAiService.class);

    private static final String DEFAULT_REPLY = "抱歉，AI助手暂时出现故障，请稍后再试！";

    private final ChatClient chatClient;

    public QwenAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是微信智能AI助手，回答简洁、友好、通俗易懂，适合日常聊天回复，不输出冗长内容。")
                .build();
    }

    /**
     * 单轮对话调用阿里通义千问
     */
    public String singleChat(String userMsg) {
        long start = System.currentTimeMillis();
        try {
            String result = chatClient.prompt(userMsg).call().content();
            long cost = System.currentTimeMillis() - start;
            log.info("[通义千问调用成功] 耗时:{}ms | 用户:{} | 回复:{}", cost, userMsg, result);
            return result;
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[通义千问调用失败] 耗时:{}ms | 用户:{} | 异常:{}", cost, userMsg, e.getMessage(), e);
            return DEFAULT_REPLY;
        }
    }
}
