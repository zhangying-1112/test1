package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeChatConfig {

    @Value("${wechat.version:1.0.0}")
    private String version;

    @Value("${weather.api.key:}")
    private String weatherApiKey;

    @Value("${weather.api.now-url:https://api.seniverse.com/v3/weather/now.json}")
    private String weatherNowUrl;

    @Value("${weather.api.daily-url:https://api.seniverse.com/v3/weather/daily.json}")
    private String weatherDailyUrl;

    public String getVersion() { return version; }
    public String getWeatherApiKey() { return weatherApiKey; }
    public String getWeatherNowUrl() { return weatherNowUrl; }
    public String getWeatherDailyUrl() { return weatherDailyUrl; }
}
