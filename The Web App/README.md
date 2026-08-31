# 🌐 Retro Yearbook - Web App & PWA

> Progressive Web Application (PWA) client for Retro Yearbook, built with **React 18**, **TypeScript**, and **Vite 6**.

---

## 🌟 Overview

The Web App provides full cross-platform access for iOS (Safari "Add to Home Screen"), Android, and Desktop browsers, replicating the tactile vintage experience of the native Android application while connecting to the shared Supabase backend.

---

## 🛠️ Tech Stack & Libraries

* **Framework**: React 18 + Vite 6
* **Language**: TypeScript
* **Styling**: Vanilla CSS Design Tokens (`src/styles/index.css`)
* **Icons**: `lucide-react`
* **PDF Scrapbook Generation**: `pdf-lib`
* **Video Reel Generation**: HTML5 Canvas 2D & `MediaRecorder` API
* **Confetti**: `canvas-confetti`
* **PWA & Offline Support**: `vite-plugin-pwa`

---

## 🚀 Local Development

```bash
# 1. Install dependencies
npm install

# 2. Configure environment
cp .env.example .env

# 3. Start local development server
npm run dev

# 4. Build production bundle
npm run build
```

---

## 📦 Scripts

| Script | Command | Description |
| :--- | :--- | :--- |
| `dev` | `vite` | Start local Vite development server |
| `build` | `tsc && vite build` | Type-check TypeScript and build production bundle |
| `preview` | `vite preview` | Preview production build locally |
