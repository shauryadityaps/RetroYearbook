import { supabase } from './supabaseClient';
import { User } from '../types';

const STORAGE_KEY_USER = 'retro_yearbook_user';

// Web Crypto SHA-256 Hashing matching Kotlin AuthRepositoryImpl.kt
async function sha256(message: string): Promise<string> {
  const msgUint8 = new TextEncoder().encode(message);
  const hashBuffer = await window.crypto.subtle.digest('SHA-256', msgUint8);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

function generateSalt(): string {
  const array = new Uint8Array(16);
  window.crypto.getRandomValues(array);
  return Array.from(array).map(b => b.toString(16).padStart(2, '0')).join('');
}

function emailToUid(email: string): string {
  return 'g_' + email.trim().toLowerCase().replace(/@/g, '_').replace(/\./g, '_');
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
    const salt = generateSalt();
    const hash = await sha256(salt + password);

    const user: User = {
      uid,
      displayName: displayName.trim() || cleanEmail.split('@')[0],
      email: cleanEmail,
      photoUrl: ''
    };

    // Upsert into Supabase profiles table
    const { error } = await supabase.from('profiles').upsert({
      id: uid,
      display_name: user.displayName,
      email: user.email,
      photo_url: user.photoUrl,
      password_hash: hash,
      password_salt: salt,
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
      throw new Error('Account not found. Please check your email or register a new account.');
    }

    const salt = data.password_salt || '';
    const storedHash = data.password_hash || '';
    const computedHash = await sha256(salt + password);

    if (storedHash && storedHash !== computedHash) {
      throw new Error('Incorrect password. Please try again.');
    }

    const user: User = {
      uid: data.id,
      displayName: data.display_name || cleanEmail.split('@')[0],
      email: data.email || cleanEmail,
      photoUrl: data.photo_url || ''
    };

    this.setCurrentUser(user);
    return user;
  },

  signOut(): void {
    this.setCurrentUser(null);
  }
};
