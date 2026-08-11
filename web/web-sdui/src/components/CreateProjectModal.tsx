import React, { useState } from 'react';
import { createMotionProject } from '../services/firestoreService';
import { useAuth } from '../context/AuthContext';
import type { MotionSDUI } from '../infra/types';

interface CreateProjectModalProps {
  initialSdui: MotionSDUI;
  onClose: () => void;
  onCreated: (projectId: string) => void;
}

export const CreateProjectModal: React.FC<CreateProjectModalProps> = ({ initialSdui, onClose, onCreated }) => {
  const { user } = useAuth();
  const [name, setName] = useState('');
  const [metadataStr, setMetadataStr] = useState('{}');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCreate = async () => {
    if (!user) {
      setError('You must be signed in to create a project.');
      return;
    }
    if (!name.trim()) {
      setError('Project name is required.');
      return;
    }

    let metadata = {};
    try {
      metadata = JSON.parse(metadataStr);
    } catch (e) {
      setError('Invalid metadata JSON.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const projectId = await createMotionProject(user.uid, name, initialSdui, metadata);
      onCreated(projectId);
      onClose();
    } catch (err: any) {
      setError(err.message || 'Failed to create project.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.75)',
      backdropFilter: 'blur(4px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 10000,
      padding: '20px'
    }}>
      <div style={{
        width: '100%',
        maxWidth: '400px',
        backgroundColor: '#181820',
        borderRadius: '12px',
        border: '1px solid rgba(255, 255, 255, 0.12)',
        padding: '24px',
        boxShadow: '0 20px 40px rgba(0, 0, 0, 0.6)',
        display: 'flex',
        flexDirection: 'column',
        gap: '20px'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h2 style={{ margin: 0, fontSize: '18px', fontWeight: 600, color: '#fff' }}>Create New Project</h2>
          <button onClick={onClose} style={{
            background: 'none',
            border: 'none',
            color: '#8b8b9e',
            cursor: 'pointer',
            fontSize: '20px',
            padding: '4px'
          }}>×</button>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <label style={{ fontSize: '12px', color: '#8b8b9e', fontWeight: 500 }}>PROJECT NAME</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="My Awesome Video"
            style={{
              backgroundColor: '#0e0e11',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              borderRadius: '8px',
              padding: '10px 12px',
              color: '#fff',
              fontSize: '14px',
              outline: 'none'
            }}
          />
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <label style={{ fontSize: '12px', color: '#8b8b9e', fontWeight: 500 }}>METADATA (JSON)</label>
          <textarea
            value={metadataStr}
            onChange={(e) => setMetadataStr(e.target.value)}
            rows={4}
            style={{
              backgroundColor: '#0e0e11',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              borderRadius: '8px',
              padding: '10px 12px',
              color: '#fff',
              fontSize: '13px',
              outline: 'none',
              fontFamily: 'monospace',
              resize: 'vertical'
            }}
          />
        </div>

        {error && (
          <div style={{
            padding: '10px',
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            borderRadius: '8px',
            color: '#f87171',
            fontSize: '12px'
          }}>
            {error}
          </div>
        )}

        <button
          onClick={handleCreate}
          disabled={loading}
          style={{
            backgroundColor: '#2563eb',
            color: '#fff',
            border: 'none',
            borderRadius: '8px',
            padding: '12px',
            fontSize: '14px',
            fontWeight: 600,
            cursor: loading ? 'not-allowed' : 'pointer',
            transition: 'all 0.2s',
            opacity: loading ? 0.7 : 1,
            marginTop: '8px'
          }}
        >
          {loading ? 'Creating...' : 'Create Project'}
        </button>
      </div>
    </div>
  );
};
