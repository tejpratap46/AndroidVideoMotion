import React, { useState, useRef, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';

export const UserProfileMenu: React.FC = () => {
  const { user, loading, error, isConfigured, signInWithGoogle, logout, clearError } = useAuth();
  const [showDropdown, setShowDropdown] = useState(false);
  const [dropdownPos, setDropdownPos] = useState<{ top: number; right: number }>({ top: 0, right: 0 });
  const containerRef = useRef<HTMLDivElement>(null);

  const toggleDropdown = () => {
    if (!showDropdown && containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      setDropdownPos({
        top: rect.bottom + 6,
        right: Math.max(8, window.innerWidth - rect.right)
      });
    }
    setShowDropdown((prev) => !prev);
  };

  // Close dropdown on click outside or Escape key press
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setShowDropdown(false);
      }
    };
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setShowDropdown(false);
      }
    };

    if (showDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
      document.addEventListener('keydown', handleKeyDown);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [showDropdown]);

  return (
    <div ref={containerRef} style={{ position: 'relative', display: 'inline-block' }}>
      {/* Error alert toast */}
      {error && (
        <div
          style={{
            position: 'absolute',
            top: 'calc(100% + 6px)',
            right: 0,
            zIndex: 1001,
            backgroundColor: '#450a0a',
            color: '#fca5a5',
            border: '1px solid #7f1d1d',
            borderRadius: '6px',
            padding: '6px 10px',
            fontSize: '11px',
            width: '210px',
            boxShadow: '0 8px 24px rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '6px',
            boxSizing: 'border-box'
          }}
        >
          <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{error}</span>
          <button
            onClick={clearError}
            style={{
              background: 'none',
              border: 'none',
              color: '#fca5a5',
              cursor: 'pointer',
              fontWeight: 'bold',
              padding: 0,
              lineHeight: 1,
              fontSize: '14px',
              flexShrink: 0
            }}
          >
            ×
          </button>
        </div>
      )}

      {/* Unconfigured state */}
      {!isConfigured && (
        <button
          className="unconfigured-auth-btn"
          title="Firebase config is missing in .env.local"
          onClick={signInWithGoogle}
        >
          <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#f59e0b', flexShrink: 0 }} />
          <span>Setup Auth</span>
        </button>
      )}

      {/* Configured state */}
      {isConfigured && (
        <>
          {user ? (
            /* Logged In User Compact Trigger Button */
            <div style={{ position: 'relative' }}>
              <button
                onClick={toggleDropdown}
                className={`user-profile-btn ${showDropdown ? 'active' : ''}`}
                aria-label="User account menu"
                aria-expanded={showDropdown}
                title={user.displayName || user.email || 'User Account'}
              >
                {user.photoURL ? (
                  <img src={user.photoURL} alt="" className="user-avatar-img" />
                ) : (
                  <div className="user-avatar-placeholder">
                    {(user.displayName || user.email || 'U').charAt(0).toUpperCase()}
                  </div>
                )}
                <span className="user-profile-name">
                  {user.displayName?.split(' ')[0] || user.email?.split('@')[0] || 'User'}
                </span>
                <svg
                  width="10"
                  height="10"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2.5"
                  style={{
                    transform: showDropdown ? 'rotate(180deg)' : 'rotate(0deg)',
                    transition: 'transform 0.15s ease',
                    opacity: 0.7,
                    flexShrink: 0
                  }}
                >
                  <path d="M6 9l6 6 6-6" />
                </svg>
              </button>

              {/* Compact Dropdown Menu */}
              {showDropdown && (
                <div
                  className="user-dropdown-menu"
                  role="menu"
                  style={{
                    position: 'fixed',
                    top: dropdownPos.top,
                    right: dropdownPos.right,
                    zIndex: 9999
                  }}
                >
                  <div className="user-menu-header">
                    {user.photoURL ? (
                      <img src={user.photoURL} alt="" className="user-menu-header-avatar" />
                    ) : (
                      <div className="user-menu-header-placeholder">
                        {(user.displayName || user.email || 'U').charAt(0).toUpperCase()}
                      </div>
                    )}
                    <div className="user-menu-info">
                      <div className="user-menu-display-name">
                        {user.displayName || 'Google Account'}
                      </div>
                      <div className="user-menu-email">{user.email}</div>
                    </div>
                  </div>

                  <div className="user-menu-divider" />

                  <button
                    onClick={() => {
                      setShowDropdown(false);
                      logout();
                    }}
                    className="user-menu-signout-btn"
                    role="menuitem"
                  >
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                      <polyline points="16 17 21 12 16 7" />
                      <line x1="21" y1="12" x2="9" y2="12" />
                    </svg>
                    <span>Sign Out</span>
                  </button>
                </div>
              )}
            </div>
          ) : (
            /* Logged Out State: Google Sign-In Button */
            <button
              onClick={signInWithGoogle}
              disabled={loading}
              className="google-login-btn"
              style={{ height: '28px' }}
              title="Sign in with Google"
            >
              <svg width="13" height="13" viewBox="0 0 24 24" style={{ flexShrink: 0 }}>
                <path
                  fill="#4285F4"
                  d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                />
                <path
                  fill="#34A853"
                  d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                />
                <path
                  fill="#FBBC05"
                  d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                />
                <path
                  fill="#EA4335"
                  d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                />
              </svg>
              <span>{loading ? 'Signing in...' : 'Sign in'}</span>
            </button>
          )}
        </>
      )}
    </div>
  );
};

