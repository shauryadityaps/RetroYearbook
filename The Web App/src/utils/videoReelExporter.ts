import { PhotoEntry, Yearbook } from '../types';

export async function exportVideoReel(
  yearbook: Yearbook,
  photos: PhotoEntry[],
  onProgress?: (progress: number) => void
): Promise<Blob> {
  if (photos.length === 0) {
    throw new Error('Cannot generate reel: No photos available');
  }

  const canvas = document.createElement('canvas');
  const WIDTH = 720;
  const HEIGHT = 1280;
  canvas.width = WIDTH;
  canvas.height = HEIGHT;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('Canvas context not available');

  // Load all images
  const loadedImages: HTMLImageElement[] = [];
  for (let i = 0; i < photos.length; i++) {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    await new Promise<void>((resolve) => {
      img.onload = () => resolve();
      img.onerror = () => resolve();
      img.src = photos[i].photoUrl;
    });
    loadedImages.push(img);
    onProgress?.(0.1 + (0.2 * (i / photos.length)));
  }

  // Setup Web Audio for nostalgic acoustic chord soundscape
  const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
  const dest = audioCtx.createMediaStreamDestination();

  // Create ambient chord oscillator
  const playChords = () => {
    const freqs = [220, 277.18, 329.63, 440]; // A major 7th nostalgic acoustic notes
    freqs.forEach((f, idx) => {
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(f, audioCtx.currentTime);
      gain.gain.setValueAtTime(0.04, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 10);
      osc.connect(gain);
      gain.connect(dest);
      osc.start(audioCtx.currentTime + (idx * 0.5));
      osc.stop(audioCtx.currentTime + 12);
    });
  };

  try {
    playChords();
  } catch (e) {
    console.warn('Audio synthesis fallback', e);
  }

  const videoStream = canvas.captureStream(30);
  const combinedStream = new MediaStream([
    ...videoStream.getVideoTracks(),
    ...dest.stream.getAudioTracks()
  ]);

  const mimeType = MediaRecorder.isTypeSupported('video/mp4;codecs=avc1')
    ? 'video/mp4;codecs=avc1'
    : MediaRecorder.isTypeSupported('video/webm;codecs=vp9')
    ? 'video/webm;codecs=vp9'
    : 'video/webm';

  const mediaRecorder = new MediaRecorder(combinedStream, {
    mimeType,
    videoBitsPerSecond: 2500000
  });

  const chunks: Blob[] = [];
  mediaRecorder.ondataavailable = (e) => {
    if (e.data.size > 0) chunks.push(e.data);
  };

  const framesPerPhoto = 90; // 3 seconds at 30 fps
  const totalFrames = photos.length * framesPerPhoto;

  return new Promise((resolve, reject) => {
    mediaRecorder.onstop = () => {
      const blob = new Blob(chunks, { type: mimeType });
      // Trigger download
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      const cleanTitle = yearbook.title.trim().replace(/[^a-zA-Z0-9_-]/g, '_');
      const ext = mimeType.includes('mp4') ? 'mp4' : 'webm';
      a.download = `${cleanTitle}_Reel.${ext}`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      resolve(blob);
    };

    mediaRecorder.onerror = (err) => reject(err);
    mediaRecorder.start();

    let frame = 0;
    const renderNextFrame = () => {
      if (frame >= totalFrames) {
        mediaRecorder.stop();
        onProgress?.(1.0);
        return;
      }

      const photoIdx = Math.floor(frame / framesPerPhoto);
      const frameInPhoto = frame % framesPerPhoto;
      const progressInPhoto = frameInPhoto / framesPerPhoto;

      const photo = photos[photoIdx];
      const img = loadedImages[photoIdx];

      // 1. Vintage Leather / Parchment Backdrop
      ctx.fillStyle = '#2B1810';
      ctx.fillRect(0, 0, WIDTH, HEIGHT);

      const grad = ctx.createRadialGradient(WIDTH / 2, HEIGHT / 2, 100, WIDTH / 2, HEIGHT / 2, 700);
      grad.addColorStop(0, '#59260B');
      grad.addColorStop(1, '#1A0C06');
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, WIDTH, HEIGHT);

      // Gold border frame
      ctx.strokeStyle = '#D4AF37';
      ctx.lineWidth = 4;
      ctx.strokeRect(30, 30, WIDTH - 60, HEIGHT - 60);

      // Header Text
      ctx.save();
      ctx.font = 'bold 24px "Cinzel", Georgia, serif';
      ctx.fillStyle = '#F3E5AB';
      ctx.textAlign = 'center';
      ctx.fillText(yearbook.title.toUpperCase(), WIDTH / 2, 90);

      ctx.font = '16px "Special Elite", monospace';
      ctx.fillStyle = '#D4AF37';
      ctx.fillText(`MEMORY ${photoIdx + 1} OF ${photos.length}`, WIDTH / 2, 125);
      ctx.restore();

      // 2. Polaroid with Ken Burns Zoom/Pan Animation
      const zoom = 1.0 + (0.08 * progressInPhoto);
      const polW = 540;
      const polH = 620;
      const polX = (WIDTH - polW) / 2;
      const polY = 180;

      ctx.save();
      ctx.translate(WIDTH / 2, polY + polH / 2);
      ctx.scale(zoom, zoom);
      ctx.translate(-WIDTH / 2, -(polY + polH / 2));

      // Polaroid Paper Frame
      ctx.fillStyle = '#FDFBF7';
      ctx.shadowColor = 'rgba(0, 0, 0, 0.4)';
      ctx.shadowBlur = 24;
      ctx.fillRect(polX, polY, polW, polH);
      ctx.shadowBlur = 0;

      // Photo inside Polaroid
      if (img && img.naturalWidth) {
        ctx.drawImage(img, polX + 24, polY + 24, polW - 48, polH - 120);
      }

      // Author & Date
      ctx.font = '16px "Special Elite", monospace';
      ctx.fillStyle = '#6E5849';
      ctx.textAlign = 'left';
      ctx.fillText(`By ${photo.authorName || 'Friend'}  •  ${photo.dateString}`, polX + 28, polY + polH - 65);

      // Handwritten Caption
      if (photo.caption) {
        ctx.font = 'bold 26px "Caveat", cursive';
        ctx.fillStyle = '#2B1810';
        ctx.fillText(`"${photo.caption}"`, polX + 28, polY + polH - 25);
      }
      ctx.restore();

      // Footer
      ctx.save();
      ctx.font = '15px "Special Elite", monospace';
      ctx.fillStyle = '#D4AF37';
      ctx.textAlign = 'center';
      ctx.fillText('★ RETRO YEARBOOK REEL ★', WIDTH / 2, HEIGHT - 70);
      ctx.restore();

      onProgress?.(0.3 + (0.65 * (frame / totalFrames)));
      frame++;
      requestAnimationFrame(renderNextFrame);
    };

    renderNextFrame();
  });
}
