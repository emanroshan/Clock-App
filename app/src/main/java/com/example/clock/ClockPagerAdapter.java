package com.example.clock;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ClockPagerAdapter extends FragmentStateAdapter {

    public ClockPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DigitalClockFragment(); // Your Digital Clock Fragment
            case 1:
                return new TimerFragment(); // Your Timer Fragment
            case 2:
                return new StopwatchFragment();
            case 3:
                return new AlarmClockFragment();
            default:
                return new DigitalClockFragment(); // Default Fragment
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Number of tabs
    }
}

