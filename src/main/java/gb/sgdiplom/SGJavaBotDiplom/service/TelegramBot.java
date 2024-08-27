package gb.sgdiplom.SGJavaBotDiplom.service;

import com.vdurmont.emoji.EmojiParser;
import gb.sgdiplom.SGJavaBotDiplom.config.BotConfig;
import gb.sgdiplom.SGJavaBotDiplom.model.User;
import gb.sgdiplom.SGJavaBotDiplom.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;


    final BotConfig config;
    static final String HELP_TEXT = "Бот создан для дипломного проекта студента С.А. Грубова. " +
            "Команды бота вы можете посмотреть в меню слева.";

    public TelegramBot(UserRepository userRepository, BotConfig config) {
        this.config = config;
        List<BotCommand> menuCommands = new ArrayList<>();
        menuCommands.add(new BotCommand("/start", "Начать общение"));
        menuCommands.add(new BotCommand("/register", "Вступить в Профсоюз"));
        menuCommands.add(new BotCommand("/law", "Получить юридическую консультацию"));
        menuCommands.add(new BotCommand("/data", "Найти свой профсоюз или профобъединение"));
        menuCommands.add(new BotCommand("/safety", "Сообщить о несчастном случае на производстве"));
        menuCommands.add(new BotCommand("/help", "Информация о боте"));
        try {
            this.execute(new SetMyCommands(menuCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot command list: " + e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/register":
                    register(chatId);
                    break;
                case "Вступить в Профсоюз":
                    register(chatId);
                    break;
                case "/law":
                    lawHelp(chatId);
                    break;
                case "Получить юридическую консультацию":
                    lawHelp(chatId);
                    break;
                case "/data":
                    dataUnions(chatId);
                    break;
                case "Найти свой профсоюз или профобъединение":
                    dataUnions(chatId);
                    break;
                case "/safety":
                    safety(chatId);
                    break;
                case "Сообщить о несчастном случае на производстве":
                    safety(chatId);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Command not found");
                    log.info(update.getMessage().getText() + " - необработанный запрос от пользователя <" + update.getMessage().getChat().getFirstName() + ">");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("YES_BUTTON")) {
                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setMessageId((int) messageId);
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("Error bot: " + e.getMessage());
                }
            } else if (callbackData.equals("NO_BUTTON")) {
                TextReceived(chatId);
                EditMessageText message = new EditMessageText();
                message.setChatId(String.valueOf(chatId));
                message.setMessageId((int) messageId);
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    log.error("Error bot: " + e.getMessage());
                }
            }
        }
    }

    private void safety(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Хотите сообщить о несчастном случае на производстве?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да!");
        yesButton.setUrl("https://fnpr.ru/instrukciya/");

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData("NO_BUTTON");

        rowInline.add(yesButton);
        rowInline.add(noButton);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        log.info("Запрос на сообщение о несчастном случае");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error bot: " + e.getMessage());
        }
    }

    private void dataUnions(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Хотите найти свой профсоюз или профобъединение?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да!");
        yesButton.setUrl("https://fnpr.ru/structure/");

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData("NO_BUTTON");

        rowInline.add(yesButton);
        rowInline.add(noButton);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        log.info("Запрос на поиск профсоюза");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error bot: " + e.getMessage());
        }
    }


    private void lawHelp(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Хотите получить юридическую консультацию?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да!");
        yesButton.setUrl("https://fnpr.ru/legal/");

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData("NO_BUTTON");

        rowInline.add(yesButton);
        rowInline.add(noButton);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        log.info("Запрос на получение юридической консультации");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error bot: " + e.getMessage());
        }
    }


    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Хотите вступить в профсоюз?");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да!");
        yesButton.setUrl("https://fnpr.ru/union/");
        yesButton.setCallbackData("YES_BUTTON");

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData("NO_BUTTON");

        rowInline.add(yesButton);
        rowInline.add(noButton);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        log.info("Запрос на вступление в профсоюз");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error bot: " + e.getMessage());
        }
    }

    private void registerUser(Message msg) {
        if (userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegistredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            log.info("Создана запись о пользователе: " + user);
        }
    }


    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Привет, " + name + ", рад с вами познакомиться " + ":blush:\n" +
                "Этот бот создан для дипломного проекта студента С.А. Грубова.\n" +
                "Команды бота: \n\n" +
                "/start — Начать общение\n\n" +
                "/register — Вступить в Профсоюз\n\n" +
                "/law — Получить юридическую консультацию\n\n" +
                "/data — Найти свой профсоюз или профобъединение\n\n" +
                "/safety — Сообщить о несчастном случае на производстве\n\n" +
                "/help — Информация о боте\n\n" +
                "Также команды бота доступны в меню.");

        log.info("Ответ на запрос пользователя " + name);
        sendMessage(chatId, answer);
    }

    private void TextReceived(long chatId) {
        String answer = EmojiParser.parseToUnicode("Команды бота: \n\n" +
                "/start — Начать общение\n\n" +
                "/register — Вступить в Профсоюз\n\n" +
                "/law — Получить юридическую консультацию\n\n" +
                "/data — Найти свой профсоюз или профобъединение\n\n" +
                "/safety — Сообщить о несчастном случае на производстве\n\n" +
                "/help — Информация о боте\n\n" +
                "Также команды бота доступны в меню.");

        log.info("Ответ на запрос ");
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Вступить в Профсоюз");

        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("Получить юридическую консультацию");

        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("Найти свой профсоюз или профобъединение");

        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("Сообщить о несчастном случае на производстве");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);


        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error bot: " + e.getMessage());
        }
    }
}