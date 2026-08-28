import React, { useState, useEffect } from 'react';
import { User, Yearbook, PhotoEntry, isAlbumSealed, getDaysUntilDeletion, getTodayDateString } from '../types';
import { yearbookService } from '../services/yearbookService';
import { photoService } from '../services/photoService';
import { exportYearbookToPdf } from '../utils/pdfScrapbookExporter';
import { exportVideoReel } from '../utils/videoReelExporter';
import { PolaroidPhotoCard } from '../components/PolaroidPhotoCard';
import { PhotoDropModal } from '../components/PhotoDropModal';
import { LeatherButton } from '../components/LeatherButton';
import confetti from 'canvas-confetti';
import {
  ArrowLeft,
  Users,
  Copy,
  Check,
  Play,
  FileText,
  Video,
  Lock,
  Camera,
  Loader2,
  Clock,
  Sparkles,
  ChevronDown
} from 'lucide-react';

interface InsideYearbookPageProps {
  yearbookId: string;
  user: User;
  onBack: () => void;
  onOpenSlideshow: (yearbookId: string) => void;
}

export const InsideYearbookPage: React.FC<InsideYearbookPageProps> = ({
  yearbookId,
  user,
  onBack,
  onOpenSlideshow
}) => {
  const [yearbook, setYearbook] = useState<Yearbook | null>(null);
  const [members, setMembers] = useState<User[]>([]);
  const [photos, setPhotos] = useState<PhotoEntry[]>([]);
  const [loading, setLoading] = useState(true);

  const [hasPostedToday, setHasPostedToday] = useState(false);
  const [showDropModal, setShowDropModal] = useState(false);
  const [showMembersDropdown, setShowMembersDropdown] = useState(false);
  const [showSealModal, setShowSealModal] = useState(false);
  const [codeCopied, setCodeCopied] = useState(false);

  // Exporters progress
  const [isExportingPdf, setIsExportingPdf] = useState(false);
  const [isExportingReel, setIsExportingReel] = useState(false);
  const [selectedEnlargedPhoto, setSelectedEnlargedPhoto] = useState<PhotoEntry | null>(null);

  const loadAlbumData = async () => {
    try {
      setLoading(true);
      const yb = await yearbookService.getYearbookById(yearbookId);
      setYearbook(yb);

      const mems = await yearbookService.getYearbookMembers(yearbookId);
      setMembers(mems);

      const pts = await photoService.getPhotosForYearbook(yearbookId);
      setPhotos(pts);

      const posted = await photoService.hasUserPostedToday(yearbookId, user.uid, getTodayDateString());
      setHasPostedToday(posted);
    } catch (err) {
      console.error('Failed to load album', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAlbumData();
  }, [yearbookId, user.uid]);

  if (loading || !yearbook) {
    return (
      <div style={{ minHeight: '80vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '12px' }}>
        <Loader2 className="animate-spin" size={36} color="var(--color-saddle-leather)" />
        <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '13px', color: 'var(--color-muted-sepia)' }}>
          Opening album from shelf...
        </p>
      </div>
    );
  }

  const sealed = isAlbumSealed(yearbook);
  const isOwner = yearbook.ownerId === user.uid;
  const daysLeft = getDaysUntilDeletion(yearbook);

  const handleCopyCode = () => {
    navigator.clipboard.writeText(yearbook.joinCode);
    setCodeCopied(true);
    setTimeout(() => setCodeCopied(false), 2000);
  };

  const handleConfirmDrop = async (blob: Blob, caption: string) => {
    await photoService.uploadDailyPhoto(yearbook.id, user, blob, caption);
    await loadAlbumData();
  };

  const handleSealAlbum = async () => {
    try {
      await yearbookService.sealYearbook(yearbook.id);
      confetti({
        particleCount: 100,
        spread: 70,
        origin: { y: 0.6 }
      });
      setShowSealModal(false);
      await loadAlbumData();
    } catch (err: any) {
      alert(err.message || 'Failed to seal album');
    }
  };

  const handleGeneratePdf = async () => {
    try {
      setIsExportingPdf(true);
      await exportYearbookToPdf(yearbook, photos);
    } catch (err: any) {
      alert(err.message || 'Failed to export PDF');
    } finally {
      setIsExportingPdf(false);
    }
  };

  const handleGenerateReel = async () => {
    try {
      setIsExportingReel(true);
      await exportVideoReel(yearbook, photos);
    } catch (err: any) {
      alert(err.message || 'Failed to export Video Reel');
    } finally {
      setIsExportingReel(false);
    }
  };

  // Group photos by date string descending
  const groupedPhotos = photos.reduce((acc, p) => {
    acc[p.dateString] = acc[p.dateString] || [];
    acc[p.dateString].push(p);
    return acc;
  }, {} as Record<string, PhotoEntry[]>);

  const dateKeys = Object.keys(groupedPhotos).sort((a, b) => b.localeCompare(a));

  return (
    <div style={{ padding: '16px 16px 120px 16px', maxWidth: '740px', margin: '0 auto' }}>
      {/* ==========================================
          TOP NAVIGATION & ACTION BAR
      ========================================== */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '16px'
        }}
      >
        <button
          onClick={onBack}
          style={{
            background: 'none',
            border: 'none',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            color: 'var(--color-dark-sepia)',
            fontFamily: 'var(--font-typewriter)',
            fontSize: '12px'
          }}
        >
          <ArrowLeft size={18} />
          <span>SHELF</span>
        </button>

        {/* Dynamic Action Buttons */}
        <div style={{ display: 'flex', gap: '8px' }}>
          {!sealed ? (
            <>
              {/* RECAP SLIDESHOW BUTTON */}
              <button
                onClick={() => onOpenSlideshow(yearbook.id)}
                style={{
                  padding: '6px 10px',
                  borderRadius: '6px',
                  backgroundColor: 'var(--color-parchment-surface)',
                  border: '1px solid var(--color-saddle-leather)',
                  color: 'var(--color-dark-sepia)',
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '11px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  cursor: 'pointer'
                }}
              >
                <Play size={12} fill="var(--color-saddle-leather)" /> RECAP
              </button>

              {/* CREATOR-ONLY SEAL ALBUM BUTTON */}
              {isOwner && (
                <button
                  onClick={() => setShowSealModal(true)}
                  style={{
                    padding: '6px 10px',
                    borderRadius: '6px',
                    backgroundColor: 'var(--color-wax-red)',
                    border: '1px solid #D4AF37',
                    color: '#F3E5AB',
                    fontFamily: 'var(--font-typewriter)',
                    fontSize: '11px',
                    fontWeight: 'bold',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    cursor: 'pointer'
                  }}
                >
                  <Lock size={12} /> SEAL ALBUM
                </button>
              )}
            </>
          ) : (
            /* SEALED ACTION BUTTONS: PDF & REEL */
            <>
              <button
                onClick={handleGeneratePdf}
                disabled={isExportingPdf}
                style={{
                  padding: '6px 10px',
                  borderRadius: '6px',
                  backgroundColor: 'var(--color-saddle-leather)',
                  border: '1px solid var(--color-gold-foil)',
                  color: '#FFF',
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '11px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  cursor: 'pointer'
                }}
              >
                {isExportingPdf ? <Loader2 className="animate-spin" size={12} /> : <FileText size={12} />} PDF
              </button>

              <button
                onClick={handleGenerateReel}
                disabled={isExportingReel}
                style={{
                  padding: '6px 10px',
                  borderRadius: '6px',
                  backgroundColor: 'var(--color-gold-foil)',
                  border: '1px solid #F3E5AB',
                  color: '#2B1810',
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '11px',
                  fontWeight: 'bold',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  cursor: 'pointer'
                }}
              >
                {isExportingReel ? <Loader2 className="animate-spin" size={12} /> : <Video size={12} />} REEL
              </button>
            </>
          )}
        </div>
      </div>

      {/* ==========================================
          ALBUM HEADER & METADATA
      ========================================== */}
      <div style={{ marginBottom: '18px' }}>
        <h1
          style={{
            fontFamily: 'var(--font-serif)',
            fontSize: '26px',
            color: 'var(--color-dark-sepia)',
            lineHeight: 1.2
          }}
        >
          {yearbook.title}
        </h1>
        {yearbook.description && (
          <p
            style={{
              fontFamily: 'var(--font-typewriter)',
              fontSize: '12px',
              color: 'var(--color-muted-sepia)',
              marginTop: '4px'
            }}
          >
            {yearbook.description}
          </p>
        )}

        {/* Collaborators & Invite Code Row */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginTop: '10px', flexWrap: 'wrap' }}>
          {/* Collaborator Dropdown Toggle */}
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setShowMembersDropdown(!showMembersDropdown)}
              style={{
                padding: '5px 8px',
                borderRadius: '6px',
                backgroundColor: 'var(--color-parchment-surface)',
                border: '1px solid var(--color-antique-border)',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '11px',
                color: 'var(--color-dark-sepia)',
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
                cursor: 'pointer'
              }}
            >
              <Users size={12} /> {members.length} {members.length === 1 ? 'Collaborator' : 'Collaborators'}
              <ChevronDown size={12} />
            </button>

            {/* Dropdown Menu */}
            {showMembersDropdown && (
              <div
                className="parchment-card"
                style={{
                  position: 'absolute',
                  top: '100%',
                  left: 0,
                  marginTop: '4px',
                  width: '240px',
                  borderRadius: '8px',
                  padding: '8px',
                  zIndex: 200,
                  boxShadow: '0 6px 16px rgba(0,0,0,0.2)'
                }}
              >
                <div style={{ fontFamily: 'var(--font-typewriter)', fontSize: '10px', color: 'var(--color-saddle-leather)', marginBottom: '6px', padding: '0 4px' }}>
                  ALBUM MEMBERS
                </div>
                {members.map(m => (
                  <div
                    key={m.uid}
                    style={{
                      padding: '4px 6px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      fontFamily: 'var(--font-typewriter)',
                      fontSize: '11px',
                      borderBottom: '1px solid rgba(196, 180, 154, 0.4)'
                    }}
                  >
                    <span>{m.displayName}</span>
                    {m.uid === yearbook.ownerId && (
                      <span style={{ fontSize: '9px', color: 'var(--color-gold-foil)', fontWeight: 'bold' }}>
                        (OWNER)
                      </span>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Invite Code Badge (ONLY visible when album is NOT sealed) */}
          {!sealed ? (
            <button
              onClick={handleCopyCode}
              title="Click to copy invite code"
              style={{
                padding: '5px 10px',
                borderRadius: '6px',
                backgroundColor: 'rgba(46, 125, 50, 0.1)',
                border: '1px solid var(--color-wax-green)',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '11px',
                color: 'var(--color-wax-green)',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                gap: '5px',
                cursor: 'pointer'
              }}
            >
              {codeCopied ? <Check size={12} /> : <Copy size={12} />}
              <span>CODE: {yearbook.joinCode}</span>
            </button>
          ) : (
            <span
              style={{
                padding: '5px 10px',
                borderRadius: '6px',
                backgroundColor: 'rgba(212, 175, 55, 0.15)',
                border: '1px solid var(--color-gold-foil)',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '11px',
                color: 'var(--color-gold-foil)',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
            >
              <Lock size={11} /> ★ SEALED ARCHIVE
            </span>
          )}
        </div>
      </div>

      {/* ==========================================
          SEALED ARCHIVE BANNER (30-Day Cloud Timer)
      ========================================== */}
      {sealed && (
        <div
          className="leather-card animate-fade-in"
          style={{
            borderRadius: '12px',
            padding: '16px',
            marginBottom: '20px',
            border: '2px solid var(--color-gold-foil)'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
            <span style={{ fontFamily: 'var(--font-serif)', fontSize: '15px', color: '#F3E5AB', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Sparkles size={16} color="#D4AF37" /> ARCHIVE COMPLETE
            </span>
            <span
              style={{
                fontFamily: 'var(--font-typewriter)',
                fontSize: '10.5px',
                backgroundColor: 'rgba(255, 140, 0, 0.25)',
                border: '1px solid #FFA500',
                borderRadius: '4px',
                padding: '2px 6px',
                color: '#FFB74D',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
            >
              <Clock size={11} /> Deletion in {daysLeft} days
            </span>
          </div>

          <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'rgba(243, 229, 171, 0.8)', marginBottom: '14px', lineHeight: 1.35 }}>
            This yearbook is sealed and completed. Generate your high-res PDF Scrapbook and Video Reel to save your memories locally.
          </p>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            <button
              onClick={handleGeneratePdf}
              disabled={isExportingPdf}
              style={{
                padding: '10px',
                backgroundColor: '#D4AF37',
                border: 'none',
                borderRadius: '6px',
                color: '#2B1810',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '11px',
                fontWeight: 'bold',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px'
              }}
            >
              {isExportingPdf ? <Loader2 className="animate-spin" size={14} /> : <FileText size={14} />}
              <span>GENERATE PDF</span>
            </button>

            <button
              onClick={handleGenerateReel}
              disabled={isExportingReel}
              style={{
                padding: '10px',
                backgroundColor: 'var(--color-saddle-leather)',
                border: '1px solid #D4AF37',
                borderRadius: '6px',
                color: '#FFF',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '11px',
                fontWeight: 'bold',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px'
              }}
            >
              {isExportingReel ? <Loader2 className="animate-spin" size={14} /> : <Video size={14} />}
              <span>MAKE REEL</span>
            </button>
          </div>
        </div>
      )}

      {/* ==========================================
          PHOTO STREAM FEED (SCRAPBOOK GRID)
      ========================================== */}
      {photos.length > 0 ? (
        <div>
          {dateKeys.map((dateStr) => (
            <div key={dateStr} style={{ marginBottom: '28px' }}>
              {/* Date Header */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  marginBottom: '14px',
                  borderBottom: '1px dashed var(--color-antique-border)',
                  paddingBottom: '6px'
                }}
              >
                <span style={{ fontFamily: 'var(--font-typewriter)', fontSize: '13px', fontWeight: 'bold', color: 'var(--color-saddle-leather)' }}>
                  {dateStr}
                </span>
                <span style={{ fontFamily: 'var(--font-typewriter)', fontSize: '10.5px', color: 'var(--color-muted-sepia)' }}>
                  ({groupedPhotos[dateStr].length} {groupedPhotos[dateStr].length === 1 ? 'memory' : 'memories'})
                </span>
              </div>

              {/* Responsive 2-Column Polaroid Grid */}
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
                  gap: '16px',
                  alignItems: 'start'
                }}
              >
                {groupedPhotos[dateStr].map((photo, pIdx) => (
                  <div
                    key={photo.id}
                    style={{
                      transform: pIdx % 2 === 0 ? 'rotate(-0.7deg)' : 'rotate(0.7deg)'
                    }}
                  >
                    <PolaroidPhotoCard
                      photo={photo}
                      onClick={() => setSelectedEnlargedPhoto(photo)}
                    />
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div
          className="parchment-card"
          style={{
            borderRadius: '12px',
            padding: '40px 20px',
            textAlign: 'center',
            border: '1.5px dashed var(--color-antique-border)',
            marginTop: '20px'
          }}
        >
          <Camera size={36} color="var(--color-saddle-leather)" style={{ margin: '0 auto 10px auto' }} />
          <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '18px', color: 'var(--color-dark-sepia)' }}>
            No Memories Dropped Yet
          </h3>
          <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginTop: '4px' }}>
            Be the first to drop today's polaroid photo into this scrapbook!
          </p>
        </div>
      )}

      {/* Floating Action Button for Dropping Today's Memory (Active albums only) */}
      {!sealed && !hasPostedToday && (
        <button
          onClick={() => setShowDropModal(true)}
          className="btn-leather"
          style={{
            position: 'fixed',
            bottom: 'calc(76px + var(--safe-bottom))',
            right: '20px',
            padding: '14px 20px',
            borderRadius: '30px',
            fontSize: '12px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            boxShadow: '0 8px 24px rgba(0, 0, 0, 0.4)',
            zIndex: 90
          }}
        >
          <Camera size={18} />
          <span>DROP TODAY'S PHOTO</span>
        </button>
      )}

      {/* Photo Drop Modal */}
      <PhotoDropModal
        isOpen={showDropModal}
        onClose={() => setShowDropModal(false)}
        onConfirmDrop={handleConfirmDrop}
      />

      {/* SEAL ALBUM CONFIRMATION MODAL */}
      {showSealModal && (
        <div
          style={{
            position: 'fixed',
            top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.65)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999,
            padding: '16px'
          }}
        >
          <div
            className="parchment-card animate-fade-in"
            style={{
              width: '100%',
              maxWidth: '400px',
              borderRadius: '14px',
              padding: '24px 20px',
              border: '2px solid var(--color-wax-red)'
            }}
          >
            <div style={{ textAlign: 'center', marginBottom: '14px' }}>
              <div className="wax-seal wax-seal-red" style={{ width: '48px', height: '48px', fontSize: '20px', margin: '0 auto 10px auto' }}>
                ★
              </div>
              <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: '20px', color: 'var(--color-dark-sepia)' }}>
                Seal This Yearbook?
              </h2>
            </div>

            <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', lineHeight: 1.4, marginBottom: '20px', textAlign: 'center' }}>
              Once sealed, the album is marked complete. No more daily photos can be added. Members will be prompted to generate their PDF and Video Reel. A 30-day cloud retention timer will begin.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              <LeatherButton
                text="YES, SEAL & COMPLETE ALBUM"
                variant="danger"
                onClick={handleSealAlbum}
              />
              <button
                onClick={() => setShowSealModal(false)}
                style={{
                  padding: '10px',
                  background: 'none',
                  border: 'none',
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '11px',
                  color: 'var(--color-muted-sepia)',
                  cursor: 'pointer'
                }}
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ENLARGED PHOTO MODAL */}
      {selectedEnlargedPhoto && (
        <div
          onClick={() => setSelectedEnlargedPhoto(null)}
          style={{
            position: 'fixed',
            top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.85)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999,
            padding: '16px'
          }}
        >
          <div
            onClick={(e) => e.stopPropagation()}
            style={{ width: '100%', maxWidth: '420px' }}
          >
            <PolaroidPhotoCard photo={selectedEnlargedPhoto} />
          </div>
        </div>
      )}
    </div>
  );
};
