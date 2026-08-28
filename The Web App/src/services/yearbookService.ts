import { supabase } from './supabaseClient';
import { User, Yearbook } from '../types';

function generateJoinCode(): string {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let code = '';
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return code;
}

function mapYearbook(row: any, memberIds: string[] = []): Yearbook {
  return {
    id: row.id,
    title: row.title || '',
    description: row.description || '',
    joinCode: row.join_code || '',
    coverPhotoUrl: row.cover_photo_url || '',
    ownerId: row.owner_id || '',
    memberIds: memberIds.length > 0 ? memberIds : [row.owner_id],
    startDate: Number(row.start_date) || Date.now(),
    endDate: Number(row.end_date) || (Date.now() + 90 * 24 * 60 * 60 * 1000),
    createdAt: Number(row.created_at_ms) || Date.now(),
    isArchived: Boolean(row.is_archived),
    isCompleted: Boolean(row.is_archived),
    completedAtMs: Boolean(row.is_archived) ? (Number(row.end_date) || Date.now()) : 0,
    retentionDays: 30,
    totalMemories: 0
  };
}

export const yearbookService = {
  async getYearbooksForUser(userId: string): Promise<Yearbook[]> {
    // 1. Get yearbooks where user is owner
    const { data: owned } = await supabase
      .from('yearbooks')
      .select('*')
      .eq('owner_id', userId);

    // 2. Get yearbooks where user is member
    const { data: memberships } = await supabase
      .from('yearbook_members')
      .select('yearbook_id')
      .eq('user_id', userId);

    const memberYbs: any[] = [];
    if (memberships && memberships.length > 0) {
      const ybIds = memberships.map(m => m.yearbook_id).filter(id => id && (!owned || !owned.some(o => o.id === id)));
      if (ybIds.length > 0) {
        const { data: memberData } = await supabase
          .from('yearbooks')
          .select('*')
          .in('id', ybIds);
        if (memberData) memberYbs.push(...memberData);
      }
    }

    const allRows = [...(owned || []), ...memberYbs];
    const uniqueMap = new Map<string, any>();
    allRows.forEach(r => {
      if (r && r.id) uniqueMap.set(r.id, r);
    });

    const results: Yearbook[] = [];
    for (const [_, row] of uniqueMap.entries()) {
      // Fetch members
      const { data: members } = await supabase
        .from('yearbook_members')
        .select('user_id')
        .eq('yearbook_id', row.id);
      const memberIds = members ? members.map(m => m.user_id) : [row.owner_id];
      results.push(mapYearbook(row, Array.from(new Set([row.owner_id, ...memberIds]))));
    }

    return results.sort((a, b) => b.createdAt - a.createdAt);
  },

  async getYearbookById(yearbookId: string): Promise<Yearbook | null> {
    const { data, error } = await supabase
      .from('yearbooks')
      .select('*')
      .eq('id', yearbookId)
      .single();

    if (error || !data) return null;

    const { data: members } = await supabase
      .from('yearbook_members')
      .select('user_id')
      .eq('yearbook_id', yearbookId);
    const memberIds = members ? members.map(m => m.user_id) : [data.owner_id];

    return mapYearbook(data, Array.from(new Set([data.owner_id, ...memberIds])));
  },

  async getYearbookMembers(yearbookId: string): Promise<User[]> {
    const { data, error } = await supabase
      .from('yearbook_members')
      .select('user_id')
      .eq('yearbook_id', yearbookId);

    if (error || !data || data.length === 0) {
      // Fallback: check owner
      const yb = await this.getYearbookById(yearbookId);
      if (yb && yb.ownerId) {
        const { data: profile } = await supabase.from('profiles').select('*').eq('id', yb.ownerId).single();
        if (profile) {
          return [{
            uid: profile.id,
            displayName: profile.display_name || 'Friend',
            email: profile.email || '',
            photoUrl: profile.photo_url || ''
          }];
        }
      }
      return [];
    }

    const userIds = data.map(d => d.user_id);
    const { data: profiles } = await supabase
      .from('profiles')
      .select('*')
      .in('id', userIds);

    if (!profiles) return [];

    return profiles.map(p => ({
      uid: p.id,
      displayName: p.display_name || 'Friend',
      email: p.email || '',
      photoUrl: p.photo_url || ''
    }));
  },

  async createYearbook(
    title: string,
    description: string,
    ownerId: string,
    coverPhotoUrl: string = ''
  ): Promise<Yearbook> {
    const ybId = `yb_${Math.random().toString(16).substring(2, 10)}`;
    const joinCode = generateJoinCode();
    const now = Date.now();
    const endDate = now + (90 * 24 * 60 * 60 * 1000);

    const row = {
      id: ybId,
      title: title.trim(),
      description: description.trim(),
      join_code: joinCode,
      cover_photo_url: coverPhotoUrl,
      owner_id: ownerId,
      start_date: now,
      end_date: endDate,
      created_at_ms: now,
      is_archived: false
    };

    const { error } = await supabase.from('yearbooks').insert(row);
    if (error) {
      throw new Error(`Failed to create yearbook: ${error.message}`);
    }

    // Insert owner as first member
    await supabase.from('yearbook_members').upsert({
      yearbook_id: ybId,
      user_id: ownerId
    });

    return mapYearbook(row, [ownerId]);
  },

  async findYearbookByCode(code: string): Promise<Yearbook | null> {
    const cleanCode = code.trim().toUpperCase();
    const { data, error } = await supabase
      .from('yearbooks')
      .select('*')
      .eq('join_code', cleanCode)
      .single();

    if (error || !data) return null;
    return mapYearbook(data);
  },

  async joinYearbookByCode(code: string, userId: string): Promise<Yearbook> {
    const yb = await this.findYearbookByCode(code);
    if (!yb) {
      throw new Error(`No yearbook found with code '${code}'`);
    }

    // Add membership
    const { error } = await supabase.from('yearbook_members').upsert({
      yearbook_id: yb.id,
      user_id: userId
    });

    if (error) {
      throw new Error(`Failed to join yearbook: ${error.message}`);
    }

    const updated = await this.getYearbookById(yb.id);
    return updated || yb;
  },

  async sealYearbook(yearbookId: string): Promise<void> {
    const now = Date.now();
    const { error } = await supabase
      .from('yearbooks')
      .update({
        is_archived: true,
        end_date: now
      })
      .eq('id', yearbookId);

    if (error) {
      throw new Error(`Failed to seal yearbook: ${error.message}`);
    }
  }
};
