package com.example.voicecontrolledinterface;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private TextView outputTextView;

    // Register ActivityResultLauncher
    private final ActivityResultLauncher<Intent> speechRecognizerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    List<String> results = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (results != null && !results.isEmpty()) {
                        String command = results.get(0).toLowerCase();
                        processCommand(command);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputTextView = findViewById(R.id.outputTextView);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int langResult = textToSpeech.setLanguage(Locale.US);
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language is not supported or missing data");
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
    }

    public void onSpeakButtonClick(View view) {
        listenForVoiceCommand();
    }

    private void listenForVoiceCommand() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...");
        speechRecognizerLauncher.launch(intent); // New way
    }

    private void processCommand(String command) {
        if (command.contains("date")) {
            speakAndShow("Today's date is " + java.text.DateFormat.getDateInstance().format(new java.util.Date()));
        } else if (command.contains("time")) {
            speakAndShow("The current time is " + java.text.DateFormat.getTimeInstance().format(new java.util.Date()));
        } else if (command.contains("weather")) {
            fetchWeather();
        } else if (command.contains("joke")) {
            fetchJoke();
        } else if (command.contains("fun fact")) {
            fetchFunFact();
        } else if (command.contains("news")) {
            fetchNews();
        } else if (command.contains("alarm")) {
            speakAndShow("Alarm feature is not implemented in this demo.");
        } else {
            speakAndShow("Sorry, I didn't understand the command.");
        }
    }

    private void fetchWeather() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://api.openweathermap.org/data/2.5/weather?q=London&appid=60dbc83afe15d29c724d84cefb0523a0&units=metric";
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    String weatherDescription = json.getJSONArray("weather").getJSONObject(0).getString("description");
                    String temperature = json.getJSONObject("main").getString("temp");

                    String weatherInfo = "The weather is " + weatherDescription + " with a temperature of " + temperature + " degrees Celsius.";
                    runOnUiThread(() -> speakAndShow(weatherInfo));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> speakAndShow("Failed to fetch or parse weather data."));
            }
        }).start();
    }

    private void fetchJoke() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://official-joke-api.appspot.com/random_joke";
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    String joke = json.getString("setup") + " " + json.getString("punchline");

                    runOnUiThread(() -> speakAndShow("Here is a joke: " + joke));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> speakAndShow("Failed to get a joke."));
            }
        }).start();
    }

    private void fetchFunFact() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://uselessfacts.jsph.pl/random.json?language=en";
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    String fact = json.getString("text");

                    runOnUiThread(() -> speakAndShow("Here's a fun fact: " + fact));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> speakAndShow("Failed to retrieve fun fact."));
            }
        }).start();
    }

    private void fetchNews() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://newsapi.org/v2/top-headlines?country=us&apiKey=037bb4ae230c45c6b306b6119bdd3f48";
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);

                    StringBuilder newsBuilder = new StringBuilder("Here are the top 5 headlines:\n\n");
                    int count = Math.min(5, json.getJSONArray("articles").length());

                    for (int i = 0; i < count; i++) {
                        String title = json.getJSONArray("articles").getJSONObject(i).getString("title");
                        newsBuilder.append(i + 1).append(". ").append(title).append("\n\n");
                    }

                    String newsText = newsBuilder.toString().trim();

                    runOnUiThread(() -> {
                        outputTextView.setText(newsText); // show all headlines
                        textToSpeech.speak("Here are the top 5 news headlines.", TextToSpeech.QUEUE_FLUSH, null, null);
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> speakAndShow("Failed to fetch news."));
            }
        }).start();
    }

    private void speakAndShow(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        outputTextView.setText(text);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
