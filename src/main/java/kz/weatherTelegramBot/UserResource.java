package kz.weatherTelegramBot;

public class UserResource {
    private final String chat_id;
    private final String city;

    public UserResource(String chat_id, String city) {
        this.chat_id = chat_id;
        this.city = city;
    }

    public String getChat_id() {
        return chat_id;
    }

    public String getCity() {
        return city;
    }
}