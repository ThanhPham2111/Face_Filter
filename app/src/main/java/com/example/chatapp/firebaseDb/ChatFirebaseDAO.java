package com.example.chatapp.firebaseDb;

import static com.example.chatapp.Globals.CHAT_DB;
import static com.example.chatapp.Globals.CONVERSATION_TABLE;
import static com.example.chatapp.Globals.C_COLUMN_LAST_MESSAGE;
import static com.example.chatapp.Globals.C_COLUMN_MESSAGE_TYPE;
import static com.example.chatapp.Globals.C_COLUMN_NAME;
import static com.example.chatapp.Globals.C_COLUMN_TIMESTAMP;
import static com.example.chatapp.Globals.Full_Name;
import static com.example.chatapp.Globals.MESSAGE_SENDER;
import static com.example.chatapp.Globals.MESSAGE_TABLE;
import static com.example.chatapp.Globals.M_COLUMN_CONTENT_TYPE;
import static com.example.chatapp.Globals.M_COLUMN_C_ID;
import static com.example.chatapp.Globals.M_COLUMN_DETAIL;
import static com.example.chatapp.Globals.M_COLUMN_IS_SENDER;
import static com.example.chatapp.Globals.M_COLUMN_MEDIA_DURATION;
import static com.example.chatapp.Globals.M_COLUMN_MEDIA_PAYLOAD;
import static com.example.chatapp.Globals.M_COLUMN_TIME;
import static com.example.chatapp.Globals.M_COLUMN_USERNAME;
import static com.example.chatapp.Globals.extractUserIdFromEmail;
import static com.example.chatapp.Globals.formatPhoneNumber;

import android.util.Log;

import com.example.chatapp.IChatInterface;
import com.example.chatapp.MessageType;
import com.example.chatapp.Person;
import com.example.chatapp.conversation.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ChatFirebaseDAO implements IChatInterface {

    public interface DataObserver{
        public void update();
    }

    private DataObserver observer;
    FirebaseDatabase database;
    DatabaseReference myRef;

    ArrayList<Person> personArrayList;
    ArrayList<Message> messageArrayList;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String userPhoneNumber;
    String userName;

    private String normalizeConversationId(String rawId) {
        if (rawId == null) {
            return "";
        }
        String formatted = formatPhoneNumber(rawId);
        if (!"-1".equals(formatted)) {
            return formatted;
        }
        return rawId.trim();
    }

    private String readStringValue(DataSnapshot parent, String key, String fallback) {
        Object raw = parent.child(key).getValue();
        if (raw == null) {
            return fallback;
        }
        return String.valueOf(raw);
    }

    private long readLongValue(DataSnapshot parent, String key, long fallback) {
        Object raw = parent.child(key).getValue();
        if (raw == null) {
            return fallback;
        }
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(raw));
        } catch (NumberFormatException ignore) {
            return fallback;
        }
    }

    private int readIntValue(DataSnapshot parent, String key, int fallback) {
        Object raw = parent.child(key).getValue();
        if (raw == null) {
            return fallback;
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException ignore) {
            return fallback;
        }
    }

    public ChatFirebaseDAO(DataObserver obs){

        //firebase auth and user
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        
        // Check if user is authenticated
        if (firebaseUser == null) {
            Log.e("firebasedb", "User is not authenticated. Cannot initialize ChatFirebaseDAO.");
            observer = obs;
            database = FirebaseDatabase.getInstance();
            return;
        }
        
        String email = firebaseUser.getEmail();
        if (email == null || email.trim().isEmpty()) {
            Log.e("firebasedb", "Invalid email format from Firebase user.");
            observer = obs;
            database = FirebaseDatabase.getInstance();
            return;
        }

        userPhoneNumber = extractUserIdFromEmail(email);
        if (userPhoneNumber.isEmpty()) {
            Log.e("firebasedb", "Cannot resolve user id from Firebase email.");
            observer = obs;
            database = FirebaseDatabase.getInstance();
            return;
        }

        observer = obs;
        database = FirebaseDatabase.getInstance();

        //setting full name
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(Full_Name);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    userName = dataSnapshot.getValue(String.class);
                    MESSAGE_SENDER = userName;
                    observer.update();
                    Log.d("yyyyy",userName);
                }
                catch (Exception ex) {
                    Log.e("firebasedb", ex.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("firebasedb", "Failed to read value.", error.toException());
            }
        });

        //Load Conversations/Persons
        //handling conversation class
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(CONVERSATION_TABLE);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    personArrayList = new ArrayList<>();

                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String conversationId = childSnapshot.getKey();
                        String lastMessage = readStringValue(childSnapshot, C_COLUMN_LAST_MESSAGE, "");
                        String personName = readStringValue(childSnapshot, C_COLUMN_NAME, "");
                        long timestamp = readLongValue(childSnapshot, C_COLUMN_TIMESTAMP, 0L);
                        String messageType = readStringValue(childSnapshot, C_COLUMN_MESSAGE_TYPE, MessageType.SENT.toString());
                        if (conversationId == null) {
                            continue;
                        }
                        conversationId = normalizeConversationId(conversationId);
                        if (lastMessage == null) {
                            lastMessage = "";
                        }
                        if (personName == null || personName.trim().isEmpty()) {
                            personName = conversationId;
                        }
                        if (messageType == null || messageType.trim().isEmpty()) {
                            messageType = MessageType.SENT.toString();
                        }

                        //store in array list
//                        int id = Integer.parseInt(conversationId.substring(2, conversationId.length()));
                        Person person = new Person(conversationId,personName, lastMessage, timestamp,messageType);

                        // Finally, you can add this conversation object to an ArrayList.
                        personArrayList.add(person);
                    }

                    observer.update();
                }
                catch (Exception ex) {
                    Log.e("firebasedb", ex.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("firebasedb", "Failed to read value.", error.toException());
            }
        });

        //Load Messages
        //handling messages class
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(MESSAGE_TABLE);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    messageArrayList = new ArrayList<>();

                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        String messsageUsername = readStringValue(childSnapshot, M_COLUMN_USERNAME, "");
                        String messageDetail = readStringValue(childSnapshot, M_COLUMN_DETAIL, "");
                        long messageTime = readLongValue(childSnapshot, M_COLUMN_TIME, System.currentTimeMillis());
                        int messageIsSender = readIntValue(childSnapshot, M_COLUMN_IS_SENDER, 0);
                        String messagePersonId = readStringValue(childSnapshot, M_COLUMN_C_ID, "");
                        String contentType = readStringValue(childSnapshot, M_COLUMN_CONTENT_TYPE, Message.CONTENT_TEXT);
                        String mediaPayload = readStringValue(childSnapshot, M_COLUMN_MEDIA_PAYLOAD, "");
                        int mediaDuration = readIntValue(childSnapshot, M_COLUMN_MEDIA_DURATION, 0);
                        if (messageDetail == null) {
                            messageDetail = "";
                        }
                        if (messagePersonId == null) {
                            messagePersonId = "";
                        }
                        messagePersonId = normalizeConversationId(messagePersonId);

                        //store in array list
//                        int id = Integer.parseInt(conversationId.substring(2, conversationId.length()));
                        Message message = new Message(
                                messsageUsername,
                                messageDetail,
                                messageTime,
                                messageIsSender,
                                messagePersonId,
                                contentType,
                                mediaPayload,
                                mediaDuration
                        );

                        // Finally, you can add this conversation object to an ArrayList.
                        messageArrayList.add(message);
                    }

                    observer.update();
                }
                catch (Exception ex) {
                    Log.e("firebasedb", ex.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("firebasedb", "Failed to read value.", error.toException());
            }
        });

    }


    @Override
    public void savePerson(Person person) {
        String personId = normalizeConversationId(person.getId());
        if (personId.isEmpty()) {
            Log.e("firebasedb", "Cannot save person: empty id");
            return;
        }
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(CONVERSATION_TABLE);
        //making hashmap
        Map<String, Object> childObject = new HashMap<>();
        childObject.put(C_COLUMN_NAME, person.getName());
        childObject.put(C_COLUMN_LAST_MESSAGE, person.getLastMessage());
        childObject.put(C_COLUMN_TIMESTAMP, person.getTimeStamp());
        childObject.put(C_COLUMN_MESSAGE_TYPE, person.getMessageType());

        myRef.child(personId).setValue(childObject);
    }

    @Override
    public ArrayList<Person> loadPersonList() {
        if(personArrayList == null){
            personArrayList = new ArrayList<>();
        }
        personArrayList.sort(Comparator.comparingLong(Person::getTimeStamp).reversed());
        return personArrayList;
    }

    @Override
    public void deleteOnePerson(String id) {
        String normalizedId = normalizeConversationId(id);
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(CONVERSATION_TABLE).child(normalizedId);
        myRef.removeValue();
    }

    @Override
    public void deleteAllPersons() {
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(CONVERSATION_TABLE);
        myRef.removeValue();
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(MESSAGE_TABLE);
        myRef.removeValue();
    }

    @Override
    public void updatePersonConversation(Person person) {
        String personId = normalizeConversationId(person.getId());
        if (personId.isEmpty()) {
            Log.e("firebasedb", "Cannot update person: empty id");
            return;
        }
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(CONVERSATION_TABLE);
        //making hashmap
        Map<String, Object> childObject = new HashMap<>();
        childObject.put(C_COLUMN_NAME, person.getName());
        childObject.put(C_COLUMN_LAST_MESSAGE, person.getLastMessage());
        childObject.put(C_COLUMN_TIMESTAMP, person.getTimeStamp());
        childObject.put(C_COLUMN_MESSAGE_TYPE, person.getMessageType());

        myRef.child(personId).updateChildren(childObject);
    }

    @Override
    public void saveMessage(Message message, String conversationID) {
        String targetConversationId = normalizeConversationId(conversationID);
        if (targetConversationId.isEmpty()) {
            Log.e("firebasedb", "Cannot save message: empty conversationID");
            return;
        }
        if (userPhoneNumber == null || userPhoneNumber.trim().isEmpty()) {
            Log.e("firebasedb", "Cannot save message: missing sender user id");
            return;
        }
        if (userName == null || userName.trim().isEmpty()) {
            userName = userPhoneNumber;
        }

        //add messsage at sender side
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(MESSAGE_TABLE);
        String messageId = UUID.randomUUID().toString();
        Map<String, Object> childObject = new HashMap<>();
        childObject.put(M_COLUMN_USERNAME, userName);
        childObject.put(M_COLUMN_DETAIL, message.getMessage());
        childObject.put(M_COLUMN_TIME, message.getTime());
        childObject.put(M_COLUMN_IS_SENDER, 0);
        childObject.put(M_COLUMN_C_ID, targetConversationId);
        childObject.put(M_COLUMN_CONTENT_TYPE, message.getContentType());
        childObject.put(M_COLUMN_MEDIA_PAYLOAD, message.getMediaPayload());
        childObject.put(M_COLUMN_MEDIA_DURATION, message.getMediaDurationMs());
        myRef.child(messageId).setValue(childObject);

        //add messsage at receiver side
        myRef = database.getReference().child(CHAT_DB).child(targetConversationId).child(MESSAGE_TABLE);
        String messageId2 = UUID.randomUUID().toString();
        Map<String, Object> childObject2 = new HashMap<>();
        childObject2.put(M_COLUMN_USERNAME, userName);
        childObject2.put(M_COLUMN_DETAIL, message.getMessage());
        childObject2.put(M_COLUMN_TIME, message.getTime());
        childObject2.put(M_COLUMN_IS_SENDER, 1);
        childObject2.put(M_COLUMN_C_ID, userPhoneNumber);
        childObject2.put(M_COLUMN_CONTENT_TYPE, message.getContentType());
        childObject2.put(M_COLUMN_MEDIA_PAYLOAD, message.getMediaPayload());
        childObject2.put(M_COLUMN_MEDIA_DURATION, message.getMediaDurationMs());

        myRef.child(messageId2).setValue(childObject2);

        //update sender conversation row
        myRef = database.getReference().child(CHAT_DB).child(userPhoneNumber).child(CONVERSATION_TABLE);
        //making hashmap
        Map<String, Object> childObject3 = new HashMap<>();
        childObject3.put(C_COLUMN_LAST_MESSAGE, message.getPreviewText());
        childObject3.put(C_COLUMN_TIMESTAMP, System.currentTimeMillis());
        childObject3.put(C_COLUMN_MESSAGE_TYPE, MessageType.SENT.toString());

        myRef.child(targetConversationId).updateChildren(childObject3);


        //update receiver conversation row
        myRef = database.getReference().child(CHAT_DB).child(targetConversationId).child(CONVERSATION_TABLE);
        //making hashmap
        Map<String, Object> childObject4 = new HashMap<>();
        childObject4.put(C_COLUMN_NAME, userName);
        childObject4.put(C_COLUMN_LAST_MESSAGE, message.getPreviewText());
        childObject4.put(C_COLUMN_TIMESTAMP, System.currentTimeMillis());
        childObject4.put(C_COLUMN_MESSAGE_TYPE, MessageType.RECEIVED.toString());

        myRef.child(userPhoneNumber).updateChildren(childObject4);

    }

    @Override
    public ArrayList<Message> loadMessageList(String CID) {
        String normalizedCid = normalizeConversationId(CID);
        ArrayList<Message> filteredMessageList;
        filteredMessageList = new ArrayList<>();
        if(messageArrayList == null){
            messageArrayList = new ArrayList<>();
        }else{
            for (Message m :messageArrayList) {
                String normalizedMessageCid = normalizeConversationId(m.getConversation_ID());
                if(Objects.equals(m.getConversation_ID(), CID)
                        || Objects.equals(m.getConversation_ID(), normalizedCid)
                        || Objects.equals(normalizedMessageCid, normalizedCid)){
                    filteredMessageList.add(m);
                }
            }
        }
        filteredMessageList.sort(Comparator.comparingLong(Message::getTime));
        return filteredMessageList;
    }
}
