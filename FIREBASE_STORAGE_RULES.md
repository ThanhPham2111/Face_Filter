# Firebase Storage Rules (Chat Media)

File nay la mau rule de bao ve media chat:

- Chi user da dang nhap moi duoc truy cap.
- Chi 2 user trong cuoc tro chuyen (`userA`, `userB`) moi duoc doc/ghi.
- Gioi hanh content type + dung luong cho image/voice.

## Cach ap dung nhanh

1. Mo Firebase Console -> Storage -> Rules.
2. Copy noi dung trong `firebase-storage.rules`.
3. Publish rules.

## Luu y

- App dang luu media theo duong dan:
  `chat_media/{senderId}/{receiverId}/images/...`
  va
  `chat_media/{senderId}/{receiverId}/voices/...`
- `senderId`/`receiverId` o day dang map tu email prefix (11 ky tu dau) nhu code hien tai.
- Neu ban doi format user id trong app, can cap nhat rule tuong ung.
