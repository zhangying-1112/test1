package org.example.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.example.config.WeChatConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final WeChatConfig config;
    private final HttpClient httpClient;

    public WeatherService(WeChatConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * 查询城市天气 - 同时调用 now.json + daily.json，合并为完整天气回复
     */
    public String queryWeather(String city) {
        long start = System.currentTimeMillis();

        // ====== 并发请求两个接口 ======
        String nowJson = callApi(config.getWeatherNowUrl(), city, "now");
        String dailyJson = callApi(config.getWeatherDailyUrl(), city, "daily");

        // ====== 任一失败做容错：now 必须成功，daily 失败则只展示实况 ======
        if (nowJson == null) {
            log.error("[天气合并] now.json 请求失败，无法生成天气回复 | 城市:{}", city);
            return null;
        }

        JSONObject nowData = parseFirstResult(nowJson);
        JSONObject dailyData = parseFirstResult(dailyJson);

        if (nowData == null) {
            log.error("[天气合并] now.json 解析失败 | 城市:{}", city);
            return null;
        }

        String result = buildReply(city, nowData, dailyData);
        long cost = System.currentTimeMillis() - start;
        log.info("[天气合并] 完成 | 城市:{} | 耗时:{}ms", city, cost);
        return result;
    }

    /**
     * 调用单个天气API接口
     */
    private String callApi(String apiUrl, String city, String tag) {
        long start = System.currentTimeMillis();
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = apiUrl + "?key=" + config.getWeatherApiKey() + "&location=" + encodedCity;

        log.info("[天气API-{}] >>> 请求 | 城市:{} | URL:{}", tag, city, url);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long cost = System.currentTimeMillis() - start;

            log.info("[天气API-{}] <<< 响应 | 城市:{} | HTTP:{} | 耗时:{}ms | body:{}",
                    tag, city, response.statusCode(), cost, response.body());

            if (response.statusCode() != 200) {
                log.error("[天气API-{}] 请求失败 | HTTP:{} | 响应:{}", tag, response.statusCode(), response.body());
                return null;
            }

            // 检查业务错误
            JSONObject json = JSON.parseObject(response.body());
            JSONObject status = json.getJSONObject("status");
            if (status != null) {
                String code = status.getString("code");
                if (code != null && !"0".equals(code)) {
                    log.error("[天气API-{}] 业务错误 | code:{} | msg:{}", tag, code, status.getString("status"));
                    return null;
                }
            }

            return response.body();

        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[天气API-{}] 异常 | 城市:{} | 耗时:{}ms | 异常:{}", tag, city, cost, e.getMessage());
            return null;
        }
    }

    /**
     * 从API响应JSON中提取 results[0]
     */
    private JSONObject parseFirstResult(String jsonBody) {
        try {
            JSONObject json = JSON.parseObject(jsonBody);
            JSONArray results = json.getJSONArray("results");
            if (results == null || results.isEmpty()) return null;
            return results.getJSONObject(0);
        } catch (Exception e) {
            log.warn("[天气解析] JSON解析异常 | 异常:{}", e.getMessage());
            return null;
        }
    }

    /**
     * 合并 now + daily 数据，组装最终回复
     *
     * now数据: location.name, now.text, now.temperature, last_update
     * daily数据: daily[0].text_day, text_night, high, low, humidity, wind_direction, wind_scale
     */
    private String buildReply(String city, JSONObject nowResult, JSONObject dailyResult) {
        // ====== 从 now 提取实况数据 ======
        JSONObject location = nowResult.getJSONObject("location");
        JSONObject now = nowResult.getJSONObject("now");
        String cityName = location != null ? location.getString("name") : city;
        String nowText = now != null ? now.getString("text") : "--";
        String nowTemp = now != null ? now.getString("temperature") : "--";
        String updateTime = nowResult.getString("last_update");

        // ====== 从 daily 提取今日预报（容错：daily 为 null 时用默认值） ======
        String textDay = "--";
        String textNight = "--";
        String high = "--";
        String low = "--";
        String humidity = "--";
        String windDir = "--";
        String windScale = "--";

        if (dailyResult != null) {
            JSONArray dailyArr = dailyResult.getJSONArray("daily");
            if (dailyArr != null && !dailyArr.isEmpty()) {
                JSONObject today = dailyArr.getJSONObject(0);
                textDay = getOrDefault(today, "text_day", "--");
                textNight = getOrDefault(today, "text_night", "--");
                high = getOrDefault(today, "high", "--");
                low = getOrDefault(today, "low", "--");
                humidity = getOrDefault(today, "humidity", "--");
                windDir = getOrDefault(today, "wind_direction", "--");
                windScale = getOrDefault(today, "wind_scale", "--");
            }
            // daily 有独立的 last_update，优先用它
            String dailyUpdate = dailyResult.getString("last_update");
            if (dailyUpdate != null) updateTime = dailyUpdate;
        }

        log.info("[天气合并] 组装完成 | 城市:{} | 当前:{} {}°C | 白天:{} 夜间:{} | {}~{}°C | 湿度:{}% | 风:{} {}级",
                cityName, nowText, nowTemp, textDay, textNight, low, high, humidity, windDir, windScale);

        // ====== 拼接用户友好文案 ======
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(cityName).append("天气】\n");
        sb.append("白天：").append(textDay).append(" | 夜间：").append(textNight).append("\n");
        sb.append("当前温度：").append(nowTemp).append("℃\n");
        sb.append("今日气温：").append(low).append("℃ ~ ").append(high).append("℃\n");
        sb.append("空气湿度：").append(humidity).append("%\n");
        sb.append("风向风力：").append(windDir).append("风 ").append(windScale).append(" 级\n");
        if (updateTime != null) {
            sb.append("数据更新时间：").append(updateTime);
        }

        return sb.toString();
    }

    private String getOrDefault(JSONObject obj, String key, String def) {
        String val = obj.getString(key);
        return val != null ? val : def;
    }

    /**
     * 检查天气API连接状态
     */
    public boolean checkConnection() {
        try {
            if (config.getWeatherApiKey() == null || config.getWeatherApiKey().isEmpty()) {
                log.warn("[天气API连接检查] APIKey未配置");
                return false;
            }
            String url = config.getWeatherNowUrl()
                    + "?key=" + config.getWeatherApiKey()
                    + "&location=hangzhou";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.warn("[天气API连接检查失败] 异常:{}", e.getMessage());
            return false;
        }
    }
}
