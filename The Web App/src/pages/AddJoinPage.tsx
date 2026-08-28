import React, { useState } from 'react';
import { User, Yearbook } from '../types';
import { yearbookService } from '../services/yearbookService';
import { LeatherButton } from '../components/LeatherButton';
import { Plus, KeyRound, Sparkles, CheckCircle2 } from 'lucide-react';

interface AddJoinPageProps {
  user: User;
  onOpenYearbook: (yearbookId: string) => void;
}

export const AddJoinPage: React.FC<AddJoinPageProps> = ({ user, onOpenYearbook }) => {
  const [activeTab, setActiveTab] = useState<'create' | 'join'>('create');

  // Create Form State
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [isCreating, setIsCreating] = useState(false);

  // Join Form State
  const [joinCode, setJoinCode] = useState('');
  const [previewBook, setPreviewBook] = useState<Yearbook | null>(null);
  const [isCheckingCode, setIsCheckingCode] = useState(false);
  const [isJoining, setIsJoining] = useState(false);
  const [joinError, setJoinError] = useState<string | null>(null);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;

    try {
      setIsCreating(true);
      const created = await yearbookService.createYearbook(title, description, user.uid);
      onOpenYearbook(created.id);
    } catch (err: any) {
      alert(err.message || 'Failed to create yearbook');
    } finally {
      setIsCreating(false);
    }
  };

  const handleCodeChange = async (val: string) => {
    const clean = val.toUpperCase().trim().slice(0, 6);
    setJoinCode(clean);
    setJoinError(null);

    if (clean.length === 6) {
      try {
        setIsCheckingCode(true);
        const book = await yearbookService.findYearbookByCode(clean);
        if (book) {
          setPreviewBook(book);
        } else {
          setPreviewBook(null);
          setJoinError(`No album found for code "${clean}"`);
        }
      } catch {
        setPreviewBook(null);
      } finally {
        setIsCheckingCode(false);
      }
    } else {
      setPreviewBook(null);
    }
  };

  const handleJoin = async () => {
    if (!joinCode || joinCode.length !== 6) return;
    try {
      setIsJoining(true);
      const joined = await yearbookService.joinYearbookByCode(joinCode, user.uid);
      onOpenYearbook(joined.id);
    } catch (err: any) {
      setJoinError(err.message || 'Failed to join album');
    } finally {
      setIsJoining(false);
    }
  };

  return (
    <div style={{ padding: '20px 16px 100px 16px', maxWidth: '540px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ marginBottom: '20px' }}>
        <div style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', letterSpacing: '1px' }}>
          EXPAND YOUR ARCHIVE
        </div>
        <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: '26px', color: 'var(--color-dark-sepia)' }}>
          CREATE OR JOIN
        </h1>
      </div>

      {/* Tabs */}
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
          onClick={() => setActiveTab('create')}
          style={{
            flex: 1,
            padding: '10px',
            border: 'none',
            borderRadius: '6px',
            backgroundColor: activeTab === 'create' ? 'var(--color-saddle-leather)' : 'transparent',
            color: activeTab === 'create' ? '#FFF' : 'var(--color-dark-sepia)',
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
          <Plus size={15} /> CREATE ALBUM
        </button>

        <button
          onClick={() => setActiveTab('join')}
          style={{
            flex: 1,
            padding: '10px',
            border: 'none',
            borderRadius: '6px',
            backgroundColor: activeTab === 'join' ? 'var(--color-saddle-leather)' : 'transparent',
            color: activeTab === 'join' ? '#FFF' : 'var(--color-dark-sepia)',
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
          <KeyRound size={15} /> JOIN WITH CODE
        </button>
      </div>

      {/* CREATE TAB */}
      {activeTab === 'create' ? (
        <div
          className="parchment-card animate-fade-in"
          style={{
            borderRadius: '14px',
            padding: '24px 20px',
            border: '1.5px solid var(--color-gold-foil)',
            boxShadow: '0 8px 24px rgba(43, 24, 16, 0.12)'
          }}
        >
          <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: '20px', color: 'var(--color-dark-sepia)', marginBottom: '4px' }}>
            New Collaborative Yearbook
          </h2>
          <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginBottom: '18px' }}>
            Creates a vintage leather scrapbook with a unique 6-digit code for friends to drop daily memories.
          </p>

          <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontFamily: 'var(--font-typewriter)', fontSize: '10.5px', color: 'var(--color-muted-sepia)', marginBottom: '4px' }}>
                YEARBOOK TITLE *
              </label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Goa Trip 2026 or College Seniors"
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '13px',
                  backgroundColor: 'var(--color-parchment-bg)',
                  border: '1px solid var(--color-antique-border)',
                  borderRadius: '6px',
                  outline: 'none'
                }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontFamily: 'var(--font-typewriter)', fontSize: '10.5px', color: 'var(--color-muted-sepia)', marginBottom: '4px' }}>
                DESCRIPTION / THEME
              </label>
              <textarea
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Write a little note about what moments belong in this scrapbook..."
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '13px',
                  backgroundColor: 'var(--color-parchment-bg)',
                  border: '1px solid var(--color-antique-border)',
                  borderRadius: '6px',
                  outline: 'none',
                  resize: 'none'
                }}
              />
            </div>

            <div style={{ marginTop: '10px' }}>
              <LeatherButton
                text="CREATE & OPEN SCRAPBOOK"
                type="submit"
                isLoading={isCreating}
              />
            </div>
          </form>
        </div>
      ) : (
        /* JOIN TAB */
        <div
          className="parchment-card animate-fade-in"
          style={{
            borderRadius: '14px',
            padding: '24px 20px',
            border: '1.5px solid var(--color-gold-foil)',
            boxShadow: '0 8px 24px rgba(43, 24, 16, 0.12)'
          }}
        >
          <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: '20px', color: 'var(--color-dark-sepia)', marginBottom: '4px' }}>
            Join Friend's Yearbook
          </h2>
          <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginBottom: '18px' }}>
            Enter the 6-character alphanumeric invite code shared by the album creator.
          </p>

          <div>
            <label style={{ display: 'block', fontFamily: 'var(--font-typewriter)', fontSize: '10.5px', color: 'var(--color-muted-sepia)', marginBottom: '6px' }}>
              6-CHARACTER INVITE CODE
            </label>
            <input
              type="text"
              maxLength={6}
              value={joinCode}
              onChange={(e) => handleCodeChange(e.target.value)}
              placeholder="e.g. AL7K4A"
              style={{
                width: '100%',
                padding: '12px 14px',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '22px',
                textAlign: 'center',
                letterSpacing: '6px',
                fontWeight: 'bold',
                textTransform: 'uppercase',
                backgroundColor: 'var(--color-parchment-bg)',
                border: '2px solid var(--color-saddle-leather)',
                borderRadius: '8px',
                outline: 'none',
                color: 'var(--color-dark-sepia)'
              }}
            />
          </div>

          {isCheckingCode && (
            <p style={{ textAlign: 'center', fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginTop: '10px' }}>
              Looking up scrapbook...
            </p>
          )}

          {joinError && (
            <div
              style={{
                backgroundColor: 'rgba(139, 0, 0, 0.1)',
                border: '1px solid var(--color-wax-red)',
                borderRadius: '6px',
                padding: '10px 12px',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '11px',
                color: 'var(--color-wax-red)',
                marginTop: '14px',
                textAlign: 'center'
              }}
            >
              {joinError}
            </div>
          )}

          {previewBook && (
            <div
              className="animate-fade-in"
              style={{
                marginTop: '18px',
                padding: '16px',
                backgroundColor: 'rgba(46, 125, 50, 0.08)',
                border: '1.5px solid var(--color-wax-green)',
                borderRadius: '10px'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '6px' }}>
                <CheckCircle2 size={16} color="var(--color-wax-green)" />
                <span style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', fontWeight: 'bold', color: 'var(--color-wax-green)' }}>
                  ALBUM FOUND
                </span>
              </div>

              <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '18px', color: 'var(--color-dark-sepia)' }}>
                {previewBook.title}
              </h3>
              {previewBook.description && (
                <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginTop: '2px' }}>
                  {previewBook.description}
                </p>
              )}

              <div style={{ marginTop: '14px' }}>
                <LeatherButton
                  text="JOIN THIS YEARBOOK"
                  icon={<Sparkles size={16} />}
                  onClick={handleJoin}
                  isLoading={isJoining}
                />
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
