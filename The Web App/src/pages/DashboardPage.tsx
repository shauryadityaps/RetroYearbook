import React, { useState, useEffect } from 'react';
import { User, Yearbook, isAlbumSealed, getTodayDateString } from '../types';
import { yearbookService } from '../services/yearbookService';
import { photoService } from '../services/photoService';
import { LeatherButton } from '../components/LeatherButton';
import { LeatherBookCover } from '../components/LeatherBookCover';
import { PhotoDropModal } from '../components/PhotoDropModal';
import { Camera, Sparkles, LogOut, BookOpen, PlusCircle, CheckCircle } from 'lucide-react';

interface DashboardPageProps {
  user: User;
  onOpenYearbook: (yearbookId: string) => void;
  onNavigateToTab: (tab: 'library' | 'add_join') => void;
  onSignOut: () => void;
}

export const DashboardPage: React.FC<DashboardPageProps> = ({
  user,
  onOpenYearbook,
  onNavigateToTab,
  onSignOut
}) => {
  const [yearbooks, setYearbooks] = useState<Yearbook[]>([]);
  const [pendingBooks, setPendingBooks] = useState<Yearbook[]>([]);
  const [loading, setLoading] = useState(true);

  // Drop photo modal state
  const [activeDropBook, setActiveDropBook] = useState<Yearbook | null>(null);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const allBooks = await yearbookService.getYearbooksForUser(user.uid);
      setYearbooks(allBooks);

      // Check which active yearbooks need today's photo drop
      const today = getTodayDateString();
      const activeUnsealed = allBooks.filter(b => !isAlbumSealed(b));
      const pending: Yearbook[] = [];

      for (const book of activeUnsealed) {
        const hasPosted = await photoService.hasUserPostedToday(book.id, user.uid, today);
        if (!hasPosted) {
          pending.push(book);
        }
      }
      setPendingBooks(pending);
    } catch (err) {
      console.error('Failed to load dashboard', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, [user.uid]);

  const handleConfirmDrop = async (blob: Blob, caption: string) => {
    if (!activeDropBook) return;
    await photoService.uploadDailyPhoto(activeDropBook.id, user, blob, caption);
    setActiveDropBook(null);
    await loadDashboardData();
  };

  const activeBooks = yearbooks.filter(b => !isAlbumSealed(b));

  return (
    <div style={{ padding: '20px 16px 100px 16px', maxWidth: '600px', margin: '0 auto' }}>
      {/* Header */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '20px'
        }}
      >
        <div>
          <div style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', letterSpacing: '1px' }}>
            DAILY ACTION HUB
          </div>
          <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: '24px', color: 'var(--color-dark-sepia)', textTransform: 'uppercase' }}>
            {user.displayName}
          </h1>
        </div>

        <button
          onClick={onSignOut}
          title="Sign Out"
          style={{
            background: 'none',
            border: '1px solid var(--color-antique-border)',
            borderRadius: '8px',
            padding: '8px 12px',
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            color: 'var(--color-muted-sepia)',
            fontFamily: 'var(--font-typewriter)',
            fontSize: '11px',
            cursor: 'pointer',
            backgroundColor: 'var(--color-parchment-surface)'
          }}
        >
          <LogOut size={14} />
          <span>EXIT</span>
        </button>
      </div>

      {/* SECTION 1: PENDING TODAY'S DROP CAROUSEL */}
      <div style={{ marginBottom: '28px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '10px' }}>
          <Sparkles size={16} color="var(--color-saddle-leather)" />
          <h2 style={{ fontFamily: 'var(--font-typewriter)', fontSize: '12px', color: 'var(--color-saddle-leather)', letterSpacing: '1px' }}>
            PENDING TODAY'S MEMORY
          </h2>
        </div>

        {loading ? (
          <div style={{ padding: '24px', textAlign: 'center', fontFamily: 'var(--font-typewriter)', color: 'var(--color-muted-sepia)', fontSize: '12px' }}>
            Checking today's album drops...
          </div>
        ) : pendingBooks.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {pendingBooks.map((book) => (
              <div
                key={book.id}
                className="parchment-card animate-fade-in"
                style={{
                  borderRadius: '12px',
                  padding: '16px',
                  border: '1.5px solid #D4AF37',
                  boxShadow: '0 4px 14px rgba(43, 24, 16, 0.12)'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                  <span
                    style={{
                      fontFamily: 'var(--font-serif)',
                      fontSize: '17px',
                      color: 'var(--color-dark-sepia)',
                      fontWeight: 'bold'
                    }}
                  >
                    {book.title}
                  </span>
                  <span
                    style={{
                      fontFamily: 'var(--font-typewriter)',
                      fontSize: '10px',
                      backgroundColor: 'rgba(139, 0, 0, 0.1)',
                      color: 'var(--color-wax-red)',
                      padding: '2px 6px',
                      borderRadius: '4px',
                      border: '1px solid var(--color-wax-red)'
                    }}
                  >
                    DROP DUE TODAY
                  </span>
                </div>

                <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginBottom: '12px' }}>
                  {book.description || 'Drop your daily polaroid snapshot to preserve today in this yearbook.'}
                </p>

                <div style={{ display: 'flex', gap: '10px' }}>
                  <LeatherButton
                    text="DROP TODAY'S PHOTO"
                    icon={<Camera size={16} />}
                    onClick={() => setActiveDropBook(book)}
                  />
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div
            className="parchment-card"
            style={{
              borderRadius: '12px',
              padding: '20px',
              textAlign: 'center',
              backgroundColor: 'rgba(46, 125, 50, 0.06)',
              border: '1.5px solid rgba(46, 125, 50, 0.3)'
            }}
          >
            <CheckCircle size={32} color="var(--color-wax-green)" style={{ margin: '0 auto 8px auto' }} />
            <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '16px', color: 'var(--color-dark-sepia)' }}>
              All Caught Up for Today!
            </h3>
            <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginTop: '4px' }}>
              You have preserved your daily memories across all active scrapbooks.
            </p>
          </div>
        )}
      </div>

      {/* SECTION 2: RECENT YEARBOOKS SHELF */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '10px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <BookOpen size={16} color="var(--color-saddle-leather)" />
            <h2 style={{ fontFamily: 'var(--font-typewriter)', fontSize: '12px', color: 'var(--color-saddle-leather)', letterSpacing: '1px' }}>
              YOUR ACTIVE YEARBOOKS ({activeBooks.length})
            </h2>
          </div>

          <button
            onClick={() => onNavigateToTab('library')}
            style={{
              background: 'none',
              border: 'none',
              fontFamily: 'var(--font-typewriter)',
              fontSize: '11px',
              color: 'var(--color-saddle-leather)',
              textDecoration: 'underline',
              cursor: 'pointer'
            }}
          >
            View All
          </button>
        </div>

        {activeBooks.length > 0 ? (
          <div>
            {activeBooks.slice(0, 3).map((book) => (
              <LeatherBookCover
                key={book.id}
                yearbook={book}
                onClick={() => onOpenYearbook(book.id)}
              />
            ))}
          </div>
        ) : !loading ? (
          <div
            className="parchment-card"
            style={{
              borderRadius: '12px',
              padding: '24px',
              textAlign: 'center',
              border: '1.5px dashed var(--color-antique-border)'
            }}
          >
            <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '12px', color: 'var(--color-muted-sepia)', marginBottom: '14px' }}>
              You don't have any active yearbooks yet.
            </p>
            <div style={{ maxWidth: '240px', margin: '0 auto' }}>
              <LeatherButton
                text="CREATE OR JOIN"
                icon={<PlusCircle size={16} />}
                onClick={() => onNavigateToTab('add_join')}
              />
            </div>
          </div>
        ) : null}
      </div>

      {/* Photo Drop Modal */}
      {activeDropBook && (
        <PhotoDropModal
          isOpen={true}
          onClose={() => setActiveDropBook(null)}
          onConfirmDrop={handleConfirmDrop}
        />
      )}
    </div>
  );
};
