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

  // Cover Photo or Centerpiece Frame
  const centerFrameW = 340;
  const centerFrameH = 340;
  const centerX = (PAGE_WIDTH - centerFrameW) / 2;
  const centerY = PAGE_HEIGHT / 2 - 80;

  coverPage.drawRectangle({
    x: centerX,
    y: centerY,
    width: centerFrameW,
    height: centerFrameH,
    color: colorDarkParchment,
    borderColor: colorGold,
    borderWidth: 1.5
  });

  if (photos.length > 0) {
    try {
      const firstPhoto = photos[0];
      const imgBytes = await fetch(firstPhoto.photoUrl).then(res => res.arrayBuffer());
      let embeddedImg;
      if (firstPhoto.photoUrl.toLowerCase().includes('.png')) {
        embeddedImg = await pdfDoc.embedPng(imgBytes);
      } else {
        embeddedImg = await pdfDoc.embedJpg(imgBytes);
      }
      coverPage.drawImage(embeddedImg, {
        x: centerX + 12,
        y: centerY + 12,
        width: centerFrameW - 24,
        height: centerFrameH - 24
      });
    } catch {
      // Draw placeholder text if image fetch fails
      coverPage.drawText('SEALED MEMORIES', {
        x: centerX + 100,
        y: centerY + 160,
        size: 16,
        font: fontTypewriterBold,
        color: colorDarkSepia
      });
    }
  }

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
      try {
        const imgBytes = await fetch(photo.photoUrl).then(res => res.arrayBuffer());
        let embedded;
        if (photo.photoUrl.toLowerCase().includes('.png')) {
          embedded = await pdfDoc.embedPng(imgBytes);
        } else {
          embedded = await pdfDoc.embedJpg(imgBytes);
        }
        page.drawImage(embedded, {
          x: polX + 12,
          y: polY + 45,
          width: polW - 24,
          height: polH - 57
        });
      } catch {
        page.drawText('[Photo Memory]', {
          x: polX + 90,
          y: polY + 120,
          size: 12,
          font: fontTypewriter,
          color: colorMutedSepia
        });
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
    x: 30,
    y: 30,
    width: PAGE_WIDTH - 60,
    height: PAGE_HEIGHT - 60,
    borderColor: colorGold,
    borderWidth: 1.5
  });

  backPage.drawText('TIME PASSES, BUT MEMORIES REMAIN.', {
    x: PAGE_WIDTH / 2 - 130,
    y: PAGE_HEIGHT / 2 + 20,
    size: 13,
    font: fontSerif,
    color: colorDarkSepia
  });

  backPage.drawText('Exported from Retro Yearbook', {
    x: PAGE_WIDTH / 2 - 80,
    y: PAGE_HEIGHT / 2 - 10,
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
  const cleanTitle = yearbook.title.trim().replace(/[^a-zA-Z0-9_-]/g, '_');
  a.download = `${cleanTitle}_Scrapbook.pdf`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
  onProgress?.(1.0);
}
