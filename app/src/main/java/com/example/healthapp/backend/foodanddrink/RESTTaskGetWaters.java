package com.example.healthapp.backend.foodanddrink;

import com.example.healthapp.HealthApplication;
import com.example.healthapp.backend.RESTTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;

public class RESTTaskGetWaters implements RESTTask<Integer> {

    private final Date day;

    public static void enqueue(Date day, Consumer<Integer> onSuccess, Consumer<String> onFailure) {
        if(day == null) {
            day = Date.from(Instant.now());
        }

        Consumer<Integer> onSuccessProxy = val -> {
          if(val == -1) onFailure.accept("API failure");
          else onSuccess.accept(val);
        };

        HealthApplication.getInstance().getAPIClient().submitTask(new RESTTaskGetWaters(day), onSuccessProxy, () -> onFailure.accept("API failure"));
    }

    public RESTTaskGetWaters(Date day) { this.day = day; }

    @Override public String getMethod() { return "POST"; }
    @Override public String getEndpoint() { return "waterintake"; }
    @Override public String getMessage() { return "Getting water intake..."; }
    @Override public JSONObject getParameters() throws JSONException { return new JSONObject().accumulate("year", day.getYear() + 1900).accumulate("day", day.getDay()); }

    @Override
    public Integer process(int responseCode, JSONObject json, Map<String, List<String>> headers) throws JSONException {
        if(responseCode == HttpsURLConnection.HTTP_OK) {
            return json.getInt("glasses");
        }

        return -1;
    }
}