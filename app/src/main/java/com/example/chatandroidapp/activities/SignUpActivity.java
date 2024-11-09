package com.example.chatandroidapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatandroidapp.databinding.ActivitySignupBinding;
import com.example.chatandroidapp.utilities.Constants;
import com.example.chatandroidapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * SignUpActivity handles the user registration process for the chat application.
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
        // Navigate back when "Sign In" text is clicked
        binding.textSignIn.setOnClickListener(v -> onBackPressed());

        // Handle sign-up button click
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidateSignUpDetails()) {
                signUp();
            }
        });

        // Handle image layout click to pick an image
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
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
     * Registers the new user to the Firebase Firestore database.
     */
    private void signUp() {
        // Show loading indicator
        setLoading(true);

        // Initialize Firebase Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Create a HashMap to store user data
        HashMap<String, String> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);

        // Add user data to the "Users" collection and handle success/failure
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    // Save user info in preferences
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                    // Navigate to MainActivity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    // Hide loading indicator and show error message
                    setLoading(false);
                    showToast(exception.getMessage());
                });
    }

    /**
     * Encodes the selected image into a Base64 string.
     * @param bitmap The bitmap image to encode.
     * @return The Base64 encoded string of the image.
     */
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        // Scale the bitmap to a smaller size
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Compress the bitmap and convert to byte array
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        // Encode the byte array to a Base64 string
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Launches the image picker and handles the result.
     */
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
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
                        encodedImage = encodeImage(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    /**
     * Validates the sign-up details entered by the user.
     * @return true if the details are valid, false otherwise.
     */
    private Boolean isValidateSignUpDetails() {
        if (encodedImage == null) {
            showToast("Please select your image");
            return false;
        } else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Please enter your name");
            return false;
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please enter a valid email");
            return false;
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter password");
            return false;
        } else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter password confirmation");
            return false;
        } else if (!binding.inputPassword.getText().toString().trim()
                .equals(binding.inputConfirmPassword.getText().toString().trim())) {
            showToast("Password and Confirm Password must be the same");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Toggles the loading state of the sign-up process.
     * @param isLoading true to show loading, false to hide loading.
     */
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            // Hide the sign-up button and show the progress bar
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            // Show the sign-up button and hide the progress bar
            binding.buttonSignUp.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}