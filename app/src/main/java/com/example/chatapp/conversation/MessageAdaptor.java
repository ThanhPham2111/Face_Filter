package com.example.chatapp.conversation;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdaptor extends RecyclerView.Adapter<MessageAdaptor.MessageViewHolder> {
    private ArrayList<Message> messages;
    private static final int[] AVATARS = new int[]{
            R.drawable.ic_avatar_cat_pastel,
            R.drawable.ic_avatar_bunny_pastel,
            R.drawable.ic_avatar_bear_pastel
    };

    private MediaPlayer mediaPlayer;
    private File currentAudioFile;
    private int playingPosition = RecyclerView.NO_POSITION;
    private int preparingPosition = RecyclerView.NO_POSITION;
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && playingPosition != RecyclerView.NO_POSITION && mediaPlayer.isPlaying()) {
                if (playingPosition < 0 || playingPosition >= getItemCount()) {
                    stopPlayback();
                    return;
                }
                notifyItemChanged(playingPosition);
                progressHandler.postDelayed(this, 250);
            }
        }
    };

    public MessageAdaptor(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getType() == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;

        if (viewType == 0) {
            layoutId = R.layout.sender_message_layout;
        } else {
            layoutId = R.layout.receiver_message_layout;
        }

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layoutId, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message item = messages.get(position);
        String title = item.getUsername() == null ? "" : item.getUsername();
        long time = item.getTime();

        Date dateTime = new Date(time);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatted = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String formattedTime = formatted.format(dateTime);
        int avatarIndex = Math.abs(title.hashCode()) % AVATARS.length;

        if (item.getType() == 0) {
            bindSender(holder, item, title, formattedTime, avatarIndex, position);
        } else {
            bindReceiver(holder, item, title, formattedTime, avatarIndex, position);
        }
    }

    private void bindSender(@NonNull MessageViewHolder holder, Message item, String title, String time, int avatarIndex, int position) {
        holder.senderIcon.setImageResource(AVATARS[avatarIndex]);
        holder.senderTextTitle.setText(title);
        holder.senderTextTime.setText(time);

        resetSender(holder);
        if (item.isImage()) {
            holder.senderImage.setVisibility(View.VISIBLE);
            bindImagePayload(holder.senderImage, item.getMediaPayload());
            return;
        }

        if (item.isVoice()) {
            holder.senderVoiceLayout.setVisibility(View.VISIBLE);
            bindVoiceMeta(
                    holder.senderVoiceProgress,
                    holder.senderVoiceElapsed,
                    holder.senderVoiceDuration,
                    item,
                    position
            );
            bindVoiceAction(holder.senderVoicePlayBtn, item.getMediaPayload(), position);
            syncPlayButtonState(holder.senderVoicePlayBtn, position);
            return;
        }

        holder.senderTextMessage.setVisibility(View.VISIBLE);
        holder.senderTextMessage.setText(item.getMessage());
    }

    private void bindReceiver(@NonNull MessageViewHolder holder, Message item, String title, String time, int avatarIndex, int position) {
        holder.receiverIcon.setImageResource(AVATARS[avatarIndex]);
        holder.receiverTextTitle.setText(title);
        holder.receiverTextTime.setText(time);

        resetReceiver(holder);
        if (item.isImage()) {
            holder.receiverImage.setVisibility(View.VISIBLE);
            bindImagePayload(holder.receiverImage, item.getMediaPayload());
            return;
        }

        if (item.isVoice()) {
            holder.receiverVoiceLayout.setVisibility(View.VISIBLE);
            bindVoiceMeta(
                    holder.receiverVoiceProgress,
                    holder.receiverVoiceElapsed,
                    holder.receiverVoiceDuration,
                    item,
                    position
            );
            bindVoiceAction(holder.receiverVoicePlayBtn, item.getMediaPayload(), position);
            syncPlayButtonState(holder.receiverVoicePlayBtn, position);
            return;
        }

        holder.receiverTextMessage.setVisibility(View.VISIBLE);
        holder.receiverTextMessage.setText(item.getMessage());
    }

    private void resetSender(@NonNull MessageViewHolder holder) {
        holder.senderTextMessage.setVisibility(View.GONE);
        holder.senderImage.setVisibility(View.GONE);
        holder.senderVoiceLayout.setVisibility(View.GONE);
        holder.senderVoicePlayBtn.setImageResource(android.R.drawable.ic_media_play);
        holder.senderVoiceProgress.setProgress(0);
        holder.senderVoiceElapsed.setText("0:00");
    }

    private void resetReceiver(@NonNull MessageViewHolder holder) {
        holder.receiverTextMessage.setVisibility(View.GONE);
        holder.receiverImage.setVisibility(View.GONE);
        holder.receiverVoiceLayout.setVisibility(View.GONE);
        holder.receiverVoicePlayBtn.setImageResource(android.R.drawable.ic_media_play);
        holder.receiverVoiceProgress.setProgress(0);
        holder.receiverVoiceElapsed.setText("0:00");
    }

    private void syncPlayButtonState(ImageButton button, int position) {
        if (position == playingPosition || position == preparingPosition) {
            button.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            button.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void bindVoiceAction(ImageButton button, String mediaPayload, int position) {
        button.setOnClickListener(v -> {
            if (mediaPayload == null || mediaPayload.isEmpty()) {
                return;
            }

            if ((position == playingPosition || position == preparingPosition) && mediaPlayer != null) {
                stopPlayback();
                notifyItemChanged(position);
                return;
            }

            int previousPosition = playingPosition != RecyclerView.NO_POSITION ? playingPosition : preparingPosition;
            stopPlayback();
            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }

            startPlayback(mediaPayload, position, button);
        });
    }

    private void startPlayback(String mediaPayload, int position, ImageButton button) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                int failedPos = playingPosition != RecyclerView.NO_POSITION ? playingPosition : preparingPosition;
                stopPlayback();
                if (failedPos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(failedPos);
                }
                return true;
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                int finishedPos = playingPosition;
                stopPlayback();
                if (finishedPos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(finishedPos);
                }
            });

            if (isUrlPayload(mediaPayload)) {
                preparingPosition = position;
                notifyItemChanged(position);
                mediaPlayer.setDataSource(mediaPayload);
                mediaPlayer.setOnPreparedListener(mp -> {
                    if (mediaPlayer != mp) {
                        return;
                    }
                    preparingPosition = RecyclerView.NO_POSITION;
                    playingPosition = position;
                    mp.start();
                    startProgressUpdates();
                    notifyItemChanged(position);
                });
                mediaPlayer.prepareAsync();
                return;
            }

            byte[] audioBytes = Base64.decode(mediaPayload, Base64.DEFAULT);
            currentAudioFile = File.createTempFile("voice_", ".m4a", button.getContext().getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(currentAudioFile)) {
                fos.write(audioBytes);
            }
            mediaPlayer.setDataSource(currentAudioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            playingPosition = position;
            startProgressUpdates();
            notifyItemChanged(position);
        } catch (IOException | IllegalArgumentException ex) {
            stopPlayback();
        }
    }

    private void bindVoiceMeta(ProgressBar progressBar, TextView elapsedView, TextView durationView, Message item, int position) {
        int totalMs = Math.max(item.getMediaDurationMs(), 0);
        int elapsedMs = 0;
        int progress = 0;

        if (position == playingPosition && mediaPlayer != null) {
            int mediaDuration = mediaPlayer.getDuration();
            if (mediaDuration > 0) {
                totalMs = mediaDuration;
            }
            elapsedMs = Math.max(0, mediaPlayer.getCurrentPosition());
            if (totalMs > 0) {
                progress = Math.min(1000, (elapsedMs * 1000) / totalMs);
            }
        }

        progressBar.setProgress(progress);
        elapsedView.setText(formatDuration(elapsedMs));
        durationView.setText(formatDuration(totalMs));
    }

    private void bindImagePayload(ImageView target, String mediaPayload) {
        if (isUrlPayload(mediaPayload)) {
            Glide.with(target).load(mediaPayload).into(target);
            return;
        }
        target.setImageBitmap(decodeBase64Image(mediaPayload));
    }

    private boolean isUrlPayload(String payload) {
        return payload != null && (payload.startsWith("http://") || payload.startsWith("https://"));
    }

    private Bitmap decodeBase64Image(String mediaPayload) {
        if (mediaPayload == null || mediaPayload.isEmpty()) {
            return null;
        }

        byte[] raw = Base64.decode(mediaPayload, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(raw, 0, raw.length, options);

        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, 720, 720);
        return BitmapFactory.decodeByteArray(raw, 0, raw.length, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private String formatDuration(int durationMs) {
        int totalSeconds = Math.max(0, durationMs / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateData(ArrayList<Message> ds) {
        messages = ds;
        notifyDataSetChanged();
    }

    public void releaseAudioPlayer() {
        stopPlayback();
    }

    private void startProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable);
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdates() {
        progressHandler.removeCallbacks(progressRunnable);
    }

    private void stopPlayback() {
        stopProgressUpdates();
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
                // Ignore invalid states when stopping player.
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (currentAudioFile != null && currentAudioFile.exists()) {
            currentAudioFile.delete();
        }
        currentAudioFile = null;
        playingPosition = RecyclerView.NO_POSITION;
        preparingPosition = RecyclerView.NO_POSITION;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        ImageView senderIcon;
        TextView senderTextMessage;
        TextView senderTextTitle;
        TextView senderTextTime;
        ImageView senderImage;
        LinearLayout senderVoiceLayout;
        ImageButton senderVoicePlayBtn;
        ProgressBar senderVoiceProgress;
        TextView senderVoiceElapsed;
        TextView senderVoiceDuration;

        ImageView receiverIcon;
        TextView receiverTextMessage;
        TextView receiverTextTitle;
        TextView receiverTextTime;
        ImageView receiverImage;
        LinearLayout receiverVoiceLayout;
        ImageButton receiverVoicePlayBtn;
        ProgressBar receiverVoiceProgress;
        TextView receiverVoiceElapsed;
        TextView receiverVoiceDuration;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderIcon = itemView.findViewById(R.id.sender_image);
            senderTextMessage = itemView.findViewById(R.id.sender_text);
            senderTextTitle = itemView.findViewById(R.id.sender_username);
            senderTextTime = itemView.findViewById(R.id.sender_time);
            senderImage = itemView.findViewById(R.id.sender_media_image);
            senderVoiceLayout = itemView.findViewById(R.id.sender_voice_layout);
            senderVoicePlayBtn = itemView.findViewById(R.id.sender_voice_play);
            senderVoiceProgress = itemView.findViewById(R.id.sender_voice_progress);
            senderVoiceElapsed = itemView.findViewById(R.id.sender_voice_elapsed);
            senderVoiceDuration = itemView.findViewById(R.id.sender_voice_duration);

            receiverIcon = itemView.findViewById(R.id.receiver_image);
            receiverTextMessage = itemView.findViewById(R.id.receiver_text);
            receiverTextTitle = itemView.findViewById(R.id.receiver_username);
            receiverTextTime = itemView.findViewById(R.id.receiver_time);
            receiverImage = itemView.findViewById(R.id.receiver_media_image);
            receiverVoiceLayout = itemView.findViewById(R.id.receiver_voice_layout);
            receiverVoicePlayBtn = itemView.findViewById(R.id.receiver_voice_play);
            receiverVoiceProgress = itemView.findViewById(R.id.receiver_voice_progress);
            receiverVoiceElapsed = itemView.findViewById(R.id.receiver_voice_elapsed);
            receiverVoiceDuration = itemView.findViewById(R.id.receiver_voice_duration);
        }
    }
}
