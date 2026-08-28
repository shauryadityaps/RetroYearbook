import React from 'react';
import { Yearbook, isAlbumSealed, getDaysUntilDeletion } from '../types';
import { Users, Lock, Sparkles, Clock } from 'lucide-react';

interface LeatherBookCoverProps {
  yearbook: Yearbook;
  onClick: () => void;
}

export const LeatherBookCover: React.FC<LeatherBookCoverProps> = ({ yearbook, onClick }) => {
  const sealed = isAlbumSealed(yearbook);
  const daysLeft = getDaysUntilDeletion(yearbook);

  return (
    <div
      onClick={onClick}
      className="leather-card"
      style={{
        borderRadius: '12px',
        padding: '16px',
        cursor: 'pointer',
        position: 'relative',
        marginBottom: '14px',
        transition: 'transform 0.15s ease, box-shadow 0.15s ease'
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-3px)';
        e.currentTarget.style.boxShadow = '0 12px 28px rgba(0,0,0,0.45)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'none';
        e.currentTarget.style.boxShadow = '0 8px 24px rgba(0,0,0,0.35)';
      }}
    >
      {/* Book Spine Stitching Effect */}
      <div
        style={{
          position: 'absolute',
          left: '12px',
          top: '0',
          bottom: '0',
          width: '2px',
          borderLeft: '1px dashed rgba(212, 175, 55, 0.4)'
        }}
      />

      <div style={{ marginLeft: '10px' }}>
        {/* Top Badges Row */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: '8px'
          }}
        >
          {sealed ? (
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '4px',
                backgroundColor: 'rgba(212, 175, 55, 0.2)',
                border: '1px solid #D4AF37',
                borderRadius: '4px',
                padding: '2px 6px',
                fontSize: '10px',
                color: '#F3E5AB',
                fontFamily: 'var(--font-typewriter)'
              }}
            >
              <Lock size={10} /> ★ SEALED ARCHIVE
            </span>
          ) : (
            <span
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '4px',
                backgroundColor: 'rgba(46, 125, 50, 0.3)',
                border: '1px solid #4CAF50',
                borderRadius: '4px',
                padding: '2px 6px',
                fontSize: '10px',
                color: '#A5D6A7',
                fontFamily: 'var(--font-typewriter)'
              }}
            >
              <Sparkles size={10} /> CODE: {yearbook.joinCode}
            </span>
          )}

          <div
            className={`wax-seal ${sealed ? 'wax-seal-red' : 'wax-seal-green'}`}
            style={{ width: '26px', height: '26px', fontSize: '11px' }}
          >
            {sealed ? '★' : '✓'}
          </div>
        </div>

        {/* Title */}
        <h3
          style={{
            fontFamily: 'var(--font-serif)',
            fontSize: '19px',
            color: '#F3E5AB',
            letterSpacing: '0.5px',
            marginBottom: '4px',
            textShadow: '0 1px 2px rgba(0,0,0,0.8)'
          }}
        >
          {yearbook.title}
        </h3>

        {/* Description */}
        {yearbook.description && (
          <p
            style={{
              fontFamily: 'var(--font-typewriter)',
              fontSize: '11px',
              color: 'rgba(243, 229, 171, 0.75)',
              marginBottom: '12px',
              lineHeight: 1.3
            }}
          >
            {yearbook.description}
          </p>
        )}

        {/* Bottom Details Row */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            borderTop: '1px solid rgba(212, 175, 55, 0.25)',
            paddingTop: '8px',
            fontSize: '10px',
            color: '#D4AF37',
            fontFamily: 'var(--font-typewriter)'
          }}
        >
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Users size={12} /> {yearbook.memberIds.length} {yearbook.memberIds.length === 1 ? 'Collaborator' : 'Collaborators'}
          </span>

          {sealed ? (
            <span style={{ display: 'flex', alignItems: 'center', gap: '3px', color: '#FFB74D' }}>
              <Clock size={11} /> {daysLeft} days in cloud
            </span>
          ) : (
            <span>Active Yearbook</span>
          )}
        </div>
      </div>
    </div>
  );
};
