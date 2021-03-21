package com.lis.simpleretrofit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lis.simpleretrofit.annotation.Click;
import com.lis.simpleretrofit.api.SimpleWeatherApi;
import com.lis.simpleretrofit_library.SimpleRetrofit;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InjectUtil.injectClick(this);
        SimpleRetrofit retrofit = new SimpleRetrofit.Builder().baseUrl("https://restapi.amap.com").callFactory(new OkHttpClient()).build();
        SimpleWeatherApi simpleWeatherApi = retrofit.create(SimpleWeatherApi.class);
        // Call call = simpleWeatherApi.getWeather("110101", "ae6c53e2186f33bbf240a12d80672d1b");
        Call call = simpleWeatherApi.postWeather("110101", "ae6c53e2186f33bbf240a12d80672d1b");
        TextView textView = findViewById(R.id.text);
        try {
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.body().string();
                        runOnUiThread(() -> {
                            textView.setText(result);
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Click({R.id.text, R.id.button})
    public void abcClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.text:
                ((TextView) view).setText("abcClick");
                break;
            case R.id.button:
                Toast.makeText(this, "点击了Button", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}