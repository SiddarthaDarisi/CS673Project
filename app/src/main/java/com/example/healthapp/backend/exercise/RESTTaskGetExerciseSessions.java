package com.example.healthapp.backend.exercise;

import com.example.healthapp.HealthApplication;
import com.example.healthapp.backend.RESTTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;

public class RESTTaskGetExerciseSessions implements RESTTask<ExerciseSession[]> {

    private final LocalDate day;

    public static void enqueue(Consumer<ExerciseSession[]> onSuccess, Consumer<String> onFailure) {
        enqueue(null, onSuccess, onFailure);
    }

    public static void enqueue(LocalDate day, Consumer<ExerciseSession[]> onSuccess, Consumer<String> onFailure) {
        if(day == null) day = LocalDate.now();

        Consumer<ExerciseSession[]> onSuccessProxy = val -> {
          if(val == null) onFailure.accept("API failure");
          else onSuccess.accept(val);
        };

        HealthApplication.getInstance().getAPIClient().submitTask(new RESTTaskGetExerciseSessions(day), onSuccessProxy, () -> onFailure.accept("API failure"));
    }

    public RESTTaskGetExerciseSessions(LocalDate day) { this.day = day; }

    @Override public String getMethod() { return "POST"; }
    @Override public String getEndpoint() { return "exercise"; }
    @Override public String getMessage() { return "Getting exercise sessions..."; }
    @Override public JSONObject getParameters() throws JSONException { return new JSONObject().accumulate("year", day.getYear()).accumulate("day", day.getDayOfYear()); }

    @Override
    public ExerciseSession[] process(int responseCode, JSONObject json, Map<String, List<String>> headers) throws JSONException {
        if(responseCode == HttpsURLConnection.HTTP_OK) {
            JSONArray sessionsRaw = json.getJSONArray("sessions");
            ExerciseSession[] sessions = new ExerciseSession[sessionsRaw.length()];

            for(int i = 0; i < sessions.length; i++) {
                JSONObject sessionRaw = sessionsRaw.getJSONObject(i);
                long start = -1, end = -1;
                int heart = -1, calories = -1;
                float measuredValue = -1;

                if(sessionRaw.has("start") && sessionRaw.get("start") != JSONObject.NULL) {
                    start = sessionRaw.getLong("start");
                }

                if(sessionRaw.has("end") && sessionRaw.get("end") != JSONObject.NULL) {
                    end = sessionRaw.getLong("end");
                }

                if(sessionRaw.has("heartrate") && sessionRaw.get("heartrate") != JSONObject.NULL) {
                    heart = sessionRaw.getInt("heartrate");
                }

                if(sessionRaw.has("calories") && sessionRaw.get("calories") != JSONObject.NULL) {
                    calories = sessionRaw.getInt("calories");
                }

                if(sessionRaw.has("parameter") && sessionRaw.get("parameter") != JSONObject.NULL) {
                    measuredValue = sessionRaw.getInt("parameter");
                }

                sessions[i] = new ExerciseSession(sessionRaw.getString("name"), ExerciseType.valueOf(sessionRaw.getString("type")), start, end, heart, calories, measuredValue);
            }

            return sessions;
        }

        return null;
    }
}
