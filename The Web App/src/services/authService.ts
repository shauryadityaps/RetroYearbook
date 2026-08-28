import { supabase } from './supabaseClient';
import { User } from '../types';

const STORAGE_KEY_USER = 'retro_yearbook_user';
const SALT = 'retro_yearbook_salt_2026_secure_';

// Web Crypto SHA-256 Hashing matching Android PasswordHasher.kt
async function sha256(message: string): Promise<string> {
  const msgUint8 = new TextEncoder().encode(message);
  const hashBuffer = await window.crypto.subtle.digest('SHA-256', msgUint8);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

function emailToUid(email: string): string {
  return 'g_' + email.trim().toLowerCase().replace(/[^a-z0-9]/g, '_');
}

function encodeHash(hash: string, photoUrl: string = ''): string {
  return photoUrl ? `pwd:${hash}|${photoUrl}` : `pwd:${hash}`;
}

function extractHash(encoded: string): string {
  if (encoded && encoded.startsWith('pwd:')) {
    return encoded.slice(4).split('|')[0];
  }
  return encoded || '';
}

function extractPhotoUrl(encoded: string): string {
  if (encoded && encoded.startsWith('pwd:')) {
    const parts = encoded.slice(4).split('|');
    return parts.length > 1 ? parts[1] : '';
  }
  return encoded || '';
}

export const authService = {
  getCurrentUser(): User | null {
    try {
      const stored = localStorage.getItem(STORAGE_KEY_USER);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  },

  setCurrentUser(user: User | null): void {
    if (user) {
      localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(user));
    } else {
      localStorage.removeItem(STORAGE_KEY_USER);
    }
  },

  async signUp(email: string, password: string, displayName: string): Promise<User> {
    const cleanEmail = email.trim().toLowerCase();
    const uid = emailToUid(cleanEmail);
    const hash = await sha256(SALT + password.trim());
    const encodedStorage = encodeHash(hash);

    const user: User = {
      uid,
      displayName: displayName.trim() || cleanEmail.split('@')[0],
      email: cleanEmail,
      photoUrl: ''
    };

    // Upsert into Supabase profiles table matching exact PostgreSQL schema
    const { error } = await supabase.from('profiles').upsert({
      id: uid,
      display_name: user.displayName,
      email: user.email,
      photo_url: encodedStorage,
      created_at_ms: Date.now()
    });

    if (error) {
      throw new Error(`Failed to create account: ${error.message}`);
    }

    this.setCurrentUser(user);
    return user;
  },

  async signIn(email: string, password: string): Promise<User> {
    const cleanEmail = email.trim().toLowerCase();
    const uid = emailToUid(cleanEmail);

    const { data, error } = await supabase
      .from('profiles')
      .select('*')
      .eq('id', uid)
      .single();

    if (error || !data) {
      throw new Error('Account not found. Please check your email or create a new account.');
    }

    const storedHash = extractHash(data.photo_url || '');
    const computedHash = await sha256(SALT + password.trim());

    if (storedHash && storedHash.toLowerCase() !== computedHash.toLowerCase()) {
      throw new Error('Incorrect password. Please check and try again.');
    }

    const user: User = {
      uid: data.id,
      displayName: data.display_name || cleanEmail.split('@')[0],
      email: data.email || cleanEmail,
      photoUrl: extractPhotoUrl(data.photo_url || '')
    };

    this.setCurrentUser(user);
    return user;
  },

  signOut(): void {
    this.setCurrentUser(null);
  }
};
