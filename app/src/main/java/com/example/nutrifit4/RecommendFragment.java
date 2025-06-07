package com.example.nutrifit4;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecommendFragment extends Fragment {
    private final String SERVER_URL = "http://43.203.201.216:5000/recommend";

    private View rootView;

    public RecommendFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_recommend, container, false);

        sendRecommendationRequest();
        rootView.findViewById(R.id.voteButton1).setOnClickListener(v -> handleLikeButton(v));
        rootView.findViewById(R.id.voteButton2).setOnClickListener(v -> handleLikeButton(v));
        return rootView;
    }

    private void handleLikeButton(View button) {
        button.setAlpha(0.5f);  // 50% 투명도
        if (button instanceof android.widget.Button) {
            ((android.widget.Button) button).setText("좋아요 완료!");
        }
        button.setEnabled(false); // 중복 클릭 방지
    }

    private void sendRecommendationRequest() {
        try {
            File userFile = new File(requireContext().getFilesDir(), "user_data.json");

            // 오늘 날짜의 food_data 파일 찾기
            String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
            File[] files = requireContext().getFilesDir().listFiles((dir, name) -> name.startsWith("food_data_" + today));
            if (files == null || files.length == 0) {
                Toast.makeText(getContext(), "오늘 날짜의 food_data 파일이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            File foodFile = files[0];

            // JSON 파싱
            Gson gson = new Gson();
            JsonObject userJson = gson.fromJson(new InputStreamReader(new FileInputStream(userFile)), JsonObject.class);
            JsonArray foodArray = gson.fromJson(new InputStreamReader(new FileInputStream(foodFile)), JsonArray.class);

            // 병합 JSON
            JsonObject merged = new JsonObject();
            merged.add("user", userJson.get("user"));
            merged.add("foods", foodArray);

            // HTTP 요청
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(
                    merged.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(SERVER_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "추천 요청 실패", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "추천 응답 실패", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    String resultJson = response.body().string();
                    requireActivity().runOnUiThread(() -> displayRecommendations(resultJson));
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "요청 준비 중 오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void displayRecommendations(String resultJson) {
        try {
            JsonArray resultArray = JsonParser.parseString(resultJson).getAsJsonArray();

            if (resultArray.size() < 2) {
                Toast.makeText(getContext(), "추천 결과가 부족합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            String rice = "";
            String soup = "";
            StringBuilder sideDishBuilder = new StringBuilder();

            if (resultArray.size() == 4) {
                rice = resultArray.get(0).getAsJsonObject().get("대표식품명").getAsString();
                for (int i = 1; i < 4; i++) {
                    JsonObject dishObj = resultArray.get(i).getAsJsonObject();
                    sideDishBuilder.append(dishObj.get("대표식품명").getAsString());
                    if (i < 3) sideDishBuilder.append(", ");
                }
            } else {
                JsonObject riceObj = resultArray.get(0).getAsJsonObject();
                JsonObject soupObj = resultArray.get(1).getAsJsonObject();

                rice = riceObj.get("대표식품명").getAsString();
                soup = soupObj.get("대표식품명").getAsString();

                for (int i = 2; i < resultArray.size(); i++) {
                    JsonObject dishObj = resultArray.get(i).getAsJsonObject();
                    sideDishBuilder.append(dishObj.get("대표식품명").getAsString());
                    if (i < resultArray.size() - 1) sideDishBuilder.append(", ");
                }
            }

            ((TextView) rootView.findViewById(R.id.rice1)).setText("밥: " + rice);
            ((TextView) rootView.findViewById(R.id.soup1)).setText(soup.isEmpty() ? "" : "국: " + soup);
            ((TextView) rootView.findViewById(R.id.sideDish1)).setText("반찬: " + sideDishBuilder);

            ((TextView) rootView.findViewById(R.id.rice2)).setText("밥: " + rice);
            ((TextView) rootView.findViewById(R.id.soup2)).setText(soup.isEmpty() ? "" : "국: " + soup);
            ((TextView) rootView.findViewById(R.id.sideDish2)).setText("반찬: " + sideDishBuilder);

        } catch (Exception e) {
            Toast.makeText(getContext(), "추천 데이터 파싱 오류: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
