import React, { useState, useRef } from 'react';
import { Camera, Image as ImageIcon, X, Loader2 } from 'lucide-react';
import { LeatherButton } from './LeatherButton';
import { applyDateStampToImage } from '../utils/dateStampRenderer';

interface PhotoDropModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirmDrop: (fileOrBlob: Blob, caption: string) => Promise<void>;
}

export const PhotoDropModal: React.FC<PhotoDropModalProps> = ({ isOpen, onClose, onConfirmDrop }) => {
  const [stampedDataUrl, setStampedDataUrl] = useState<string | null>(null);
  const [stampedBlob, setStampedBlob] = useState<Blob | null>(null);
  const [caption, setCaption] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  const cameraInputRef = useRef<HTMLInputElement>(null);
  const galleryInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handleFileSelected = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      setIsProcessing(true);
      const { blob, dataUrl } = await applyDateStampToImage(file);
      setStampedBlob(blob);
      setStampedDataUrl(dataUrl);
    } catch (err) {
      console.error('Failed to process and stamp photo', err);
      alert('Failed to process image. Please try again.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleDrop = async () => {
    if (!stampedBlob) return;
    try {
      setIsUploading(true);
      await onConfirmDrop(stampedBlob, caption);
      onClose();
    } catch (err: any) {
      alert(err.message || 'Failed to drop photo');
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.65)',
        backdropFilter: 'blur(3px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 9999,
        padding: '16px'
      }}
    >
      {/* Hidden file inputs for Camera and Gallery */}
      <input
        ref={cameraInputRef}
        type="file"
        accept="image/*"
        capture="environment"
        style={{ display: 'none' }}
        onChange={handleFileSelected}
      />
      <input
        ref={galleryInputRef}
        type="file"
        accept="image/*"
        style={{ display: 'none' }}
        onChange={handleFileSelected}
      />

      <div
        className="parchment-card animate-fade-in"
        style={{
          width: '100%',
          maxWidth: '460px',
          borderRadius: '16px',
          padding: '20px',
          maxHeight: '90vh',
          overflowY: 'auto',
          position: 'relative'
        }}
      >
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '14px' }}>
          <div>
            <div style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', letterSpacing: '1px' }}>
              TODAY'S MEMORY
            </div>
            <h2 style={{ fontFamily: 'var(--font-handwriting)', fontSize: '26px', color: 'var(--color-dark-sepia)' }}>
              Drop Today's Photo
            </h2>
          </div>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              color: 'var(--color-muted-sepia)'
            }}
          >
            <X size={22} />
          </button>
        </div>

        {/* Image Preview or Source Selector */}
        {stampedDataUrl ? (
          <div>
            <div
              style={{
                width: '100%',
                aspectRatio: '1/1',
                borderRadius: '8px',
                overflow: 'hidden',
                backgroundColor: '#2B1810',
                border: '1px solid var(--color-antique-border)',
                position: 'relative',
                boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
              }}
            >
              <img
                src={stampedDataUrl}
                alt="Stamped Preview"
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            </div>

            {/* Retake / Change row */}
            <div style={{ display: 'flex', gap: '8px', marginTop: '10px' }}>
              <button
                onClick={() => cameraInputRef.current?.click()}
                style={{
                  flex: 1,
                  padding: '8px',
                  borderRadius: '6px',
                  border: '1px solid var(--color-antique-border)',
                  backgroundColor: 'var(--color-parchment-bg)',
                  color: 'var(--color-dark-sepia)',
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '11px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                  cursor: 'pointer'
                }}
              >
                <Camera size={14} color="var(--color-saddle-leather)" /> Retake Photo
              </button>
              <button
                onClick={() => galleryInputRef.current?.click()}
                style={{
                  flex: 1,
                  padding: '8px',
                  borderRadius: '6px',
                  border: '1px solid var(--color-antique-border)',
                  backgroundColor: 'var(--color-parchment-bg)',
                  color: 'var(--color-dark-sepia)',
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '11px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '6px',
                  cursor: 'pointer'
                }}
              >
                <ImageIcon size={14} color="var(--color-saddle-leather)" /> Change from Gallery
              </button>
            </div>
          </div>
        ) : isProcessing ? (
          <div
            style={{
              width: '100%',
              height: '170.dp',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: 'var(--color-parchment-bg)',
              borderRadius: '10px',
              border: '1px solid var(--color-antique-border)',
              padding: '30px'
            }}
          >
            <Loader2 className="animate-spin" size={32} color="var(--color-saddle-leather)" />
            <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '12px', color: 'var(--color-dark-sepia)', marginTop: '10px' }}>
              Applying vintage amber date stamp...
            </p>
          </div>
        ) : (
          <div>
            <div style={{ fontFamily: 'var(--font-typewriter)', fontSize: '10px', color: 'var(--color-saddle-leather)', letterSpacing: '1px', marginBottom: '8px' }}>
              CHOOSE PHOTO SOURCE
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              {/* Tile 1: Camera on spot */}
              <div
                onClick={() => cameraInputRef.current?.click()}
                style={{
                  padding: '16px 12px',
                  backgroundColor: 'var(--color-parchment-bg)',
                  border: '1.5px solid rgba(89, 38, 11, 0.4)',
                  borderRadius: '10px',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  textAlign: 'center',
                  transition: 'background-color 0.15s ease'
                }}
              >
                <div
                  style={{
                    width: '44px',
                    height: '44px',
                    borderRadius: '50%',
                    backgroundColor: 'var(--color-saddle-leather)',
                    border: '1px solid var(--color-gold-foil)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    marginBottom: '8px'
                  }}
                >
                  <Camera size={22} color="#F3E5AB" />
                </div>
                <span style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', fontWeight: 'bold', color: 'var(--color-dark-sepia)' }}>
                  TAKE PHOTO
                </span>
                <span style={{ fontFamily: 'var(--font-typewriter)', fontSize: '9px', color: 'var(--color-muted-sepia)' }}>
                  On the spot
                </span>
              </div>

              {/* Tile 2: Gallery Picker */}
              <div
                onClick={() => galleryInputRef.current?.click()}
                style={{
                  padding: '16px 12px',
                  backgroundColor: 'var(--color-parchment-bg)',
                  border: '1.5px solid rgba(212, 175, 55, 0.5)',
                  borderRadius: '10px',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  textAlign: 'center'
                }}
              >
                <div
                  style={{
                    width: '44px',
                    height: '44px',
                    borderRadius: '50%',
                    backgroundColor: 'var(--color-parchment-surface)',
                    border: '1px solid var(--color-saddle-leather)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    marginBottom: '8px'
                  }}
                >
                  <ImageIcon size={22} color="var(--color-saddle-leather)" />
                </div>
                <span style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', fontWeight: 'bold', color: 'var(--color-dark-sepia)' }}>
                  FROM GALLERY
                </span>
                <span style={{ fontFamily: 'var(--font-typewriter)', fontSize: '9px', color: 'var(--color-muted-sepia)' }}>
                  Choose existing
                </span>
              </div>
            </div>

            <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '10px', color: 'var(--color-muted-sepia)', textAlign: 'center', marginTop: '10px' }}>
              Automatic vintage date stamp is stamped onto both camera and gallery memories.
            </p>
          </div>
        )}

        {/* Caption Input */}
        <div style={{ marginTop: '16px' }}>
          <label style={{ display: 'block', fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginBottom: '4px' }}>
            HANDWRITTEN CAPTION
          </label>
          <input
            type="text"
            value={caption}
            onChange={(e) => setCaption(e.target.value)}
            placeholder="Write a little note about this moment..."
            style={{
              width: '100%',
              padding: '10px 14px',
              fontFamily: 'var(--font-handwriting)',
              fontSize: '20px',
              color: 'var(--color-dark-sepia)',
              backgroundColor: 'var(--color-parchment-bg)',
              border: '1px solid var(--color-antique-border)',
              borderRadius: '6px',
              outline: 'none'
            }}
          />
        </div>

        {/* Action Button */}
        <div style={{ marginTop: '20px' }}>
          <LeatherButton
            text="SEAL & DROP MEMORY"
            onClick={handleDrop}
            disabled={!stampedBlob || isProcessing}
            isLoading={isUploading}
          />
        </div>
      </div>
    </div>
  );
};
