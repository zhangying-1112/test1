package org.example.controller;

import org.example.service.QwenAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final QwenAiService qwenAiService;

    public AiChatController(QwenAiService qwenAiService) {
        this.qwenAiService = qwenAiService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String msg) {
        return qwenAiService.singleChat(msg);
    }
}
