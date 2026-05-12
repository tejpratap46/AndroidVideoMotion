import React from 'react';
import type { MotionViewProps } from '../infra/types';
import { TransparentTextView } from './TransparentTextView';
import { GradientView } from './GradientView';
import { VideoFrameView } from './VideoFrameView';
import { WordWriterTextView } from './WordWriterTextView';
import { AudioWaveformView } from './AudioWaveformView';
import { PopUpTextView } from './PopUpTextView';
import { MultiLyricsContainer } from './MultiLyricsContainer';

export const ViewRegistry: Record<string, React.FC<{ props: MotionViewProps; currentFrame: number }>> = {
  TransparentTextView: TransparentTextView,
  TypeWriterTextView: WordWriterTextView,
  WordWriterTextView: WordWriterTextView,
  GradientView: GradientView,
  VideoFrameView: VideoFrameView,
  CircularAudioWaveformView: AudioWaveformView,
  RadialAudioWaveformView: AudioWaveformView,
  PopUpTextView: PopUpTextView,
  MultiLyricsContainer: MultiLyricsContainer,
};

export const MotionViewRenderer: React.FC<{ props: MotionViewProps; currentFrame: number }> = ({ props, currentFrame }) => {
  const Component = ViewRegistry[props.type];

  // Visibility check
  if (currentFrame < props.startFrame || currentFrame > props.endFrame) {
    return null;
  }

  if (!Component) {
    return <div style={{ border: '1px dashed red', padding: '10px' }}>Unknown View: {props.type}</div>;
  }

  return <Component props={props} currentFrame={currentFrame} />;
};
