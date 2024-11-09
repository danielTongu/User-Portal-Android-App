package com.example.chatandroidapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatandroidapp.databinding.ActivitySignupBinding;
import com.example.chatandroidapp.utilities.Constants;
import com.example.chatandroidapp.utilities.PreferenceManager;
import com.example.chatandroidapp.utilities.ToastType;
import com.example.chatandroidapp.utilities.Utilities; // Import Utilities class
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * SignUpActivity handles the user registration process for the chat application.
 *
 * @author Daniel Tongu
 */
public class SignUpActivity extends AppCompatActivity {

    // View binding for activity_signup.xml
    private ActivitySignupBinding binding;

    // Encoded image string for the user's profile picture
    private String encodedImage;

    // PreferenceManager to manage shared preferences
    private PreferenceManager preferenceManager;

    /**
     * Called when the activity is starting. Initializes the activity components.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize view binding
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        // Initialize PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        setListeners();
    }

    /**
     * Sets up the listeners for the UI elements.
     */
    private void setListeners() {
        // Handle image layout click to pick an image
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        // Handle sign-up button click
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidateSignUpDetails()) {
                signUp();
            }
        });
        // Navigate back when "Sign In" text is clicked
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Registers the new user to the Firebase Firestore database.
     * Checks for duplicate email before adding.
     */
    private void signUp() {
        Utilities.showToast(this, "Onboarding...", ToastType.INFO);
        // Show loading indicator
        showLoadingIndicator(true);

        // Initialize Firebase Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        String email = binding.inputEmail.getText().toString().trim();

        // Check if a user with the same email already exists
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        showLoadingIndicator(false);
                        Utilities.showToast(this, "Email already in use. Please use a different email.", ToastType.WARNING);
                    } else {
                        addUserToDatabase();
                    }
                })
                .addOnFailureListener(exception -> {
                    showLoadingIndicator(false);
                    Utilities.showToast(this, exception.getMessage(), ToastType.DANGER);
                });
    }

    /**
     * Adds the new user to the database.
     */
    private void addUserToDatabase() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Create a HashMap to store user data
        Utilities.showToast(this, "Registering...", ToastType.INFO);
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString().trim());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString().trim());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString().trim());
        user.put(Constants.KEY_IMAGE, encodedImage);

        // Add user data to the "Users" collection
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Utilities.showToast(this, "Registration successful", ToastType.INFO);
                    showLoadingIndicator(false);

                    // Save user info in preferences
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString().trim());
                    preferenceManager.putString(Constants.KEY_EMAIL, binding.inputEmail.getText().toString().trim());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                    // Navigate to MainActivity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    showLoadingIndicator(false);
                    Utilities.showToast(this, exception.getMessage(), ToastType.DANGER);
                });
    }

    /**
     * Launches the image picker and handles the result.
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the selected image URI
                    Uri imageUri = result.getData().getData();

                    try {
                        // Open an InputStream to the image
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        // Decode the InputStream into a Bitmap
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                        // Set the selected image in the profile ImageView
                        binding.imageProfile.setImageBitmap(bitmap);
                        // Hide the "Add Image" text
                        binding.textAddImage.setVisibility(View.GONE);
                        // Encode the image to Base64
                        encodedImage = Utilities.encodeImage(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Utilities.showToast(this, "Image not found", ToastType.DANGER);
                    }
                }
            }
    );

    /**
     * Validates the sign-up details entered by the user.
     * @return true if the details are valid, false otherwise.
     */
    private Boolean isValidateSignUpDetails() {
        boolean isValid = false;

        if (encodedImage == null) {
            Utilities.showToast(this, "Please select your image", ToastType.WARNING);
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            Utilities.showToast(this, "Please enter your name", ToastType.WARNING);
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            Utilities.showToast(this, "Please enter your email", ToastType.WARNING);
        } else if (!Utilities.isValidEmail(binding.inputEmail.getText().toString().trim())) {
            Utilities.showToast(this, "Please enter a valid email", ToastType.WARNING);
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            Utilities.showToast(this, "Please enter password", ToastType.WARNING);
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            Utilities.showToast(this, "Please enter password confirmation", ToastType.WARNING);
        } else if (!binding.inputPassword.getText().toString().trim()
                .equals(binding.inputConfirmPassword.getText().toString().trim())) {
            Utilities.showToast(this, "Password and Confirm Password must be the same", ToastType.WARNING);
        } else {
            isValid = true;
        }

        return isValid;
    }

    /**
     * Toggles the loading state of the sign-up process.
     * @param isLoading true to show loading, false to hide loading.
     */
    private void showLoadingIndicator(boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.textSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.textSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}