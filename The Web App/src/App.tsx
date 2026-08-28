import React, { useState, useEffect } from 'react';
import { User, TabType } from './types';
import { authService } from './services/authService';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { LibraryPage } from './pages/LibraryPage';
import { AddJoinPage } from './pages/AddJoinPage';
import { InsideYearbookPage } from './pages/InsideYearbookPage';
import { SlideshowPage } from './pages/SlideshowPage';
import { DownloadApkPage } from './pages/DownloadApkPage';
import { NavigationBottomBar } from './components/NavigationBottomBar';

export const App: React.FC = () => {
  const [currentUser, setCurrentUser] = useState<User | null>(authService.getCurrentUser());
  const [currentTab, setCurrentTab] = useState<TabType>('dashboard');
  const [activeYearbookId, setActiveYearbookId] = useState<string | null>(null);
  const [activeSlideshowId, setActiveSlideshowId] = useState<string | null>(null);

  useEffect(() => {
    const user = authService.getCurrentUser();
    setCurrentUser(user);
  }, []);

  const handleLoginSuccess = (user: User) => {
    setCurrentUser(user);
    setCurrentTab('dashboard');
  };

  const handleSignOut = () => {
    authService.signOut();
    setCurrentUser(null);
    setActiveYearbookId(null);
    setActiveSlideshowId(null);
  };

  const handleOpenYearbook = (yearbookId: string) => {
    setActiveYearbookId(yearbookId);
  };

  const handleBackToShelf = () => {
    setActiveYearbookId(null);
  };

  // If not logged in -> Show Login Page
  if (!currentUser) {
    return <LoginPage onLoginSuccess={handleLoginSuccess} />;
  }

  // Fullscreen Slideshow View
  if (activeSlideshowId) {
    return (
      <SlideshowPage
        yearbookId={activeSlideshowId}
        onClose={() => setActiveSlideshowId(null)}
      />
    );
  }

  // Inside Yearbook View
  if (activeYearbookId) {
    return (
      <InsideYearbookPage
        yearbookId={activeYearbookId}
        user={currentUser}
        onBack={handleBackToShelf}
        onOpenSlideshow={(ybId) => setActiveSlideshowId(ybId)}
      />
    );
  }

  // Main Tabs View (Dashboard / Library / AddJoin / Download)
  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <main style={{ flex: 1 }}>
        {currentTab === 'dashboard' && (
          <DashboardPage
            user={currentUser}
            onOpenYearbook={handleOpenYearbook}
            onNavigateToTab={(tab) => setCurrentTab(tab)}
            onSignOut={handleSignOut}
          />
        )}

        {currentTab === 'library' && (
          <LibraryPage
            user={currentUser}
            onOpenYearbook={handleOpenYearbook}
          />
        )}

        {currentTab === 'add_join' && (
          <AddJoinPage
            user={currentUser}
            onOpenYearbook={handleOpenYearbook}
          />
        )}

        {currentTab === 'download' && (
          <DownloadApkPage />
        )}
      </main>

      <NavigationBottomBar
        currentTab={currentTab}
        onSelectTab={(tab) => {
          setActiveYearbookId(null);
          setCurrentTab(tab);
        }}
      />
    </div>
  );
};
