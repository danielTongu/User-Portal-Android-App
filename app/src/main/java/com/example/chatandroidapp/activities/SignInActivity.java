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

public class SignInActivity extends AppCompatActivity {


    private ActivitySigninBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySigninBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getApplicationContext());

        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.textCreateNewAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
            addDataToFirebaseStore();
        });


    }

    public void addDataToFirebaseStore() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Create users
        HashMap<String, String> data = new HashMap<>();
        data.put("first name", "Daniel");
        data.put("last name", "Tongu");

        // add users to the database and notify success/failure
        database.collection("Users").add(data).addOnSuccessListener(documentReference -> {
            Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(exception -> {
            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signIn() {
        setLoading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()) {
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

    private Boolean isValidSignInDetails() {
        boolean isValid = false;
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Please Enter your Email");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Please Enter vail Email");
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Please Enter your Password");
        } else {
            isValid = true;
        }
        return true;
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}