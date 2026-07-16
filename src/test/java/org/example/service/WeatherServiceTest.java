package org.example.service;

import org.example.config.WeChatConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeChatConfig config;

    @InjectMocks
    private WeatherService weatherService;

    @Test
    @DisplayName("TC-16: checkConnection - APIKey未配置返回false")
    void checkConnection_noApiKey() {
        when(config.getWeatherApiKey()).thenReturn("");
        assertFalse(weatherService.checkConnection());
    }

    @Test
    @DisplayName("TC-17: checkConnection - APIKey为null返回false")
    void checkConnection_nullApiKey() {
        when(config.getWeatherApiKey()).thenReturn(null);
        assertFalse(weatherService.checkConnection());
    }
}
