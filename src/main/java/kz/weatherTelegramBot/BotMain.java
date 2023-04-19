package kz.weatherTelegramBot;


import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Map;
import java.util.Optional;


public class BotMain extends TelegramLongPollingBot {

    private static final Map<String, String> getenv = System.getenv();

    @Override
    public String getBotUsername() {
        return getenv.get("BOT_NAME");
    }

    @Override
    public String getBotToken() {
        return getenv.get("BOT_TOKEN");
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            try {
                if (message.hasText() && message.hasEntities()) {
                    commandHandle(message);
                } else if (message.hasText()) {
                    textMessageHandle(message);
                }
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void commandHandle(Message message) throws TelegramApiException {
        Optional<MessageEntity> commandEntity =
                message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
        if (commandEntity.isPresent()) {
            String command =
                    message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
            if ("/start".equals(command)) {
                execute(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("Hi! Text me a city then I'll send you the weather of this city").build());
            }
        }
    }

    private void textMessageHandle(Message message) {
        UserResource userResource = new UserResource(message.getChatId().toString(), message.getText());
        Runnable weatherData = new GetWeather(userResource, new DefaultBotOptions(), getenv.get("BOT_TOKEN"));
        weatherData.run();
    }


    public static void main(String[] args) throws TelegramApiException {
        BotMain bot = new BotMain();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

}

