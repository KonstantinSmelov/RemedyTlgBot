package com.smelov.bot;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomInlineKeyboardMarkup {

    private InlineKeyboardMarkup inlineKeyboardMarkup;
    private List<List<InlineKeyboardButton>> rowList;


    public InlineKeyboardMarkup inlineKeyboardForCancel() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        InlineKeyboardButton cancel_Button = new InlineKeyboardButton();

        cancel_Button.setText("ОТМЕНА");
        cancel_Button.setCallbackData("CANCEL_BUTTON");

        buttonsRow1.add(cancel_Button);
        rowList.add(buttonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardForDosage() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        InlineKeyboardButton mgButton = new InlineKeyboardButton();
        InlineKeyboardButton pcButton = new InlineKeyboardButton();

        mgButton.setText("МИЛЛИГРАММЫ");
        mgButton.setCallbackData("MG_BUTTON");
        pcButton.setText("МИЛЛИЛИТРЫ");
        pcButton.setCallbackData("ML_BUTTON");

        buttonsRow1.add(mgButton);
        buttonsRow1.add(pcButton);
        rowList.add(buttonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardForQuantity() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        InlineKeyboardButton pillsButton = new InlineKeyboardButton();
        InlineKeyboardButton percentButton = new InlineKeyboardButton();

        pillsButton.setText("ТАБЛЕТКИ / ШТ");
        pillsButton.setCallbackData("PILLS_BUTTON");
        percentButton.setText("% ОТ ОБЩЕГО КОЛ-ВА");
        percentButton.setCallbackData("PERCENT_BUTTON");

        buttonsRow1.add(pillsButton);
        buttonsRow1.add(percentButton);
        rowList.add(buttonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }
}
