package com.example.clock;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
                    tab.setText("Alarm");
                    break;
                case 1:
                    tab.setText("Clock");
                    break;
                case 2:
                    tab.setText("Stopwatch");
                    break;
                case 3:
                    tab.setText("Timer");
                    break;
            }
        }).attach();

// Set a listener to dynamically change tab styles
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                TextView textView = (TextView) tab.getCustomView();
                if (textView == null) {
                    textView = new TextView(MainActivity.this);
                    tab.setCustomView(textView);
                }
                textView.setText(tab.getText());
                textView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.LightPurple)); // Selected color
                textView.setTextSize(16); // Larger font size for selected tab
                textView.setTypeface(null, Typeface.BOLD); // Bold text for selected tab
                textView.setGravity(Gravity.CENTER); // Center the text
                textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // Add underline
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView textView = (TextView) tab.getCustomView();
                if (textView == null) {
                    textView = new TextView(MainActivity.this);
                    tab.setCustomView(textView);
                }
                textView.setText(tab.getText());
                textView.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.LightGray)); // Unselected color
                textView.setTextSize(14); // Smaller font size for unselected tabs
                textView.setTypeface(null, Typeface.NORMAL); // Normal text style for unselected
                textView.setGravity(Gravity.CENTER); // Center the text
                textView.setPaintFlags(textView.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG); // Remove underline
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No action needed for reselected tabs
            }
        });

// Set custom views for all tabs
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                TextView textView = new TextView(this);
                textView.setText(tab.getText());
                textView.setTextColor(ContextCompat.getColor(this, R.color.LightGray)); // Default color
                textView.setTextSize(14); // Default text size
                textView.setGravity(Gravity.CENTER); // Center the text
                tab.setCustomView(textView);
            }
        }
    }
}
