package com.example.chatandroidapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chatandroidapp.R;
import com.example.chatandroidapp.utilities.Constants;
import com.example.chatandroidapp.utilities.PreferenceManager;
import com.example.chatandroidapp.utilities.ToastType;
import com.example.chatandroidapp.utilities.Utilities;

public class MainActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        preferenceManager = new PreferenceManager(getApplicationContext());

        // Check if user is signed in
        if (!preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) { // Redirect to SignInActivity
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Get the menu item
        MenuItem menuItem = menu.findItem(R.id.action_sign_out);

        // Set the custom action view
        View actionView = menuItem.getActionView();

        // Handle click events on the custom view
        assert actionView != null;
        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Signs out the current user by clearing their preferences and redirecting to SignInActivity.
     */
    private void signOut() {
        // Clear user preferences
        preferenceManager.clear();

        // Show sign-out confirmation
        Utilities.showToast(this, "Signed out", ToastType.SUCCESS);

        // Navigate back to SignInActivity
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish MainActivity
    }
}