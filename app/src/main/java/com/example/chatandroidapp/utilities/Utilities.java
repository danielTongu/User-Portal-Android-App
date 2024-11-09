package com.example.chatandroidapp.utilities;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatandroidapp.R;

/**
 * Utilities class for common utility functions used across the application.
 *
 * @author Daniel Tongu
 */
public class Utilities {

    /**
     * Displays a Toast message with the specified type.
     *
     * @param context The context to use.
     * @param message The message to display.
     * @param type    The type of the message: INFO, WARNING, DANGER, or DEFAULT.
     */
    public static void showToast(Context context, String message, ToastType type) {
        switch (type != null ? type : ToastType.DEFAULT) {
            case INFO:
                displayCustomToast(context, message, R.layout.custom_toast_info);
                break;
            case WARNING:
                displayCustomToast(context, message, R.layout.custom_toast_warning);
                break;
            case DANGER:
                displayCustomToast(context, message, R.layout.custom_toast_danger);
                break;
            default:
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Overloaded method to display a Toast message with default type.
     *
     * @param context The context to use.
     * @param message The message to display.
     */
    public static void showToast(Context context, String message) {
        showToast(context, message, ToastType.DEFAULT);
    }

    private static void displayCustomToast(Context context, String message, int layoutId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(layoutId, null);

        TextView text = layout.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    // Additional utility methods...

    /**
     * Checks if a string is a valid email address.
     *
     * @param email The email string to validate.
     * @return True if valid, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Encodes a bitmap image to a Base64 string.
     *
     * @param bitmap The bitmap to encode.
     * @return The Base64 encoded string.
     */
    public static String encodeImage(android.graphics.Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        android.graphics.Bitmap previewBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();

        previewBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }

}