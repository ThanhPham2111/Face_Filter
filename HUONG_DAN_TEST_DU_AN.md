# Huong Dan Test Du An Chat App

Tai lieu nay dung de team test nhanh va dong bo cho du an `Face_Filter` (app chat mini).

## 1. Muc tieu test

- Xac nhan app chat hoat dong on dinh voi:
  - Gui/nhan text.
  - Gui/nhan anh (anh nho, upload Firebase Storage).
  - Gui/nhan voice chat.
  - Huy upload media.
  - Hien thi tien do phat voice.
- Xac nhan khong loi crash o cac thao tac co ban.

## 2. Dieu kien truoc khi test

1. Da clone code moi nhat va build duoc.
2. Da cau hinh Firebase (`app/google-services.json`).
3. Da bat Firebase Authentication (email/password).
4. Da bat Realtime Database va Firebase Storage.
5. Da publish rules Storage theo file:
   - `firebase-storage.rules`
   - Huong dan: `FIREBASE_STORAGE_RULES.md`
6. Can toi thieu 2 tai khoan de test chat 2 chieu.

## 3. Build va chay app

1. Build nhanh:
   ```bash
   ./gradlew.bat -q -DskipTests compileDebugJavaWithJavac
   ```
2. Chay tren 2 may/2 emulator (A va B).
3. Dang nhap bang 2 tai khoan khac nhau.

## 4. Du lieu test de xai chung

- Ten tai khoan A: `User A`
- Ten tai khoan B: `User B`
- Tin nhan text mau:
  - `hello test text`
  - `test unicode tieng viet`
- Anh mau:
  - 1 anh < 300KB
  - 1 anh > gioi han de test reject
- Voice mau:
  - 2-5 giay (hop le)
  - < 0.5 giay (qua ngan)
  - > 60 giay (qua dai)

## 5. Checklist test chuc nang

## 5.1 Dang nhap/vao danh sach chat

1. Dang nhap thanh cong.
2. Tao/cuoc tro chuyen voi doi tuong co san.
3. Mo man hinh chat khong crash.

Ket qua mong doi:
- Vao duoc man hinh chat.
- Danh sach chat hien thi binh thuong.

## 5.2 Gui text

1. Nhap text va bam nut gui.
2. Kiem tra ben A hien ngay.
3. Kiem tra ben B nhan duoc.
4. Quay lai danh sach chat, xem `last message` cap nhat.

Ket qua mong doi:
- Tin nhan text hien dung noi dung.
- Thu tu tin nhan dung theo thoi gian.

## 5.3 Gui anh

1. Bam nut dinh kem anh.
2. Chon anh hop le.
3. Xem trang thai upload `%`.
4. Sau khi xong, bubble anh hien o A va B.

Ket qua mong doi:
- Upload thanh cong.
- Anh hien thi dung, khong meo qua muc.
- Khong crash khi mo/scroll.

## 5.4 Huy upload anh

1. Bat dau upload anh.
2. Bam nut `X` huy upload.

Ket qua mong doi:
- Upload dung lai.
- Hien thong bao da huy.
- Khong tao tin nhan media loi.

## 5.5 Gui voice

1. Bam nut mic de bat dau ghi am.
2. Bam lai de dung va gui.
3. Cho upload xong.
4. Ben A/B bam play de nghe.

Ket qua mong doi:
- Gui voice thanh cong.
- Thoi luong hien dung.
- Play/pause duoc.

## 5.6 Progress voice playback

1. Bam play voice.
2. Quan sat elapsed time + progress bar.
3. Dung voice hoac cho phat het.

Ket qua mong doi:
- Progress tang theo thoi gian.
- Ket thuc thi progress reset hop ly.
- Khong crash neu doi man hinh/scroll nhanh.

## 5.7 Validation gioi han

1. Voice < 0.5 giay.
2. Voice > 60 giay.
3. Anh qua gioi han dung luong.

Ket qua mong doi:
- App bao loi than thien.
- Khong gui message khong hop le.

## 5.8 Test stability nhanh

1. Dang upload media thi bam gui text lien tuc.
2. Dang phat voice thi refresh man hinh chat (vao/ra lai).
3. Dong app mo lai.

Ket qua mong doi:
- Khong crash.
- Message da gui truoc do van con.

## 6. Kiem tra du lieu Firebase

1. Realtime Database:
   - Message text/image/voice duoc luu day du.
   - `last_message` cap nhat (`[Anh]`, `[Voice]` cho media).
2. Storage:
   - File media nam duoi `chat_media/...`.
   - Co file images va voices.

## 7. Tieu chi Pass/Fail

Pass khi:
- Toan bo testcase muc 5 dat ket qua mong doi.
- Khong co crash/blocker.
- Media gui/nhan duoc tren 2 tai khoan.

Fail khi:
- Co crash.
- Khong gui/nhan duoc text/media.
- Huy upload khong hoat dong.
- Voice progress hien sai hoac gay loi nghiem trong.

## 8. Mau bao cao bug cho team

Khi gap loi, ghi theo mau:

1. Thiet bi/Emulator:
2. Tai khoan test:
3. Buoc tai hien:
4. Ket qua thuc te:
5. Ket qua mong doi:
6. Anh/video minh chung:
7. Muc do uu tien (Blocker/High/Medium/Low):

## 9. Ghi chu

- Neu test that bai do quyen Firebase, check lai `firebase-storage.rules`.
- Neu test tren 1 tai khoan duy nhat, ket qua khong danh gia duoc full luong 2 chieu.
