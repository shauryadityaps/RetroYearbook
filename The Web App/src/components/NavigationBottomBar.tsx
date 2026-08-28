import React from 'react';
import { TabType } from '../types';
import { Sparkles, BookOpen, PlusCircle, Smartphone } from 'lucide-react';

interface NavigationBottomBarProps {
  currentTab: TabType;
  onSelectTab: (tab: TabType) => void;
}

export const NavigationBottomBar: React.FC<NavigationBottomBarProps> = ({ currentTab, onSelectTab }) => {
  const tabs = [
    { id: 'dashboard' as TabType, label: 'TODAY', icon: Sparkles },
    { id: 'library' as TabType, label: 'SHELF', icon: BookOpen },
    { id: 'add_join' as TabType, label: 'NEW / JOIN', icon: PlusCircle },
    { id: 'download' as TabType, label: 'GET APK', icon: Smartphone }
  ];

  return (
    <nav
      style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        height: 'calc(62px + var(--safe-bottom))',
        paddingBottom: 'var(--safe-bottom)',
        backgroundColor: '#2B1810',
        borderTop: '2px solid #D4AF37',
        boxShadow: '0 -4px 16px rgba(0, 0, 0, 0.35)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-around',
        zIndex: 100
      }}
    >
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const isActive = currentTab === tab.id;

        return (
          <button
            key={tab.id}
            onClick={() => onSelectTab(tab.id)}
            style={{
              flex: 1,
              height: '100%',
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '3px',
              color: isActive ? '#D4AF37' : 'rgba(243, 229, 171, 0.55)',
              transition: 'all 0.15s ease'
            }}
          >
            <div
              style={{
                width: '32px',
                height: '24px',
                borderRadius: '12px',
                backgroundColor: isActive ? 'rgba(212, 175, 55, 0.15)' : 'transparent',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
            >
              <Icon size={18} strokeWidth={isActive ? 2.5 : 2} />
            </div>
            <span
              style={{
                fontFamily: 'var(--font-typewriter)',
                fontSize: '9.5px',
                letterSpacing: '1px',
                fontWeight: isActive ? 'bold' : 'normal'
              }}
            >
              {tab.label}
            </span>
          </button>
        );
      })}
    </nav>
  );
};
