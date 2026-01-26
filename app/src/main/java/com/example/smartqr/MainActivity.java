package com.example.smartqr;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide toolbar/action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Load Login fragment as first screen
        loadLoginFragment();
    }

    // Load Login fragment
    public void loadLoginFragment() {
        loadFragment(new Login(), false);
    }

    // Load SignUp fragment
    public void loadSignUpFragment() {
        loadFragment(new SignUp(), true);
    }

    // Called after successful login
    public void onLoginSuccess() {
        // Start Drawer Activity (Dashboard + Drawer)
        Intent intent = new Intent(this, ScreenChange.class);
        startActivity(intent);
        finish(); // remove login activity from back stack
    }

    // Generic fragment loader
    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }
}
