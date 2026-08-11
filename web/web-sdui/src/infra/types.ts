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

export interface SyncTracker {
  is_dirty: boolean;
  updated_by: string;
  created_on: number;
  updated_on: number;
  uploaded_at: any; // Can be number or Firestore FieldValue
}

export interface MotionProject {
  id: string;
  name: string;
  path: string;
  sdui: string; // stringified JSON
  metadata: string; // stringified JSON
  created: number;
  updated: number;

  // Flattened SyncTracker fields
  is_dirty: boolean;
  updated_by: string;
  created_on: number;
  updated_on: number;
  uploaded_at: any; // Can be number or Firestore FieldValue
}
