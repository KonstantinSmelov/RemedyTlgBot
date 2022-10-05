//package com.smelov.bot;
//
//import com.smelov.service.BotService;
//import lombok.SneakyThrows;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
//import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
//import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class MyBot extends TelegramLongPollingBot {
//
////    private final BotService botService;
//    private final BotConfig botConfig;
//
//    @SneakyThrows
//    public MyBot(BotService botService, BotConfig botConfig) {
////        this.botService = botService;
//        this.botConfig = botConfig;
//
//        List<BotCommand> commandList = new ArrayList<>();
//        commandList.add(new BotCommand("/by_name", "Список по названию"));
//        commandList.add(new BotCommand("/by_exp_date", "Список по сроку годности"));
//        commandList.add(new BotCommand("/exit", "Выход в главное меню"));
//        execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
//    }
//
//    @Override
//    public String getBotUsername() {
//        return botConfig.getBotName();
//    }
//
//    @Override
//    public String getBotToken() {
//        return botConfig.getToken();
//    }
//
//    @SneakyThrows
//    @Override
//    public void onUpdateReceived(Update update) {
////        BotApiMethod<?> botApiMethod = botService.onUpdateReceived(update);
////        execute(botApiMethod);
//    }
//}
