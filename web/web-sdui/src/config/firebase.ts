import { initializeApp, getApps, getApp, type FirebaseApp } from 'firebase/app';
import {
  getAuth,
  GoogleAuthProvider,
  browserLocalPersistence,
  setPersistence,
  type Auth
} from 'firebase/auth';
import { getFirestore, type Firestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: "AIzaSyDH6E9gqvFDvuiy6HDA7FVKheynoxdsz2I",
  authDomain: "tps-lyrics-maker.firebaseapp.com",
  projectId: "tps-lyrics-maker",
  storageBucket: "tps-lyrics-maker.firebasestorage.app",
  messagingSenderId: "657750458942",
  appId: "1:657750458942:web:ed4d3d59d1f557d0e8876f",
  measurementId: "G-37WVEKD9VV"
};

export const isFirebaseConfigured = Boolean(
  firebaseConfig.apiKey && firebaseConfig.projectId
);

// Initialize Firebase App singleton safely
const app: FirebaseApp = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);

// Initialize Firebase Auth singleton
export const auth: Auth = getAuth(app);

// Safely set persistence using browserLocalPersistence
setPersistence(auth, browserLocalPersistence).catch((err) => {
  console.warn('[Firebase Auth] Persistence error:', err);
});

// Configure Google Auth Provider
export const googleProvider = new GoogleAuthProvider();
googleProvider.setCustomParameters({
  prompt: 'select_account'
});

// Initialize Firestore singleton
export const db: Firestore = getFirestore(app);

export default app;
