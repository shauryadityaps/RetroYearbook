export interface User {
  uid: string;
  displayName: string;
  email: string;
  photoUrl: string;
}

export interface Yearbook {
  id: string;
  title: string;
  description: string;
  joinCode: string;
  coverPhotoUrl: string;
  ownerId: string;
  memberIds: string[];
  startDate: number;
  endDate: number;
  createdAt: number;
  isArchived: boolean;
  isCompleted: boolean;
  completedAtMs: number;
  retentionDays: number;
  totalMemories: number;
}

export interface PhotoEntry {
  id: string;
  yearbookId: string;
  authorId: string;
  authorName: string;
  authorAvatar: string;
  photoUrl: string;
  dateString: string; // YYYY-MM-DD
  caption: string;
  timestamp: number;
}

export type TabType = 'dashboard' | 'library' | 'add_join' | 'download';

export function isAlbumSealed(yearbook: Yearbook): boolean {
  return (
    yearbook.isCompleted ||
    yearbook.isArchived ||
    (yearbook.endDate > 1000000000000 && Date.now() >= yearbook.endDate)
  );
}

export function getRetentionExpiryMs(yearbook: Yearbook): number {
  let baseTime = Date.now();
  if (yearbook.completedAtMs > 1000000000000) {
    baseTime = yearbook.completedAtMs;
  } else if (yearbook.endDate > 1000000000000 && yearbook.endDate <= Date.now()) {
    baseTime = yearbook.endDate;
  } else if (yearbook.createdAt > 1000000000000) {
    baseTime = yearbook.createdAt;
  }
  const retentionDays = yearbook.retentionDays || 30;
  return baseTime + retentionDays * 24 * 60 * 60 * 1000;
}

export function getDaysUntilDeletion(yearbook: Yearbook): number {
  const expiry = getRetentionExpiryMs(yearbook);
  const remaining = expiry - Date.now();
  if (remaining > 0) {
    return Math.max(1, Math.floor(remaining / (24 * 60 * 60 * 1000)));
  }
  return 0;
}

export function getTodayDateString(): string {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
