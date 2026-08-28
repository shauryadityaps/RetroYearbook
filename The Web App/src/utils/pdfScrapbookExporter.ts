import { PDFDocument, rgb, StandardFonts } from 'pdf-lib';
import { PhotoEntry, Yearbook } from '../types';

function formatFullDate(dateString: string): string {
  try {
    const [y, m, d] = dateString.split('-').map(Number);
    const date = new Date(y, m - 1, d);
    return date.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  } catch {
    return dateString;
  }
}

/**
 * Converts any image format (.webp, .png, .jpg) to standard JPEG bytes via HTML5 Canvas
 * so it can be reliably embedded into pdf-lib without format rejection.
 */
async function convertImageToJpegBytes(imageUrl: string): Promise<Uint8Array | null> {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';

    img.onload = () => {
      try {
        const canvas = document.createElement('canvas');
        canvas.width = img.naturalWidth || img.width;
        canvas.height = img.naturalHeight || img.height;
        const ctx = canvas.getContext('2d');
        if (!ctx) return resolve(null);

        // Fill background white in case of transparency
        ctx.fillStyle = '#FFFFFF';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        ctx.drawImage(img, 0, 0);

        canvas.toBlob(
          async (blob) => {
            if (!blob) return resolve(null);
            const buffer = await blob.arrayBuffer();
            resolve(new Uint8Array(buffer));
          },
          'image/jpeg',
          0.92
        );
      } catch (err) {
        console.warn('Canvas JPEG conversion failed:', err);
        resolve(null);
      }
    };

    img.onerror = (err) => {
      console.warn('Failed to load image for PDF embedding:', imageUrl, err);
      resolve(null);
    };

    img.src = imageUrl;
  });
}

export async function exportYearbookToPdf(
  yearbook: Yearbook,
  photos: PhotoEntry[],
  onProgress?: (progress: number) => void
): Promise<void> {
  onProgress?.(0.1);
  const pdfDoc = await PDFDocument.create();

  const fontSerif = await pdfDoc.embedFont(StandardFonts.TimesRomanBold);
  const fontRegular = await pdfDoc.embedFont(StandardFonts.TimesRoman);
  const fontTypewriter = await pdfDoc.embedFont(StandardFonts.Courier);
  const fontTypewriterBold = await pdfDoc.embedFont(StandardFonts.CourierBold);

  const PAGE_WIDTH = 595.28; // A4 Portrait
  const PAGE_HEIGHT = 841.89;

  const colorParchment = rgb(0.968, 0.949, 0.906); // #F7F2E7
  const colorDarkParchment = rgb(0.929, 0.894, 0.816); // #EDE4D0
  const colorGold = rgb(0.831, 0.686, 0.216); // #D4AF37
  const colorDarkSepia = rgb(0.168, 0.094, 0.063); // #2B1810
  const colorMutedSepia = rgb(0.431, 0.345, 0.286); // #6E5849
  const colorWhite = rgb(1, 1, 1);

  // ==========================================
  // PAGE 1: COVER PAGE
  // ==========================================
  onProgress?.(0.2);
  const coverPage = pdfDoc.addPage([PAGE_WIDTH, PAGE_HEIGHT]);

  // Background
  coverPage.drawRectangle({
    x: 0,
    y: 0,
    width: PAGE_WIDTH,
    height: PAGE_HEIGHT,
    color: colorParchment
  });

  // Vintage Double Border
  coverPage.drawRectangle({
    x: 24,
    y: 24,
    width: PAGE_WIDTH - 48,
    height: PAGE_HEIGHT - 48,
    borderColor: colorGold,
    borderWidth: 2
  });

  coverPage.drawRectangle({
    x: 30,
    y: 30,
    width: PAGE_WIDTH - 60,
    height: PAGE_HEIGHT - 60,
    borderColor: colorDarkSepia,
    borderWidth: 0.8
  });

  // Top Subtitle
  coverPage.drawText('THE RETRO SCRAPBOOK ARCHIVE', {
    x: PAGE_WIDTH / 2 - 120,
    y: PAGE_HEIGHT - 90,
    size: 11,
    font: fontTypewriter,
    color: colorMutedSepia
  });

  // Album Title
  const titleText = yearbook.title.toUpperCase();
  const titleWidth = fontSerif.widthOfTextAtSize(titleText, 26);
  coverPage.drawText(titleText, {
    x: Math.max(40, (PAGE_WIDTH - titleWidth) / 2),
    y: PAGE_HEIGHT - 130,
    size: 26,
    font: fontSerif,
    color: colorDarkSepia
  });

  // Description
  if (yearbook.description) {
    const descWidth = fontRegular.widthOfTextAtSize(yearbook.description, 13);
    coverPage.drawText(yearbook.description, {
      x: Math.max(40, (PAGE_WIDTH - descWidth) / 2),
      y: PAGE_HEIGHT - 160,
      size: 13,
      font: fontRegular,
      color: colorMutedSepia
    });
  }

  // Centerpiece Decorative Wax Seal Symbol
  coverPage.drawCircle({
    x: PAGE_WIDTH / 2,
    y: PAGE_HEIGHT / 2 + 30,
    size: 50,
    color: rgb(0.545, 0, 0) // Wax Seal Red
  });

  coverPage.drawCircle({
    x: PAGE_WIDTH / 2,
    y: PAGE_HEIGHT / 2 + 30,
    size: 44,
    borderColor: colorGold,
    borderWidth: 1.5
  });

  coverPage.drawText('★', {
    x: PAGE_WIDTH / 2 - 9,
    y: PAGE_HEIGHT / 2 + 20,
    size: 24,
    font: fontSerif,
    color: colorGold
  });

  // Cover Bottom Details
  coverPage.drawText(`Total Memories Preserved: ${photos.length}`, {
    x: PAGE_WIDTH / 2 - 80,
    y: 110,
    size: 11,
    font: fontTypewriter,
    color: colorDarkSepia
  });

  coverPage.drawText('Sealed with Nostalgia', {
    x: PAGE_WIDTH / 2 - 60,
    y: 85,
    size: 11,
    font: fontRegular,
    color: colorMutedSepia
  });

  // ==========================================
  // PAGES 2+: PHOTO ENTRIES (2 PER PAGE)
  // ==========================================
  const sortedPhotos = [...photos].sort((a, b) => a.timestamp - b.timestamp);
  const photosPerPage = 2;

  for (let i = 0; i < sortedPhotos.length; i += photosPerPage) {
    const pagePhotos = sortedPhotos.slice(i, i + photosPerPage);
    const page = pdfDoc.addPage([PAGE_WIDTH, PAGE_HEIGHT]);

    page.drawRectangle({
      x: 0,
      y: 0,
      width: PAGE_WIDTH,
      height: PAGE_HEIGHT,
      color: colorParchment
    });

    page.drawRectangle({
      x: 24,
      y: 24,
      width: PAGE_WIDTH - 48,
      height: PAGE_HEIGHT - 48,
      borderColor: colorDarkParchment,
      borderWidth: 1
    });

    for (let slot = 0; slot < pagePhotos.length; slot++) {
      const photo = pagePhotos[slot];
      const slotY = slot === 0 ? PAGE_HEIGHT - 60 : PAGE_HEIGHT / 2 - 30;

      // Clean Date Header (No Calendar Emoji!)
      const fullDateStr = formatFullDate(photo.dateString);
      page.drawText(fullDateStr, {
        x: 48,
        y: slotY,
        size: 13,
        font: fontTypewriterBold,
        color: colorDarkSepia
      });

      page.drawText(`Captured by ${photo.authorName || 'Friend'}`, {
        x: 48,
        y: slotY - 14,
        size: 10,
        font: fontRegular,
        color: colorMutedSepia
      });

      // Polaroid Card Frame
      const polW = 280;
      const polH = 260;
      const polX = (PAGE_WIDTH - polW) / 2;
      const polY = slotY - 290;

      page.drawRectangle({
        x: polX,
        y: polY,
        width: polW,
        height: polH,
        color: colorWhite,
        borderColor: colorDarkParchment,
        borderWidth: 1
      });

      // Photo Image inside Polaroid
      const jpegBytes = await convertImageToJpegBytes(photo.photoUrl);
      if (jpegBytes) {
        try {
          const embedded = await pdfDoc.embedJpg(jpegBytes);
          page.drawImage(embedded, {
            x: polX + 12,
            y: polY + 45,
            width: polW - 24,
            height: polH - 57
          });
        } catch (embedErr) {
          console.warn('Failed to embed JPG into PDF:', embedErr);
        }
      }

      // Handwritten Caption below Polaroid
      if (photo.caption) {
        page.drawText(`"${photo.caption}"`, {
          x: polX + 16,
          y: polY + 20,
          size: 12,
          font: fontRegular,
          color: colorDarkSepia
        });
      }
    }

    onProgress?.(0.2 + 0.7 * ((i + photosPerPage) / sortedPhotos.length));
  }

  // ==========================================
  // FINAL PAGE: BACK COVER
  // ==========================================
  const backPage = pdfDoc.addPage([PAGE_WIDTH, PAGE_HEIGHT]);
  backPage.drawRectangle({
    x: 0,
    y: 0,
    width: PAGE_WIDTH,
    height: PAGE_HEIGHT,
    color: colorParchment
  });

  backPage.drawRectangle({
    x: 24,
    y: 24,
    width: PAGE_WIDTH - 48,
    height: PAGE_HEIGHT - 48,
    borderColor: colorGold,
    borderWidth: 1.5
  });

  backPage.drawText('THE END OF THIS CHAPTER', {
    x: PAGE_WIDTH / 2 - 95,
    y: PAGE_HEIGHT / 2 + 40,
    size: 14,
    font: fontSerif,
    color: colorDarkSepia
  });

  backPage.drawText('Preserved forever in your personal archive.', {
    x: PAGE_WIDTH / 2 - 115,
    y: PAGE_HEIGHT / 2 + 15,
    size: 11,
    font: fontRegular,
    color: colorMutedSepia
  });

  backPage.drawText('Retro Yearbook • Nostalgic Scrapbook App', {
    x: PAGE_WIDTH / 2 - 110,
    y: 60,
    size: 10,
    font: fontTypewriter,
    color: colorMutedSepia
  });

  onProgress?.(0.95);

  // Trigger browser download
  const pdfBytes = await pdfDoc.save();
  const blob = new Blob([pdfBytes.buffer as ArrayBuffer], { type: 'application/pdf' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${yearbook.title.toLowerCase().replace(/\s+/g, '_')}_scrapbook.pdf`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);

  onProgress?.(1.0);
}
