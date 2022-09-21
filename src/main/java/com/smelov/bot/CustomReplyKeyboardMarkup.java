package com.smelov.bot;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomReplyKeyboardMarkup {
    private ReplyKeyboardMarkup replyKeyboardMarkup;
    private List<KeyboardRow> rowList;
    private KeyboardRow row;

    public ReplyKeyboardMarkup replyKeyboardMarkupForDateYears() {
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        rowList = new ArrayList<>();

        row = new KeyboardRow();
        row.add("2022");
        row.add("2023");
        row.add("2024");
        row.add("2025");
        row.add("2026");
        rowList.add(row);

        row = new KeyboardRow();
        row.add("2027");
        row.add("2028");
        row.add("2029");
        row.add("2030");
        row.add("2031");
        rowList.add(row);

        replyKeyboardMarkup.setKeyboard(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup replyKeyboardMarkupForDateMonth() {
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        rowList = new ArrayList<>();

        row = new KeyboardRow();
        row.add("01");
        row.add("02");
        row.add("03");
        row.add("04");
        row.add("05");
        row.add("06");
        rowList.add(row);

        row = new KeyboardRow();
        row.add("07");
        row.add("08");
        row.add("09");
        row.add("10");
        row.add("11");
        row.add("12");
        rowList.add(row);

        replyKeyboardMarkup.setKeyboard(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup replyKeyboardMarkupForEdit() {
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        rowList = new ArrayList<>();
        row = new KeyboardRow();

        row.add("РЕДАКТИРОВАТЬ ПОЗИЦИЮ");
        rowList.add(row);
        replyKeyboardMarkup.setKeyboard(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup replyKeyboardMarkupForMainMenu() {
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        rowList = new ArrayList<>();

        row = new KeyboardRow();
        row.add("РЕДАКТИРОВАТЬ ПОЗИЦИЮ");
        row.add("УДАЛИТЬ ПОЗИЦИЮ");
        rowList.add(row);

        row = new KeyboardRow();
        row.add("ПОМОЩЬ");
        row.add("ТЕСТ");
        rowList.add(row);


        replyKeyboardMarkup.setKeyboard(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

}
