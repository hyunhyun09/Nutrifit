package com.nutrifit_n;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton cameraButton;
    private ImageButton userInformationButton;
    private TextView dateTextView;
    private Fragment currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        cameraButton = findViewById(R.id.searchButton);
        userInformationButton = findViewById(R.id.userInformationButton);
        dateTextView = findViewById(R.id.dateTextView);

        String today = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA).format(new Date());
        dateTextView.setText(today);

        if (savedInstanceState == null) {
            currentFragment = new PostFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, currentFragment)
                    .commit();
            cameraButton.setVisibility(View.VISIBLE);
        }

        // 카메라 버튼
        cameraButton.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, CameraActivity.class));
        });

        // 유저 정보 버튼
        userInformationButton.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, UserInformActivity.class));
        });

        // 하단 네비게이션 버튼 처리
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment nextFragment = null;

            if (item.getItemId() == R.id.navigation_tracking) {
                nextFragment = new PostFragment();
            } else if (item.getItemId() == R.id.navigation_statistics) {
                nextFragment = new StatisticsFragment();
            } else if (item.getItemId() == R.id.navigation_recommend) {
                nextFragment = new RecommendFragment();
            }

            if (nextFragment != null && !nextFragment.getClass().equals(currentFragment.getClass())) {
                boolean toRight = isForwardNavigation(currentFragment, nextFragment);
                animateFragmentDirectionally(nextFragment, toRight);

                if (nextFragment instanceof PostFragment) {
                    cameraButton.setVisibility(View.VISIBLE);
                } else {
                    cameraButton.setVisibility(View.GONE);
                }

                currentFragment = nextFragment;
                return true;
            }

            return false;
        });
    }

    // 현재 → 다음 프래그먼트의 순서 비교 (작으면 오른쪽 슬라이드)
    private boolean isForwardNavigation(Fragment current, Fragment next) {
        return getFragmentOrder(current) < getFragmentOrder(next);
    }

    // 프래그먼트 순서 지정
    private int getFragmentOrder(Fragment fragment) {
        if (fragment instanceof PostFragment) return 0;
        if (fragment instanceof StatisticsFragment) return 1;
        if (fragment instanceof RecommendFragment) return 2;
        return -1;
    }

    // 슬라이드 애니메이션 적용
    private void animateFragmentDirectionally(Fragment fragment, boolean toRight) {
        int enter, exit, popEnter, popExit;

        if (toRight) {
            enter = R.anim.slide_in_right;
            exit = R.anim.slide_out_left;
            popEnter = R.anim.slide_in_left;
            popExit = R.anim.slide_out_right;
        } else {
            enter = R.anim.slide_in_left;
            exit = R.anim.slide_out_right;
            popEnter = R.anim.slide_in_right;
            popExit = R.anim.slide_out_left;
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}