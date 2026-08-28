# 📜 Retro Yearbook: Collaborative Daily Scrapbook

> A nostalgic, tactile collaborative yearbook and daily memory scrapbook app. Built with **Android Jetpack Compose**, **React 18 + Vite PWA**, and powered by **Supabase PostgreSQL & Cloud Storage**.

---

## 📁 Repository Structure

```
├── app/                  # 📱 Native Android App (Kotlin, Jetpack Compose, Media3)
├── The Web App/          # 🌐 Web App / PWA (React 18, Vite, TypeScript, PWA)
├── .gitignore            # 🔒 Comprehensive Git security (No secrets, API keys, or build files)
└── README.md             # 📖 Documentation
```

---

## 🌟 Key Features

* **Authentic Retro Aesthetics**: Saddle leather bindings, parchment cards, gold foil embossing, wax seals, washi tape badges, and handwritten polaroids.
* **Amber LED Date Stamping**: Real-time 90s camera LED date stamps burned into the bottom-right corner (`'YY MM DD`).
* **Shared Authentication & Database**: Instant cross-platform sync between Android, iPhone (Safari PWA), iPad, and Desktop.
* **On-Device Scrapbook PDF Generator**: High-resolution multi-page scrapbook with cover, polaroid memories, and handwritten notes.
* **9:16 Nostalgia Video Reel**: Ken Burns motion reel generator with acoustic ambient soundscape.
* **Fullscreen Slideshow**: Auto-playing nostalgic photo slideshow with audio and interactive playback.
* **Sealed Phase & 30-Day Cloud Retention**: Completed albums transition to sealed archives with countdown reminders for downloading memories locally.

---

## 🚀 Web App Deployment (Vercel)

### 1. Import Repository into Vercel
1. Go to [Vercel Dashboard](https://vercel.com/new).
2. Click **"Import"** on your GitHub repository.
3. In **Root Directory**, click *Edit* and select **`The Web App`**.
4. Framework Preset will auto-detect as **Vite**.

### 2. Configure Environment Variables in Vercel
Add the following in Vercel **Settings ➔ Environment Variables**:

| Variable | Description | Example |
| :--- | :--- | :--- |
| `VITE_SUPABASE_URL` | Supabase Project URL | `https://your-project.supabase.co` |
| `VITE_SUPABASE_KEY` | Supabase Authorized Key | `eyJhbGciOi...` |
| `VITE_GITHUB_REPO` | GitHub repository for APK downloads | `your-username/RetroYearbook` |
| `VITE_APK_FILENAME` | APK release filename | `RetroYearbook.apk` |

5. Click **Deploy**. Your web app and PWA will be live with full SSL and global CDN!

---

## 📲 Releasing New Android APK Versions (Always Linked to Web App)

The Web App's **`GET APK`** page dynamically fetches the latest release from the GitHub Releases API:

1. In Android Studio, build your release APK:
   ```bash
   ./gradlew assembleRelease
   ```
2. On GitHub, navigate to **Releases ➔ Draft a new release**.
3. Enter a tag (e.g. `v1.0.0`, `v1.0.1`).
4. Attach your `RetroYearbook.apk` file to the release assets.
5. Click **Publish Release**.

🎉 **That's it!** The Web App will immediately update with the new version number, file size, release date, and direct download link.

---

## 💻 Local Development

### Web App
```bash
cd "The Web App"
npm install
npm run dev
```

### Android App
Open the root directory in **Android Studio** and run on a connected device or emulator.
