import { getTodayDateString } from '../types';

export function formatDateToVintageStamp(dateString: string): string {
  // Input: YYYY-MM-DD -> Output: 'YY MM DD e.g. '26 08 28
  const parts = dateString.split('-');
  if (parts.length === 3) {
    const yearShort = parts[0].slice(-2);
    const month = parts[1];
    const day = parts[2];
    return `'${yearShort} ${month} ${day}`;
  }
  const d = new Date();
  const yy = String(d.getFullYear()).slice(-2);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `'${yy} ${mm} ${dd}`;
}

export async function applyDateStampToImage(
  imageSource: File | Blob | string,
  customDateString?: string
): Promise<{ blob: Blob; dataUrl: string; width: number; height: number }> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';

    img.onload = () => {
      const srcW = img.naturalWidth || img.width;
      const srcH = img.naturalHeight || img.height;

      // 1. Center crop to 1:1 square (Authentic Polaroid Standard)
      // This prevents the bottom-right date stamp from being clipped by 1:1 polaroid frame containers
      const cropSize = Math.min(srcW, srcH);
      const cropX = Math.round((srcW - cropSize) / 2);
      const cropY = Math.round((srcH - cropSize) / 2);

      // Output size (max 1440px)
      const targetSize = Math.min(cropSize, 1440);

      const canvas = document.createElement('canvas');
      canvas.width = targetSize;
      canvas.height = targetSize;
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        reject(new Error('Canvas 2D context not supported'));
        return;
      }

      // 2. Draw center-cropped square
      ctx.drawImage(img, cropX, cropY, cropSize, cropSize, 0, 0, targetSize, targetSize);

      // 3. Render Amber Digital Date Stamp in bottom-right corner of the square
      const stampText = formatDateToVintageStamp(customDateString || getTodayDateString());
      const fontSize = Math.max(26, Math.round(targetSize * 0.045));

      ctx.save();
      ctx.font = `bold ${fontSize}px 'Courier Prime', 'Special Elite', 'VT323', Courier, monospace`;
      ctx.textAlign = 'right';
      ctx.textBaseline = 'bottom';

      const paddingX = Math.round(targetSize * 0.05);
      const paddingY = Math.round(targetSize * 0.045);
      const stampX = targetSize - paddingX;
      const stampY = targetSize - paddingY;

      // Glow / Shadow for authentic 90s camera LED effect
      ctx.shadowColor = 'rgba(255, 69, 0, 0.9)';
      ctx.shadowBlur = Math.round(fontSize * 0.25);
      ctx.fillStyle = '#FF7700';
      ctx.fillText(stampText, stampX, stampY);

      // Double-pass fill for crisp LED readability
      ctx.shadowBlur = 0;
      ctx.fillStyle = '#FFA500';
      ctx.fillText(stampText, stampX, stampY);
      ctx.restore();

      // 4. Export WebP
      canvas.toBlob(
        (blob) => {
          if (blob) {
            const dataUrl = canvas.toDataURL('image/webp', 0.88);
            resolve({ blob, dataUrl, width: targetSize, height: targetSize });
          } else {
            reject(new Error('Failed to generate image blob'));
          }
        },
        'image/webp',
        0.88
      );
    };

    img.onerror = () => reject(new Error('Failed to load image for date stamping'));

    if (typeof imageSource === 'string') {
      img.src = imageSource;
    } else {
      img.src = URL.createObjectURL(imageSource);
    }
  });
}
