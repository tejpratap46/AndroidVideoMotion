import React, { useState, useRef, useEffect, useCallback } from "react";

// ─── Types ────────────────────────────────────────────────────────────────────

export type JsonPrimitive = string | number | boolean | null;
export type JsonValue = JsonPrimitive | JsonValue[] | { [key: string]: JsonValue };
type JsonType = "string" | "number" | "boolean" | "null" | "array" | "object";

interface ColumnItem {
  key: string | number;
  value: JsonValue;
  type: JsonType;
}

interface Column {
  items: ColumnItem[];
  selectedKey: string | number | null;
}

// ─── Utils ────────────────────────────────────────────────────────────────────

function getType(v: JsonValue): JsonType {
  if (v === null) return "null";
  if (Array.isArray(v)) return "array";
  return typeof v as JsonType;
}

function isExpandable(v: JsonValue): boolean {
  const t = getType(v);
  if (t === "object") return Object.keys(v as object).length > 0;
  if (t === "array") return (v as JsonValue[]).length > 0;
  return false;
}

function getChildren(v: JsonValue): ColumnItem[] {
  if (Array.isArray(v)) return v.map((c, i) => ({ key: i, value: c, type: getType(c) }));
  if (v !== null && typeof v === "object")
    return Object.entries(v).map(([k, c]) => ({ key: k, value: c, type: getType(c) }));
  return [];
}

function preview(v: JsonValue, t: JsonType): string {
  if (t === "null") return "null";
  if (t === "string") { const s = v as string; return `"${s.length > 36 ? s.slice(0, 36) + "…" : s}"`; }
  if (t === "number" || t === "boolean") return String(v);
  if (t === "array") { const n = (v as JsonValue[]).length; return `${n} item${n !== 1 ? "s" : ""}`; }
  if (t === "object") { const n = Object.keys(v as object).length; return `${n} key${n !== 1 ? "s" : ""}`; }
  return "";
}

// ─── URL Detection ────────────────────────────────────────────────────────────

function isUrl(str: string): boolean {
  try {
    const url = new URL(str);
    return url.protocol === "http:" || url.protocol === "https:";
  } catch {
    return false;
  }
}

function isImageUrl(str: string): boolean {
  if (!isUrl(str)) return false;
  const imageExts = [".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg", ".bmp", ".ico", ".avif"];
  const lower = str.toLowerCase();
  return imageExts.some((ext) => lower.endsWith(ext) || lower.includes(ext + "?"));
}

function isVideoUrl(str: string): boolean {
  if (!isUrl(str)) return false;
  const videoExts = [".mp4", ".webm", ".ogg", ".mov", ".mkv", ".avi"];
  const lower = str.toLowerCase();
  return videoExts.some((ext) => lower.endsWith(ext) || lower.includes(ext + "?"));
}

function isAudioUrl(str: string): boolean {
  if (!isUrl(str)) return false;
  const audioExts = [".mp3", ".wav", ".ogg", ".flac", ".aac", ".m4a", ".wma"];
  const lower = str.toLowerCase();
  return audioExts.some((ext) => lower.endsWith(ext) || lower.includes(ext + "?"));
}

type MediaType = "image" | "video" | "audio" | "link" | "null";

function detectMediaType(str: string): MediaType {
  if (isImageUrl(str)) return "image";
  if (isVideoUrl(str)) return "video";
  if (isAudioUrl(str)) return "audio";
  if (isUrl(str)) return "link";
  return "null";
}

// ─── Deep value setters ─────────────────────────────────────────────────────

function setValueAtPath(root: JsonValue, path: (string | number)[], newValue: JsonValue): JsonValue {
  if (path.length === 0) return newValue;
  const [head, ...tail] = path;
  if (Array.isArray(root)) {
    const idx = typeof head === "number" ? head : parseInt(head, 10);
    const copy = [...root];
    copy[idx] = setValueAtPath(copy[idx], tail, newValue);
    return copy;
  }
  if (root !== null && typeof root === "object") {
    const copy = { ...root };
    copy[String(head)] = setValueAtPath(copy[String(head)], tail, newValue);
    return copy;
  }
  return root;
}

function deleteAtPath(root: JsonValue, path: (string | number)[]): JsonValue {
  if (path.length === 0) return root;
  const [head, ...tail] = path;
  if (Array.isArray(root)) {
    const idx = typeof head === "number" ? head : parseInt(head, 10);
    if (tail.length === 0) {
      const copy = [...root];
      copy.splice(idx, 1);
      return copy;
    }
    const copy = [...root];
    copy[idx] = deleteAtPath(copy[idx], tail);
    return copy;
  }
  if (root !== null && typeof root === "object") {
    const copy: Record<string, JsonValue> = {};
    for (const [k, v] of Object.entries(root)) {
      if (k !== String(head)) {
        copy[k] = v;
      } else if (tail.length > 0) {
        copy[k] = deleteAtPath(v, tail);
      }
    }
    return copy;
  }
  return root;
}

// ─── Style constants ──────────────────────────────────────────────────────────

const COLUMN_WIDTH = 256;
const PREVIEW_WIDTH = 280;

const TYPE_STYLE: Record<JsonType, { bg: string; fg: string; label: string }> = {
  string:  { bg: "rgba(78,201,137,0.14)",  fg: "#4ec989", label: "STR"  },
  number:  { bg: "rgba(245,166,35,0.14)",  fg: "#f5a623", label: "NUM"  },
  boolean: { bg: "rgba(255,124,92,0.14)",  fg: "#ff7c5c", label: "BOOL" },
  null:    { bg: "rgba(144,144,170,0.14)", fg: "#9090aa", label: "NULL" },
  array:   { bg: "rgba(107,164,255,0.14)", fg: "#6ba4ff", label: "ARR"  },
  object:  { bg: "rgba(190,130,255,0.14)", fg: "#be82ff", label: "OBJ"  },
};

const css = `
  @import url('https://fonts.googleapis.com/css2?family=Geist+Mono:wght@400;500;600&display=swap');

  .jf-root {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    background: #111114;
    padding: 16px;
    gap: 12px;
    font-family: 'Geist Mono', 'SF Mono', 'JetBrains Mono', monospace;
  }

  /* ── Toolbar ── */
  .jf-toolbar {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-shrink: 0;
  }
  .jf-logo {
    font-size: 13px;
    font-weight: 600;
    color: #9898b0;
    letter-spacing: -0.02em;
    white-space: nowrap;
  }
  .jf-logo span { color: #6ba4ff; }
  .jf-breadcrumb {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 2px;
    overflow: hidden;
    background: #0e0e11;
    border: 1px solid rgba(255,255,255,0.11);
    border-radius: 7px;
    padding: 5px 10px;
    font-size: 11px;
    color: #58586a;
    min-width: 0;
  }
  .jf-breadcrumb-seg {
    cursor: pointer;
    color: #9898b0;
    white-space: nowrap;
    transition: color 0.1s;
  }
  .jf-breadcrumb-seg:hover { color: #6ba4ff; }
  .jf-breadcrumb-seg.active { color: #e8e8f0; font-weight: 500; }
  .jf-breadcrumb-sep { color: #58586a; margin: 0 2px; }
  .jf-btn {
    background: #0e0e11;
    border: 1px solid rgba(255,255,255,0.11);
    color: #9898b0;
    font-family: inherit;
    font-size: 11px;
    padding: 5px 12px;
    border-radius: 7px;
    cursor: pointer;
    white-space: nowrap;
    transition: all 0.13s;
  }
  .jf-btn:hover { border-color: rgba(255,255,255,0.2); color: #e8e8f0; }
  .jf-btn.primary {
    background: #2563eb;
    border-color: #2563eb;
    color: #fff;
  }
  .jf-btn.primary:hover { background: #1d4ed8; }
  .jf-btn.danger {
    background: rgba(239,68,68,0.12);
    border-color: rgba(239,68,68,0.3);
    color: #f87171;
  }
  .jf-btn.danger:hover { background: rgba(239,68,68,0.2); border-color: #ef4444; }
  .jf-btn.success {
    background: rgba(34,197,94,0.12);
    border-color: rgba(34,197,94,0.3);
    color: #4ade80;
  }
  .jf-btn.success:hover { background: rgba(34,197,94,0.2); border-color: #22c55e; }

  /* ── Input area ── */
  .jf-input-wrap {
    flex-shrink: 0;
    background: #161619;
    border: 1px solid rgba(255,255,255,0.11);
    border-radius: 10px;
    overflow: hidden;
  }
  .jf-input-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 14px;
    border-bottom: 1px solid rgba(255,255,255,0.07);
    background: #0e0e11;
  }
  .jf-input-label {
    font-size: 10px;
    font-weight: 600;
    letter-spacing: 0.08em;
    color: #58586a;
  }
  .jf-input-error { font-size: 11px; color: #f87171; }
  .jf-textarea {
    display: block;
    width: 100%;
    background: transparent;
    border: none;
    outline: none;
    color: #e8e8f0;
    font-family: inherit;
    font-size: 12px;
    padding: 10px 14px;
    resize: none;
    line-height: 1.6;
  }

  /* ── Finder window ── */
  .jf-finder {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    background: #161619;
    border: 1px solid rgba(255,255,255,0.11);
    border-radius: 10px;
    overflow: hidden;
  }

  /* Traffic lights + title */
  .jf-titlebar {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    background: #0e0e11;
    border-bottom: 1px solid rgba(255,255,255,0.07);
    flex-shrink: 0;
  }
  .jf-traffic { display: flex; gap: 6px; }
  .jf-dot {
    width: 12px; height: 12px; border-radius: 50%;
    flex-shrink: 0;
  }
  .jf-dot-r { background: #ff5f57; }
  .jf-dot-y { background: #febc2e; }
  .jf-dot-g { background: #28c840; }
  .jf-title {
    flex: 1;
    text-align: center;
    font-size: 12px;
    font-weight: 500;
    color: #9898b0;
    letter-spacing: -0.01em;
  }
  .jf-title-spacer { width: 46px; }

  /* Columns scroll area */
  .jf-columns-wrap {
    flex: 1;
    min-height: 0;
    display: flex;
    overflow-x: auto;
    overflow-y: hidden;
  }
  .jf-columns-wrap::-webkit-scrollbar { height: 4px; }
  .jf-columns-wrap::-webkit-scrollbar-track { background: transparent; }
  .jf-columns-wrap::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.1); border-radius: 2px; }

  /* Single column */
  .jf-column {
    min-width: ${COLUMN_WIDTH}px;
    width: ${COLUMN_WIDTH}px;
    flex-shrink: 0;
    border-right: 1px solid rgba(255,255,255,0.07);
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  .jf-column-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 12px;
    font-size: 10px;
    font-weight: 600;
    letter-spacing: 0.07em;
    color: #58586a;
    background: #0e0e11;
    border-bottom: 1px solid rgba(255,255,255,0.07);
    flex-shrink: 0;
    position: sticky;
    top: 0;
  }
  .jf-column-items {
    flex: 1;
    overflow-y: auto;
  }
  .jf-column-items::-webkit-scrollbar { width: 3px; }
  .jf-column-items::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.08); border-radius: 2px; }

  /* Row */
  .jf-row {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 10px;
    cursor: pointer;
    user-select: none;
    transition: background 0.08s;
    border-bottom: 1px solid rgba(255,255,255,0.025);
    position: relative;
  }
  .jf-row:hover { background: rgba(255,255,255,0.04); }
  .jf-row.selected { background: #2563eb; }
  .jf-row.editing { background: rgba(37,99,235,0.08); }
  .jf-row-key {
    flex: 1;
    min-width: 0;
    overflow: hidden;
  }
  .jf-row-key-name {
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #e8e8f0;
    font-weight: 400;
  }
  .jf-row.selected .jf-row-key-name { color: #fff; font-weight: 500; }
  .jf-row-preview {
    font-size: 10px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: #58586a;
    margin-top: 1px;
  }
  .jf-row.selected .jf-row-preview { color: rgba(255,255,255,0.55); }
  .jf-chevron {
    color: #58586a;
    flex-shrink: 0;
    display: flex;
    align-items: center;
  }
  .jf-row.selected .jf-chevron { color: rgba(255,255,255,0.5); }

  /* Inline editor in row */
  .jf-row-edit-input {
    flex: 1;
    background: #0e0e11;
    border: 1px solid #2563eb;
    border-radius: 5px;
    color: #e8e8f0;
    font-family: inherit;
    font-size: 12px;
    padding: 3px 6px;
    outline: none;
    min-width: 0;
  }
  .jf-row-edit-select {
    flex: 1;
    background: #0e0e11;
    border: 1px solid #2563eb;
    border-radius: 5px;
    color: #e8e8f0;
    font-family: inherit;
    font-size: 12px;
    padding: 3px 6px;
    outline: none;
    cursor: pointer;
  }

  /* Type badge */
  .jf-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 9px;
    font-weight: 700;
    letter-spacing: 0.03em;
    border-radius: 4px;
    padding: 1px 4px;
    flex-shrink: 0;
    line-height: 1.5;
  }

  /* URL badge */
  .jf-url-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 8px;
    font-weight: 700;
    letter-spacing: 0.03em;
    border-radius: 4px;
    padding: 1px 4px;
    flex-shrink: 0;
    line-height: 1.5;
    background: rgba(107,164,255,0.14);
    color: #6ba4ff;
  }

  /* Preview panel */
  .jf-preview {
    min-width: ${PREVIEW_WIDTH}px;
    width: ${PREVIEW_WIDTH}px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
    padding: 16px 14px;
    gap: 14px;
    background: #0e0e11;
  }
  .jf-preview::-webkit-scrollbar { width: 3px; }
  .jf-preview::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.08); border-radius: 2px; }

  .jf-preview-icon {
    width: 48px; height: 48px; border-radius: 12px;
    background: rgba(255,255,255,0.04);
    border: 1px solid rgba(255,255,255,0.11);
    display: flex; align-items: center; justify-content: center;
    font-size: 24px;
  }
  .jf-preview-section { display: flex; flex-direction: column; gap: 4px; }
  .jf-preview-label {
    font-size: 9px;
    font-weight: 700;
    letter-spacing: 0.1em;
    color: #58586a;
  }
  .jf-preview-value {
    font-size: 12px;
    color: #e8e8f0;
    word-break: break-all;
    line-height: 1.5;
  }
  .jf-preview-code {
    background: #0e0e11;
    border: 1px solid rgba(255,255,255,0.07);
    border-radius: 6px;
    padding: 8px 10px;
    font-size: 11px;
    line-height: 1.6;
    word-break: break-all;
    white-space: pre-wrap;
    max-height: 160px;
    overflow-y: auto;
  }
  .jf-preview-code::-webkit-scrollbar { width: 3px; }
  .jf-preview-code::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.08); border-radius: 2px; }
  .jf-copy-btn {
    background: #0e0e11;
    border: 1px solid rgba(255,255,255,0.11);
    color: #9898b0;
    font-family: inherit;
    font-size: 10px;
    padding: 5px 10px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.13s;
    width: 100%;
    text-align: left;
  }
  .jf-copy-btn:hover { border-color: rgba(255,255,255,0.18); color: #e8e8f0; }
  .jf-copy-btn.done { border-color: #4ec989; color: #4ec989; }

  /* Inline editor in preview */
  .jf-preview-edit-input {
    background: #0e0e11;
    border: 1px solid #2563eb;
    border-radius: 6px;
    color: #e8e8f0;
    font-family: inherit;
    font-size: 12px;
    padding: 6px 8px;
    outline: none;
    width: 100%;
    resize: vertical;
    min-height: 28px;
  }
  .jf-preview-edit-select {
    background: #0e0e11;
    border: 1px solid #2563eb;
    border-radius: 6px;
    color: #e8e8f0;
    font-family: inherit;
    font-size: 12px;
    padding: 6px 8px;
    outline: none;
    width: 100%;
    cursor: pointer;
  }
  .jf-preview-edit-actions {
    display: flex;
    gap: 6px;
    margin-top: 6px;
  }
  .jf-preview-edit-actions .jf-btn {
    flex: 1;
    text-align: center;
    padding: 4px 8px;
    font-size: 10px;
  }

  /* Media preview */
  .jf-media-preview {
    width: 100%;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid rgba(255,255,255,0.11);
    background: #0e0e11;
  }
  .jf-media-preview img {
    width: 100%;
    height: auto;
    display: block;
    object-fit: cover;
    max-height: 200px;
  }
  .jf-media-preview video {
    width: 100%;
    max-height: 200px;
    display: block;
  }
  .jf-media-preview audio {
    width: 100%;
    display: block;
  }
  .jf-media-link {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    font-size: 11px;
    color: #6ba4ff;
    text-decoration: none;
    word-break: break-all;
    transition: background 0.1s;
  }
  .jf-media-link:hover {
    background: rgba(107,164,255,0.08);
  }
  .jf-media-link-icon {
    font-size: 18px;
    flex-shrink: 0;
  }
  .jf-media-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 24px 12px;
    gap: 8px;
    color: #58586a;
    font-size: 11px;
    text-align: center;
  }
  .jf-media-placeholder-icon {
    font-size: 32px;
    opacity: 0.5;
  }

  /* Empty states */
  .jf-empty {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #58586a;
    font-size: 12px;
    gap: 8px;
    padding: 24px;
    text-align: center;
  }
  .jf-empty-icon { font-size: 28px; opacity: 0.5; }

  /* Divider line */
  .jf-divider { width: 1px; background: rgba(255,255,255,0.07); flex-shrink: 0; }

  /* Keyboard focus ring */
  .jf-row.focused {
    outline: 2px solid rgba(107,164,255,0.6);
    outline-offset: -2px;
  }
  .jf-row.focused:not(.selected) {
    background: rgba(107,164,255,0.08);
  }

  /* Animate new columns sliding in */
  @keyframes slideIn {
    from { opacity: 0; transform: translateX(12px); }
    to   { opacity: 1; transform: translateX(0); }
  }
  .jf-column-enter { animation: slideIn 0.18s ease; }

  /* ── Formula Bar ── */
  .jf-formula-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 14px;
    background: #0e0e11;
    border: 1px solid rgba(255,255,255,0.11);
    border-radius: 10px;
    flex-shrink: 0;
  }
  .jf-formula-label {
    font-size: 10px;
    font-weight: 600;
    color: #58586a;
    letter-spacing: 0.05em;
    white-space: nowrap;
  }
  .jf-formula-input {
    background: #111114;
    border: 1px solid rgba(255,255,255,0.07);
    color: #e8e8f0;
    font-family: inherit;
    font-size: 11px;
    padding: 5px 10px;
    border-radius: 6px;
    outline: none;
    min-width: 0;
  }
  .jf-formula-input:focus { border-color: #2563eb; }
  .jf-formula-input.path { width: 120px; }
  .jf-formula-input.field { width: 120px; }
  .jf-formula-input.formula { flex: 1; }
`;

// ─── Sub-components ───────────────────────────────────────────────────────────

const TypeBadge: React.FC<{ type: JsonType }> = ({ type }) => {
  const { bg, fg, label } = TYPE_STYLE[type];
  return (
    <span className="jf-badge" style={{ background: bg, color: fg }}>
      {label}
    </span>
  );
};

const UrlBadge: React.FC<{ mediaType: MediaType }> = ({ mediaType }) => {
  const labels: Record<MediaType, string> = {
    image: "IMG",
    video: "VID",
    audio: "AUD",
    link: "URL",
    null: "",
  };
  if (!mediaType) return null;
  return (
    <span className="jf-url-badge">
      {labels[mediaType]}
    </span>
  );
};

const ChevronIcon = () => (
  <svg width="6" height="10" viewBox="0 0 6 10" fill="none">
    <path d="M1 1l4 4-4 4" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

// ─── Inline Row Editor ────────────────────────────────────────────────────────

interface InlineEditorProps {
  value: JsonValue;
  type: JsonType;
  onSave: (val: JsonValue) => void;
  onCancel: () => void;
}

const InlineEditor: React.FC<InlineEditorProps> = ({ value, type, onSave, onCancel }) => {
  const [editVal, setEditVal] = useState<string>(String(value ?? ""));
  const inputRef = useRef<HTMLInputElement | HTMLTextAreaElement>(null);

  useEffect(() => {
    inputRef.current?.focus();
    if (inputRef.current && type !== "boolean") {
      (inputRef.current as HTMLInputElement).select?.();
    }
  }, [type]);

  const handleKey = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && type !== "string") {
      e.preventDefault();
      handleSave();
    }
    if (e.key === "Escape") onCancel();
  };

  const handleSave = () => {
    let parsed: JsonValue;
    if (type === "string") parsed = editVal;
    else if (type === "number") {
      const n = parseFloat(editVal);
      parsed = isNaN(n) ? 0 : n;
    }
    else if (type === "boolean") parsed = editVal === "true";
    else if (type === "null") parsed = null;
    else parsed = editVal;
    onSave(parsed);
  };

  if (type === "boolean") {
    return (
      <select
        className="jf-row-edit-select"
        value={editVal}
        onChange={(e) => { setEditVal(e.target.value); onSave(e.target.value === "true"); }}
        onKeyDown={handleKey}
        ref={inputRef as any}
      >
        <option value="true">true</option>
        <option value="false">false</option>
      </select>
    );
  }

  if (type === "null") {
    return (
      <span style={{ fontSize: 11, color: "#58586a" }}>null (read-only)</span>
    );
  }

  if (type === "string") {
    return (
      <textarea
        className="jf-row-edit-input"
        value={editVal}
        onChange={(e) => setEditVal(e.target.value)}
        onKeyDown={handleKey}
        onBlur={handleSave}
        rows={1}
        ref={inputRef as any}
        style={{ resize: "none", minHeight: 20 }}
      />
    );
  }

  return (
    <input
      className="jf-row-edit-input"
      type={type === "number" ? "number" : "text"}
      value={editVal}
      onChange={(e) => setEditVal(e.target.value)}
      onKeyDown={handleKey}
      onBlur={handleSave}
      ref={inputRef as any}
    />
  );
};

// ─── Column Panel ─────────────────────────────────────────────────────────────

interface ColumnPanelProps {
  items: ColumnItem[];
  selectedKey: string | number | null;
  depth: number;
  onSelect: (item: ColumnItem) => void;
  onDoubleClick: (item: ColumnItem) => void;
  editingKey: string | number | null;
  onSaveEdit: (val: JsonValue) => void;
  onCancelEdit: () => void;
  isNew: boolean;
  focusedIndex: number;
  isKeyboardActive: boolean;
}

const ColumnPanel: React.FC<ColumnPanelProps> = ({
  items, selectedKey, depth, onSelect, onDoubleClick,
  editingKey, onSaveEdit, onCancelEdit, isNew,
  focusedIndex, isKeyboardActive
}) => {
  const ref = useRef<HTMLDivElement>(null);
  const itemsRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    ref.current?.scrollIntoView({ behavior: "smooth", block: "nearest", inline: "end" });
  }, []);

  // Scroll focused row into view
  useEffect(() => {
    if (focusedIndex >= 0 && itemsRef.current) {
      const row = itemsRef.current.children[focusedIndex] as HTMLElement;
      row?.scrollIntoView({ behavior: "smooth", block: "nearest" });
    }
  }, [focusedIndex]);

  return (
    <div ref={ref} className={`jf-column ${isNew ? "jf-column-enter" : ""}`}>
      <div className="jf-column-header">
        <span>{depth === 0 ? "ROOT" : `LEVEL ${depth}`}</span>
        <span>{items.length}</span>
      </div>
      <div className="jf-column-items" ref={itemsRef}>
        {items.map((item, idx) => {
          const sel = item.key === selectedKey;
          const editing = item.key === editingKey;
          const expandable = isExpandable(item.value);
          const mediaType = item.type === "string" ? detectMediaType(item.value as string) : null;
          const focused = isKeyboardActive && idx === focusedIndex;
          return (
            <div
              key={String(item.key)}
              className={`jf-row${sel ? " selected" : ""}${editing ? " editing" : ""}${focused ? " focused" : ""}`}
              onClick={() => onSelect(item)}
              onDoubleClick={() => onDoubleClick(item)}
            >
              {editing ? (
                <InlineEditor
                  value={item.value}
                  type={item.type}
                  onSave={onSaveEdit}
                  onCancel={onCancelEdit}
                />
              ) : (
                <>
                  <TypeBadge type={item.type} />
                  {mediaType && <UrlBadge mediaType={mediaType} />}
                  <div className="jf-row-key">
                    <div className="jf-row-key-name">{String(item.key)}</div>
                    <div className="jf-row-preview">{preview(item.value, item.type)}</div>
                  </div>
                  {expandable && (
                    <span className="jf-chevron">
                      <ChevronIcon />
                    </span>
                  )}
                </>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

// ─── Media Preview ────────────────────────────────────────────────────────────

interface MediaPreviewProps {
  url: string;
  mediaType: MediaType;
}

const MediaPreview: React.FC<MediaPreviewProps> = ({ url, mediaType }) => {
  const [error, setError] = useState(false);

  if (error) {
    return (
      <div className="jf-media-placeholder">
        <div className="jf-media-placeholder-icon">⚠️</div>
        <span>Failed to load media</span>
        <span style={{ fontSize: 10, opacity: 0.7 }}>{url}</span>
      </div>
    );
  }

  if (mediaType === "image") {
    return (
      <div className="jf-media-preview">
        <img src={url} alt="Preview" onError={() => setError(true)} loading="lazy" />
      </div>
    );
  }
  if (mediaType === "video") {
    return (
      <div className="jf-media-preview">
        <video src={url} controls preload="metadata" onError={() => setError(true)} />
      </div>
    );
  }
  if (mediaType === "audio") {
    return (
      <div className="jf-media-preview">
        <audio src={url} controls preload="metadata" onError={() => setError(true)} />
      </div>
    );
  }
  if (mediaType === "link") {
    return (
      <a href={url} target="_blank" rel="noopener noreferrer" className="jf-media-link">
        <span className="jf-media-link-icon">🔗</span>
        <span>{url}</span>
      </a>
    );
  }
  return null;
};

// ─── Preview Panel Editor ─────────────────────────────────────────────────────

interface PreviewEditorProps {
  value: JsonValue;
  type: JsonType;
  onSave: (val: JsonValue) => void;
  onCancel: () => void;
}

const PreviewEditor: React.FC<PreviewEditorProps> = ({ value, type, onSave, onCancel }) => {
  const [editVal, setEditVal] = useState<string>(
    type === "string" ? (value as string) : String(value ?? "")
  );

  const handleSave = () => {
    let parsed: JsonValue;
    if (type === "string") parsed = editVal;
    else if (type === "number") {
      const n = parseFloat(editVal);
      parsed = isNaN(n) ? 0 : n;
    }
    else if (type === "boolean") parsed = editVal === "true";
    else if (type === "null") parsed = null;
    else parsed = editVal;
    onSave(parsed);
  };

  if (type === "boolean") {
    return (
      <>
        <select
          className="jf-preview-edit-select"
          value={editVal}
          onChange={(e) => setEditVal(e.target.value)}
        >
          <option value="true">true</option>
          <option value="false">false</option>
        </select>
        <div className="jf-preview-edit-actions">
          <button className="jf-btn primary" onClick={handleSave}>Save</button>
          <button className="jf-btn" onClick={onCancel}>Cancel</button>
        </div>
      </>
    );
  }

  if (type === "null") {
    return (
      <div className="jf-preview-value" style={{ color: "#58586a" }}>
        null (read-only)
      </div>
    );
  }

  if (type === "string") {
    return (
      <>
        <textarea
          className="jf-preview-edit-input"
          value={editVal}
          onChange={(e) => setEditVal(e.target.value)}
          rows={4}
          autoFocus
        />
        <div className="jf-preview-edit-actions">
          <button className="jf-btn primary" onClick={handleSave}>Save</button>
          <button className="jf-btn" onClick={onCancel}>Cancel</button>
        </div>
      </>
    );
  }

  return (
    <>
      <input
        className="jf-preview-edit-input"
        type={type === "number" ? "number" : "text"}
        value={editVal}
        onChange={(e) => setEditVal(e.target.value)}
        autoFocus
        onKeyDown={(e) => { if (e.key === "Enter") handleSave(); if (e.key === "Escape") onCancel(); }}
      />
      <div className="jf-preview-edit-actions">
        <button className="jf-btn primary" onClick={handleSave}>Save</button>
        <button className="jf-btn" onClick={onCancel}>Cancel</button>
      </div>
    </>
  );
};

// ─── Preview Panel ────────────────────────────────────────────────────────────

interface PreviewPanelProps {
  item: ColumnItem;
  path: string;
  onEditValue: (val: JsonValue) => void;
  onDelete: () => void;
}

const PreviewPanel: React.FC<PreviewPanelProps> = ({ item, path, onEditValue, onDelete }) => {
  const [copied, setCopied] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);

  const copy = (text: string, key: string) => {
    navigator.clipboard.writeText(text).then(() => {
      setCopied(key);
      setTimeout(() => setCopied(null), 1400);
    });
  };

  const { type, value, key } = item;
  const { fg } = TYPE_STYLE[type];
  const icon =
    type === "object" ? "🗂" : type === "array" ? "📋" : type === "string" ? "📄"
    : type === "number" ? "🔢" : type === "boolean" ? "⚡" : "∅";

  const displayValue =
    type === "string" ? `"${value}"` : type === "null" ? "null" : JSON.stringify(value, null, 2);

  const sizeInfo =
    type === "array" ? `${(value as JsonValue[]).length} items`
    : type === "object" ? `${Object.keys(value as object).length} keys`
    : type === "string" ? `${(value as string).length} chars`
    : null;

  const mediaType = type === "string" ? detectMediaType(value as string) : null;

  return (
    <div className="jf-preview">
      <div className="jf-preview-icon">{icon}</div>

      <div className="jf-preview-section">
        <div className="jf-preview-label">KEY</div>
        <div className="jf-preview-value" style={{ fontWeight: 600 }}>{String(key)}</div>
      </div>

      <div className="jf-preview-section">
        <div className="jf-preview-label">TYPE</div>
        <TypeBadge type={type} />
        {mediaType && <span style={{ marginLeft: 6 }}><UrlBadge mediaType={mediaType} /></span>}
      </div>

      {sizeInfo && (
        <div className="jf-preview-section">
          <div className="jf-preview-label">SIZE</div>
          <div className="jf-preview-value">{sizeInfo}</div>
        </div>
      )}

      {mediaType && (
        <div className="jf-preview-section">
          <div className="jf-preview-label">MEDIA PREVIEW</div>
          <MediaPreview url={value as string} mediaType={mediaType} />
        </div>
      )}

      <div className="jf-preview-section">
        <div className="jf-preview-label">VALUE</div>
        {editing ? (
          <PreviewEditor
            value={value}
            type={type}
            onSave={(val) => { onEditValue(val); setEditing(false); }}
            onCancel={() => setEditing(false)}
          />
        ) : (
          <div
            className="jf-preview-code"
            style={{ color: fg, cursor: "pointer" }}
            onDoubleClick={() => setEditing(true)}
            title="Double-click to edit"
          >
            {displayValue}
          </div>
        )}
      </div>

      <div className="jf-preview-section">
        <div className="jf-preview-label">PATH</div>
        <div className="jf-preview-value" style={{ fontSize: 11, color: "#6ba4ff", wordBreak: "break-all" }}>
          {path}
        </div>
      </div>

      <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
        <button className={`jf-copy-btn${copied === "path" ? " done" : ""}`} onClick={() => copy(path, "path")}>
          {copied === "path" ? "✓ Copied!" : "Copy path"}
        </button>
        <button
          className={`jf-copy-btn${copied === "value" ? " done" : ""}`}
          onClick={() => copy(type === "string" ? String(value) : JSON.stringify(value, null, 2), "value")}
        >
          {copied === "value" ? "✓ Copied!" : "Copy value"}
        </button>
        <button className="jf-btn" onClick={() => setEditing(true)} style={{ textAlign: "center" }}>
          ✏️ Edit value
        </button>
        <button className="jf-btn danger" onClick={onDelete} style={{ textAlign: "center" }}>
          🗑 Delete
        </button>
      </div>
    </div>
  );
};

// ─── Main component ───────────────────────────────────────────────────────────

interface JsonFinderProps {
  /** Optional initial JSON string to load on first visit (before localStorage). */
  initialJson?: string;
  /** Whether to show the textarea input area. Default true. */
  showInput?: boolean;
  /** Callback when JSON is updated. */
  onUpdate?: (data: JsonValue) => void;
}

const JsonFinder: React.FC<JsonFinderProps> = ({
  initialJson = "",
  showInput = true,
  onUpdate,
}) => {
  const [jsonText, setJsonText] = useState(initialJson);
  const [parseError, setParseError] = useState<string | null>(null);
  const [root, setRoot] = useState<JsonValue | null>(null);
  const [columns, setColumns] = useState<Column[]>([]);
  const [selectedItem, setSelectedItem] = useState<ColumnItem | null>(null);
  const [inputOpen, setInputOpen] = useState(false);
  const [editingKey, setEditingKey] = useState<string | number | null>(null);
  const [exportFlash, setExportFlash] = useState(false);
  const [focusedCol, setFocusedCol] = useState<number>(0);
  const [focusedRow, setFocusedRow] = useState<number>(-1);
  const [keyboardMode, setKeyboardMode] = useState(false);

  const [bulkPath, setBulkPath] = useState("views");
  const [bulkField, setBulkField] = useState("");
  const [bulkFormula, setBulkFormula] = useState("");

  const columnsEndRef = useRef<HTMLDivElement>(null);
  const columnsWrapRef = useRef<HTMLDivElement>(null);

  // On mount: try localStorage first, then initialJson prop, else empty editor
  useEffect(() => {
    const saved = localStorage.getItem("jsonfinder_data");
    if (saved) {
      try {
        JSON.parse(saved); // validate
        parseAndLoad(saved);
        setJsonText(saved);
        setInputOpen(false);
        return;
      } catch {
        localStorage.removeItem("jsonfinder_data");
      }
    }
    if (initialJson && initialJson.trim()) {
      parseAndLoad(initialJson);
      setJsonText(initialJson);
      setInputOpen(false);
    } else {
      setJsonText("");
      setInputOpen(true);
      setRoot(null);
      setColumns([]);
      setSelectedItem(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const parseAndLoad = useCallback((text: string) => {
    try {
      const parsed = JSON.parse(text);
      setRoot(parsed);
      setParseError(null);
      const rootItems = getChildren(parsed);
      setColumns([{ items: rootItems, selectedKey: null }]);
      setSelectedItem(null);
      setEditingKey(null);
      if (onUpdate) onUpdate(parsed);
    } catch (e: unknown) {
      setParseError((e as Error).message);
    }
  }, [onUpdate]);

  const handleTextChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const val = e.target.value;
    setJsonText(val);
    try {
      JSON.parse(val);
      setParseError(null);
    } catch (err: unknown) {
      setParseError((err as Error).message);
    }
  };

  const handleLoad = () => {
    try {
      const parsed = JSON.parse(jsonText);
      const pretty = JSON.stringify(parsed, null, 2);
      parseAndLoad(pretty);
      setJsonText(pretty);
      setInputOpen(false);
      try {
        localStorage.setItem("jsonfinder_data", pretty);
      } catch {
        // ignore
      }
    } catch {
      // parseAndLoad will handle the error display
      parseAndLoad(jsonText);
    }
  };

  // Build path segments from columns + selected item
  const getPathSegments = useCallback((): (string | number)[] => {
    const segs: (string | number)[] = [];
    columns.forEach((col) => {
      if (col.selectedKey !== null) segs.push(col.selectedKey);
    });
    return segs;
  }, [columns]);

  const saveToStorage = useCallback((data: JsonValue) => {
    try {
      localStorage.setItem("jsonfinder_data", JSON.stringify(data, null, 2));
    } catch {
      // Storage full or unavailable — silently ignore
    }
  }, []);

  const refreshFromRoot = useCallback((newRoot: JsonValue) => {
    setRoot(newRoot);
    saveToStorage(newRoot);
    if (onUpdate) onUpdate(newRoot);

    // Rebuild columns from current selection path
    const segs = getPathSegments();
    const newCols: Column[] = [];
    let current = newRoot;
    let currentItems = getChildren(current);
    newCols.push({ items: currentItems, selectedKey: segs[0] ?? null });

    for (let i = 0; i < segs.length; i++) {
      const seg = segs[i];
      const item = currentItems.find((c) => c.key === seg);
      if (!item || !isExpandable(item.value)) break;
      current = item.value;
      currentItems = getChildren(current);
      newCols.push({ items: currentItems, selectedKey: segs[i + 1] ?? null });
    }

    setColumns(newCols);

    // Update selectedItem if it was a leaf
    if (segs.length > 0) {
      const lastSeg = segs[segs.length - 1];
      const lastCol = newCols[newCols.length - 1];
      const leaf = lastCol?.items.find((c) => c.key === lastSeg);
      if (leaf && !isExpandable(leaf.value)) {
        setSelectedItem(leaf);
      } else {
        setSelectedItem(null);
      }
    }
  }, [getPathSegments, onUpdate, saveToStorage]);

  const handleSelect = useCallback((colIndex: number, item: ColumnItem) => {
    setEditingKey(null);
    setFocusedCol(colIndex);
    setFocusedRow(colIndex < columns.length ? columns[colIndex].items.findIndex((c) => c.key === item.key) : -1);
    const newCols = columns.slice(0, colIndex + 1).map((col, i) =>
      i === colIndex ? { ...col, selectedKey: item.key } : col
    );

    if (isExpandable(item.value)) {
      const children = getChildren(item.value);
      newCols.push({ items: children, selectedKey: null });
      setSelectedItem(null);
    } else {
      setSelectedItem(item);
    }

    setColumns(newCols);

    setTimeout(() => {
      columnsEndRef.current?.scrollIntoView({ behavior: "smooth", inline: "end", block: "nearest" });
    }, 30);
  }, [columns]);

  const handleDoubleClick = useCallback((item: ColumnItem) => {
    if (isExpandable(item.value)) return; // Only edit leaf values
    setEditingKey(item.key);
  }, []);

  const handleSaveInlineEdit = (val: JsonValue) => {
    const segs = getPathSegments();
    if (root) {
      const newRoot = setValueAtPath(root, segs, val);
      refreshFromRoot(newRoot);
    }
    setEditingKey(null);
  };

  const handleSavePreviewEdit = (val: JsonValue) => {
    const segs = getPathSegments();
    if (root) {
      const newRoot = setValueAtPath(root, segs, val);
      refreshFromRoot(newRoot);
    }
  };

  const handleDelete = useCallback(() => {
    const segs = getPathSegments();
    if (root && segs.length > 0) {
      const newRoot = deleteAtPath(root, segs);
      // Pop last segment since item is deleted
      const newCols = columns.slice(0, -1).map((col) => ({ ...col, selectedKey: null }));
      setColumns(newCols);
      setSelectedItem(null);
      setRoot(newRoot);
      if (onUpdate) onUpdate(newRoot);
      const newText = JSON.stringify(newRoot, null, 2);
      setJsonText(newText);
      try {
        localStorage.setItem("jsonfinder_data", newText);
      } catch {
        // ignore
      }
    }
  }, [columns, getPathSegments, onUpdate, root]);

  const handleExport = () => {
    if (!root) return;
    const exported = JSON.stringify(root, null, 2);
    const blob = new Blob([exported], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "export.json";
    a.click();
    URL.revokeObjectURL(url);
    setExportFlash(true);
    setTimeout(() => setExportFlash(false), 1200);
  };

  const handleClear = () => {
    localStorage.removeItem("jsonfinder_data");
    setRoot(null);
    setColumns([]);
    setSelectedItem(null);
    setJsonText("");
    setInputOpen(true);
    setEditingKey(null);
    setParseError(null);
    setFocusedCol(0);
    setFocusedRow(-1);
    setKeyboardMode(false);
    if (onUpdate) onUpdate(null as any);
  };

  const handleBulkApply = () => {
    if (!root || !bulkPath || !bulkField || !bulkFormula) return;

    const parts = bulkPath.split('.');
    let target: any = root;
    for (const part of parts) {
      if (target && typeof target === 'object' && part in target) {
        target = target[part];
      } else {
        return;
      }
    }

    if (!Array.isArray(target)) return;

    const newRoot = JSON.parse(JSON.stringify(root));
    let targetCopy: any = newRoot;
    for (const part of parts) {
      targetCopy = targetCopy[part];
    }

    targetCopy.forEach((item: any) => {
      if (typeof item !== 'object' || item === null) return;

      const currentVal = item[bulkField];
      let newVal: any;

      const op = bulkFormula[0];
      const valStr = bulkFormula.substring(1);
      const valNum = parseFloat(valStr);

      if (['+', '-', '*', '/'].includes(op) && !isNaN(valNum)) {
        if (typeof currentVal === 'number') {
          if (op === '+') newVal = currentVal + valNum;
          else if (op === '-') newVal = currentVal - valNum;
          else if (op === '*') newVal = currentVal * valNum;
          else if (op === '/') newVal = currentVal / valNum;
        }
      } else {
        const staticNum = parseFloat(bulkFormula);
        newVal = isNaN(staticNum) ? bulkFormula : staticNum;
      }

      if (newVal !== undefined) {
        item[bulkField] = newVal;
      }
    });

    refreshFromRoot(newRoot);
  };

  // ─── Keyboard navigation ───────────────────────────────────────────────────

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      // Ignore if typing in an input/textarea
      const target = e.target as HTMLElement;
      if (target.tagName === "INPUT" || target.tagName === "TEXTAREA" || target.tagName === "SELECT") {
        if (e.key === "Escape") {
          (target as HTMLElement).blur();
          setKeyboardMode(true);
        }
        return;
      }

      if (!root) return;

      const maxCol = columns.length - 1;
      const currentCol = columns[focusedCol];
      if (!currentCol) return;

      const maxRow = currentCol.items.length - 1;

      switch (e.key) {
        case "ArrowDown": {
          e.preventDefault();
          setKeyboardMode(true);
          setFocusedRow((prev) => {
            const next = prev < 0 ? 0 : Math.min(prev + 1, maxRow);
            return next;
          });
          break;
        }
        case "ArrowUp": {
          e.preventDefault();
          setKeyboardMode(true);
          setFocusedRow((prev) => {
            const next = prev < 0 ? maxRow : Math.max(prev - 1, 0);
            return next;
          });
          break;
        }
        case "ArrowRight": {
          e.preventDefault();
          setKeyboardMode(true);
          const item = currentCol.items[focusedRow >= 0 ? focusedRow : 0];
          if (item && isExpandable(item.value)) {
            handleSelect(focusedCol, item);
            setFocusedCol((prev) => Math.min(prev + 1, maxCol + 1));
            setFocusedRow(0);
          } else if (focusedCol < maxCol) {
            setFocusedCol((prev) => prev + 1);
            setFocusedRow(0);
          }
          break;
        }
        case "ArrowLeft": {
          e.preventDefault();
          setKeyboardMode(true);
          if (focusedCol > 0) {
            setFocusedCol((prev) => prev - 1);
            setFocusedRow(-1);
          }
          break;
        }
        case "Enter": {
          e.preventDefault();
          setKeyboardMode(true);
          const item = currentCol.items[focusedRow >= 0 ? focusedRow : 0];
          if (item) {
            handleSelect(focusedCol, item);
            if (isExpandable(item.value)) {
              setFocusedCol((prev) => Math.min(prev + 1, maxCol + 1));
              setFocusedRow(0);
            }
          }
          break;
        }
        case "Escape": {
          setEditingKey(null);
          if (selectedItem) {
            setSelectedItem(null);
          }
          break;
        }
        case "Delete":
        case "Backspace": {
          if (selectedItem && focusedRow >= 0) {
            e.preventDefault();
            handleDelete();
            setFocusedRow((prev) => Math.max(prev - 1, 0));
          }
          break;
        }
        case "e":
        case "E": {
          if (!e.ctrlKey && !e.metaKey && !e.altKey) {
            const item = currentCol.items[focusedRow >= 0 ? focusedRow : 0];
            if (item && !isExpandable(item.value)) {
              e.preventDefault();
              handleDoubleClick(item);
            }
          }
          break;
        }
        case "Home": {
          e.preventDefault();
          setKeyboardMode(true);
          setFocusedRow(0);
          break;
        }
        case "End": {
          e.preventDefault();
          setKeyboardMode(true);
          setFocusedRow(maxRow);
          break;
        }
      }
    };

    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [root, columns, focusedCol, focusedRow, selectedItem, handleSelect, handleDelete, handleDoubleClick]);

  const jumpToDepth = (depth: number) => {
    setEditingKey(null);
    setFocusedCol(depth);
    setFocusedRow(-1);
    if (depth === 0) {
      const rootItems = root ? getChildren(root) : [];
      setColumns([{ items: rootItems, selectedKey: null }]);
      setSelectedItem(null);
      return;
    }
    setColumns((prev) => {
      const trimmed = prev.slice(0, depth + 1).map((col, i) =>
        i === depth ? { ...col, selectedKey: null } : col
      );
      return trimmed;
    });
    setSelectedItem(null);
  };

  // Build breadcrumb from columns
  const breadcrumb: Array<{ label: string; depth: number }> = [{ label: "root", depth: 0 }];
  columns.forEach((col, i) => {
    if (col.selectedKey !== null) {
      breadcrumb.push({ label: String(col.selectedKey), depth: i + 1 });
    }
  });

  const currentPath = (() => {
    const parts = ["$"];
    columns.forEach((col) => {
      if (col.selectedKey !== null)
        parts.push(typeof col.selectedKey === "number" ? `[${col.selectedKey}]` : `.${col.selectedKey}`);
    });
    return parts.join("");
  })();

  return (
    <div className="jf-root" onClick={() => setKeyboardMode(false)}>
      <style>{css}</style>
      {/* Toolbar */}
      <div className="jf-toolbar">
        <span className="jf-logo">
          <span>❴</span> json finder
        </span>

        {/* Breadcrumb */}
        <div className="jf-breadcrumb">
          {breadcrumb.map((seg, i) => (
            <React.Fragment key={i}>
              <span
                className={`jf-breadcrumb-seg${i === breadcrumb.length - 1 ? " active" : ""}`}
                onClick={() => jumpToDepth(seg.depth)}
              >
                {seg.label}
              </span>
              {i < breadcrumb.length - 1 && <span className="jf-breadcrumb-sep">/</span>}
            </React.Fragment>
          ))}
        </div>

        <button
          className={`jf-btn success${exportFlash ? " done" : ""}`}
          onClick={handleExport}
          disabled={!root}
        >
          {exportFlash ? "✓ Exported!" : "⬇ Export JSON"}
        </button>

        {root && (
          <button className="jf-btn danger" onClick={handleClear}>
            Clear
          </button>
        )}

        {showInput && (
          <button className="jf-btn" onClick={() => setInputOpen((v) => !v)}>
            {inputOpen ? "Hide input" : "Edit JSON"}
          </button>
        )}
      </div>

      {/* JSON Input */}
      {showInput && inputOpen && (
        <div className="jf-input-wrap">
          <div className="jf-input-header">
            <span className="jf-input-label">JSON INPUT</span>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              {parseError && <span className="jf-input-error">⚠ {parseError}</span>}
              <button className="jf-btn primary" onClick={handleLoad} disabled={!!parseError}>
                Explore →
              </button>
            </div>
          </div>
          <textarea
            className="jf-textarea"
            rows={8}
            value={jsonText}
            onChange={handleTextChange}
            spellCheck={false}
            autoCapitalize="off"
            autoCorrect="off"
          />
        </div>
      )}

      {/* Finder window */}
      <div className="jf-finder">
        {/* Title bar */}
        <div className="jf-titlebar">
          <div className="jf-traffic">
            <div className="jf-dot jf-dot-r" />
            <div className="jf-dot jf-dot-y" />
            <div className="jf-dot jf-dot-g" />
          </div>
          <div className="jf-title">
            {currentPath}
          </div>
          <div className="jf-title-spacer" />
        </div>

        {/* Columns */}
        <div className="jf-columns-wrap" ref={columnsWrapRef}>
          {root === null ? (
            <div className="jf-empty">
              <div className="jf-empty-icon">📋</div>
              <span>Paste JSON in the editor above and click "Explore →"</span>
              <span style={{ fontSize: 11, opacity: 0.6 }}>Your data is auto-saved to browser storage</span>
            </div>
          ) : (
            <>
              {columns.map((col, i) => (
                <ColumnPanel
                  key={i}
                  items={col.items}
                  selectedKey={col.selectedKey}
                  depth={i}
                  onSelect={(item) => handleSelect(i, item)}
                  onDoubleClick={handleDoubleClick}
                  editingKey={editingKey}
                  onSaveEdit={handleSaveInlineEdit}
                  onCancelEdit={() => setEditingKey(null)}
                  isNew={i === columns.length - 1 && i > 0}
                  focusedIndex={focusedCol === i ? focusedRow : -1}
                  isKeyboardActive={keyboardMode}
                />
              ))}

              {/* Preview detail for leaf values */}
              {selectedItem && (
                <>
                  <div className="jf-divider" />
                  <PreviewPanel
                    item={selectedItem}
                    path={currentPath}
                    onEditValue={handleSavePreviewEdit}
                    onDelete={handleDelete}
                  />
                </>
              )}

              {/* Empty right-side nudge when nothing selected */}
              {!selectedItem && columns.length > 0 && columns[columns.length - 1].selectedKey === null && (
                <div className="jf-empty" style={{ minWidth: 180 }}>
                  <div className="jf-empty-icon">👆</div>
                  <span>Select an item</span>
                </div>
              )}
            </>
          )}
          <div ref={columnsEndRef} />
        </div>
      </div>

      {/* Formula Bar */}
      <div className="jf-formula-bar">
        <span className="jf-formula-label">BULK EDIT ARRAY:</span>
        <input
          className="jf-formula-input path"
          placeholder="Array Path (e.g. views)"
          value={bulkPath}
          onChange={(e) => setBulkPath(e.target.value)}
        />
        <span className="jf-formula-label">FIELD:</span>
        <input
          className="jf-formula-input field"
          placeholder="Field Name"
          value={bulkField}
          onChange={(e) => setBulkField(e.target.value)}
        />
        <span className="jf-formula-label">FORMULA:</span>
        <input
          className="jf-formula-input formula"
          placeholder="Value or Formula (e.g. 1.5, +10, *0.5)"
          value={bulkFormula}
          onChange={(e) => setBulkFormula(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleBulkApply()}
        />
        <button className="jf-btn primary" onClick={handleBulkApply}>
          Apply
        </button>
      </div>
    </div>
  );
};

export default JsonFinder;
