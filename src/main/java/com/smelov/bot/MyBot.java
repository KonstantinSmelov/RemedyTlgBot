package com.smelov.bot;

import com.smelov.service.impl.BotServiceImpl;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import java.util.ArrayList;
import java.util.List;

@Component
public class MyBot extends TelegramLongPollingBot {

    private final BotServiceImpl botServiceImpl;
    private final BotConfig botConfig;

    @SneakyThrows
    public MyBot(BotServiceImpl botService, BotConfig botConfig) {
        this.botServiceImpl = botService;
        this.botConfig = botConfig;

        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/detailed_list", "Детализированный список всех лекарств"));
        commandList.add(new BotCommand("/only_name_list", "Список названий всех лекарств"));
        commandList.add(new BotCommand("/add", "Добавление лекарства в базу"));
        commandList.add(new BotCommand("/del", "Удаление лекарства из базы"));
        commandList.add(new BotCommand("/test1", "test1"));
        commandList.add(new BotCommand("/test2", "test2"));
        commandList.add(new BotCommand("/test3", "test3"));
        execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message = botServiceImpl.onUpdateReceived(update);
        execute(message);
    }
}
