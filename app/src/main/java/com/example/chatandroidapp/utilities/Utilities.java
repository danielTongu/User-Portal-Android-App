package com.example.chatandroidapp.utilities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.chatandroidapp.R;

/**
 * Utilities class for common utility functions used across the application.
 */
public class Utilities {

    /**
     * Overloaded method to display a Toast message with default type.
     *
     * @param context The context to use.
     * @param message The message to display.
     */
    public static void showToast(Context context, String message) {
        showToast(context, message, ToastType.DEFAULT);
    }

    /**
     * Displays a Toast message with the specified type.
     *
     * @param context The context to use.
     * @param message The message to display.
     * @param type    The type of the message: INFO, WARNING, DANGER, SUCCESS, or DEFAULT.
     */
    public static void showToast(Context context, String message, ToastType type) {
        if (type == null ) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            return;
        }
        displayCustomToast(context, message, type);
    }

    private static void displayCustomToast(Context context, String message, ToastType type) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.toast_custom, null);

        LinearLayout toastRoot = layout.findViewById(R.id.toast_layout_root);
        ImageView imageView = layout.findViewById(R.id.image);
        TextView textView = layout.findViewById(R.id.text);
        textView.setText(message);

        int backgroundDrawableId;
        int iconResId;
        int textColor = ContextCompat.getColor(context, R.color.white);

        switch (type) {
            case INFO:
                backgroundDrawableId = R.drawable.background_toast_info;
                iconResId = R.drawable.ic_info;
                break;
            case WARNING:
                textColor = ContextCompat.getColor(context, R.color.black);
                backgroundDrawableId = R.drawable.background_toast_warning;
                iconResId = R.drawable.ic_warning;
                break;
            case DANGER:
                backgroundDrawableId = R.drawable.background_toast_error;
                iconResId = R.drawable.ic_error;
                break;
            case SUCCESS:
                backgroundDrawableId = R.drawable.background_toast_success;
                iconResId = R.drawable.ic_success;
                break;
            default:
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                return;
        }

        // Set the background drawable
        toastRoot.setBackgroundResource(backgroundDrawableId);
        // Set the icon
        imageView.setImageResource(iconResId);
        // Set the icon tint color
        imageView.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
        // Set the text color
        textView.setTextColor(textColor);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

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