export interface MotionConfig {
  aspectRatio: VideoAspectRatio;
  fps: number;
  outputQuality: number;
}

export interface VideoAspectRatio {
  width: number;
  height: number;
  label: string;
}

export interface MotionAssetProps {
  type: string;
  uri: string;
  metadata?: Record<string, any>;
  [key: string]: any;
}

export interface MotionViewProps {
  type: string;
  startFrame: number;
  endFrame: number;
  asset?: MotionAssetProps;
  loop?: {
    start: number;
    end: number;
  };
  effects?: MotionEffectProps[];
  children?: MotionViewProps[];
  [key: string]: any;
}

export interface MotionEffectProps {
  type: string;
  startFrame: number;
  endFrame: number;
  [key: string]: any;
}

export interface MotionSDUI {
  views: MotionViewProps[];
  audios?: any[];
  plugins?: any[];
  config?: MotionConfig;
}
