import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { useApplyEffects } from '../effects/useApplyEffects';
import { getAssetUri } from '../infra/assetUtils';

export const SyncedLyricsMotionTextView: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const style = useApplyEffects(props, currentFrame, config);

  const fontFamily = props.fontAsset ? getAssetUri(props.fontAsset) : undefined;

  const lyrics = props.lyrics || [];

  // Find current lyric
  let currentText = "";
  if (lyrics.length > 0) {
    const currentLyric = [...lyrics].reverse().find((l: any) => l.frame <= currentFrame);
    currentText = currentLyric ? currentLyric.text : lyrics[0].text;
  }

  // Map MotionTextVariant to font size (rough estimation)
  const getFontSize = (variant?: string) => {
    switch (variant) {
      case 'H1': return '4rem';
      case 'H2': return '3rem';
      case 'H3': return '2.5rem';
      case 'H4': return '2rem';
      case 'H5': return '1.5rem';
      case 'H6': return '1.2rem';
      case 'P': return '1rem';
      default: return '2rem';
    }
  };

  return (
    <div
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: getFontSize(props.textSizeVariant),
        color: props.textColor || 'white',
        textAlign: 'center',
        fontFamily: fontFamily,
        padding: '20px',
        boxSizing: 'border-box',
        ...style,
      }}
    >
      {currentText}
    </div>
  );
};
