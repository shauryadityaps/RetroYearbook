import React, { useState } from 'react';
import { authService } from '../services/authService';
import { User } from '../types';
import { LeatherButton } from '../components/LeatherButton';

interface LoginPageProps {
  onLoginSuccess: (user: User) => void;
}

export const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess }) => {
  const [isRegister, setIsRegister] = useState(false);
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!email || !password) {
      setErrorMessage('Please enter both email and password.');
      return;
    }

    try {
      setIsLoading(true);
      let user: User;
      if (isRegister) {
        user = await authService.signUp(email, password, displayName);
      } else {
        user = await authService.signIn(email, password);
      }
      onLoginSuccess(user);
    } catch (err: any) {
      setErrorMessage(err.message || 'Authentication failed. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '20px',
        backgroundColor: 'var(--color-deep-leather)'
      }}
    >
      <div
        className="parchment-card animate-fade-in"
        style={{
          width: '100%',
          maxWidth: '400px',
          borderRadius: '16px',
          padding: '28px 24px',
          boxShadow: '0 12px 32px rgba(0,0,0,0.5)',
          border: '2px solid var(--color-gold-foil)'
        }}
      >
        {/* Logo / Header */}
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <div
            className="wax-seal wax-seal-red"
            style={{ width: '56px', height: '56px', fontSize: '24px', margin: '0 auto 12px auto' }}
          >
            YB
          </div>
          <div style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-saddle-leather)', letterSpacing: '2px' }}>
            EST. 2026
          </div>
          <h1 style={{ fontFamily: 'var(--font-serif)', fontSize: '24px', color: 'var(--color-dark-sepia)', marginTop: '2px' }}>
            RETRO YEARBOOK
          </h1>
          <p style={{ fontFamily: 'var(--font-typewriter)', fontSize: '11px', color: 'var(--color-muted-sepia)', marginTop: '4px' }}>
            Vintage collaborative photo scrapbooks sealed with wax
          </p>
        </div>

        {/* Tab Switcher */}
        <div
          style={{
            display: 'flex',
            backgroundColor: 'var(--color-parchment-bg)',
            border: '1px solid var(--color-antique-border)',
            borderRadius: '8px',
            padding: '3px',
            marginBottom: '20px'
          }}
        >
          <button
            type="button"
            onClick={() => { setIsRegister(false); setErrorMessage(null); }}
            style={{
              flex: 1,
              padding: '8px',
              border: 'none',
              borderRadius: '6px',
              backgroundColor: !isRegister ? 'var(--color-saddle-leather)' : 'transparent',
              color: !isRegister ? '#FFF' : 'var(--color-muted-sepia)',
              fontFamily: 'var(--font-typewriter)',
              fontSize: '11px',
              fontWeight: 'bold',
              cursor: 'pointer',
              transition: 'all 0.15s ease'
            }}
          >
            SIGN IN
          </button>
          <button
            type="button"
            onClick={() => { setIsRegister(true); setErrorMessage(null); }}
            style={{
              flex: 1,
              padding: '8px',
              border: 'none',
              borderRadius: '6px',
              backgroundColor: isRegister ? 'var(--color-saddle-leather)' : 'transparent',
              color: isRegister ? '#FFF' : 'var(--color-muted-sepia)',
              fontFamily: 'var(--font-typewriter)',
              fontSize: '11px',
              fontWeight: 'bold',
              cursor: 'pointer',
              transition: 'all 0.15s ease'
            }}
          >
            REGISTER
          </button>
        </div>

        {/* Error Notification */}
        {errorMessage && (
          <div
            style={{
              backgroundColor: 'rgba(139, 0, 0, 0.1)',
              border: '1px solid var(--color-wax-red)',
              borderRadius: '6px',
              padding: '10px 12px',
              fontFamily: 'var(--font-typewriter)',
              fontSize: '11px',
              color: 'var(--color-wax-red)',
              marginBottom: '16px'
            }}
          >
            {errorMessage}
          </div>
        )}

        {/* Auth Form */}
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          {isRegister && (
            <div>
              <label style={{ display: 'block', fontFamily: 'var(--font-typewriter)', fontSize: '10px', color: 'var(--color-muted-sepia)', marginBottom: '4px' }}>
                YOUR NAME / NICKNAME
              </label>
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                placeholder="e.g. Shauryaditya"
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  fontFamily: 'var(--font-typewriter)',
                  fontSize: '13px',
                  backgroundColor: 'var(--color-parchment-bg)',
                  border: '1px solid var(--color-antique-border)',
                  borderRadius: '6px',
                  outline: 'none'
                }}
              />
            </div>
          )}

          <div>
            <label style={{ display: 'block', fontFamily: 'var(--font-typewriter)', fontSize: '10px', color: 'var(--color-muted-sepia)', marginBottom: '4px' }}>
              EMAIL ADDRESS
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              style={{
                width: '100%',
                padding: '10px 12px',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '13px',
                backgroundColor: 'var(--color-parchment-bg)',
                border: '1px solid var(--color-antique-border)',
                borderRadius: '6px',
                outline: 'none'
              }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontFamily: 'var(--font-typewriter)', fontSize: '10px', color: 'var(--color-muted-sepia)', marginBottom: '4px' }}>
              PASSWORD
            </label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              style={{
                width: '100%',
                padding: '10px 12px',
                fontFamily: 'var(--font-typewriter)',
                fontSize: '13px',
                backgroundColor: 'var(--color-parchment-bg)',
                border: '1px solid var(--color-antique-border)',
                borderRadius: '6px',
                outline: 'none'
              }}
            />
          </div>

          <div style={{ marginTop: '10px' }}>
            <LeatherButton
              text={isRegister ? 'CREATE YEARBOOK ACCOUNT' : 'ENTER ARCHIVE'}
              type="submit"
              isLoading={isLoading}
            />
          </div>
        </form>

        <p style={{ textAlign: 'center', fontFamily: 'var(--font-typewriter)', fontSize: '10px', color: 'var(--color-muted-sepia)', marginTop: '20px' }}>
          Connects to the shared Retro Yearbook cloud database.
        </p>
      </div>
    </div>
  );
};
