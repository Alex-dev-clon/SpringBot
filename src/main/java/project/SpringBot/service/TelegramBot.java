package project.SpringBot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import project.SpringBot.config.BotConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    static final String HELP_TEXT = "Бот помощник для отправления праздничных сообщений. Список праздников ниже или в меню";

    //Записываем в массивы варианты поздравлений для каждого праздника
    List<String> listOfBirthDay = readFile("src/main/resources/HappyBirthDay.txt");
    List<String> listOfWedding = readFile("src/main/resources/Wedding.txt");
    List<String> listOfNewYear = readFile("src/main/resources/NewYear.txt");
    List<String> listOfFebruary23 = readFile("src/main/resources/February23.txt");
    List<String> listOfMarch8 = readFile("src/main/resources/March8.txt");
    List<String> listOfMay9 = readFile("src/main/resources/May9.txt");


    public TelegramBot(BotConfig config) {
        this.config = config;
        //Создаем список команд поддерживаемых ботом
        List<BotCommand> listOfCommand = new ArrayList<>();
        listOfCommand.add(new BotCommand("/start", "Запуск бота"));
        listOfCommand.add(new BotCommand("/help", "Информация о боте"));
        listOfCommand.add(new BotCommand("/birthday", "День рождения"));
        listOfCommand.add(new BotCommand("/wedding", "Свадьба"));
        listOfCommand.add(new BotCommand("/newyear", "Новый год"));
        listOfCommand.add(new BotCommand("/february23", "23 февраля"));
        listOfCommand.add(new BotCommand("/mart8", "8 марта"));
        listOfCommand.add(new BotCommand("/may9", "9 мая"));
        //Добавляем команды в меню бота
        try {
            this.execute(new SetMyCommands(listOfCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка в создании меню: " + e.getMessage());
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

        Random random = new Random();

        //Проверяем принимаемый аргумент
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            //Определяем полученную команду
            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;

                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;

                case "/birthDay":
                    sendMessage(chatId, listOfBirthDay.get(random.nextInt(listOfBirthDay.size())));
                    break;

                case "/wedding":
                    sendMessage(chatId, listOfWedding.get(random.nextInt(listOfWedding.size())));
                    break;

                case "/newyear":
                    sendMessage(chatId, listOfNewYear.get(random.nextInt(listOfNewYear.size())));
                    break;

                case "/february23":
                    sendMessage(chatId, listOfFebruary23.get(random.nextInt(listOfFebruary23.size())));
                    break;

                case "/mart8":
                    sendMessage(chatId, listOfMarch8.get(random.nextInt(listOfMarch8.size())));
                    break;

                case "/may9":
                    sendMessage(chatId, listOfMay9.get(random.nextInt(listOfMay9.size())));
                    break;

                default:
                    sendMessage(chatId, "Команда неизвестна");
            }
            //Если была использована одна из кнопок
        } else if (update.hasCallbackQuery()) {

            String callBackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals("HELP")) {
                sendMessage(chatId, HELP_TEXT);

            } else if (callBackData.equals("BIRTHDAY")) {
                sendMessage(chatId, listOfBirthDay.get(random.nextInt(listOfBirthDay.size())));

            } else if (callBackData.equals("WEDDING")) {
                sendMessage(chatId, listOfWedding.get(random.nextInt(listOfWedding.size())));

            } else if (callBackData.equals("NEWYEAR")) {
                sendMessage(chatId, listOfNewYear.get(random.nextInt(listOfNewYear.size())));

            } else if (callBackData.equals("FEBRUARY23")) {
                sendMessage(chatId, listOfFebruary23.get(random.nextInt(listOfFebruary23.size())));
            } else if (callBackData.equals("MART8")) {
                sendMessage(chatId, listOfMarch8.get(random.nextInt(listOfMarch8.size())));
            } else if (callBackData.equals("MAY9")) {
                sendMessage(chatId, listOfMay9.get(random.nextInt(listOfMay9.size())));
            }
        }
    }

    //Стартовый метод с приветствием пользователя
    private void startCommandReceived(long chatId, String name) {
        String answer = "Привет " + name + "!";
        log.info("Replied to User {}", name);
        sendMessage(chatId, answer);
        try {
            this.execute(new SetChatMenuButton(String.valueOf(chatId), new MenuButton() {
                @Override
                public String getType() {
                    return "some";
                }
            }));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    //Метод для отправки сообщения пользователю
    private void sendMessage(long chatId, String textToSend) {

        SendMessage message = new SendMessage(String.valueOf(chatId), textToSend);
        message.setReplyMarkup(createButtons());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error in class TelegramBot: {}", e.getMessage());
        }
    }

    //Метод для чтения вариантов поздравления из файла
    private List<String> readFile(String filename) {
        List<String> list = new ArrayList<>();
        try {
            File file = new File(filename);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    list.add(line);
                }
            }
            br.close();
            fr.close();
        } catch (Exception e) {
            log.error("Ошибка чтения файла: {}", e.getMessage());
        }
        return list;
    }

    //Метод для создания кнопок под сообщением
    private InlineKeyboardMarkup createButtons () {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("/may9");
        inlineKeyboardButton1.setCallbackData("MAY9");

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("/birthDay");
        inlineKeyboardButton2.setCallbackData("BIRTHDAY");

        rowInline1.add(inlineKeyboardButton1);
        rowInline1.add(inlineKeyboardButton2);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton3.setText("/wedding");
        inlineKeyboardButton3.setCallbackData("WEDDING");

        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton4.setText("/newYear");
        inlineKeyboardButton4.setCallbackData("NEWYEAR");

        rowInline2.add(inlineKeyboardButton3);
        rowInline2.add(inlineKeyboardButton4);

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton5 = new InlineKeyboardButton();
        inlineKeyboardButton5.setText("/february23");
        inlineKeyboardButton5.setCallbackData("FEBRUARY23");

        InlineKeyboardButton inlineKeyboardButton6 = new InlineKeyboardButton();
        inlineKeyboardButton6.setText("/mart8");
        inlineKeyboardButton6.setCallbackData("MART8");

        rowInline3.add(inlineKeyboardButton5);
        rowInline3.add(inlineKeyboardButton6);

        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);
        rowsInline.add(rowInline3);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}
