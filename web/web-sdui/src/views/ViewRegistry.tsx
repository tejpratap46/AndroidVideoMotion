import React from 'react';
import type { MotionViewProps, MotionConfig } from '../infra/types';
import { TransparentTextView } from './TransparentTextView';
import { GradientView } from './GradientView';
import { VideoFrameView } from './VideoFrameView';
import { WordWriterTextView } from './WordWriterTextView';
import { AudioWaveformView } from './AudioWaveformView';
import { PopUpTextView } from './PopUpTextView';
import { MultiLyricsContainer } from './MultiLyricsContainer';
import { WordBlinkTextView } from './WordBlinkTextView';
import { MotionImageView } from './MotionImageView';
import { RainbowPopUpTextView } from './RainbowPopUpTextView';
import { AccentMiddlePopUpTextView } from './AccentMiddlePopUpTextView';
import { TranslucentMotionView } from './TranslucentMotionView';

export const ViewRegistry: Record<string, React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }>> = {
  TransparentTextView: TransparentTextView,
  TypeWriterTextView: WordWriterTextView,
  WordWriterTextView: WordWriterTextView,
  WordBlinkTextView: WordBlinkTextView,
  GradientView: GradientView,
  VideoFrameView: VideoFrameView,
  MotionImageView: MotionImageView,
  CircularMotionImageView: MotionImageView,
  CircularAudioWaveformView: AudioWaveformView,
  RadialAudioWaveformView: AudioWaveformView,
  PopUpTextView: PopUpTextView,
  MultiLyricsContainer: MultiLyricsContainer,
  RainbowPopUpTextView: RainbowPopUpTextView,
  AccentMiddlePopUpTextView: AccentMiddlePopUpTextView,
  TranslucentMotionView: TranslucentMotionView,
  CoilVideoPlayer: VideoFrameView,
};

export const MotionViewRenderer: React.FC<{ props: MotionViewProps; currentFrame: number; config: MotionConfig }> = ({ props, currentFrame, config }) => {
  const Component = ViewRegistry[props.type];

  // Visibility check
  if (currentFrame < props.startFrame || currentFrame > props.endFrame) {
    return null;
  }

  if (!Component) {
    return <div style={{ border: '1px dashed red', padding: '10px' }}>Unknown View: {props.type}</div>;
  }

  return <Component props={props} currentFrame={currentFrame} config={config} />;
};
