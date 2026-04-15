# 🚀 Quick Reference - Face_Filter Project

> **Tệp hướng dẫn nhanh cho người trình bày**

---

## ⚡ Quick Facts

| Thuộc Tính | Thông Tin |
|-----------|----------|
| **Tên Dự Án** | Face_Filter (Chat Application) |
| **Package** | com.example.chatapp |
| **Min SDK** | API 24 (Android 7.0) |
| **Target SDK** | API 33 (Android 13) |
| **Ngôn Ngữ** | Java |
| **Backend** | Firebase Realtime DB |
| **UI Framework** | Material Design |
| **Thời Gian Phát Triển** | 8 ngày (36 giờ) |

---

## 🎯 Tính Năng Chính

✅ **Authentication** - Đăng ký, đăng nhập, quản lý session  
✅ **Real-time Messaging** - Gửi/nhận tin nhắn theo thời gian thực  
✅ **Contact Management** - Danh sách liên hệ, tìm kiếm  
✅ **Firebase Integration** - Database, Auth, Security  
✅ **Local Storage** - SQLite for offline support  
✅ **Material UI** - Modern, responsive design  

---

## 📊 Tech Stack Summary

```
BACKEND
├── Firebase Auth (v21.3.0)
├── Firebase Realtime DB (v20.0.4)
└── SQLite Local DB

FRONTEND
├── Android SDK 33
├── Material Design (v1.8.0)
├── ConstraintLayout (v2.1.4)
└── RecyclerView + FirebaseUI

BUILD
├── Gradle 8.13.2
├── Android Gradle Plugin 8.13.2
└── Google Services Plugin 4.3.15
```

---

## 📁 Project Structure

```
Face_Filter/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/chatapp/
│   │   │   ├── login/          (Login tính năng)
│   │   │   ├── signUp/         (Sign up tính năng)
│   │   │   ├── conversation/   (Messaging)
│   │   │   ├── contacts/       (Liên hệ)
│   │   │   ├── firebaseDb/     (Firebase manager)
│   │   │   ├── sqliteDB/       (SQLite manager)
│   │   │   ├── Globals.java
│   │   │   ├── Person.java
│   │   │   └── Message.java
│   │   └── res/               (Resources)
│   └── build.gradle
├── build.gradle
├── gradle.properties
└── settings.gradle
```

---

## 🔧 Setup Checklist (15 phút)

- [ ] Android Studio 2023.1+
- [ ] JDK 11+
- [ ] Android SDK API 33
- [ ] Firebase Project tạo mới
- [ ] google-services.json download
- [ ] Đặt google-services.json vào app/
- [ ] Sync Gradle
- [ ] Install Android Emulator hoặc kết nối device

---

## 💻 Essential Commands

### Build & Run
```bash
# Development
./gradlew assembleDebug
adb install app-debug.apk

# Release
./gradlew assembleRelease
```

### Clean Project
```bash
./gradlew clean
./gradlew build
```

### View Gradle Tasks
```bash
./gradlew tasks
```

---

## 🔐 Firebase Database Rules (Copy-Paste)

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid",
        ".validate": "newData.hasChildren(['name', 'email'])"
      }
    },
    "messages": {
      "$msgId": {
        ".read": "auth != null",
        ".write": "newData.child('senderId').val() === auth.uid",
        ".validate": "newData.hasChildren(['content', 'timestamp'])"
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

## 🔑 Key Code Snippets

### Firebase Authentication
```java
// Đăng ký
FirebaseAuth.getInstance()
    .createUserWithEmailAndPassword(email, password)
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            // Create user in database
        }
    });

// Đăng nhập
FirebaseAuth.getInstance()
    .signInWithEmailAndPassword(email, password)
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            // Navigate to main activity
        }
    });
```

### Send Message
```java
Message msg = new Message(senderId, recipientId, content, 
                          System.currentTimeMillis());

mDatabase.getReference("conversations/" + convId + "/messages")
    .push()
    .setValue(msg);
```

### Receive Messages (Real-time)
```java
mDatabase.getReference("conversations/" + convId + "/messages")
    .addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot snapshot, String prev) {
            Message msg = snapshot.getValue(Message.class);
            adapter.add(msg);
        }
    });
```

### Setup RecyclerView with FirebaseUI
```java
FirebaseRecyclerOptions<Message> options = 
    new FirebaseRecyclerOptions.Builder<Message>()
        .setQuery(query, Message.class)
        .build();

adapter = new FirebaseRecyclerAdapter<Message, ViewHolder>(options) {
    @Override
    protected void onBindViewHolder(ViewHolder holder, int pos, Message msg) {
        holder.bind(msg);
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }
};

recyclerView.setAdapter(adapter);
adapter.startListening();
```

---

## 🧪 Testing Checklist

### Functional Tests
- [ ] Đăng ký tài khoản mới
- [ ] Đăng nhập với credentials hợp lệ
- [ ] Đăng nhập với credentials không hợp lệ
- [ ] Gửi tin nhắn
- [ ] Nhận tin nhắn thời gian thực
- [ ] Xem danh sách trò chuyện
- [ ] Xem danh sách liên hệ
- [ ] Tạo trò chuyện mới
- [ ] Xóa trò chuyện
- [ ] Cập nhật trạng thái online/offline

### Performance Tests
- [ ] Gửi 100+ tin nhắn
- [ ] Load message history
- [ ] Swipe RecyclerView smoothly
- [ ] Memory usage monitoring
- [ ] Battery consumption

### Security Tests
- [ ] Cannot access other user's data
- [ ] Cannot modify others' messages
- [ ] Secure login session
- [ ] Session timeout (nếu có)
- [ ] Sensitive data encrypted

---

## 🎨 Material Design Components Used

| Component | Usage |
|-----------|-------|
| **Material Button** | Nút send, login, etc |
| **TextInputLayout** | Email, password inputs |
| **Card View** | Message items, conversation items |
| **RecyclerView** | Danh sách (messages, conversations, contacts) |
| **Toolbar** | Conversation header |
| **Floating Action Button** | Create new chat |
| **Dialog** | Confirmations, options |
| **BottomSheet** | Additional options |

---

## ⚠️ Common Issues & Quick Fixes

### Issue: Gradle Sync Failed
```gradle
// Solution: Update gradle.properties
org.gradle.java.home=C:\\Program Files\\Java\\jdk-11
org.gradle.jvmargs=-Xmx3g
```

### Issue: Firebase Connection Error
- ✔ Verify google-services.json in app/
- ✔ Check Firebase project ID matches
- ✔ Verify database rules allow read/write

### Issue: RecyclerView Empty
```java
// Verify adapter is properly set
recyclerView.setAdapter(adapter);
// Verify data exists in Firebase
// Verify query is correct
```

### Issue: Memory Leak
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    // Remove all listeners
    ref.removeEventListener(valueEventListener);
    adapter.stopListening();
}
```

### Issue: Messages Not Real-time
- ✔ Verify ChildEventListener is added
- ✔ Check database rules
- ✔ Verify Firebase connection active

---

## 📈 Performance Optimization Tips

### Query Optimization
```java
// ✅ GOOD: Limited query
query.limitToLast(20).addValueEventListener(listener);

// ❌ BAD: Fetch all data
query.addValueEventListener(listener);
```

### Memory Management
```java
// ✅ Always remove listeners
ref.removeEventListener(listener);
adapter.stopListening();

// ✅ Use weak references for contexts
WeakReference<Context> weakContext = new WeakReference<>(context);
```

### Image Handling
```java
// Compress before upload
Bitmap compressed = Bitmap.createScaledBitmap(bitmap, width, height, true);
```

---

## 🚀 Deployment Quick Guide

### For Testing (Internal)
```bash
./gradlew assembleDebug
adb install app-debug.apk
```

### For Release (Production)
```bash
# Create keystore (one-time)
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key

# Build signed APK
./gradlew assembleRelease

# Upload to Firebase/Play Store
```

### Distribution Options
1. **Play Store** - Official app store (recommended)
2. **Firebase App Distribution** - Beta testing
3. **Direct APK** - Direct installation
4. **GitHub Releases** - For developers

---

## 📊 Presentation Script Template

```
"Xin chào mọi người! Hôm nay tôi sẽ giới thiệu 
Face_Filter - một ứng dụng chat Android.

Dự án này được xây dựng bằng:
- Android SDK 33
- Firebase Realtime Database
- Material Design UI

Ứng dụng hỗ trợ:
✅ Đăng nhập/Đăng ký
✅ Nhắn tin thời gian thực
✅ Quản lý liên hệ
✅ Lưu trữ ngoại tuyến

Cây cấu trúc này giúp tăng khả năng mở rộng 
và dễ dàng bảo trì trong tương lai.

Bây giờ hãy xem bản demo..."
```

---

## 🎓 Key Learning Points

1. **Firebase is powerful** - Handles auth, database, real-time updates
2. **MVVM architecture** - Separates concerns, improves testability
3. **Real-time databases** - Challenge: handling conflicts & latency
4. **Security first** - Always validate & authorize data access
5. **Performance matters** - Query optimization & memory management critical

---

## 📞 Support Resources

| Resource | Link |
|----------|------|
| Android Docs | https://developer.android.com |
| Firebase Docs | https://firebase.google.com/docs |
| Material Design | https://material.io/design |
| Stack Overflow | https://stackoverflow.com/tags/android |
| Firebase Community | https://stackoverflow.com/questions/tagged/firebase |

---

## 💡 Improvement Ideas for Future Versions

- [ ] Push notifications
- [ ] Image/file sharing
- [ ] Video calls
- [ ] Voice messages
- [ ] Group chats
- [ ] Typing indicators
- [ ] Message search
- [ ] End-to-end encryption
- [ ] Dark mode
- [ ] Custom stickers

---

## ✅ Pre-Presentation Checklist

- [ ] Demo device/emulator fully charged
- [ ] Sample account created for demo
- [ ] Test conversations seeded in Firebase
- [ ] Network connection stable
- [ ] Presentation slides prepared
- [ ] Code snippets ready to show
- [ ] Q&A notes prepared
- [ ] Backup PDF of slides
- [ ] Microphone tested
- [ ] Screen resolution adjusted

---

**Last Updated**: 07/04/2026  
**Project Version**: 1.0  
**Presentation Duration**: 30 minutes  

