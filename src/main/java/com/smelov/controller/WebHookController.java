package com.smelov.controller;

import com.smelov.bot.RemedyBot;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

//ИСПОЛЬЗУЕТСЯ, ТОЛЬКО ЕСЛИ ВЫБРАН ТИП БОТА - WEBHOOK
@RestController
@AllArgsConstructor
public class WebHookController {

    //ИСПОЛЬЗУЕТСЯ, ТОЛЬКО ЕСЛИ ВЫБРАН ТИП БОТА - WEBHOOK
    private final RemedyBot remedyBot;

    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return remedyBot.onWebhookUpdateReceived(update);
    }
}
