package com.example.nutrifit1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;

import com.doinglab.foodlens.sdk.FoodLens;
import com.doinglab.foodlens.sdk.LanguageConfig;
import com.doinglab.foodlens.sdk.NetworkService;
import com.doinglab.foodlens.sdk.errors.BaseError;
import com.doinglab.foodlens.sdk.network.model.RecognitionResult;
import com.doinglab.foodlens.sdk.RecognizeResultHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class CameraActivity extends AppCompatActivity {

    private Button cameraButton, galleryButton, sendButton;
    private ImageView imageView;

    private ActivityResultLauncher<String> permissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    private byte[] imageByteData;
    private Uri imageUri;
    private static final String TAG_E = "ERROR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setEnabled(false);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result) openCamera();
                    else Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        imageView.setImageURI(imageUri);
                        imageByteData = convertUriToByteArray(imageUri);
                        sendButton.setEnabled(true);
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> handleImageSelection(null, uri)
        );

        cameraButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                openCamera();
            else permissionLauncher.launch(Manifest.permission.CAMERA);
        });

        galleryButton.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        sendButton.setOnClickListener(v -> {
            if (imageByteData != null) {
                sendImageToFoodLens(imageByteData);
            } else {
                Toast.makeText(this, "사진이 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(imageUri);
        } catch (Exception e) {
            Toast.makeText(this, "촬영 실패.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws Exception {
        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir("Pictures");
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    private void handleImageSelection(Bitmap bitmap, Uri uri) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageByteData = convertBitmapToByteArray(bitmap);
        } else if (uri != null) {
            imageUri = uri;
            imageView.setImageURI(uri);
            imageByteData = convertUriToByteArray(uri);
        }

        sendButton.setEnabled(imageByteData != null);
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private byte[] convertUriToByteArray(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            return convertBitmapToByteArray(bitmap);
        } catch (Exception e) {
            return null;
        }
    }

    private void sendImageToFoodLens(byte[] imageData) {
        NetworkService networkService = FoodLens.createNetworkService(getApplicationContext());
        networkService.setLanguageConfig(LanguageConfig.KO);

        networkService.predictMultipleFood(imageData, new RecognizeResultHandler() {
            @Override
            public void onSuccess(RecognitionResult result) {
                String resultJson = result.toJSONString();

                // food_data_*.json 파일 생성
                File foodFile = saveToJSONFile(resultJson);
                if (foodFile != null) {
                    Intent intent = new Intent(CameraActivity.this, CameraResultActivity.class);
                    intent.putExtra("result_json", resultJson);
                    intent.putExtra("image_uri", imageUri.toString());
                    intent.putExtra("food_file_name", foodFile.getName()); // 파일명 전달
                    startActivity(intent);
                }
            }

            @Override
            public void onError(BaseError error) {
                Toast.makeText(CameraActivity.this, "FoodLens 분석 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File saveToJSONFile(String resultJson) {
        ArrayList<HashMap<String, String>> foodResult = new ArrayList<>();
        HashSet<Integer> processedFoodIds = new HashSet<>();

        try {
            JSONObject jsonObject = new JSONObject(resultJson);
            JSONArray foodPositionList = jsonObject.getJSONArray("foodPositionList");

            for (int i = 0; i < foodPositionList.length(); i++) {
                JSONObject foodItem = foodPositionList.getJSONObject(i);
                float eatAmount = (float) foodItem.getDouble("eatAmount");
                JSONArray foodCandidates = foodItem.getJSONArray("foodCandidates");

                for (int j = 0; j < foodCandidates.length(); j++) {
                    JSONObject candidate = foodCandidates.getJSONObject(j);
                    int foodId = candidate.getInt("foodId");

                    if (processedFoodIds.contains(foodId)) continue;
                    if (!candidate.has("nutrition") || candidate.isNull("nutrition")) continue;

                    JSONObject nutrition = candidate.getJSONObject("nutrition");
                    JSONObject formatted = new JSONObject();
                    Iterator<String> keys = nutrition.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object val = nutrition.get(key);
                        if (val instanceof Number)
                            formatted.put(key, (float) nutrition.getDouble(key));
                        else
                            formatted.put(key, val);
                    }

                    HashMap<String, String> foodMap = new HashMap<>();
                    foodMap.put("foodID", String.valueOf(foodId));
                    foodMap.put("foodName", candidate.getString("foodName"));
                    foodMap.put("nutrition", formatted.toString());
                    foodMap.put("eatAmount", String.valueOf(eatAmount));

                    foodResult.add(foodMap);
                    processedFoodIds.add(foodId);
                }
            }

            JSONArray jsonArray = new JSONArray();
            for (HashMap<String, String> foodMap : foodResult) {
                JSONObject foodObject = new JSONObject();
                foodObject.put("foodID", Integer.parseInt(foodMap.get("foodID")));
                foodObject.put("foodName", foodMap.get("foodName"));
                foodObject.put("nutrition", new JSONObject(foodMap.get("nutrition")));
                foodObject.put("eatAmount", Float.parseFloat(foodMap.get("eatAmount")));
                jsonArray.put(foodObject);
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "food_data_" + timestamp + ".json";
            File file = new File(getFilesDir(), fileName);
            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter writer = new OutputStreamWriter(fos)) {
                writer.write(jsonArray.toString(4));
            }

            return file;

        } catch (Exception e) {
            Log.e(TAG_E, "food_data 저장 실패", e);
            return null;
        }
    }
}
