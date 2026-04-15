package com.example.chatapp.conversation;

import static com.example.chatapp.Globals.MESSAGE_SENDER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.IChatInterface;
import com.example.chatapp.R;
import com.example.chatapp.firebaseDb.ChatFirebaseDAO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO = 1201;
    private static final int IMAGE_MAX_DIMENSION = 720;
    private static final int IMAGE_QUALITY = 70;
    private static final int IMAGE_MAX_UPLOAD_BYTES = 450 * 1024;
    private static final int VOICE_MIN_DURATION_MS = 500;
    private static final int VOICE_MAX_DURATION_MS = 60_000;
    private static final long VOICE_MAX_FILE_BYTES = 1_500_000;

    private ArrayList<Message> SRMessages;
    private EditText editText;
    private RecyclerView recyclerViewMessageLists;
    private MessageAdaptor mAdaptor;
    private ImageButton voiceRecordBtn;
    private ImageButton sendBtn;
    private ImageButton attachImageBtn;
    private ImageButton cancelUploadBtn;
    private TextView uploadStatusText;
    private View uploadStatusContainer;

    private String receiverId;
    private String receiverName;
    private int recyclerViewItemId;
    private IChatInterface dao;
    private long timeStamp;
    private MessageViewModel vm;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private android.media.MediaRecorder mediaRecorder;
    private String recordingFilePath;
    private long recordingStartTime;
    private boolean isRecording;
    private boolean isUploading;

    private StorageReference mediaStorageRef;
    private UploadTask currentUploadTask;
    private boolean currentUploadIsVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dao = new ChatFirebaseDAO(new ChatFirebaseDAO.DataObserver() {
            @Override
            public void update() {
                refresh();
            }
        });

        Intent intent = getIntent();
        receiverId = intent.getStringExtra("id");
        receiverName = intent.getStringExtra("name");
        recyclerViewItemId = intent.getIntExtra("recyclerViewItemId", -1);

        if (getSupportActionBar() != null && receiverName != null) {
            getSupportActionBar().setTitle(receiverName);
        }

        vm = new ViewModelProvider(this).get(MessageViewModel.class);
        vm.setDao(dao);
        SRMessages = vm.getMessages(savedInstanceState, "data", receiverId);

        recyclerViewMessageLists = findViewById(R.id.messageLists);
        recyclerViewMessageLists.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewMessageLists.setLayoutManager(linearLayoutManager);

        editText = findViewById(R.id.sender1Text);
        voiceRecordBtn = findViewById(R.id.voiceRecordBtn);
        sendBtn = findViewById(R.id.sender1TextBtn);
        attachImageBtn = findViewById(R.id.attachImageBtn);
        cancelUploadBtn = findViewById(R.id.cancelUploadBtn);
        uploadStatusText = findViewById(R.id.uploadStatusText);
        uploadStatusContainer = findViewById(R.id.uploadStatusContainer);

        mAdaptor = new MessageAdaptor(SRMessages);
        recyclerViewMessageLists.setAdapter(mAdaptor);

        setupImagePicker();
        initStorage();
        timeStamp = -1;
    }

    private void initStorage() {
        String userKey = resolveCurrentUserKey();
        mediaStorageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("chat_media")
                .child(userKey)
                .child(receiverId == null ? "unknown" : receiverId);
    }

    private String resolveCurrentUserKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            return "anonymous";
        }
        String email = user.getEmail();
        if (email.length() >= 11) {
            return email.substring(0, 11);
        }
        return email.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != RESULT_OK) {
                        return;
                    }
                    ActivityResult activityResult = result;
                    Intent data = activityResult.getData();
                    if (data == null || data.getData() == null) {
                        return;
                    }
                    try {
                        sendImageMessage(data.getData());
                    } catch (IOException e) {
                        Toast.makeText(this, "Khong the xu ly anh da chon", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @SuppressLint("NotifyDataSetChanged")
    public void handleOnClick(View v) {
        if (v.getId() == R.id.sender1TextBtn) {
            sendTextMessage();
            return;
        }

        if (v.getId() == R.id.attachImageBtn) {
            openImagePicker();
            return;
        }

        if (v.getId() == R.id.voiceRecordBtn) {
            toggleVoiceRecording();
            return;
        }

        if (v.getId() == R.id.cancelUploadBtn) {
            cancelCurrentUpload();
        }
    }

    private void sendTextMessage() {
        if (isUploading) {
            Toast.makeText(this, "Dang upload media, vui long doi", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = editText.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Field is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        timeStamp = System.currentTimeMillis();
        Message newMessage = new Message(
                MESSAGE_SENDER,
                content,
                timeStamp,
                0,
                Message.CONTENT_TEXT,
                "",
                0,
                dao
        );
        appendMessage(newMessage);
        editText.setText("");
    }

    private void openImagePicker() {
        if (isUploading || isRecording) {
            Toast.makeText(this, "Vui long hoan tat thao tac hien tai", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent pickImage = new Intent(Intent.ACTION_GET_CONTENT);
        pickImage.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(pickImage, "Chon anh"));
    }

    private void sendImageMessage(Uri imageUri) throws IOException {
        byte[] imageBytes = compressImageToJpegBytes(imageUri, IMAGE_MAX_DIMENSION, IMAGE_QUALITY);
        if (imageBytes.length == 0) {
            Toast.makeText(this, "Khong the nen anh", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageBytes.length > IMAGE_MAX_UPLOAD_BYTES) {
            Toast.makeText(this, getString(R.string.image_too_large), Toast.LENGTH_SHORT).show();
            return;
        }

        String imageName = "img_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = mediaStorageRef.child("images").child(imageName);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        showUploadState(true, "Dang tai anh: 0%");
        UploadTask uploadTask = imageRef.putBytes(imageBytes, metadata);
        currentUploadTask = uploadTask;
        currentUploadIsVoice = false;

        uploadTask
                .addOnProgressListener(taskSnapshot -> updateUploadProgress("Dang tai anh", taskSnapshot))
                .addOnFailureListener(e -> {
                    currentUploadTask = null;
                    showUploadState(false, "");
                    Toast.makeText(this, "Gui anh that bai", Toast.LENGTH_SHORT).show();
                })
                .addOnCanceledListener(() -> {
                    currentUploadTask = null;
                    showUploadState(false, "");
                    Toast.makeText(this, getString(R.string.upload_cancelled), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    currentUploadTask = null;
                                    timeStamp = System.currentTimeMillis();
                                    Message newMessage = new Message(
                                            MESSAGE_SENDER,
                                            "",
                                            timeStamp,
                                            0,
                                            Message.CONTENT_IMAGE,
                                            uri.toString(),
                                            0,
                                            dao
                                    );
                                    appendMessage(newMessage);
                                    showUploadState(false, "");
                                })
                                .addOnFailureListener(e -> {
                                    currentUploadTask = null;
                                    showUploadState(false, "");
                                    Toast.makeText(this, "Lay link anh that bai", Toast.LENGTH_SHORT).show();
                                })
                );
    }

    private void toggleVoiceRecording() {
        if (isUploading) {
            Toast.makeText(this, "Dang upload media, vui long doi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isRecording) {
            startVoiceRecording();
        } else {
            stopVoiceRecordingAndSend();
        }
    }

    private void startVoiceRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            return;
        }

        File outputFile = new File(getCacheDir(), "voice_" + System.currentTimeMillis() + ".m4a");
        recordingFilePath = outputFile.getAbsolutePath();

        mediaRecorder = new android.media.MediaRecorder();
        mediaRecorder.setAudioSource(android.media.MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(64000);
        mediaRecorder.setAudioSamplingRate(22050);
        mediaRecorder.setOutputFile(recordingFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();
            voiceRecordBtn.setImageResource(android.R.drawable.ic_media_pause);
            Toast.makeText(this, "Dang ghi am...", Toast.LENGTH_SHORT).show();
        } catch (IOException | RuntimeException ex) {
            releaseRecorder();
            Toast.makeText(this, "Khong the bat dau ghi am", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVoiceRecordingAndSend() {
        if (!isRecording) {
            return;
        }

        boolean stoppedSuccessfully = true;
        try {
            mediaRecorder.stop();
        } catch (RuntimeException ex) {
            stoppedSuccessfully = false;
        } finally {
            releaseRecorder();
            isRecording = false;
            voiceRecordBtn.setImageResource(android.R.drawable.ic_btn_speak_now);
        }

        if (!stoppedSuccessfully || recordingFilePath == null) {
            deleteRecordingFileIfExists();
            Toast.makeText(this, "File voice khong hop le", Toast.LENGTH_SHORT).show();
            return;
        }

        int durationMs = (int) (System.currentTimeMillis() - recordingStartTime);
        if (durationMs < VOICE_MIN_DURATION_MS) {
            deleteRecordingFileIfExists();
            Toast.makeText(this, "Voice qua ngan", Toast.LENGTH_SHORT).show();
            return;
        }
        if (durationMs > VOICE_MAX_DURATION_MS) {
            deleteRecordingFileIfExists();
            Toast.makeText(this, getString(R.string.voice_too_long), Toast.LENGTH_SHORT).show();
            return;
        }

        File voiceFile = new File(recordingFilePath);
        if (!voiceFile.exists() || voiceFile.length() > VOICE_MAX_FILE_BYTES) {
            deleteRecordingFileIfExists();
            Toast.makeText(this, "Voice qua lon, vui long thu lai", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadVoiceRecording(Uri.fromFile(voiceFile), durationMs);
    }

    private void uploadVoiceRecording(Uri voiceUri, int durationMs) {
        String voiceName = "voice_" + System.currentTimeMillis() + ".m4a";
        StorageReference voiceRef = mediaStorageRef.child("voices").child(voiceName);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("audio/mp4")
                .build();

        showUploadState(true, "Dang tai voice: 0%");
        UploadTask uploadTask = voiceRef.putFile(voiceUri, metadata);
        currentUploadTask = uploadTask;
        currentUploadIsVoice = true;
        uploadTask
                .addOnProgressListener(taskSnapshot -> updateUploadProgress("Dang tai voice", taskSnapshot))
                .addOnFailureListener(e -> {
                    currentUploadTask = null;
                    deleteRecordingFileIfExists();
                    showUploadState(false, "");
                    Toast.makeText(this, "Gui voice that bai", Toast.LENGTH_SHORT).show();
                })
                .addOnCanceledListener(() -> {
                    currentUploadTask = null;
                    deleteRecordingFileIfExists();
                    showUploadState(false, "");
                    Toast.makeText(this, getString(R.string.upload_cancelled), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(taskSnapshot ->
                        voiceRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    currentUploadTask = null;
                                    timeStamp = System.currentTimeMillis();
                                    Message newMessage = new Message(
                                            MESSAGE_SENDER,
                                            "",
                                            timeStamp,
                                            0,
                                            Message.CONTENT_VOICE,
                                            uri.toString(),
                                            durationMs,
                                            dao
                                    );
                                    appendMessage(newMessage);
                                    deleteRecordingFileIfExists();
                                    showUploadState(false, "");
                                })
                                .addOnFailureListener(e -> {
                                    currentUploadTask = null;
                                    deleteRecordingFileIfExists();
                                    showUploadState(false, "");
                                    Toast.makeText(this, "Lay link voice that bai", Toast.LENGTH_SHORT).show();
                                })
                );
    }

    private void showUploadState(boolean uploading, String statusText) {
        isUploading = uploading;
        if (uploading) {
            uploadStatusContainer.setVisibility(View.VISIBLE);
            uploadStatusText.setText(statusText);
            cancelUploadBtn.setEnabled(true);
        } else {
            uploadStatusContainer.setVisibility(View.GONE);
            uploadStatusText.setText("");
            cancelUploadBtn.setEnabled(false);
            currentUploadTask = null;
            currentUploadIsVoice = false;
        }

        sendBtn.setEnabled(!uploading);
        attachImageBtn.setEnabled(!uploading);
        voiceRecordBtn.setEnabled(!uploading);
    }

    private void updateUploadProgress(String prefix, UploadTask.TaskSnapshot taskSnapshot) {
        long total = taskSnapshot.getTotalByteCount();
        if (total <= 0) {
            uploadStatusText.setText(prefix + "...");
            return;
        }
        int progress = (int) ((taskSnapshot.getBytesTransferred() * 100) / total);
        uploadStatusText.setText(String.format(Locale.getDefault(), "%s: %d%%", prefix, progress));
    }

    private void appendMessage(Message newMessage) {
        SRMessages.add(newMessage);
        newMessage.save(receiverId);
        mAdaptor.notifyDataSetChanged();
        recyclerViewMessageLists.scrollToPosition(mAdaptor.getItemCount() - 1);
    }

    private byte[] compressImageToJpegBytes(Uri uri, int maxDimension, int quality) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;

        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) {
                return new byte[0];
            }
            BitmapFactory.decodeStream(input, null, bounds);
        }

        int inSampleSize = 1;
        int maxSize = Math.max(bounds.outWidth, bounds.outHeight);
        while (maxSize / inSampleSize > maxDimension * 2) {
            inSampleSize *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = inSampleSize;

        Bitmap bitmap;
        try (InputStream input = getContentResolver().openInputStream(uri)) {
            if (input == null) {
                return new byte[0];
            }
            bitmap = BitmapFactory.decodeStream(input, null, decodeOptions);
        }

        if (bitmap == null) {
            return new byte[0];
        }

        Bitmap resized = resizeBitmap(bitmap, maxDimension);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, quality, output);
        return output.toByteArray();
    }

    private Bitmap resizeBitmap(Bitmap source, int maxDimension) {
        int width = source.getWidth();
        int height = source.getHeight();
        int longest = Math.max(width, height);
        if (longest <= maxDimension) {
            return source;
        }

        float ratio = (float) maxDimension / (float) longest;
        int targetWidth = Math.round(width * ratio);
        int targetHeight = Math.round(height * ratio);
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void deleteRecordingFileIfExists() {
        if (recordingFilePath == null) {
            return;
        }
        File file = new File(recordingFilePath);
        if (file.exists()) {
            file.delete();
        }
        recordingFilePath = null;
    }

    private void cancelVoiceRecording() {
        if (!isRecording) {
            return;
        }

        try {
            mediaRecorder.stop();
        } catch (RuntimeException ignored) {
            // Ignore very short record errors while cancelling.
        } finally {
            releaseRecorder();
            isRecording = false;
            voiceRecordBtn.setImageResource(android.R.drawable.ic_btn_speak_now);
            deleteRecordingFileIfExists();
        }
    }

    private void cancelCurrentUpload() {
        if (!isUploading || currentUploadTask == null) {
            return;
        }
        currentUploadTask.cancel();
        if (currentUploadIsVoice) {
            deleteRecordingFileIfExists();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("id", receiverId);
        String lastMessage = SRMessages.size() == 0 ? "" : SRMessages.get(SRMessages.size() - 1).getPreviewText();
        intent.putExtra("lastMessage", lastMessage);
        intent.putExtra("timeStamp", timeStamp);
        intent.putExtra("recyclerViewItemId", recyclerViewItemId);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            cancelVoiceRecording();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCurrentUpload();
        releaseRecorder();
        if (mAdaptor != null) {
            mAdaptor.releaseAudioPlayer();
        }
    }

    public void refresh() {
        SRMessages = Message.load(dao, receiverId);
        if (SRMessages != null) {
            mAdaptor.updateData(SRMessages);
            recyclerViewMessageLists.scrollToPosition(Math.max(0, mAdaptor.getItemCount() - 1));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecording();
            } else {
                Toast.makeText(this, "Can cap quyen micro de gui voice", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
