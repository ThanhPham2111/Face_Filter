# 📚 Kiến Thức Cần Thiết - Xây Dựng Ứng Dụng Chat Android

> **Tài liệu này tổng hợp các kiến thức lập trình cần nắm vững để xây dựng dự án Face_Filter**

---

## 📑 Mục Lục

1. [Android Fundamentals](#android-fundamentals)
2. [Java Concepts](#java-concepts)
3. [Firebase & Backend](#firebase--backend)
4. [Database & Storage](#database--storage)
5. [UI/UX Design Patterns](#uiux-design-patterns)
6. [Architecture Patterns](#architecture-patterns)
7. [Real-time Communication](#real-time-communication)
8. [Testing & Debugging](#testing--debugging)

---

## 🤖 Android Fundamentals

### 1. Project Structure & Gradle

#### Gradle Build System
```
Project Structure:
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          (Java source code)
│   │   │   ├── res/           (Resources: layouts, strings, images)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/              (Unit tests)
│   │   └── androidTest/       (Instrumented tests)
│   └── build.gradle           (App configuration)
├── build.gradle               (Project configuration)
└── settings.gradle            (Project settings)
```

**Gradle Build Files**:
```gradle
// build.gradle (Project)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.google.gms:google-services:4.3.15'
    }
}

// build.gradle (App)
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services'  // Firebase plugin
}

android {
    compileSdk 33          // Target SDK version
    
    defaultConfig {
        applicationId "com.example.chatapp"
        minSdk 24          // Minimum supported Android version
        targetSdk 33       // Target Android version
        versionCode 1      // Internal version number
        versionName "1.0"  // User-facing version
    }
    
    buildTypes {
        release {
            minifyEnabled true  // Enable code shrinking
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt')
        }
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
}
```

**Key Concepts**:
- `compileSdk`: SDK version used to compile code
- `minSdk`: Oldest Android version supported
- `targetSdk`: Optimized for this Android version
- `dependencies`: External libraries imported

---

### 2. Activities & Lifecycle

#### Activity Lifecycle
```
onCreate()          ← Activity được tạo (first time)
        ↓
onStart()           ← Activity trở nên visible
        ↓
onResume()          ← Activity nhận focus (running state)
        │
        ├─→ User navigates away
        │
        ↓
onPause()           ← Activity bị mất focus (still visible)
        ↓
onStop()            ← Activity bị ẩn hoàn toàn
        │
        ├─→ User returns to Activity
        ├─→ onCreate() → onStart() → onResume()
        │
        ├─→ User leaves app/Activity destroyed
        │
        ↓
onDestroy()         ← Activity bị hủy
```

#### Lifecycle Methods trong Chat App
```java
public class ConversationDetailActivity extends AppCompatActivity {
    
    private DatabaseReference messagesRef;
    private ValueEventListener messageListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_detail);
        
        // 1. Initialize views
        recyclerView = findViewById(R.id.messages_recycler);
        editText = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        
        // 2. Setup LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // 3. Setup adapter
        adapter = new MessageAdapter();
        recyclerView.setAdapter(adapter);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // 2. Add firebase listener khi activity visible
        messagesRef.addValueEventListener(messageListener);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 3. Update UI, restore state
        adapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 4. Save temporary state
        saveDraft();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // 5. Remove listeners to prevent memory leak
        messagesRef.removeEventListener(messageListener);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 6. Cleanup resources
        adapter = null;
        messagesRef = null;
    }
}
```

**Important Notes**:
- OnCreate: Called once, setup UI
- OnStart: Called every time activity becomes visible
- OnResume: Called when activity gets focus
- OnPause: Called immediately, save state
- OnStop: Called when activity is not visible
- OnDestroy: Called before activity is destroyed
- **Always remove listeners in onStop() to prevent memory leaks!**

---

### 3. Fragments

#### Fragment Basics
```java
public class ConversationListFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, 
                            ViewGroup container, 
                            Bundle savedInstanceState) {
        // 1. Inflate layout
        return inflater.inflate(R.layout.fragment_conversation_list, 
                               container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 2. Initialize views AFTER layout is inflated
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 3. Load data
        loadConversations();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up views
        recyclerView = null;
    }
}
```

**Fragment Lifecycle**:
```
onAttach()
    ↓
onCreate()
    ↓
onCreateView()
    ↓
onViewCreated()
    ↓
onStart()
    ↓
onResume()
    ↓
onPause()
    ↓
onStop()
    ↓
onDestroyView()
    ↓
onDestroy()
    ↓
onDetach()
```

**Fragment vs Activity**:
| Activity | Fragment |
|----------|----------|
| Full screen component | Reusable part of UI |
| Can exist independently | Must be hosted in Activity |
| Has lifecycle | Has extended lifecycle |
| Managed by OS | Managed by Activity |

---

### 4. Intents & Navigation

#### Explicit Intent (Navigate to specific activity)
```java
// Start LoginActivity from SplashActivity
Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
startActivity(intent);

// Pass data
Intent intent = new Intent(MainActivity.this, ConversationDetailActivity.class);
intent.putExtra("conversation_id", "conv_123");
intent.putExtra("user_name", "John Doe");
startActivity(intent);

// Receive data in destination activity
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    Intent intent = getIntent();
    String conversationId = intent.getStringExtra("conversation_id");
    String userName = intent.getStringExtra("user_name");
}
```

#### Implicit Intent (Let system choose app)
```java
// Open URL
Intent intent = new Intent(Intent.ACTION_VIEW);
intent.setData(Uri.parse("https://www.example.com"));
startActivity(intent);

// Send email
Intent intent = new Intent(Intent.ACTION_SEND);
intent.setType("message/rfc822");
intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"email@example.com"});
intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
startActivity(Intent.createChooser(intent, "Send Email"));
```

---

### 5. Services & Background Tasks

#### Service Types

**Foreground Service** (chạy trong foreground, người dùng biết):
```java
public class ChatForegroundService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create notification
        Notification notification = createNotification();
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, notification);
        
        // Do work
        listenForMessages();
        
        return START_STICKY; // Restart if killed
    }
}
```

**Background Service** (chạy invisible):
```java
public class SyncService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Do background work
        syncMessagesWithServer();
        
        // Stop này service khi xong
        stopSelf();
        
        return START_NOT_STICKY;
    }
}
```

**WorkManager** (Preferred for scheduled tasks):
```java
// Schedule background sync every 15 minutes
PeriodicWorkRequest syncWork = new PeriodicWorkRequest
    .Builder(MessageSyncWorker.class, 15, TimeUnit.MINUTES)
    .build();

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "message_sync",
    ExistingPeriodicWorkPolicy.KEEP,
    syncWork
);
```

---

### 6. Permissions

#### Runtime Permissions (Android 6.0+)
```java
// Check if permission is granted
if (ContextCompat.checkSelfPermission(this, 
        Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED) {
    
    // Permission is not granted, request it
    ActivityCompat.requestPermissions(this,
        new String[]{Manifest.permission.READ_CONTACTS},
        PERMISSION_REQUEST_CODE);
}

// Handle permission result
@Override
public void onRequestPermissionsResult(int requestCode, 
        String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    
    if (requestCode == PERMISSION_REQUEST_CODE) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
            loadContacts();
        } else {
            // Permission denied
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
```

#### Declare in AndroidManifest.xml
```xml
<!-- Declare permission -->
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
```

---

## ☕ Java Concepts

### 1. Object-Oriented Programming (OOP)

#### Encapsulation
```java
public class Person {
    // Private data (hidden)
    private String name;
    private String email;
    private int age;
    
    // Public getter/setter (controlled access)
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        if (name != null && !name.isEmpty()) {
            this.name = name;
        }
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        if (age > 0 && age < 150) {
            this.age = age;
        }
    }
}
```

#### Inheritance
```java
// Parent/Base class
public class User {
    protected String id;
    protected String email;
    
    public void login() {
        System.out.println("Logging in...");
    }
}

// Child class inherits from User
public class ChatUser extends User {
    private String status;
    
    @Override
    public void login() {
        super.login();  // Call parent method
        this.status = "ONLINE";
        System.out.println("Chat user logged in");
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
```

#### Polymorphism
```java
public interface MessageHandler {
    void handleMessage(Message msg);
}

public class TextMessageHandler implements MessageHandler {
    @Override
    public void handleMessage(Message msg) {
        System.out.println("Text: " + msg.getContent());
    }
}

public class ImageMessageHandler implements MessageHandler {
    @Override
    public void handleMessage(Message msg) {
        System.out.println("Loading image: " + msg.getImageUrl());
    }
}

// Usage
MessageHandler handler;
if (message.isText()) {
    handler = new TextMessageHandler();
} else if (message.isImage()) {
    handler = new ImageMessageHandler();
}
handler.handleMessage(message);  // Polymorphic call
```

#### Abstraction
```java
public abstract class BaseActivity extends AppCompatActivity {
    // Abstract method - must be implemented by subclasses
    protected abstract void setupUI();
    protected abstract void loadData();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();      // Called by subclass
        loadData();     // Called by subclass
    }
}

public class ChatActivity extends BaseActivity {
    @Override
    protected void setupUI() {
        setContentView(R.layout.activity_chat);
        recyclerView = findViewById(R.id.messages);
    }
    
    @Override
    protected void loadData() {
        loadMessagesFromFirebase();
    }
}
```

---

### 2. Collections Framework

#### List, Set, Map
```java
// List - ordered, allows duplicates
List<String> names = new ArrayList<>();
names.add("Alice");
names.add("Bob");
names.add("Alice");  // Duplicate allowed
System.out.println(names); // [Alice, Bob, Alice]

// Set - unique values only
Set<String> uniqueNames = new HashSet<>();
uniqueNames.add("Alice");
uniqueNames.add("Bob");
uniqueNames.add("Alice");  // Skipped (duplicate)
System.out.println(uniqueNames); // [Alice, Bob]

// Map - key-value pairs
Map<String, Integer> agemap = new HashMap<>();
ageMap.put("Alice", 25);
ageMap.put("Bob", 30);
System.out.println(ageMap.get("Alice")); // 25
```

#### Iteration
```java
// List iteration
for (String name : names) {
    System.out.println(name);
}

// Iterator
Iterator<String> iterator = names.iterator();
while (iterator.hasNext()) {
    System.out.println(iterator.next());
}

// Map iteration
for (Map.Entry<String, Integer> entry : ageMap.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

---

### 3. Exception Handling

```java
try {
    // Code that might throw exception
    Message msg = getMessage(messageId);
    String content = msg.getContent();
    
    // Parse to int
    int priority = Integer.parseInt(content);
    
} catch (NullPointerException e) {
    // Handle specific exception
    Log.e("Error", "Message is null", e);
    
} catch (NumberFormatException e) {
    // Handle another exception
    Log.e("Error", "Invalid number format", e);
    
} catch (Exception e) {
    // Catch-all for any other exceptions
    Log.e("Error", "Unexpected error", e);
    
} finally {
    // Always executed
    closeConnection();
}
```

---

### 4. Generics

```java
// Generic class
public class GenericRepository<T> {
    private List<T> items = new ArrayList<>();
    
    public void add(T item) {
        items.add(item);
    }
    
    public T get(int index) {
        return items.get(index);
    }
    
    public List<T> getAll() {
        return items;
    }
}

// Usage
GenericRepository<Message> messageRepo = new GenericRepository<>();
messageRepo.add(new Message("Hello"));
messageRepo.add(new Message("Hi"));
Message msg = messageRepo.get(0);

GenericRepository<Person> personRepo = new GenericRepository<>();
personRepo.add(new Person("Alice"));
Person person = personRepo.get(0);

// Generic method
public static <T> T getFirstItem(List<T> list) {
    return list.isEmpty() ? null : list.get(0);
}

Message firstMsg = getFirstItem(messages);
Person firstPerson = getFirstItem(people);
```

---

## 🔥 Firebase & Backend

### 1. Firebase Authentication

#### Email/Password Authentication
```java
public class FirebaseAuthManager {
    private FirebaseAuth mAuth;
    
    public FirebaseAuthManager() {
        mAuth = FirebaseAuth.getInstance();
    }
    
    // Sign Up
    public void signUp(String email, String password, 
                      AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    callback.onSuccess(user);
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }
    
    // Sign In
    public void signIn(String email, String password, 
                      AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    callback.onSuccess(mAuth.getCurrentUser());
                } else {
                    callback.onFailure(task.getException());
                }
            });
    }
    
    // Sign Out
    public void signOut() {
        mAuth.signOut();
    }
    
    // Get current user
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}

// Callback interface
public interface AuthCallback {
    void onSuccess(FirebaseUser user);
    void onFailure(Exception exception);
}
```

#### Listen to Authentication State
```java
public class MainActivityAdapter {
    private FirebaseAuth mAuth;
    
    private FirebaseAuth.AuthStateListener mAuthStateListener = 
        new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            
            if (user != null) {
                // User is logged in
                Log.d("Auth", "User: " + user.getEmail());
                startActivity(new Intent(MainActivity.this, ChatActivity.class));
                finish();
            } else {
                // User is logged out
                Log.d("Auth", "User logged out");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }
    };
    
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }
}
```

---

### 2. Firebase Realtime Database

#### CRUD Operations

**Create/Write Data**:
```java
// Write user data
Map<String, Object> userData = new HashMap<>();
userData.put("name", "John Doe");
userData.put("email", "john@example.com");
userData.put("timestamp", System.currentTimeMillis());

String userId = "user_123";
FirebaseDatabase.getInstance()
    .getReference("users/" + userId)
    .setValue(userData)
    .addOnSuccessListener(aVoid -> {
        Log.d("Firebase", "Data written successfully");
    })
    .addOnFailureListener(e -> {
        Log.e("Firebase", "Error writing data", e);
    });

// Write message
Message message = new Message(
    senderId, recipientId, content, 
    System.currentTimeMillis(), "TEXT"
);

FirebaseDatabase.getInstance()
    .getReference("conversations/" + conversationId + "/messages")
    .push()  // Auto-generate key
    .setValue(message);
```

**Read Data (One-time)**:
```java
FirebaseDatabase.getInstance()
    .getReference("users/user_123")
    .addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                Person user = snapshot.getValue(Person.class);
                Log.d("Firebase", "User: " + user.getName());
            }
        }
        
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e("Firebase", "Error reading data", error.toException());
        }
    });
```

**Real-time Listener (Continuous)**:
```java
ValueEventListener valueEventListener = new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot child : snapshot.getChildren()) {
            Message message = child.getValue(Message.class);
            adapter.add(message);
        }
        adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e("Firebase", error.getMessage());
    }
};

FirebaseDatabase.getInstance()
    .getReference("conversations/" + conversationId + "/messages")
    .addValueEventListener(valueEventListener);
```

**Child Event Listener (More efficient)**:
```java
ChildEventListener childEventListener = new ChildEventListener() {
    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, 
                            String previousChildName) {
        // New message added
        Message msg = snapshot.getValue(Message.class);
        adapter.add(msg);
    }
    
    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, 
                              String previousChildName) {
        // Message updated (e.g., status change)
        Message updated = snapshot.getValue(Message.class);
        adapter.update(updated);
    }
    
    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
        // Message deleted
        Message deleted = snapshot.getValue(Message.class);
        adapter.remove(deleted);
    }
    
    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, 
                            String previousChildName) {
        // Message moved in list
    }
    
    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e("Firebase", error.getMessage());
    }
};

FirebaseDatabase.getInstance()
    .getReference("conversations/" + conversationId + "/messages")
    .addChildEventListener(childEventListener);
```

**Update Data**:
```java
// Update specific fields
Map<String, Object> updates = new HashMap<>();
updates.put("status", "READ");
updates.put("timestamp", System.currentTimeMillis());

FirebaseDatabase.getInstance()
    .getReference("messages/msg_123")
    .updateChildren(updates);
```

**Delete Data**:
```java
FirebaseDatabase.getInstance()
    .getReference("messages/msg_123")
    .removeValue();
```

---

### 3. Firebase Database Rules

#### Security Rules Structure
```json
{
  "rules": {
    ".read": false,           // Default: no read access
    ".write": false,          // Default: no write access
    
    "users": {
      "$uid": {               // $uid = wildcard for each user ID
        ".read": "$uid === auth.uid",  // Can read own data
        ".write": "$uid === auth.uid", // Can write own data
        
        // Validate data structure
        ".validate": "newData.hasChildren(['name', 'email'])",
        
        "name": {
          ".validate": "newData.isString() && newData.val().length > 0"
        },
        
        "email": {
          ".validate": "newData.isString() && newData.val().contains('@')"
        }
      }
    },
    
    "messages": {
      "$msgId": {
        ".read": "auth != null",  // Any authenticated user can read
        
        // Only sender can write
        ".write": "newData.child('senderId').val() === auth.uid",
        
        // Validate message structure
        ".validate": "newData.hasChildren(['content', 'senderId', 'timestamp'])"
      }
    },
    
    "conversations": {
      "$convId": {
        ".read": "auth != null",
        ".write": "auth != null"
      }
    }
  }
}
```

---

## 💾 Database & Storage

### 1. SQLite Database Design

#### Create Database
```java
public class ChatDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "chatapp.db";
    
    public ChatDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL(SQL_CREATE_USERS_TABLE);
        db.execSQL(SQL_CREATE_MESSAGES_TABLE);
        db.execSQL(SQL_CREATE_CONVERSATIONS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, 
                         int newVersion) {
        // Handle database version upgrade
        db.execSQL("DROP TABLE IF EXISTS " + UsersTable.TABLE_NAME);
        onCreate(db);
    }
    
    // SQL statements
    private static final String SQL_CREATE_USERS_TABLE =
        "CREATE TABLE " + UsersTable.TABLE_NAME + " (" +
        UsersTable.COLUMN_ID + " TEXT PRIMARY KEY," +
        UsersTable.COLUMN_NAME + " TEXT NOT NULL," +
        UsersTable.COLUMN_EMAIL + " TEXT UNIQUE NOT NULL," +
        UsersTable.COLUMN_PHONE + " TEXT," +
        UsersTable.COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
        ")";
    
    private static final String SQL_CREATE_MESSAGES_TABLE =
        "CREATE TABLE " + MessagesTable.TABLE_NAME + " (" +
        MessagesTable.COLUMN_ID + " TEXT PRIMARY KEY," +
        MessagesTable.COLUMN_CONVERSATION_ID + " TEXT NOT NULL," +
        MessagesTable.COLUMN_SENDER_ID + " TEXT NOT NULL," +
        MessagesTable.COLUMN_CONTENT + " TEXT NOT NULL," +
        MessagesTable.COLUMN_TIMESTAMP + " LONG NOT NULL," +
        MessagesTable.COLUMN_SYNCED + " BOOLEAN DEFAULT 0," +
        "FOREIGN KEY(" + MessagesTable.COLUMN_CONVERSATION_ID + ") " +
        "REFERENCES " + ConversationsTable.TABLE_NAME + "(" + 
        ConversationsTable.COLUMN_ID + ")" +
        ")";
}

// Database contract
public static class UsersTable {
    public static final String TABLE_NAME = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_CREATED_AT = "created_at";
}
```

#### Database Operations
```java
public class UserDao {
    private SQLiteDatabase db;
    
    public UserDao(SQLiteDatabase db) {
        this.db = db;
    }
    
    // Insert
    public long insertUser(Person user) {
        ContentValues values = new ContentValues();
        values.put(UsersTable.COLUMN_ID, user.getId());
        values.put(UsersTable.COLUMN_NAME, user.getName());
        values.put(UsersTable.COLUMN_EMAIL, user.getEmail());
        values.put(UsersTable.COLUMN_PHONE, user.getPhone());
        
        return db.insert(UsersTable.TABLE_NAME, null, values);
    }
    
    // Query
    public Person getUserById(String id) {
        Cursor cursor = db.query(
            UsersTable.TABLE_NAME,
            null,  // columns
            UsersTable.COLUMN_ID + "=?",  // selection
            new String[]{id},  // args
            null,  // groupBy
            null,  // having
            null   // orderBy
        );
        
        if (cursor.moveToFirst()) {
            Person user = new Person(
                cursor.getString(cursor.getColumnIndexOrThrow(UsersTable.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(UsersTable.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(UsersTable.COLUMN_EMAIL))
            );
            cursor.close();
            return user;
        }
        return null;
    }
    
    // Update
    public int updateUser(Person user) {
        ContentValues values = new ContentValues();
        values.put(UsersTable.COLUMN_NAME, user.getName());
        values.put(UsersTable.COLUMN_PHONE, user.getPhone());
        
        return db.update(
            UsersTable.TABLE_NAME,
            values,
            UsersTable.COLUMN_ID + "=?",
            new String[]{user.getId()}
        );
    }
    
    // Delete
    public int deleteUser(String id) {
        return db.delete(
            UsersTable.TABLE_NAME,
            UsersTable.COLUMN_ID + "=?",
            new String[]{id}
        );
    }
}
```

---

### 2. SharedPreferences (Simple Key-Value Storage)

```java
public class PreferenceManager {
    private Context context;
    private SharedPreferences prefs;
    
    public PreferenceManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(
            "com.example.chatapp.prefs", 
            Context.MODE_PRIVATE
        );
    }
    
    // Save
    public void saveUserId(String userId) {
        prefs.edit()
            .putString("user_id", userId)
            .apply();  // apply() is asynchronous
    }
    
    public void saveUserIsLoggedIn(boolean isLoggedIn) {
        prefs.edit()
            .putBoolean("is_logged_in", isLoggedIn)
            .apply();
    }
    
    public void saveLastSyncTime(long time) {
        prefs.edit()
            .putLong("last_sync_time", time)
            .apply();
    }
    
    // Retrieve
    public String getUserId() {
        return prefs.getString("user_id", null);
    }
    
    public boolean isUserLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }
    
    public long getLastSyncTime() {
        return prefs.getLong("last_sync_time", 0);
    }
    
    // Clear (Logout)
    public void clear() {
        prefs.edit().clear().apply();
    }
}
```

---

## 🎨 UI/UX Design Patterns

### 1. RecyclerView & Adapters

#### Basic RecyclerView Setup
```java
public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    private List<Message> messages = new ArrayList<>();
    
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void addMessage(Message msg) {
        messages.add(msg);
        notifyItemInserted(messages.size() - 1);
    }
    
    public void updateMessage(Message msg, int position) {
        messages.set(position, msg);
        notifyItemChanged(position);
    }
    
    public void removeMessage(int position) {
        messages.remove(position);
        notifyItemRemoved(position);
    }
    
    public void setMessages(List<Message> msgs) {
        messages = msgs;
        notifyDataSetChanged();
    }
}

public class MessageViewHolder extends RecyclerView.ViewHolder {
    private TextView senderName;
    private TextView messageContent;
    private TextView timestamp;
    
    public MessageViewHolder(View itemView) {
        super(itemView);
        senderName = itemView.findViewById(R.id.sender_name);
        messageContent = itemView.findViewById(R.id.message_content);
        timestamp = itemView.findViewById(R.id.timestamp);
    }
    
    public void bind(Message message) {
        senderName.setText(message.getSenderName());
        messageContent.setText(message.getContent());
        
        // Format timestamp
        long time = message.getTimestamp();
        String timeStr = formatTime(time);
        timestamp.setText(timeStr);
    }
    
    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(timestamp));
    }
}
```

#### RecyclerView Configuration
```xml
<!-- layout_messages.xml -->
<RecyclerView
    android:id="@+id/messages_recycler"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_above="@id/message_input_container" />
```

```java
// Activity
RecyclerView recyclerView = findViewById(R.id.messages_recycler);

// Set LayoutManager (defines how items are positioned)
recyclerView.setLayoutManager(new LinearLayoutManager(this));

// Set Adapter
MessageAdapter adapter = new MessageAdapter();
recyclerView.setAdapter(adapter);

// Scroll to bottom when new message arrives
adapter.addMessage(newMessage);
recyclerView.scrollToPosition(adapter.getItemCount() - 1);
```

---

### 2. Material Design Components

#### TextInputLayout (Email/Password Input)
```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/email_input_layout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Email"
    app:errorEnabled="true">
    
    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/email_input"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="textEmailAddress" />
</com.google.android.material.textfield.TextInputLayout>
```

#### Material Button
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/login_button"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Login"
    app:cornerRadius="8dp" />
```

#### CardView (Container)
```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <TextView
            android:id="@+id/user_name"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="16sp"
            android:textStyle="bold" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

---

### 3. Layout Management (ConstraintLayout)

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <!-- Header -->
    <TextView
        android:id="@+id/header"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:gravity="center"
        android:text="Chat"
        android:textSize="18sp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
    
    <!-- RecyclerView (stretch between header and input) -->
    <RecyclerView
        android:id="@+id/messages_recycler"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintTop_toBottomOf="@id/header"
        app:layout_constraintBottom_toTopOf="@id/message_input_layout"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
    
    <!-- Input Layout -->
    <LinearLayout
        android:id="@+id/message_input_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="8dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">
        
        <!-- EditText -->
        <EditText
            android:id="@+id/message_input"
            android:layout_width="0dp"
            android:layout_height="48dp"
            android:layout_weight="1"
            android:hint="Type a message..."
            android:paddingHorizontal="16dp"
            android:background="@drawable/rounded_background" />
        
        <!-- Send Button -->
        <ImageButton
            android:id="@+id/send_button"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:layout_marginStart="8dp"
            android:contentDescription="Send"
            android:src="@android:drawable/ic_menu_send" />
    </LinearLayout>
</androidx.constraintlayout.widget.ConstraintLayout>
```

---

## 🏛️ Architecture Patterns

### 1. MVVM (Model-View-ViewModel)

```
┌─────────────────┐
│     View        │ (Activity, Fragment)
│   (Observe)     │
└────────┬────────┘
         │
    ┌────▼─────┐
    │ViewModel │ (Holds data, handles logic)
    │(LiveData) │ (Observe & Push)
    └────┬──────┘
         │
    ┌────▼──────────┐
    │ Repository    │ (Data source abstraction)
    └────┬──────┬───┘
         │      │
    ┌────▼──┐ ┌─▼──────┐
    │Firebase  │SQLite   │
    │Database  │Database │
    └──────────┴────────┘
```

#### ViewModel Example
```java
public class ChatViewModel extends ViewModel {
    // LiveData for view to observe
    private final MutableLiveData<List<Message>> messages = 
        new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = 
        new MutableLiveData<>();
    
    private final ChatRepository repository;
    
    public ChatViewModel() {
        repository = new ChatRepository();
    }
    
    // Public methods for view to call
    public void loadMessages(String conversationId) {
        repository.getMessages(conversationId, new Callback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> msg) {
                messages.setValue(msg);  // Update LiveData
            }
            
            @Override
            public void onError(Exception e) {
                errorMessage.setValue(e.getMessage());
            }
        });
    }
    
    public void sendMessage(Message msg) {
        repository.sendMessage(msg);
    }
    
    // Getters for LiveData
    public LiveData<List<Message>> getMessages() {
        return messages;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}

// Activity observes ViewModel
public class ChatActivity extends AppCompatActivity {
    private ChatViewModel viewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        // Create ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        
        // Observe LiveData
        viewModel.getMessages().observe(this, messages -> {
            adapter.setMessages(messages);
        });
        
        viewModel.getErrorMessage().observe(this, errorMsg -> {
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
        });
        
        // Load data
        viewModel.loadMessages("conv_123");
    }
}
```

---

### 2. Repository Pattern

```java
public class ChatRepository {
    private FirebaseDatabase firebaseDb;
    private ChatDatabase localDb;
    
    public ChatRepository() {
        firebaseDb = FirebaseDatabase.getInstance();
        localDb = new ChatDatabase(context);
    }
    
    // Get messages from either source
    public void getMessages(String conversationId, 
                           Callback<List<Message>> callback) {
        // Try local cache first
        ChatDatabase db = new ChatDatabase(context);
        List<Message> cached = db.getMessages(conversationId);
        
        if (!cached.isEmpty()) {
            callback.onSuccess(cached);
        }
        
        // Then sync with Firebase
        firebaseDb.getReference("conversations/" + conversationId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Message> messages = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Message msg = child.getValue(Message.class);
                        messages.add(msg);
                    }
                    
                    // Save to local cache
                    db.saveMessages(messages);
                    
                    // Notify view
                    callback.onSuccess(messages);
                }
            });
    }
    
    public void sendMessage(Message msg, Callback<Boolean> callback) {
        // Save to Firebase
        firebaseDb.getReference("messages").push()
            .setValue(msg)
            .addOnSuccessListener(aVoid -> {
                // Save locally too
                localDb.saveMessage(msg);
                callback.onSuccess(true);
            })
            .addOnFailureListener(e -> {
                callback.onSuccess(false);
            });
    }
}

interface Callback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
```

---

## 🔴 Real-time Communication

### 1. WebSocket vs Polling vs Long Polling

#### Polling (❌ Inefficient)
```java
// Client asks server repeatedly
Timer timer = new Timer();
timer.scheduleAtFixedRate(new TimerTask() {
    @Override
    public void run() {
        String messages = getMessagesFromServer();  // HTTP request
        updateUI(messages);
    }
}, 0, 1000);  // Every 1 second
```
**Problem**: Lots of unnecessary HTTP requests, high battery/data usage

#### Long Polling (⚠️ Better but still slow)
```java
// Client waits until server has data
private void longPoll() {
    makeRequest(new Callback() {
        @Override
        public void onSuccess(List<Message> messages) {
            updateUI(messages);
            longPoll();  // Poll again
        }
    });
}
```
**Problem**: Latency, connection overhead

#### WebSocket (✅ Optimal)
```java
// Persistent connection, server pushes data
WebSocket ws;
private void connect() {
    ws = new WebSocket("ws://server.com");
    ws.onMessage(message -> {
        updateUI(message);
    });
    ws.onClose(() -> {
        reconnect();
    });
}
```
**Benefit**: Real-time, bidirectional, low latency

### 2. Firebase Realtime Updates

```java
// Firebase handles real-time communication internally
DatabaseReference ref = FirebaseDatabase.getInstance()
    .getReference("conversations/conv_1/messages");

ChildEventListener listener = new ChildEventListener() {
    @Override
    public void onChildAdded(DataSnapshot snapshot, String prev) {
        // New message received in real-time
        Message msg = snapshot.getValue(Message.class);
        adapter.add(msg);
    }
};

ref.addChildEventListener(listener);
```

**How Firebase Works**:
1. Client connects via WebSocket
2. Server sends new data to connected clients
3. App updates UI immediately
4. Fully managed by Firebase (no manual WebSocket code needed)

---

## 🧪 Testing & Debugging

### 1. Logging

```java
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        Log.d(TAG, "Activity created"); // Debug
        Log.i(TAG, "User ID: " + userId); // Info
        Log.w(TAG, "No messages found"); // Warning
        Log.e(TAG, "Error loading data", exception); // Error
    }
    
    private void sendMessage(String content) {
        if (content.isEmpty()) {
            Log.w(TAG, "Attempting to send empty message");
            return;
        }
        
        Message msg = new Message(content);
        firebaseDb.sendMessage(msg);
        Log.d(TAG, "Message sent: " + msg.getId());
    }
}
```

**Log Levels**:
- `Log.d()` - Debug (verbose info)
- `Log.i()` - Info (important info)
- `Log.w()` - Warning (warning data)
- `Log.e()` - Error (error stacktrace)

### 2. Unit Testing

```java
// Test ViewModel
@RunWith(AndroidTestRunner.class)
public class ChatViewModelTest {
    private ChatViewModel viewModel;
    
    @Before
    public void setup() {
        viewModel = new ChatViewModel();
    }
    
    @Test
    public void testLoadMessages() {
        List<Message> testMessages = Arrays.asList(
            new Message("Hello"),
            new Message("Hi")
        );
        
        viewModel.loadMessages("conv_1");
        
        // Assert
        assertEquals(2, viewModel.getMessages().getValue().size());
    }
    
    @Test
    public void testSendMessage() {
        Message msg = new Message("Test");
        viewModel.sendMessage(msg);
        
        // Verify message was added
        assertNotNull(viewModel.getMessages().getValue());
    }
}
```

### 3. Android Studio Debugger

```
1. Set breakpoint: Click line number
2. Run app: Run → Debug 'app'
3. App pauses at breakpoint
4. Inspect variables in Variables panel
5. Step through code: Step Over (F10), Step Into (F11)
6. Continue: Resume (F9)
```

### 4. Firebase Console Debugging

```
1. Open Firebase Console
2. Realtime Database → Observe data changes
3. Check read/write operations
4. Monitor database rules violations
5. View authentication logs
```

---

## 📋 Summary of Key Concepts

| Concept | Purpose | Usage |
|---------|---------|-------|
| **Activity Lifecycle** | Manage activity state | Handle resume/pause/destroy |
| **Fragment** | Reusable UI components | Complex multi-panel UI |
| **Intent** | Communication between components | Start activity, pass data |
| **Service** | Background work | Sync, notifications |
| **RecyclerView** | Efficient list display | Show messages, conversations |
| **Firebase Auth** | User authentication | Login, signup, session |
| **Firebase DB** | Real-time data | Messages, data sync |
| **MVVM** | Clean architecture | Testable, maintainable code |
| **Repository** | Data abstraction | Cache, sync, offline support |
| **LiveData** | Observable data | Auto-update UI |

---

**Tài liệu này cập nhật: 07/04/2026**  
**Cho dự án: Face_Filter Chat Application**

