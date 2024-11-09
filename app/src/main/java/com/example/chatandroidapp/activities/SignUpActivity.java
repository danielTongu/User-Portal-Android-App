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

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignupBinding binding;
    private String encodedImage ;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> onBackPressed());

        binding.buttonSignUp.setOnClickListener(v -> {
            if(isValidateSignUpDetails()) {
                signUp();
            }
        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });


    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        //check loading
        setLoading(true);

        // Post to Firebase
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, String> user = new HashMap<>();
        user.put(Constants.KEY_NAME,binding.inputName.getText().toString());
        user.put (Constants.KEY_EMAIL,binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put (Constants.KEY_IMAGE, encodedImage);

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);

                    // user can jump to main activity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    setLoading(false);
                    showToast(exception.getMessage());
                });
    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight()*previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT) ;
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult (
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK) {
                Uri imageUri = result.getData().getData();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    binding.imageProfile.setImageBitmap(bitmap);
                    binding.textAddImage.setVisibility(View.GONE);
                    encodedImage = encodeImage(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    );

    private Boolean isValidateSignUpDetails() {
        boolean isValid = false;
        if (encodedImage == null) {
            showToast("Please select your image");
        }else if (binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Please enter your Name");
        } else if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please enter your email");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please enter valid Email");
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter password");
        }else if (binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Please enter password confirmation");
        }else if (!binding.inputPassword.getText().toString().trim().equals(binding.inputConfirmPassword.getText().toString().trim())) {
            showToast("Password and Confirm Password, must be the same");
        }else {
            isValid = true;
        }
        return isValid;
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
