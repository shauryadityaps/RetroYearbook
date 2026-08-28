import React, { useState, useEffect } from 'react';
import { Yearbook, PhotoEntry } from '../types';
import { yearbookService } from '../services/yearbookService';
import { photoService } from '../services/photoService';
import { X, Play, Pause, ChevronLeft, ChevronRight, Loader2 } from 'lucide-react';

interface SlideshowPageProps {
  yearbookId: string;
  onClose: () => void;
}

export const SlideshowPage: React.FC<SlideshowPageProps> = ({ yearbookId, onClose }) => {
  const [yearbook, setYearbook] = useState<Yearbook | null>(null);
  const [photos, setPhotos] = useState<PhotoEntry[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(true);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        setLoading(true);
        const yb = await yearbookService.getYearbookById(yearbookId);
        setYearbook(yb);
        const pts = await photoService.getPhotosForYearbook(yearbookId);
        // Sort chronologically for slideshow
        setPhotos([...pts].sort((a, b) => a.timestamp - b.timestamp));
      } catch (err) {
        console.error('Failed to load slideshow', err);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [yearbookId]);

  // Slideshow Timer
  useEffect(() => {
    if (!isPlaying || photos.length === 0) return;
    const interval = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % photos.length);
    }, 4500);
    return () => clearInterval(interval);
  }, [isPlaying, photos.length]);

  if (loading || !yearbook) {
    return (
      <div style={{ height: '100vh', backgroundColor: '#1A0C06', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Loader2 className="animate-spin" size={36} color="#D4AF37" />
      </div>
    );
  }

  if (photos.length === 0) {
    return (
      <div style={{ height: '100vh', backgroundColor: '#1A0C06', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#F3E5AB' }}>
        <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '14px', marginBottom: '16px' }}>
          No memories available to play in this album.
        </p>
        <button
          onClick={onClose}
          style={{
            padding: '8px 16px',
            backgroundColor: 'var(--color-gold-foil)',
            border: 'none',
            borderRadius: '6px',
            fontFamily: 'var(--font-typewriter)',
            color: '#2B1810',
            cursor: 'pointer'
          }}
        >
          Return to Album
        </button>
      </div>
    );
  }

  const currentPhoto = photos[currentIndex];

  return (
    <div
      style={{
        position: 'fixed',
        top: 0, left: 0, right: 0, bottom: 0,
        backgroundColor: '#150A05',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 99999,
        padding: '16px'
      }}
    >
      {/* Top Controls Bar */}
      <div
        style={{
          position: 'absolute',
          top: '20px',
          left: '20px',
          right: '20px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          zIndex: 10
        }}
      >
        <div>
          <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: '18px', color: '#F3E5AB' }}>
            {yearbook.title}
          </h2>
          <span style={{ fontFamily: 'var(--font-typewriter)', fontSize: '10.5px', color: '#D4AF37' }}>
            MEMORY {currentIndex + 1} OF {photos.length}
          </span>
        </div>

        <button
          onClick={onClose}
          style={{
            backgroundColor: 'rgba(43, 24, 16, 0.7)',
            border: '1px solid #D4AF37',
            borderRadius: '50%',
            width: '38px',
            height: '38px',
            color: '#F3E5AB',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer'
          }}
        >
          <X size={20} />
        </button>
      </div>

      {/* Centerpiece Polaroid with Ken Burns Animation */}
      <div
        key={currentPhoto.id}
        className="polaroid-frame animate-fade-in"
        style={{
          width: '100%',
          maxWidth: '420px',
          padding: '16px 16px 24px 16px',
          backgroundColor: '#FDFBF7',
          borderRadius: '6px',
          boxShadow: '0 20px 48px rgba(0,0,0,0.7)',
          position: 'relative'
        }}
      >
        <div
          style={{
            width: '100%',
            aspectRatio: '1/1',
            borderRadius: '4px',
            overflow: 'hidden',
            backgroundColor: '#000',
            position: 'relative'
          }}
        >
          <img
            src={currentPhoto.photoUrl}
            alt={currentPhoto.caption || 'Memory'}
            style={{
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              animation: 'kenBurns 4.5s ease-out forwards'
            }}
          />
        </div>

        <div style={{ marginTop: '14px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: '#6E5849' }}>
            <span>By {currentPhoto.authorName || 'Friend'}</span>
            <span>{currentPhoto.dateString}</span>
          </div>

          {currentPhoto.caption && (
            <p
              style={{
                fontFamily: 'var(--font-handwriting)',
                fontSize: '22px',
                fontWeight: 600,
                color: '#2B1810',
                marginTop: '6px',
                lineHeight: 1.25
              }}
            >
              "{currentPhoto.caption}"
            </p>
          )}
        </div>
      </div>

      {/* Bottom Playback Navigation */}
      <div
        style={{
          position: 'absolute',
          bottom: '24px',
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
          backgroundColor: 'rgba(43, 24, 16, 0.8)',
          border: '1px solid #D4AF37',
          borderRadius: '30px',
          padding: '8px 18px'
        }}
      >
        <button
          onClick={() => setCurrentIndex((prev) => (prev === 0 ? photos.length - 1 : prev - 1))}
          style={{ background: 'none', border: 'none', color: '#F3E5AB', cursor: 'pointer' }}
        >
          <ChevronLeft size={22} />
        </button>

        <button
          onClick={() => setIsPlaying(!isPlaying)}
          style={{
            backgroundColor: '#D4AF37',
            border: 'none',
            borderRadius: '50%',
            width: '36px',
            height: '36px',
            color: '#2B1810',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer'
          }}
        >
          {isPlaying ? <Pause size={18} /> : <Play size={18} fill="#2B1810" />}
        </button>

        <button
          onClick={() => setCurrentIndex((prev) => (prev + 1) % photos.length)}
          style={{ background: 'none', border: 'none', color: '#F3E5AB', cursor: 'pointer' }}
        >
          <ChevronRight size={22} />
        </button>
      </div>

      {/* CSS Animation */}
      <style>{`
        @keyframes kenBurns {
          0% { transform: scale(1.0); }
          100% { transform: scale(1.08); }
        }
      `}</style>
    </div>
  );
};
