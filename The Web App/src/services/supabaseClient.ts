import { createClient } from '@supabase/supabase-js';

export const SUPABASE_URL =
  import.meta.env.VITE_SUPABASE_URL || 'https://vqahvognmtqsojeoxacs.supabase.co';

export const SUPABASE_KEY =
  import.meta.env.VITE_SUPABASE_KEY ||
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZxYWh2b2dubXRxc29qZW94YWNzIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4Nzc1NjI1MCwiZXhwIjoyMTAzMzMyMjUwfQ._GUvt1ftfYbEea538uDTMs-IIkRvG6iqzerY-VExInE';

export const supabase = createClient(SUPABASE_URL, SUPABASE_KEY, {
  auth: {
    persistSession: false,
    autoRefreshToken: false
  }
});
