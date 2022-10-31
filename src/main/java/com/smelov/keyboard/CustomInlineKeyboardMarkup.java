package com.smelov.keyboard;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomInlineKeyboardMarkup {

    private InlineKeyboardMarkup inlineKeyboardMarkup;
    private List<List<InlineKeyboardButton>> rowList;

    public InlineKeyboardMarkup inlineKeyboardForEdit() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow2 = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow3 = new ArrayList<>();
        InlineKeyboardButton name_Button = new InlineKeyboardButton();
        InlineKeyboardButton dosage_Button = new InlineKeyboardButton();
        InlineKeyboardButton exp_date_Button = new InlineKeyboardButton();
        InlineKeyboardButton quantity_Button = new InlineKeyboardButton();
        InlineKeyboardButton photo_Button = new InlineKeyboardButton();
        InlineKeyboardButton mainMenu_Button = new InlineKeyboardButton();

        name_Button.setText("НАЗВАНИЕ");
        name_Button.setCallbackData("EDIT_NAME_BUTTON");
        dosage_Button.setText("ДОЗИРОВКА");
        dosage_Button.setCallbackData("EDIT_DOSAGE_BUTTON");
        exp_date_Button.setText("СРОК ГОДНОСТИ");
        exp_date_Button.setCallbackData("EDIT_EXP_DATE_BUTTON");
        quantity_Button.setText("КОЛ-ВО");
        quantity_Button.setCallbackData("EDIT_QUANTITY_BUTTON");
        photo_Button.setText("ФОТО");
        photo_Button.setCallbackData("EDIT_PHOTO_BUTTON");
        mainMenu_Button.setText("ВЫХОД");
        mainMenu_Button.setCallbackData("MAIN_MENU_BUTTON");

        buttonsRow1.add(name_Button);
        buttonsRow1.add(dosage_Button);
        buttonsRow2.add(exp_date_Button);
        buttonsRow2.add(quantity_Button);
        buttonsRow3.add(photo_Button);
        buttonsRow3.add(mainMenu_Button);

        rowList.add(buttonsRow1);
        rowList.add(buttonsRow2);
        rowList.add(buttonsRow3);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardForDetailView() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow2 = new ArrayList<>();
        InlineKeyboardButton edit_Button = new InlineKeyboardButton();
        InlineKeyboardButton del_Button = new InlineKeyboardButton();
        InlineKeyboardButton photo_Button = new InlineKeyboardButton();
        InlineKeyboardButton mainMenu_Button = new InlineKeyboardButton();

        edit_Button.setText("ИЗМЕНИТЬ");
        edit_Button.setCallbackData("EDIT_FROM_DETAIL_BUTTON");
        del_Button.setText("УДАЛИТЬ");
        del_Button.setCallbackData("DEL_FROM_DETAIL_BUTTON");
        mainMenu_Button.setText("ВЫХОД");
        mainMenu_Button.setCallbackData("MAIN_MENU_BUTTON");

        buttonsRow1.add(edit_Button);
        buttonsRow1.add(del_Button);
        buttonsRow2.add(mainMenu_Button);

        rowList.add(buttonsRow1);
        rowList.add(buttonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardForAllMedsList() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow2 = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow3 = new ArrayList<>();
        InlineKeyboardButton edit_Button = new InlineKeyboardButton();
        InlineKeyboardButton del_Button = new InlineKeyboardButton();
        InlineKeyboardButton add_Button = new InlineKeyboardButton();
        InlineKeyboardButton detail_Button = new InlineKeyboardButton();

        edit_Button.setText("ИЗМЕНИТЬ");
        edit_Button.setCallbackData("EDIT_BUTTON");
        del_Button.setText("УДАЛИТЬ");
        del_Button.setCallbackData("DEL_BUTTON");
        add_Button.setText("ДОБАВИТЬ");
        add_Button.setCallbackData("ADD_BUTTON");
        detail_Button.setText("ПОКАЗАТЬ ДЕТАЛИ");
        detail_Button.setCallbackData("DETAIL_BUTTON");

        buttonsRow1.add(edit_Button);
        buttonsRow1.add(del_Button);
        buttonsRow2.add(add_Button);
        buttonsRow2.add(detail_Button);
        rowList.add(buttonsRow1);
        rowList.add(buttonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardForMainMenu() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        InlineKeyboardButton mainMenu_Button = new InlineKeyboardButton();

        mainMenu_Button.setText("ГЛАВНОЕ МЕНЮ");
        mainMenu_Button.setCallbackData("MAIN_MENU_BUTTON");

        buttonsRow1.add(mainMenu_Button);
        rowList.add(buttonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardForSkip() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        InlineKeyboardButton skip_Button = new InlineKeyboardButton();

        skip_Button.setText("ПРОПУСТИТЬ ЭТОТ ШАГ");
        skip_Button.setCallbackData("SKIP_BUTTON");

        buttonsRow1.add(skip_Button);
        rowList.add(buttonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

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

    public InlineKeyboardMarkup inlineKeyboardForReturn() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        InlineKeyboardButton return_Button = new InlineKeyboardButton();

        return_Button.setText("ПРЕДЫДУЩЕЕ МЕНЮ");
        return_Button.setCallbackData("RETURN_BUTTON");

        buttonsRow1.add(return_Button);
        rowList.add(buttonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardForDosage() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow2 = new ArrayList<>();
        InlineKeyboardButton mgButton = new InlineKeyboardButton();
        InlineKeyboardButton pcButton = new InlineKeyboardButton();
        InlineKeyboardButton smtButton = new InlineKeyboardButton();

        mgButton.setText("МИЛЛИГРАММЫ");
        mgButton.setCallbackData("MG_BUTTON");
        pcButton.setText("МИЛЛИЛИТРЫ");
        pcButton.setCallbackData("ML_BUTTON");
        smtButton.setText("ИНОЕ");
        smtButton.setCallbackData("SMT_BUTTON");

        buttonsRow1.add(mgButton);
        buttonsRow1.add(pcButton);
        buttonsRow2.add(smtButton);
        rowList.add(buttonsRow1);
        rowList.add(buttonsRow2);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardForQuantity() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        InlineKeyboardButton pillsButton = new InlineKeyboardButton();
        InlineKeyboardButton percentButton = new InlineKeyboardButton();

        pillsButton.setText("ШТ.");
        pillsButton.setCallbackData("PILLS_BUTTON");
        percentButton.setText("% ОТ ОБЩЕГО КОЛ-ВА");
        percentButton.setCallbackData("PERCENT_BUTTON");

        buttonsRow1.add(pillsButton);
        buttonsRow1.add(percentButton);
        rowList.add(buttonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup inlineKeyboardForOk() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        rowList = new ArrayList<>();
        List<InlineKeyboardButton> buttonsRow1 = new ArrayList<>();
        InlineKeyboardButton ok_Button = new InlineKeyboardButton();

        ok_Button.setText("ПОНЯЛ");
        ok_Button.setCallbackData("OK_BUTTON");

        buttonsRow1.add(ok_Button);
        rowList.add(buttonsRow1);
        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }
}
