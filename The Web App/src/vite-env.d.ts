/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_SUPABASE_URL: string;
  readonly VITE_SUPABASE_KEY: string;
  readonly VITE_GITHUB_REPO: string;
  readonly VITE_APK_FILENAME: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
