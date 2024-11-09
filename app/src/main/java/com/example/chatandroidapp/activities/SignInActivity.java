package com.example.chatandroidapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatandroidapp.databinding.ActivitySigninBinding;
import com.example.chatandroidapp.utilities.Constants;
import com.example.chatandroidapp.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * SignInActivity handles the user sign-in functionality for the chat application.
 * @author Daniel Tongu
 */
public class SignInActivity extends AppCompatActivity {

    // View binding for activity_signin.xml
    private ActivitySigninBinding binding;

    // PreferenceManager to manage shared preferences
    private PreferenceManager preferenceManager;

    /**
     * Called when the activity is starting. Initializes the activity components.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize view binding
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        // Initialize PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        setContentView(binding.getRoot());
        setListeners();
    }

    /**
     * Sets up the listeners for the UI elements.
     */
    private void setListeners() {
        // Navigate to SignUpActivity when "Create New Account" text is clicked
        binding.textCreateNewAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));

        // Handle sign-in button click
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
            addDataToFirebaseStore();
        });
    }

    /**
     * Adds user data to the Firebase Firestore database.
     */
    public void addDataToFirebaseStore() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Create a HashMap to store user data
        HashMap<String, String> data = new HashMap<>();
        data.put("first name", "Daniel");
        data.put("last name", "Tongu");

        // Add user data to the "Users" collection and notify success/failure
        database.collection("Users").add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception -> {
                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Displays a Toast message.
     * @param message The message to be displayed.
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Authenticates the user with the provided email and password.
     */
    private void signIn() {
        setLoading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Query the database for a user matching the entered email and password
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()
                            && task.getResult() != null
                            && !task.getResult().getDocuments().isEmpty()) {
                        // If authentication is successful, save user details in preferences and navigate to MainActivity
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        setLoading(false);
                        showToast("Unable to sign in");
                    }
                });
    }

    /**
     * Validates the sign-in details entered by the user.
     * @return true if the details are valid, false otherwise.
     */
    private Boolean isValidSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please enter a valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter your password");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Toggles the loading state of the sign-in process.
     * @param isLoading true to show loading, false to hide loading.
     */
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            // Hide the sign-in button and show the progress bar
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            // Show the sign-in button and hide the progress bar
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}