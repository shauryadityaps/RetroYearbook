# 📜 Retro Yearbook

> A nostalgic, collaborative daily memory scrapbook platform. Available as a **Native Android Application** and a **Progressive Web App (PWA)**, backed by a unified **Supabase** backend.

---

## 🌟 Overview

**Retro Yearbook** brings back the tactile charm of physical photo albums and vintage disposable cameras. Friends and communities can create collaborative scrapbooks, drop one photo memory per day, watch their collective stories develop, and preserve them forever through high-resolution PDF scrapbooks and cinematic video reels.

---

## ✨ Key Features

* **📸 Daily Memory Drops & Amber Date Stamping**:
  * Real-time hardware camera and gallery photo drops.
  * Automated 90s vintage amber LED digital date stamps burned into the bottom-right corner (`'YY MM DD`).
* **🎨 Tactile Retro Design System**:
  * Rich skeuomorphic styling: saddle leather bindings, parchment cardstock, gold foil embossing, wax seals, washi tape badges, and handwritten polaroids.
  * Responsive 2-column polaroid collage grid layout across mobile, tablet, and desktop.
* **🔒 Two-Phase Lifecycle & 30-Day Cloud Retention**:
  * **Active Phase**: Daily collaborative photo drops, real-time sync, and 6-digit invite code sharing.
  * **Sealed Phase**: The creator seals the yearbook upon trip or term completion, transitioning it into a read-only archive with a 30-day cloud retention countdown.
* **📄 Client-Side Scrapbook PDF Exporter**:
  * Multi-page high-resolution PDF generator (`pdf-lib`) creating a print-ready scrapbook with cover art, polaroid layouts, date headers, and handwritten captions.
* **🎬 9:16 Vertical Video Reel Exporter**:
  * Hardware-accelerated (Android Media3) & Canvas/MediaRecorder (Web) video reel generator featuring Ken Burns motion and ambient acoustic soundscapes.
* **🎞️ Fullscreen Nostalgic Slideshow**:
  * Interactive auto-progressing photo slideshow with Ken Burns smooth zooming and soundtrack playback.
* **🔐 Interoperable Cryptographic Authentication**:
  * Custom SHA-256 salted password hashing engine designed to be 100% interoperable between JVM `MessageDigest` and browser `window.crypto.subtle`.

---

## 🏗️ Monorepo Architecture

```
RetroYearbook/
├── app/                              # 📱 Native Android Client
│   ├── src/main/java/com/yearbook/retro/
│   │   ├── data/                     # Remote Supabase REST & Storage sources, repositories
│   │   ├── domain/                   # Domain repository interfaces & business models
│   │   ├── media/                    # DateStampRenderer, ImageCompressor, VideoReelExporter
│   │   ├── ui/                       # Jetpack Compose screens, components, theme & navigation
│   │   ├── util/                     # PasswordHasher, PdfScrapbookExporter, validators
│   │   └── worker/                   # WorkManager daily drop background reminders
│   └── build.gradle.kts
│
├── The Web App/                      # 🌐 Progressive Web App (PWA)
│   ├── src/
│   │   ├── components/               # LeatherButton, PolaroidPhotoCard, NavigationBottomBar, etc.
│   │   ├── pages/                    # Dashboard, Library, InsideYearbook, AddJoin, Slideshow, DownloadApk
│   │   ├── services/                 # Supabase client, authService, yearbookService, photoService
│   │   ├── styles/                   # Core vintage design tokens & typography
│   │   ├── types/                    # TypeScript interfaces & domain helper functions
│   │   └── utils/                    # dateStampRenderer, pdfScrapbookExporter, videoReelExporter
│   ├── package.json
│   ├── vite.config.ts
│   └── vercel.json
│
├── .gitignore                        # Strict security filter (excludes .env, keystores, build artifacts)
└── README.md
```

---

## 🛠️ Tech Stack

### 📱 Android Native
* **Language & UI**: Kotlin 2.0+, Jetpack Compose, Material 3
* **Concurrency & Reactive State**: Kotlin Coroutines, StateFlow
* **Media & Video**: Media3 (ExoPlayer & Transformer), Android Canvas API
* **Image Processing**: Coil, WebP compression, custom Typeface rendering
* **Background Tasks**: AndroidX WorkManager

### 🌐 Web Application & PWA
* **Framework & Build**: React 18, TypeScript, Vite 6
* **Styling**: Vanilla CSS Design Tokens (Custom fonts: *Cinzel*, *Special Elite*, *Caveat*)
* **PWA & Offline**: `vite-plugin-pwa`, Service Workers, Web App Manifest
* **Export Engines**: `pdf-lib` (PDF generation), HTML5 Canvas 2D & `MediaRecorder` API
* **Animations**: Canvas Confetti, CSS keyframe motion

### ☁️ Backend & Storage
* **Database**: Supabase PostgreSQL
* **API Layer**: Supabase PostgREST Client
* **Object Storage**: Supabase Cloud Storage (`yearbooks` bucket)

---

## 🚀 Getting Started

### Prerequisites
* **Android Development**: Android Studio Iguana+, JDK 17, Android SDK 34
* **Web Development**: Node.js 18+ and `npm`

---

### 1. Web App Setup

1. Navigate to the web application directory:
   ```bash
   cd "The Web App"
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure environment variables:
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
   Provide your Supabase project credentials:
   ```env
   VITE_SUPABASE_URL=https://your-project.supabase.co
   VITE_SUPABASE_KEY=your-supabase-key
   VITE_GITHUB_REPO=shauryadityaps/RetroYearbook
   VITE_APK_FILENAME=RetroYearbook.apk
   ```

4. Start the local development server:
   ```bash
   npm run dev
   ```

5. Build for production:
   ```bash
   npm run build
   ```

---

### 2. Android App Setup

1. Open the project root directory in **Android Studio**.
2. Sync Gradle files (`File ➔ Sync Project with Gradle Files`).
3. Ensure `google-services.json` is present in `/app`.
4. Connect an Android device (Android 8.0 / API 26+) or launch an emulator.
5. Run the app:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🔒 Security & Best Practices

* **No Secret Leaks**: All private `.env` files, keystores, `local.properties`, and build caches are filtered out by `.gitignore`.
* **Zero Plaintext Passwords**: Passwords are never stored in plaintext. They are salted with an application-level pepper and hashed with SHA-256 before transmission.
* **Per-Album Scoped Storage**: Photo identifiers follow the strict scheme `${yearbookId}_${dateString}_${authorId}.webp` to prevent cross-album primary key collisions.

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
