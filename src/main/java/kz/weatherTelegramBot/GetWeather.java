package kz.weatherTelegramBot;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class GetWeather extends DefaultAbsSender implements Runnable {

    private final UserResource userResource;
    private final String token;

    protected GetWeather(UserResource userResource, DefaultBotOptions options, String token) {
        super(options);
        this.userResource = userResource;
        this.token = token;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void run() {
        String weatherData = getWeatherData(userResource.getCity());
        try {
            weatherData = weatherData == null ? "Not found" : weatherData;
            execute(SendMessage.builder()
                    .chatId(userResource.getChat_id())
                    .text(weatherData)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatData(String data) {
        String result = null;

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
            JSONObject mainData = (JSONObject) jsonObject.get("main");
            JSONArray descriptionDataArray = (JSONArray) jsonObject.get("weather");
            JSONObject descriptionData = (JSONObject) descriptionDataArray.get(0);

            String temp = mainData.get("temp").toString();
            String feelsLike = mainData.get("feels_like").toString();


            result = String.format("%s %.0f %nFeels like %.0f %n%s", jsonObject.get("name"),
                    Double.parseDouble(temp),
                    Double.parseDouble(feelsLike),
                    descriptionData.get("main")
            );

            System.out.println(result);

        } catch (ParseException ignored) {
        }

        return result;
    }

    private String getWeatherData(String city) {
        String key = "3bc5af74efe16bd3c5f04b878a39adf1";
        String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=en";
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = bufferedReader.readLine()) != null)
                buffer.append(line).append("\n");

            return formatData(buffer.toString());

        } catch (IOException ignored) {
        } finally {
            if (connection != null)
                connection.disconnect();
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException io) {
                throw new RuntimeException(io);
            }
        }
        return null;

    }

}