import React from 'react';
import { PhotoEntry } from '../types';

interface PolaroidPhotoCardProps {
  photo: PhotoEntry;
  onClick?: () => void;
}

export const PolaroidPhotoCard: React.FC<PolaroidPhotoCardProps> = ({ photo, onClick }) => {
  return (
    <div
      onClick={onClick}
      className="polaroid-frame"
      style={{
        padding: '12px 12px 18px 12px',
        position: 'relative',
        cursor: onClick ? 'pointer' : 'default',
        marginBottom: '16px'
      }}
    >
      {/* Vintage Washi Tape Strip */}
      <div className="tape-strip" />

      {/* Photo Frame */}
      <div
        style={{
          width: '100%',
          aspectRatio: '1/1',
          backgroundColor: '#2B1810',
          borderRadius: '3px',
          overflow: 'hidden',
          position: 'relative'
        }}
      >
        <img
          src={photo.photoUrl}
          alt={photo.caption || 'Memory'}
          loading="lazy"
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover'
          }}
        />
      </div>

      {/* Metadata & Caption */}
      <div style={{ marginTop: '12px', padding: '0 4px' }}>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: '4px'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            {photo.authorAvatar && photo.authorAvatar.trim().startsWith('http') ? (
              <img
                src={photo.authorAvatar}
                alt={photo.authorName}
                onError={(e) => {
                  (e.currentTarget as HTMLElement).style.display = 'none';
                  const sibling = (e.currentTarget as HTMLElement).nextElementSibling;
                  if (sibling) (sibling as HTMLElement).style.display = 'flex';
                }}
                style={{ width: '18px', height: '18px', borderRadius: '50%', objectFit: 'cover' }}
              />
            ) : null}
            <div
              style={{
                width: '18px',
                height: '18px',
                borderRadius: '50%',
                backgroundColor: '#59260B',
                display: photo.authorAvatar && photo.authorAvatar.trim().startsWith('http') ? 'none' : 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#F3E5AB',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '9.5px',
                fontWeight: 'bold',
                flexShrink: 0
              }}
            >
              {(photo.authorName || 'U').charAt(0).toUpperCase()}
            </div>
            <span
              style={{
                fontFamily: 'var(--font-typewriter)',
                fontSize: '11px',
                color: 'var(--color-muted-sepia)'
              }}
            >
              {photo.authorName || 'Friend'}
            </span>
          </div>

          <span
            style={{
              fontFamily: 'var(--font-typewriter)',
              fontSize: '10px',
              color: 'var(--color-muted-sepia)'
            }}
          >
            {photo.dateString}
          </span>
        </div>

        {photo.caption && (
          <p
            style={{
              fontFamily: 'var(--font-handwriting)',
              fontSize: '19px',
              fontWeight: 600,
              color: 'var(--color-dark-sepia)',
              lineHeight: 1.25,
              marginTop: '4px',
              wordBreak: 'break-word'
            }}
          >
            "{photo.caption}"
          </p>
        )}
      </div>
    </div>
  );
};
