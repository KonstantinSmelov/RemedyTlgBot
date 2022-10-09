package com.smelov.bot;

import com.smelov.config.BotConfig;
import com.smelov.service.impl.UpdateHandlerImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class RemedyBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final UpdateHandlerImpl updateHandler;

    @SneakyThrows
    public RemedyBot(BotConfig botConfig, @Lazy UpdateHandlerImpl updateHandler) {
        this.botConfig = botConfig;
        this.updateHandler = updateHandler;

        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/by_name", "Список по названию"));
        commandList.add(new BotCommand("/by_exp_date", "Список по сроку годности"));
        commandList.add(new BotCommand("/exit", "Выход в главное меню"));
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

    @Override
    public void onUpdateReceived(Update update) {
        updateHandler.onUpdateReceived(update);
    }
}
