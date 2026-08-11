import {
  doc,
  setDoc,
  getDoc,
  addDoc,
  collection,
  query,
  where,
  getDocs,
  deleteDoc,
  serverTimestamp,
  orderBy,
  type DocumentData,
  type QueryConstraint
} from 'firebase/firestore';
import { db, isFirebaseConfigured } from '../config/firebase';
import type { MotionSDUI, MotionProject } from '../infra/types';

export interface SDUIDocument {
  id?: string;
  userId: string;
  name: string;
  sdui: MotionSDUI;
  createdAt?: any;
  updatedAt?: any;
}

/**
 * Save or update an SDUI schema in Firestore.
 */
export async function saveSDUISchema(
  userId: string,
  name: string,
  sdui: MotionSDUI,
  docId?: string
): Promise<string> {
  if (!isFirebaseConfigured) {
    throw new Error('Firebase is not configured. Please set up environment variables in .env.local.');
  }

  const payload = {
    userId,
    name,
    sdui,
    updatedAt: serverTimestamp()
  };

  if (docId) {
    const docRef = doc(db, 'sdui_schemas', docId);
    await setDoc(docRef, payload, { merge: true });
    return docId;
  } else {
    const colRef = collection(db, 'sdui_schemas');
    const docRef = await addDoc(colRef, {
      ...payload,
      createdAt: serverTimestamp()
    });
    return docRef.id;
  }
}

/**
 * Fetch a single SDUI schema by Document ID.
 */
export async function fetchSDUISchema(docId: string): Promise<SDUIDocument | null> {
  if (!isFirebaseConfigured) return null;

  const docRef = doc(db, 'sdui_schemas', docId);
  const snap = await getDoc(docRef);

  if (snap.exists()) {
    return { id: snap.id, ...snap.data() } as SDUIDocument;
  }
  return null;
}

/**
 * Fetch all SDUI schemas created by a specific user.
 */
export async function fetchUserSDUISchemas(userId: string): Promise<SDUIDocument[]> {
  if (!isFirebaseConfigured) return [];

  const colRef = collection(db, 'sdui_schemas');
  const q = query(
    colRef,
    where('userId', '==', userId),
    orderBy('updatedAt', 'desc')
  );

  const snapshot = await getDocs(q);
  return snapshot.docs.map((docSnap) => ({
    id: docSnap.id,
    ...docSnap.data()
  })) as SDUIDocument[];
}

/**
 * Delete an SDUI schema document.
 */
export async function deleteSDUISchema(docId: string): Promise<void> {
  if (!isFirebaseConfigured) return;
  const docRef = doc(db, 'sdui_schemas', docId);
  await deleteDoc(docRef);
}

/**
 * Generic Helper: Write/Update a document in any Firestore collection.
 */
export async function saveDocument<T extends Record<string, any>>(
  collectionName: string,
  docId: string,
  data: T
): Promise<void> {
  if (!isFirebaseConfigured) throw new Error('Firebase is not configured.');
  const docRef = doc(db, collectionName, docId);
  await setDoc(docRef, { ...data, updatedAt: serverTimestamp() }, { merge: true });
}

/**
 * Generic Helper: Read a document from any Firestore collection.
 */
export async function getDocument<T = DocumentData>(
  collectionName: string,
  docId: string
): Promise<T | null> {
  if (!isFirebaseConfigured) return null;
  const docRef = doc(db, collectionName, docId);
  const snap = await getDoc(docRef);
  return snap.exists() ? ({ id: snap.id, ...snap.data() } as T) : null;
}

/**
 * Generic Helper: Query a collection.
 */
export async function queryCollection<T = DocumentData>(
  collectionName: string,
  ...constraints: QueryConstraint[]
): Promise<T[]> {
  if (!isFirebaseConfigured) return [];
  const colRef = collection(db, collectionName);
  const q = query(colRef, ...constraints);
  const snapshot = await getDocs(q);
  return snapshot.docs.map((d) => ({ id: d.id, ...d.data() })) as T[];
}

/**
 * Create a new MotionProject that matches the Android app's structure.
 */
export async function createMotionProject(
  userId: string,
  name: string,
  sdui: MotionSDUI,
  metadata: Record<string, any> = {}
): Promise<string> {
  if (!isFirebaseConfigured) {
    throw new Error('Firebase is not configured.');
  }

  // Create a new document reference with an auto-generated ID
  const projectColRef = collection(db, 'MotionProject');
  const projectDocRef = doc(projectColRef);
  const id = projectDocRef.id;

  const now = Date.now();

  const project: any = {
    id: id,
    name: name,
    path: `/${id}`,
    sdui: JSON.stringify(sdui),
    metadata: JSON.stringify(metadata),
    created: now,
    updated: now,
    // Flattened SyncTracker fields
    is_dirty: true,
    updated_by: userId,
    created_on: now,
    updated_on: now,
    uploaded_at: serverTimestamp(),
    // Required for filtering
    userId: userId
  };

  await setDoc(projectDocRef, project);
  return id;
}
