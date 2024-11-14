package com.example.chatandroidapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatandroidapp.R;
import com.example.chatandroidapp.databinding.ActivityMainBinding;
import com.example.chatandroidapp.utilities.Constants;
import com.example.chatandroidapp.utilities.PreferenceManager;
import com.example.chatandroidapp.utilities.ToastType;
import com.example.chatandroidapp.utilities.Utilities;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

/**
 * MainActivity serves as the primary screen of the Chat Android application.
 * It handles user interface initialization, user details loading, token management, and user sign-out functionality.
 */
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;// View binding for the activity's layout
    private PreferenceManager preferenceManager;// PreferenceManager instance for managing shared preferences

    /**
     * Called when the activity is first created. Initializes the UI, loads user details,
     * retrieves the Firebase token, and sets up event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied in onSaveInstanceState(Bundle). Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());

        setContentView(binding.getRoot());
        setUpListeners();
        loadUserDetails();
        getToken();
    }

    /**
     * Sets up event listeners for UI components.
     * Currently, it sets a click listener on the sign-out image to trigger the signOut process.
     */
    private void setUpListeners() {
        binding.imageSignOut.setOnClickListener(v -> signOut()); 
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent( getApplicationContext(), UserActivity.class)));
    }

    /**
     * Retrieves the current Firebase Cloud Messaging (FCM) token and updates it in Firestore.
     */
    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(this::updateToken)
                .addOnFailureListener(e -> Utilities.showToast(this, "Failed to get FCM token", ToastType.ERROR));
    }

    /**
     * Loads the user's details such as username and profile image from shared preferences
     * and displays them in the UI.
     */
    private void loadUserDetails() {
        // Set the username from shared preferences
        binding.inputName.setText(preferenceManager.getString(Constants.KEY_NAME));

        // Decode the Base64-encoded profile image and set it to the ImageView
        String encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);
        if (encodedImage != null && !encodedImage.isEmpty()) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.imageProfile.setImageBitmap(bitmap);
        } else { // Set a default image or handle the absence of a profile image
            binding.imageProfile.setImageResource(R.drawable.ic_default_profile);
        }
    }

    /**
     * Updates the user's FCM token in Firestore.
     * @param token The new FCM token to be updated.
     */
    private void updateToken(String token) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Reference to the current user's document in the users collection
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        // Update the FCM token field with the new token
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(unused ->
                        Utilities.showToast(this, "Token updated successfully", ToastType.SUCCESS))
                .addOnFailureListener(e ->
                        Utilities.showToast(this, "Unable to update Token", ToastType.ERROR));
    }

    /**
     * Signs out the current user by performing the following actions:
     * <ul>
     *     <li>Displays a signing out toast message.</li>
     *     <li>Removes the FCM token from Firestore.</li>
     *     <li>Clears all user preferences.</li>
     *     <li>Redirects the user to the SignInActivity.</li>
     * </ul>
     */
    private void signOut() {
        Utilities.showToast(this, "Signing out ...", ToastType.INFO);

        // Get an instance of Firestore
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Reference to the current user's document in the users collection
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));

        // Create a map to hold the fields to update
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());

        // Update the user's document to remove the FCM token
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    // Clear all preferences upon successful token removal
                    preferenceManager.clear();

                    // Start the SignInActivity and finish the current activity
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Utilities.showToast(this, "Unable to sign out", ToastType.ERROR));
    }
}