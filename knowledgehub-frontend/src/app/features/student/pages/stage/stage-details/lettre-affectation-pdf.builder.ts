import jsPDF from 'jspdf';

// ─────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────

export interface LettreAffectationData {
  studentName: string;
  filiere: string;
  companyName: string;
  companyAddress: string;
  dateDebut: string; // pre-formatted as 'dd/mm/yyyy'
  dateFin: string; // pre-formatted as 'dd/mm/yyyy'
  anneeAcademique: string;
  today: string; // pre-formatted as long French date
}

// ─────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────

const PAGE_W = 210;
const PAGE_H = 297;
const MARGIN_L = 18;
const MARGIN_R = 18;
const CONTENT_W = PAGE_W - MARGIN_L - MARGIN_R;

const COLOR = {
  red: [198, 12, 48] as [number, number, number],
  dark: [30, 30, 30] as [number, number, number],
  gray: [100, 100, 100] as [number, number, number],
  lightGray: [220, 220, 220] as [number, number, number],
  offWhite: [250, 250, 252] as [number, number, number],
};

// ─────────────────────────────────────────────
// Image loader
// ─────────────────────────────────────────────

function loadImage(url: string): Promise<HTMLImageElement> {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => resolve(img);
    img.onerror = () => resolve(img); // resolve anyway; caller checks naturalWidth
    img.src = url;
  });
}

// ─────────────────────────────────────────────
// Section drawers
// ─────────────────────────────────────────────

function drawHeader(
  doc: jsPDF,
  sloganImg: HTMLImageElement,
  logoImg: HTMLImageElement,
): void {
  const headerH = 22;

  if (logoImg.naturalWidth) {
    const h = headerH;
    const w = (logoImg.naturalWidth / logoImg.naturalHeight) * h;
    doc.addImage(logoImg, 'JPEG', PAGE_W - MARGIN_R - w, 10, w, h);
  }

  if (sloganImg.naturalWidth) {
    const h = headerH * 0.45;
    const w = (sloganImg.naturalWidth / sloganImg.naturalHeight) * h;
    doc.addImage(sloganImg, 'PNG', MARGIN_L, 10 + (headerH - h) / 2, w, h);
  }

  doc.setDrawColor(...COLOR.red);
  doc.setLineWidth(0.6);
  doc.line(MARGIN_L, 34, PAGE_W - MARGIN_R, 34);
}

function drawTitle(doc: jsPDF, anneeAcademique: string): void {
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(15);
  doc.setTextColor(...COLOR.red);
  doc.text("LETTRE D'AFFECTATION", PAGE_W / 2, 42, { align: 'center' });

  doc.setFont('helvetica', 'normal');
  doc.setFontSize(10);
  doc.setTextColor(...COLOR.dark);
  doc.text(
    `Stage d'été — Année académique ${anneeAcademique}`,
    PAGE_W / 2,
    48,
    { align: 'center' },
  );
}

function drawDate(doc: jsPDF, today: string): void {
  doc.setFontSize(9);
  doc.setTextColor(...COLOR.gray);
  doc.text(`Monastir, le ${today}`, PAGE_W - MARGIN_R, 55, { align: 'right' });
}

function drawBody(doc: jsPDF, data: LettreAffectationData): number {
  let y = 63;

  // Salutation
  doc.setFontSize(10);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(...COLOR.dark);
  doc.text('Madame, Monsieur,', MARGIN_L, y);
  y += 8;

  // First paragraph
  const para1 =
    `Nous avons le plaisir de vous informer que ${data.studentName}, ` +
    `étudiant(e) en ${data.filiere} à ESPRIM, est affecté(e) en stage au sein de ` +
    `votre entreprise. Ce stage s'inscrit dans le cadre de sa formation et a ` +
    `pour objectif de lui permettre de mettre en pratique les compétences ` +
    `acquises, tout en découvrant le milieu professionnel.`;

  doc.setFont('helvetica', 'normal');
  const lines1 = doc.splitTextToSize(para1, CONTENT_W);
  doc.text(lines1, MARGIN_L, y, { align: 'justify', maxWidth: CONTENT_W });
  y += lines1.length * 5.5 + 4;

  // Second paragraph
  const para2 =
    `Nous vous serions reconnaissants de bien vouloir accueillir cet(te) ` +
    `étudiant(e) et de lui offrir les meilleures conditions pour réaliser son stage ` +
    `dans de bonnes conditions.`;

  const lines2 = doc.splitTextToSize(para2, CONTENT_W);
  doc.text(lines2, MARGIN_L, y, { align: 'justify', maxWidth: CONTENT_W });
  y += lines2.length * 5.5 + 8;

  return y;
}

function drawInfoBox(
  doc: jsPDF,
  data: LettreAffectationData,
  startY: number,
): number {
  const rows: [string, string][] = [
    ['Période de stage :', `du ${data.dateDebut} au ${data.dateFin}`],
    ["Société d'accueil :", data.companyName],
    ['Adresse :', data.companyAddress],
  ];

  const BOX_PAD = 5;
  const ROW_H = 8;
  const boxH = BOX_PAD * 2 + rows.length * ROW_H + 4;

  // Box background + border
  doc.setFillColor(...COLOR.offWhite);
  doc.setDrawColor(...COLOR.lightGray);
  doc.setLineWidth(0.3);
  doc.roundedRect(MARGIN_L, startY, CONTENT_W, boxH, 2, 2, 'FD');

  // Red left accent bar
  doc.setFillColor(...COLOR.red);
  doc.roundedRect(MARGIN_L, startY, 3, boxH, 1, 1, 'F');

  // Box legend
  doc.setFontSize(8);
  doc.setTextColor(...COLOR.red);
  doc.setFont('helvetica', 'bold');
  doc.text('INFORMATIONS DU STAGE', MARGIN_L + 8, startY + 5.5);

  // Rows
  doc.setFontSize(9.5);
  rows.forEach(([label, value], i) => {
    const ry = startY + BOX_PAD + 6 + i * ROW_H;
    doc.setFont('helvetica', 'bold');
    doc.setTextColor(...COLOR.dark);
    doc.text(label, MARGIN_L + 8, ry);

    doc.setFont('helvetica', 'normal');
    doc.setTextColor(...COLOR.gray);
    doc.text(value, MARGIN_L + 55, ry);
  });

  return startY + boxH + 14;
}

function drawClosing(doc: jsPDF, startY: number): number {
  const text =
    `Nous restons à votre disposition pour tout renseignement complémentaire ` +
    `et vous prions d'agréer, Madame, Monsieur, l'expression de nos salutations distinguées.`;

  doc.setFontSize(10);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(...COLOR.dark);

  const lines = doc.splitTextToSize(text, CONTENT_W);
  doc.text(lines, MARGIN_L, startY, { align: 'justify', maxWidth: CONTENT_W });

  return startY + lines.length * 5.5 + 10;
}

function drawSignature(
  doc: jsPDF,
  signatureImg: HTMLImageElement,
  startY: number,
): void {
  const centerX = PAGE_W - MARGIN_R - 30;

  doc.setFont('helvetica', 'bold');
  doc.setFontSize(9.5);
  doc.setTextColor(...COLOR.dark);
  doc.text('Le Directeur des Stages', centerX, startY, { align: 'center' });

  if (signatureImg.naturalWidth) {
    const h = 22;
    const w = (signatureImg.naturalWidth / signatureImg.naturalHeight) * h;
    doc.addImage(signatureImg, 'PNG', centerX - w / 2, startY + 3, w, h);
  }
}

function drawFooter(doc: jsPDF, footerImg: HTMLImageElement): void {
  if (!footerImg.naturalWidth) return;

  // Scale image to full page width, preserving aspect ratio
  const imgW = PAGE_W;
  const imgH = (footerImg.naturalHeight / footerImg.naturalWidth) * imgW;
  doc.addImage(footerImg, 'PNG', 0, PAGE_H - imgH, imgW, imgH);
}

// ─────────────────────────────────────────────
// Public entry point
// ─────────────────────────────────────────────

export async function generateLettreAffectationPdf(
  data: LettreAffectationData,
  filename: string,
): Promise<void> {
  const origin = window.location.origin;

  const [sloganImg, logoImg, signatureImg, footerImg] = await Promise.all([
    loadImage(`${origin}/assets/images/en1.jpeg`),
    loadImage(`${origin}/assets/images/ESPRIMLOGO.jpg`),
    loadImage(`${origin}/assets/images/siganture.png`),
    loadImage(`${origin}/assets/images/p2.png`),
  ]);

  const doc = new jsPDF({ unit: 'mm', format: 'a4', orientation: 'portrait' });

  drawHeader(doc, sloganImg, logoImg);
  drawTitle(doc, data.anneeAcademique);
  drawDate(doc, data.today);

  let y = drawBody(doc, data);
  y = drawInfoBox(doc, data, y);
  y = drawClosing(doc, y);

  drawSignature(doc, signatureImg, y);
  drawFooter(doc, footerImg);

  doc.save(filename);
}
