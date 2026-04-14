package com.example.chatapp.conversation;

import com.example.chatapp.IChatInterface;

import java.util.ArrayList;

public class Message {
    public static final String CONTENT_TEXT = "text";
    public static final String CONTENT_IMAGE = "image";
    public static final String CONTENT_VOICE = "voice";

    private  String message;
    private long time;
    private int type;               //0 for sender 1 for receiver
    private String username;
    private String conversation_ID;
    private String contentType;
    private String mediaPayload;
    private int mediaDurationMs;
    private transient IChatInterface dao = null;

    public Message(String username, String message, long time, int type) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.username = username;
        this.conversation_ID = "";
        this.contentType = CONTENT_TEXT;
        this.mediaPayload = "";
        this.mediaDurationMs = 0;
    }

    public Message(String username, String message, long time, int type, IChatInterface dao) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.username = username;
        this.conversation_ID = "";
        this.contentType = CONTENT_TEXT;
        this.mediaPayload = "";
        this.mediaDurationMs = 0;
        this.dao = dao;
    }

    public Message(String username, String message, long time, int type, String conversation_ID) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.username = username;
        this.conversation_ID = conversation_ID;
        this.contentType = CONTENT_TEXT;
        this.mediaPayload = "";
        this.mediaDurationMs = 0;
    }

    public Message(String username, String message, long time, int type, String conversation_ID ,IChatInterface dao) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.username = username;
        this.conversation_ID = conversation_ID;
        this.contentType = CONTENT_TEXT;
        this.mediaPayload = "";
        this.mediaDurationMs = 0;
        this.dao = dao;
    }

    public Message(String username, String message, long time, int type, String contentType, String mediaPayload, int mediaDurationMs) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.username = username;
        this.conversation_ID = "";
        this.contentType = normalizeContentType(contentType);
        this.mediaPayload = mediaPayload == null ? "" : mediaPayload;
        this.mediaDurationMs = mediaDurationMs;
    }

    public Message(String username, String message, long time, int type, String contentType, String mediaPayload, int mediaDurationMs, IChatInterface dao) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.username = username;
        this.conversation_ID = "";
        this.contentType = normalizeContentType(contentType);
        this.mediaPayload = mediaPayload == null ? "" : mediaPayload;
        this.mediaDurationMs = mediaDurationMs;
        this.dao = dao;
    }

    public Message(String username, String message, long time, int type, String conversation_ID, String contentType, String mediaPayload, int mediaDurationMs) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.username = username;
        this.conversation_ID = conversation_ID;
        this.contentType = normalizeContentType(contentType);
        this.mediaPayload = mediaPayload == null ? "" : mediaPayload;
        this.mediaDurationMs = mediaDurationMs;
    }

    public Message(String username, String message, long time, int type, String conversation_ID, String contentType, String mediaPayload, int mediaDurationMs, IChatInterface dao) {
        this.message = message;
        this.time = time;
        this.type = type;
        this.username = username;
        this.conversation_ID = conversation_ID;
        this.contentType = normalizeContentType(contentType);
        this.mediaPayload = mediaPayload == null ? "" : mediaPayload;
        this.mediaDurationMs = mediaDurationMs;
        this.dao = dao;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = normalizeContentType(contentType);
    }

    public String getMediaPayload() {
        return mediaPayload;
    }

    public void setMediaPayload(String mediaPayload) {
        this.mediaPayload = mediaPayload == null ? "" : mediaPayload;
    }

    public int getMediaDurationMs() {
        return mediaDurationMs;
    }

    public void setMediaDurationMs(int mediaDurationMs) {
        this.mediaDurationMs = mediaDurationMs;
    }

    public boolean isText() {
        return CONTENT_TEXT.equals(contentType);
    }

    public boolean isImage() {
        return CONTENT_IMAGE.equals(contentType);
    }

    public boolean isVoice() {
        return CONTENT_VOICE.equals(contentType);
    }

    public String getPreviewText() {
        if (isImage()) {
            return "[Anh]";
        }
        if (isVoice()) {
            return "[Voice]";
        }
        return message == null ? "" : message;
    }

    public void save(String conversationId){
        if (dao != null){
            Message m = new Message(username, message, time, type, contentType, mediaPayload, mediaDurationMs);
            //save in database
            dao.saveMessage(m,conversationId);
        }
    }

    public void load(Message message){
        if(message != null){
            username = message.getUsername();
            this.message = message.getMessage();
            time = message.getTime();
            type = message.getType();
            conversation_ID = message.getConversation_ID();
            contentType = normalizeContentType(message.getContentType());
            mediaPayload = message.getMediaPayload();
            mediaDurationMs = message.getMediaDurationMs();
        }
    }

    public static ArrayList<Message> load(IChatInterface dao,String receiverId){
        ArrayList<Message> messages = new ArrayList<Message>();
        if(dao != null){
            ArrayList<Message> objects = dao.loadMessageList(receiverId);
            for(Message obj : objects){
                Message message1 = new Message(
                        obj.getUsername(),
                        obj.getMessage(),
                        obj.getTime(),
                        obj.getType(),
                        obj.getConversation_ID(),
                        obj.getContentType(),
                        obj.getMediaPayload(),
                        obj.getMediaDurationMs(),
                        dao
                );
                messages.add(message1);
            }
        }
        return messages;
    }

    public String getConversation_ID() {
        return conversation_ID;
    }

    private static String normalizeContentType(String raw) {
        if (CONTENT_IMAGE.equals(raw) || CONTENT_VOICE.equals(raw)) {
            return raw;
        }
        return CONTENT_TEXT;
    }
}
