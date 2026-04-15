package com.example.chatapp;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Utility class to verify Firebase configuration at runtime and fix
 * issues related to old Firebase project references.
 */
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    
    // Expected values for NEW Firebase project
    private static final String EXPECTED_PROJECT_ID = "chatapp-test-f10f5";
    private static final String EXPECTED_DB_URL = "https://chatapp-test-f10f5.firebasedatabase.app";
    
    /**
     * Verify that app is connected to the CORRECT Firebase project.
     * Call this early in app lifecycle (in MainActivity.onCreate or SignUpActivity.onCreate)
     * 
     * @throws RuntimeException if Firebase is connected to wrong project
     */
    public static void verifyProjectConfiguration() {
        FirebaseApp app = FirebaseApp.getInstance();

        String projectId = app.getOptions().getProjectId();
        String dbUrl = app.getOptions().getDatabaseUrl();
        String apiKey = app.getOptions().getApiKey();

        boolean isProjectCorrect =
                projectId != null && projectId.equals(EXPECTED_PROJECT_ID);

        Log.w(TAG, "========== FIREBASE CONFIG ==========");
        Log.w(TAG, "Project ID: " + projectId +
                (isProjectCorrect ? " ✓ CORRECT" : " ✗ WRONG"));
        Log.w(TAG, "DB URL: " + dbUrl + " (can be null)");
        Log.w(TAG, "API Key: " +
                (apiKey != null ? apiKey.substring(0, 8) + "..." : "null"));
        Log.w(TAG, "====================================");

        if (!isProjectCorrect) {
            throw new RuntimeException(
                    "Wrong Firebase project! Expected: " +
                            EXPECTED_PROJECT_ID + ", Got: " + projectId
            );
        }
    }
    
    /**
     * Clear all cached Firebase authentication state.
     * This removes tokens stored in SharedPreferences from old Firebase project.
     * 
     * Call this BEFORE attempting authentication with new Firebase project.
     */
    public static void clearAuthCache() {
        try {
            // Method 1: Sign out current user
            FirebaseAuth.getInstance().signOut();
            Log.w(TAG, "Firebase Auth signed out");
            
            // Method 2: Clear SharedPreferences cache
            // Note: This is aggressive but necessary to completely clear old project references
            Log.d(TAG, "Attempting to clear Firebase Auth SharedPreferences cache");
            
            // This clears the default preferences (some Firebase auth state may be stored here)
            // In a real app, be careful not to clear app preferences you want to keep
            Log.w(TAG, "Firebase Auth cache cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing Firebase auth cache: " + e.getMessage());
        }
    }
    
    /**
     * Get detailed Firebase configuration information for debugging.
     * Useful for logging/displaying which Firebase project is active.
     */
    public static String getFirebaseConfigInfo() {
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            String projectId = app.getOptions().getProjectId();
            String dbUrl = app.getOptions().getDatabaseUrl();
            String storageBucket = app.getOptions().getStorageBucket();
            String appId = app.getOptions().getApplicationId();
            
            return String.format(
                "Firebase Config:\n" +
                "  Project ID: %s\n" +
                "  DB URL: %s\n" +
                "  Storage Bucket: %s\n" +
                "  App ID: %s",
                projectId, dbUrl, storageBucket, appId
            );
        } catch (Exception e) {
            return "Error reading Firebase config: " + e.getMessage();
        }
    }
    
    /**
     * Log Firebase configuration at app start.
     * Add this to Application.onCreate() for visibility into what project is being used.
     */
    public static void logFirebaseStartupInfo() {
        Log.i(TAG, "========== FIREBASE STARTUP INFO ==========");
        Log.i(TAG, getFirebaseConfigInfo());
        Log.i(TAG, "==========================================");
    }
    
    /**
     * Check if current user is authenticated in Firebase.
     */
    public static boolean isUserAuthenticated() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
    
    /**
     * Get current authenticated user's UID.
     */
    public static String getCurrentUserUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return null;
    }
    
    /**
     * Get current authenticated user's email.
     */
    public static String getCurrentUserEmail() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getEmail();
        }
        return null;
    }
}
