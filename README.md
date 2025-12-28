# NoteCast

## Tổng quan 

**NoteCast** là ứng dụng Android ghi âm và quản lý ghi chú thông minh, sử dụng AI để chuyển đổi giọng nói thành văn bản (ASR), tóm tắt nội dung, trích xuất từ khóa và tạo mindmap tự động. Ứng dụng được xây dựng với kiến trúc **Clean Architecture** kết hợp **MVVM** pattern, sử dụng **Jetpack Compose** cho UI và **Dagger Hilt** cho Dependency Injection.

### Tech Stack

[![Kotlin](https://img.shields.io/badge/Language-Kotlin%20100%25-blue)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20(Material%203)-orange)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture%20%2B%20MVVM-purple)](https://developer.android.com/jetpack/guide)
[![DI](https://img.shields.io/badge/DI-Dagger%20Hilt-green)](https://developer.android.com/training/dependency-injection/hilt-android)
[![Database](https://img.shields.io/badge/Database-Room%20(SQLite)-brightgreen)](https://developer.android.com/training/data-storage/room)
[![Networking](https://img.shields.io/badge/Networking-Retrofit2%20%2B%20OkHttp3-lightgrey)](https://square.github.io/retrofit/)

#### Storage

[![Storage](https://img.shields.io/badge/Storage-Cloudinary-blue)](https://cloudinary.com/)
[![Storage](https://img.shields.io/badge/Storage-Firebase%20Firestore-ffca28)](https://firebase.google.com/docs/firestore)

#### Async

[![Async](https://img.shields.io/badge/Async-Kotlin%20Coroutines%20%2B%20Flow-success)](https://kotlinlang.org/docs/coroutines-overview.html)

---
## ✨ Các tính năng chính

### 🎙️ Ghi âm & Xử lý âm thanh thông minh

- Cho phép **bắt đầu, tạm dừng, tiếp tục và dừng ghi âm** linh hoạt trong một phiên làm việc.
- **Hiển thị waveform realtime**, giúp người dùng theo dõi trực quan âm lượng và tín hiệu âm thanh khi đang ghi.
- Tích hợp **Voice Activity Detection (VAD)** để phát hiện chính xác các đoạn có giọng nói và đoạn im lặng.
- Tự động **cắt bỏ các đoạn im lặng**, tối ưu chất lượng audio trước khi xử lý.
- Hỗ trợ **chuyển đổi giọng nói thành văn bản (Speech-to-Text)** thông qua dịch vụ ASR, tạo transcript từ file ghi âm.

---

### 🤖 Xử lý AI hậu kỳ cho ghi chú

- Sau khi ghi âm và tạo transcript, ghi chú được **đẩy lên backend để AI xử lý**.
- AI thực hiện các tác vụ:
  - **Chuẩn hóa nội dung (Normalize)**
  - **Trích xuất từ khóa (Keywords)**
  - **Tạo bản tóm tắt (Summary)**
  - **Sinh sơ đồ tư duy (Mindmap)**
- Cho phép **theo dõi trạng thái xử lý AI theo thời gian thực**.
- Hỗ trợ **xem kết quả AI trực tiếp** trong giao diện ghi chú.
- Cho phép **tái tạo (Regenerate) nội dung AI** khi người dùng muốn cải thiện kết quả.

---

### 📝 Quản lý ghi chú linh hoạt

- Hỗ trợ hai loại ghi chú:
  - **Voice Note** (ghi chú giọng nói)
  - **Text Note** (ghi chú văn bản)
- Cho phép **xem danh sách, xem chi tiết, tạo mới, chỉnh sửa và xóa** ghi chú.
- Hỗ trợ **tìm kiếm ghi chú** theo tiêu đề và nội dung.
- Cho phép **lọc và sắp xếp ghi chú** theo nhiều tiêu chí:
  - Loại ghi chú (Voice / Text)
  - Ngày tạo
  - Trạng thái ghim (Pinned)
  - Yêu thích (Favorite)
  - Thư mục (Folder)
- Hỗ trợ **ghim (pin)** và **đánh dấu yêu thích (favorite)** các ghi chú quan trọng.
- Cho phép **xóa nhiều ghi chú cùng lúc**, tối ưu thao tác cho người dùng nâng cao.

---

### 📂 Quản lý thư mục (Folder)

- Cho phép **tạo, xem và xóa thư mục** để phân loại ghi chú.
- Mỗi thư mục có thể **tùy chỉnh tên và màu sắc**, giúp nhận diện trực quan.
- Hỗ trợ **xem ghi chú theo từng thư mục** cụ thể.
- Cho phép **di chuyển ghi chú giữa các thư mục** một cách linh hoạt.
- Hỗ trợ **đồng bộ danh sách thư mục từ server**, đảm bảo dữ liệu nhất quán trên nhiều thiết bị.


---

## 📁 Cấu Trúc Dự Án

```
app/src/main/java/com/example/notecast/
├── NoteApplication.kt              # Application class với @HiltAndroidApp
├── MainActivity.kt                 # Entry point, setup Navigation
│
├── core/                           # Core business logic không phụ thuộc Android
│   ├── audio/
│   │   ├── AudioEngine.kt          # Quản lý AudioRecord, đọc PCM frames
│   │   ├── AudioRecorder.kt        # Wrapper Android AudioRecord API
│   │   ├── AudioBuffer.kt          # Interface buffer audio
│   │   └── RingAudioBuffer.kt      # Circular buffer implementation
│   ├── vad/                        # Voice Activity Detection
│   │   ├── VADManager.kt           # Chọn VAD detector theo device tier
│   │   ├── Segmenter.kt            # Segment audio thành speech/silence
│   │   └── FrameBuffer.kt          # Buffer frames cho VAD
│   └── device/
│       ├── DeviceTier.kt           # Enum: LOW, MEDIUM, HIGH
│       └── DeviceTierDetector.kt   # Detect device performance
│
├── data/                           # Data layer - Implementation
│   ├── local/
│   │   ├── AppDatabase.kt          # Room Database definition
│   │   ├── dao/
│   │   │   ├── NoteDao.kt          # DAO cho Note & Audio
│   │   │   └── FolderDao.kt        # DAO cho Folder
│   │   ├── entities/
│   │   │   ├── NoteEntity.kt       # Table Note
│   │   │   ├── AudioEntity.kt      # Table Audio (relation 1-1)
│   │   │   ├── FolderEntity.kt     # Table Folder
│   │   │   └── NoteWithAudio.kt    # POJO @Relation
│   │   ├── mapper/
│   │   │   └── MappingEntityToDomain.kt  # Convert Entity ↔ Domain
│   │   └── migration/
│   │       └── Migration_1_2.kt
│   │
│   ├── remote/
│   │   ├── PhoWhisperApi.kt        # Retrofit interface ASR service
│   │   ├── NoteServiceApi.kt       # Retrofit interface backend API
│   │   ├── NoteEventsSseClient.kt  # SSE client for real-time events
│   │   ├── FolderRemoteDataSource.kt
│   │   ├── dto/                    # Data Transfer Objects
│   │   │   ├── PhoWhisperDtos.kt
│   │   │   ├── NoteServiceDtos.kt
│   │   │   └── FolderDtos.kt
│   │   ├── mapping/                # Convert DTO → Domain
│   │   │   ├── MappingAPIToDomain.kt
│   │   │   ├── MappingFolderAPIToDomain.kt
│   │   │   └── MappingMindMapToDomain.kt
│   │   └── service/
│   │       └── CloudinaryStorageUploaderImpl.kt
│   │
│   ├── repository/                 # Repository implementations
│   │   ├── NoteRepositoryImpl.kt
│   │   ├── FolderRepositoryImpl.kt
│   │   ├── AudioRepositoryImpl.kt
│   │   ├── RemoteNoteServiceRepositoryImpl.kt
│   │   ├── VADRepositoryImpl.kt
│   │   └── PreferencesRepositoryImpl.kt
│   │
│   └── vad/                        # VAD implementations
│       ├── silero/
│       │   ├── SileroVADImpl.kt    # Silero VAD using ONNX
│       │   └── SileroVADOnnx.kt
│       ├── webrtc/
│       │   └── WebRtcVADImpl.kt
│       └── rms/
│           └── RmsVADImpl.kt       # Simple RMS-based VAD
│
├── domain/                         # Domain layer - Business logic
│   ├── model/                      # Domain models (pure Kotlin)
│   │   ├── Note.kt                 # NoteDomain, AudioDomain, AudioChunk
│   │   ├── Folder.kt
│   │   ├── MindMapNode.kt
│   │   ├── ProcessedTextData.kt
│   │   └── AsrChunk.kt
│   │
│   ├── repository/                 # Repository interfaces
│   │   ├── NoteRepository.kt
│   │   ├── FolderRepository.kt
│   │   ├── AudioRepository.kt
│   │   ├── NoteRemoteRepository.kt
│   │   ├── VADRepository.kt
│   │   └── PreferencesRepository.kt
│   │
│   ├── service/
│   │   └── RemoteStorageUploader.kt  # Interface upload storage
│   │
│   ├── usecase/                    # Use cases (business operations)
│   │   ├── audio/
│   │   │   ├── StartRecordingUseCase.kt
│   │   │   ├── StopRecordingUseCase.kt
│   │   │   ├── PauseRecordingUseCase.kt
│   │   │   ├── ResumeRecordingUseCase.kt
│   │   │   ├── StreamAudioUseCase.kt
│   │   │   ├── TranscribeRecordingUseCase.kt  # Upload + call PhoWhisper
│   │   │   ├── VadSegmenterUseCase.kt
│   │   │   └── TrimSilenceUseCase.kt
│   │   ├── notefolder/
│   │   │   ├── GetAllNotesUseCase.kt
│   │   │   ├── GetNoteByIdUseCase.kt
│   │   │   ├── GetNotesByFolderUseCase.kt
│   │   │   ├── SaveNoteUseCase.kt
│   │   │   ├── DeleteNoteUseCase.kt
│   │   │   ├── SyncNotesUseCase.kt         # Sync from backend
│   │   │   ├── GetAllFoldersUseCase.kt
│   │   │   ├── SaveFolderUseCase.kt
│   │   │   ├── DeleteFolderUseCase.kt
│   │   │   ├── SyncFoldersUseCase.kt
│   │   │   ├── CreateNoteOnBackendUseCase.kt
│   │   │   └── ObserveRemoteNoteUseCase.kt  # SSE observer
│   │   └── postprocess/
│   │       ├── RegenerateNoteUseCase.kt    # Trigger AI enrichment
│   │       └── SyncUseCase.kt
│   │
│   └── vad/
│       ├── VADDetector.kt          # Interface VAD detector
│       ├── VadState.kt             # Enum: SILENT, SPEECH
│       └── SegmentEvent.kt         # Sealed class events
│
├── di/                             # Dependency Injection modules
│   ├── DatabaseModule.kt           # Provide Room DB, DAOs
│   ├── NetworkModule.kt            # Provide Retrofit, OkHttp
│   ├── RepositoryModule.kt         # Provide Repository implementations
│   ├── AudioModule.kt              # Provide AudioEngine, buffers
│   └── CloudinaryModule.kt         # Provide Cloudinary uploader
│
├── presentation/                   # Presentation layer
│   ├── navigation/
│   │   ├── Screen.kt               # Sealed class định nghĩa routes
│   │   └── RootNavGraph.kt         # Root navigation graph
│   │
│   ├── viewmodel/                  # ViewModels với @HiltViewModel
│   │   ├── NoteListViewModel.kt    # Home screen logic
│   │   ├── NoteTextViewModel.kt    # Text note edit
│   │   ├── NoteAudioViewModel.kt   # Audio note detail
│   │   ├── FolderViewModel.kt
│   │   ├── AudioViewModel.kt       # Recording control
│   │   └── RecordingViewModel.kt   # Transcription logic
│   │
│   └── ui/                         # Composable UI screens
│       ├── MainAppScreen.kt        # Main app container với Drawer
│       ├── splashscreen/
│       │   └── SplashScreen.kt
│       ├── onboarding/
│       │   ├── OnboardingScreen.kt
│       │   ├── OnboardingPage.kt
│       │   └── OnboardingItem.kt
│       ├── homescreen/
│       │   └── HomeScreen.kt       # Main home composable
│       ├── record/
│       │   ├── RecordingScreen.kt  # Recording UI
│       │   └── WaveformVisualizer.kt
│       ├── notetext/
│       │   ├── NoteTextScreen.kt   # Text note editor
│       │   ├── NoteEditState.kt
│       │   └── NoteEditEvent.kt
│       ├── noteaudio/
│       │   └── NoteAudioScreen.kt  # Audio note detail 
│       ├── folderscreen/
│       │   └── FolderScreen.kt
│       ├── mindmap/
│       │   └── MindMapScreen.kt
│       ├── settingsscreen/
│       │   └── SettingsScreen.kt
│       ├── sort/
│       │   └── SortScreen.kt
│       └── filter/
│           └── FilterScreen.kt
│
└── utils/
    └── CommonUtils.kt
```
##  Cài Đặt và Chạy Dự Án

### 1️⃣ Chuẩn bị môi trường
- Android Studio Hedgehog+ (Giraffe trở lên)
- JDK 17+
- Tạo file `local.properties` với các key API:
  ```
  GEMINI_API_KEY=...
  GEMINI_API_BASE_URL=...
  PHO_WHISPER_API_BASE_URL=...
  NOTE_SERVICE_URL=...
  CLOUDINARY_URL=...
  ```
- Thêm `google-services.json` vào `app/`

### 2️⃣ Build & Run

```bash
# Sync Gradle
./gradlew build

# Chạy app trên thiết bị/emulator
./gradlew installDebug
```


## 🔧 Build Production

```bash
# Build release APK
./gradlew assembleRelease
# File output: app/build/outputs/apk/release/app-release.apk
```

## Hướng Dẫn Sử Dụng

### Đăng nhập & Onboarding
1. Mở app lần đầu → Onboarding → Đăng nhập Google
2. Sau khi đăng nhập, truy cập giao diện chính

### Tạo ghi chú mới
1. Nhấn nút "+" để tạo ghi chú
2. Chọn loại: Văn bản hoặc Ghi âm
3. Nhập nội dung hoặc ghi âm
4. Lưu ghi chú, có thể chọn thư mục

### Tóm tắt & Mindmap AI
Mở ghi chú → Nhấn Tab "Tóm tắt", "Keyword" hoặc "Mindmap" để xem kết quả

### Quản lý thư mục
1. Tab "Thư mục" để tạo, sửa, xóa thư mục
2. Di chuyển ghi chú giữa các thư mục
---
