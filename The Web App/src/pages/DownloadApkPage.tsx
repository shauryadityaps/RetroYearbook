import React, { useState, useEffect } from 'react';
import { LeatherButton } from '../components/LeatherButton';
import {
  Download,
  Smartphone,
  ShieldCheck,
  CheckCircle,
  Sparkles,
  HelpCircle,
  ExternalLink,
  Tag,
  Calendar,
  HardDrive
} from 'lucide-react';

interface GitHubReleaseInfo {
  tagName: string;
  releaseName: string;
  downloadUrl: string;
  sizeMb: string;
  publishedDate: string;
  body: string;
}

export const DownloadApkPage: React.FC = () => {
  const repo = import.meta.env.VITE_GITHUB_REPO || 'shauryadityaps/RetroYearbook';
  const defaultApkName = import.meta.env.VITE_APK_FILENAME || 'RetroYearbook.apk';

  const defaultDownloadUrl = `https://github.com/${repo}/releases/latest/download/${defaultApkName}`;

  const [releaseInfo, setReleaseInfo] = useState<GitHubReleaseInfo>({
    tagName: 'v1.0.0',
    releaseName: 'Retro Yearbook Initial Release',
    downloadUrl: defaultDownloadUrl,
    sizeMb: '~25.4 MB',
    publishedDate: 'Latest',
    body: 'Stable production build with hardware camera, amber LED date stamps, and collaborative scrapbooks.'
  });

  const [isLoading, setIsLoading] = useState(false);
  const [downloadStarted, setDownloadStarted] = useState(false);

  useEffect(() => {
    async function fetchLatestRelease() {
      if (!repo || repo.includes('your-username')) return;
      try {
        setIsLoading(true);
        const res = await fetch(`https://api.github.com/repos/${repo}/releases/latest`);
        if (!res.ok) return;

        const data = await res.json();
        // Look for .apk asset in release assets
        const apkAsset = data.assets?.find((a: any) =>
          a.name.toLowerCase().endsWith('.apk')
        );

        const downloadUrl = apkAsset?.browser_download_url || `https://github.com/${repo}/releases/latest/download/${defaultApkName}`;
        const sizeMb = apkAsset ? `${(apkAsset.size / (1024 * 1024)).toFixed(1)} MB` : '~25.4 MB';
        const dateStr = data.published_at
          ? new Date(data.published_at).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
          : 'Latest';

        setReleaseInfo({
          tagName: data.tag_name || 'v1.0.0',
          releaseName: data.name || 'Retro Yearbook APK',
          downloadUrl,
          sizeMb,
          publishedDate: dateStr,
          body: data.body || 'Latest release with performance improvements and bug fixes.'
        });
      } catch (err) {
        console.warn('Could not fetch GitHub release details, using default fallback', err);
      } finally {
        setIsLoading(false);
      }
    }

    fetchLatestRelease();
  }, [repo, defaultApkName]);

  const handleDownloadClick = () => {
    // Navigate directly to the GitHub latest release APK download URL
    window.location.href = releaseInfo.downloadUrl;
    setDownloadStarted(true);
  };

  return (
    <div style={{ padding: '20px 16px 120px 16px', maxWidth: '640px', margin: '0 auto' }}>
      {/* Page Header */}
      <div style={{ marginBottom: '20px' }}>
        <div style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', letterSpacing: '1px' }}>
          OFFICIAL ANDROID CLIENT
        </div>
        <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: '26px', color: 'var(--color-dark-sepia)' }}>
          GET ANDROID APP
        </h1>
      </div>

      {/* Main Download Card */}
      <div
        className="parchment-card animate-fade-in"
        style={{
          borderRadius: '16px',
          padding: '24px 20px',
          border: '2px solid var(--color-gold-foil)',
          boxShadow: '0 8px 24px rgba(43, 24, 16, 0.15)',
          marginBottom: '24px',
          position: 'relative'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '16px' }}>
          <div
            className="wax-seal wax-seal-red"
            style={{ width: '56px', height: '56px', fontSize: '22px', flexShrink: 0 }}
          >
            YB
          </div>

          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap' }}>
              <h2 style={{ fontFamily: 'var(--font-serif)', fontSize: '20px', color: 'var(--color-dark-sepia)' }}>
                Retro Yearbook
              </h2>
              <span
                style={{
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '10.5px',
                  backgroundColor: 'rgba(46, 125, 50, 0.15)',
                  color: 'var(--color-wax-green)',
                  border: '1px solid var(--color-wax-green)',
                  borderRadius: '4px',
                  padding: '2px 8px',
                  fontWeight: 'bold',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px'
                }}
              >
                <Tag size={11} /> {releaseInfo.tagName}
              </span>
            </div>
            <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginTop: '2px' }}>
              Native Android Package • Direct APK from GitHub
            </p>
          </div>
        </div>

        <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '12px', color: 'var(--color-dark-sepia)', lineHeight: 1.45, marginBottom: '18px' }}>
          Download the high-performance Android APK directly to your phone. Always downloads the latest version released on GitHub!
        </p>

        {/* Specs Table */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: '1fr 1fr',
            gap: '10px',
            backgroundColor: 'var(--color-parchment-bg)',
            border: '1px solid var(--color-antique-border)',
            borderRadius: '8px',
            padding: '12px',
            marginBottom: '20px',
            fontFamily: 'var(--font-typewriter)',
            fontSize: '11px'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <HardDrive size={13} color="var(--color-muted-sepia)" />
            <span>Size: <strong style={{ color: 'var(--color-dark-sepia)' }}>{releaseInfo.sizeMb}</strong></span>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Calendar size={13} color="var(--color-muted-sepia)" />
            <span>Updated: <strong style={{ color: 'var(--color-dark-sepia)' }}>{releaseInfo.publishedDate}</strong></span>
          </div>

          <div>
            <span style={{ color: 'var(--color-muted-sepia)' }}>Platform:</span>{' '}
            <strong style={{ color: 'var(--color-dark-sepia)' }}>Android 8.0+</strong>
          </div>

          <div>
            <span style={{ color: 'var(--color-muted-sepia)' }}>Security:</span>{' '}
            <strong style={{ color: 'var(--color-wax-green)' }}>Verified Safe</strong>
          </div>
        </div>

        {/* Download Button */}
        <LeatherButton
          text="DOWNLOAD LATEST ANDROID APK"
          icon={<Download size={18} />}
          onClick={handleDownloadClick}
          isLoading={isLoading}
        />

        {downloadStarted && (
          <div
            className="animate-fade-in"
            style={{
              marginTop: '14px',
              padding: '10px 12px',
              backgroundColor: 'rgba(46, 125, 50, 0.1)',
              border: '1px solid var(--color-wax-green)',
              borderRadius: '6px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              fontFamily: 'var(--font-typewriter)',
              fontSize: '11px',
              color: 'var(--color-wax-green)'
            }}
          >
            <CheckCircle size={16} />
            <span>Downloading latest APK from GitHub! Follow the installation steps below.</span>
          </div>
        )}

        {/* GitHub Releases Link */}
        <div style={{ textAlign: 'center', marginTop: '14px' }}>
          <a
            href={`https://github.com/${repo}/releases`}
            target="_blank"
            rel="noopener noreferrer"
            style={{
              fontFamily: 'var(--font-typewriter)',
              fontSize: '10.5px',
              color: 'var(--color-saddle-leather)',
              textDecoration: 'underline',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '4px'
            }}
          >
            View all releases & changelog on GitHub <ExternalLink size={11} />
          </a>
        </div>
      </div>

      {/* Step-by-Step Installation Guide */}
      <div
        className="parchment-card animate-fade-in"
        style={{
          borderRadius: '16px',
          padding: '20px',
          border: '1.5px solid var(--color-antique-border)',
          marginBottom: '24px'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
          <HelpCircle size={18} color="var(--color-saddle-leather)" />
          <h3 style={{ fontFamily: 'var(--font-serif)', fontSize: '17px', color: 'var(--color-dark-sepia)' }}>
            Installation Instructions for Android
          </h3>
        </div>

        <ol style={{ paddingLeft: '20px', fontFamily: 'var(--font-typewriter)', fontSize: '11.5px', color: 'var(--color-dark-sepia)', lineHeight: 1.6, display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <li>
            <strong>Download APK</strong>: Tap the golden <em>"DOWNLOAD LATEST ANDROID APK"</em> button above.
          </li>
          <li>
            <strong>Confirm Download</strong>: If your browser shows <em>"File might be harmful"</em>, tap <strong>Download anyway</strong> (this is standard Android security for direct APK downloads).
          </li>
          <li>
            <strong>Allow Unknown Apps</strong>: Tap the downloaded notification. If prompted, tap <em>Settings</em> and toggle <strong>"Allow from this source"</strong>.
          </li>
          <li>
            <strong>Install & Enjoy</strong>: Tap <strong>Install</strong>, then open <strong>Retro Yearbook</strong> to collaborate on scrapbooks with your friends!
          </li>
        </ol>
      </div>

      {/* Features Overview */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
          gap: '12px'
        }}
      >
        <div
          style={{
            padding: '14px',
            backgroundColor: 'var(--color-parchment-surface)',
            border: '1px solid var(--color-antique-border)',
            borderRadius: '10px',
            fontFamily: 'var(--font-typewriter)',
            fontSize: '11px'
          }}
        >
          <div style={{ color: 'var(--color-saddle-leather)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Smartphone size={16} /> <strong>Hardware Camera</strong>
          </div>
          <p style={{ color: 'var(--color-muted-sepia)' }}>
            Capture memories directly with physical camera sensor support.
          </p>
        </div>

        <div
          style={{
            padding: '14px',
            backgroundColor: 'var(--color-parchment-surface)',
            border: '1px solid var(--color-antique-border)',
            borderRadius: '10px',
            fontFamily: 'var(--font-typewriter)',
            fontSize: '11px'
          }}
        >
          <div style={{ color: 'var(--color-saddle-leather)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Sparkles size={16} /> <strong>Media3 Video Reels</strong>
          </div>
          <p style={{ color: 'var(--color-muted-sepia)' }}>
            Hardware-accelerated MP4 transformer pipeline with acoustic soundtrack.
          </p>
        </div>

        <div
          style={{
            padding: '14px',
            backgroundColor: 'var(--color-parchment-surface)',
            border: '1px solid var(--color-antique-border)',
            borderRadius: '10px',
            fontFamily: 'var(--font-typewriter)',
            fontSize: '11px'
          }}
        >
          <div style={{ color: 'var(--color-saddle-leather)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <ShieldCheck size={16} /> <strong>Cloud Sync</strong>
          </div>
          <p style={{ color: 'var(--color-muted-sepia)' }}>
            Seamlessly synced with the Web App and iOS collaborators.
          </p>
        </div>
      </div>
    </div>
  );
};
