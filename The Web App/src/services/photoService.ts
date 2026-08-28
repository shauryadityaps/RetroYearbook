import { supabase, SUPABASE_URL } from './supabaseClient';
import { PhotoEntry, User, getTodayDateString } from '../types';
import { applyDateStampToImage } from '../utils/dateStampRenderer';

export const photoService = {
  async getPhotosForYearbook(yearbookId: string): Promise<PhotoEntry[]> {
    const { data, error } = await supabase
      .from('photos')
      .select('*')
      .eq('yearbook_id', yearbookId)
      .order('timestamp', { ascending: false });

    if (error || !data) return [];

    return data.map(r => ({
      id: r.id,
      yearbookId: r.yearbook_id,
      authorId: r.author_id,
      authorName: r.author_name || 'Friend',
      authorAvatar: r.author_avatar || '',
      photoUrl: r.photo_url,
      dateString: r.date_string,
      caption: r.caption || '',
      timestamp: Number(r.timestamp) || Date.now()
    }));
  },

  async hasUserPostedToday(yearbookId: string, userId: string, dateString?: string): Promise<boolean> {
    const targetDate = dateString || getTodayDateString();
    const { data, error } = await supabase
      .from('photos')
      .select('id')
      .eq('yearbook_id', yearbookId)
      .eq('author_id', userId)
      .eq('date_string', targetDate)
      .limit(1);

    return !error && data && data.length > 0;
  },

  async uploadDailyPhoto(
    yearbookId: string,
    user: User,
    imageSource: File | Blob,
    caption: string,
    dateString?: string
  ): Promise<PhotoEntry> {
    const targetDate = dateString || getTodayDateString();
    // Strictly scoped per-yearbook to prevent cross-album primary key collisions
    const docId = `${yearbookId}_${targetDate}_${user.uid}`;

    // 1. Render vintage amber digital date stamp on canvas & compress
    const { blob } = await applyDateStampToImage(imageSource, targetDate);

    // 2. Upload to Supabase Storage bucket 'yearbooks'
    const storagePath = `yearbooks/${yearbookId}/${docId}.webp`;
    const { error: uploadError } = await supabase.storage
      .from('yearbooks')
      .upload(storagePath, blob, {
        contentType: 'image/webp',
        upsert: true
      });

    if (uploadError) {
      throw new Error(`Cloud storage upload failed: ${uploadError.message}`);
    }

    // 3. Public CDN URL
    const photoUrl = `${SUPABASE_URL}/storage/v1/object/public/yearbooks/${storagePath}`;

    // 4. Save metadata to Supabase public.photos table
    const entry: PhotoEntry = {
      id: docId,
      yearbookId,
      authorId: user.uid,
      authorName: user.displayName,
      authorAvatar: user.photoUrl,
      photoUrl,
      dateString: targetDate,
      caption: caption.trim(),
      timestamp: Date.now()
    };

    const { error: dbError } = await supabase.from('photos').upsert({
      id: entry.id,
      yearbook_id: entry.yearbookId,
      author_id: entry.authorId,
      author_name: entry.authorName,
      author_avatar: entry.authorAvatar,
      photo_url: entry.photoUrl,
      date_string: entry.dateString,
      caption: entry.caption,
      timestamp: entry.timestamp
    });

    if (dbError) {
      throw new Error(`Failed to save memory to database: ${dbError.message}`);
    }

    return entry;
  }
};
