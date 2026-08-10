import type { MotionAssetProps } from './types';

/**
 * Utility to get URI from a MotionAssetProps object.
 *
 * @param asset The asset object from SDUI JSON
 * @returns The URI string or an empty string if not found
 */
export const getAssetUri = (asset?: MotionAssetProps | string): string => {
  if (!asset) return '';
  if (typeof asset === 'string') return asset;
  return asset.uri || '';
};
