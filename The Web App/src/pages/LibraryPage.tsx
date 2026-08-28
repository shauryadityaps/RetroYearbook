import React, { useState, useEffect } from 'react';
import { User, Yearbook, isAlbumSealed } from '../types';
import { yearbookService } from '../services/yearbookService';
import { LeatherBookCover } from '../components/LeatherBookCover';
import { BookOpen, Archive, Loader2 } from 'lucide-react';

interface LibraryPageProps {
  user: User;
  onOpenYearbook: (yearbookId: string) => void;
}

export const LibraryPage: React.FC<LibraryPageProps> = ({ user, onOpenYearbook }) => {
  const [yearbooks, setYearbooks] = useState<Yearbook[]>([]);
  const [activeTab, setActiveTab] = useState<'active' | 'archived'>('active');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        const data = await yearbookService.getYearbooksForUser(user.uid);
        setYearbooks(data);
      } catch (err) {
        console.error('Failed to load library', err);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [user.uid]);

  const activeBooks = yearbooks.filter(b => !isAlbumSealed(b));
  const archivedBooks = yearbooks.filter(b => isAlbumSealed(b));

  return (
    <div style={{ padding: '20px 16px 100px 16px', maxWidth: '600px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ marginBottom: '18px' }}>
        <div style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', letterSpacing: '1px' }}>
          YEARBOOK SHELF
        </div>
        <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: '26px', color: 'var(--color-dark-sepia)' }}>
          ALL SCRAPBOOKS
        </h1>
      </div>

      {/* Tab Switcher */}
      <div
        style={{
          display: 'flex',
          backgroundColor: 'var(--color-parchment-surface)',
          border: '1.5px solid var(--color-antique-border)',
          borderRadius: '8px',
          padding: '4px',
          marginBottom: '20px'
        }}
      >
        <button
          onClick={() => setActiveTab('active')}
          style={{
            flex: 1,
            padding: '10px',
            border: 'none',
            borderRadius: '6px',
            backgroundColor: activeTab === 'active' ? 'var(--color-saddle-leather)' : 'transparent',
            color: activeTab === 'active' ? '#FFF' : 'var(--color-dark-sepia)',
            fontFamily: 'var(--font-typewriter)',
            fontSize: '11.5px',
            fontWeight: 'bold',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '6px',
            cursor: 'pointer'
          }}
        >
          <BookOpen size={14} /> ACTIVE ({activeBooks.length})
        </button>

        <button
          onClick={() => setActiveTab('archived')}
          style={{
            flex: 1,
            padding: '10px',
            border: 'none',
            borderRadius: '6px',
            backgroundColor: activeTab === 'archived' ? 'var(--color-saddle-leather)' : 'transparent',
            color: activeTab === 'archived' ? '#FFF' : 'var(--color-dark-sepia)',
            fontFamily: 'var(--font-typewriter)',
            fontSize: '11.5px',
            fontWeight: 'bold',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '6px',
            cursor: 'pointer'
          }}
        >
          <Archive size={14} /> SEALED ARCHIVES ({archivedBooks.length})
        </button>
      </div>

      {/* Content */}
      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
          <Loader2 className="animate-spin" size={32} color="var(--color-saddle-leather)" />
          <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '12px', color: 'var(--color-muted-sepia)' }}>
            Retrieving scrapbooks from shelf...
          </p>
        </div>
      ) : activeTab === 'active' ? (
        <div>
          {activeBooks.length > 0 ? (
            activeBooks.map((book) => (
              <LeatherBookCover
                key={book.id}
                yearbook={book}
                onClick={() => onOpenYearbook(book.id)}
              />
            ))
          ) : (
            <div
              className="parchment-card"
              style={{
                borderRadius: '12px',
                padding: '30px 20px',
                textAlign: 'center',
                border: '1.5px dashed var(--color-antique-border)'
              }}
            >
              <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '12px', color: 'var(--color-muted-sepia)' }}>
                No active yearbooks found on your shelf.
              </p>
            </div>
          )}
        </div>
      ) : (
        <div>
          {archivedBooks.length > 0 ? (
            archivedBooks.map((book) => (
              <LeatherBookCover
                key={book.id}
                yearbook={book}
                onClick={() => onOpenYearbook(book.id)}
              />
            ))
          ) : (
            <div
              className="parchment-card"
              style={{
                borderRadius: '12px',
                padding: '30px 20px',
                textAlign: 'center',
                border: '1.5px dashed var(--color-antique-border)'
              }}
            >
              <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '12px', color: 'var(--color-muted-sepia)' }}>
                No sealed albums in archive yet. Completed albums will appear here.
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
