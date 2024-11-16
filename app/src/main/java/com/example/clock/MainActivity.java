package com.example.clock;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TabLayout and ViewPager2
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        // Set Adapter
        ClockPagerAdapter adapter = new ClockPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager2 using TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.clock);
                    tab.setText("Clock");
                    break;

                case 1:
                    tab.setIcon(R.drawable.alarm);
                    tab.setText("Alarm");
                    break;


                case 2:
                    tab.setIcon(R.drawable.timer);
                    tab.setText("Timer");
                    break;

                case 3:
                    tab.setIcon(R.drawable.stopwatch);
                    tab.setText("Stopwatch");
                    break;

            }
        }).attach();
    }
}
