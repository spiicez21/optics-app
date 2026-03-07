/**
 * firebase-config.example.js
 *
 * SETUP INSTRUCTIONS
 * ──────────────────
 * 1. Copy this file and rename it to  firebase-config.js
 * 2. Replace every  YOUR_*  placeholder with the real values
 *    from: Firebase Console → Project Settings → Your apps → Web app
 * 3. NEVER commit  firebase-config.js  — it is listed in .gitignore
 */

import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js';
import { getAuth, onAuthStateChanged, signInWithEmailAndPassword, signOut }
    from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js';
import { getFirestore, collection, collectionGroup, getDocs, addDoc,
    updateDoc, deleteDoc, doc, setDoc, query, orderBy, limit,
    where, getCountFromServer }
    from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js';
import { getStorage, ref, uploadBytes, getDownloadURL }
    from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-storage.js';

const firebaseConfig = {
    apiKey:            "YOUR_API_KEY",
    authDomain:        "YOUR_PROJECT_ID.firebaseapp.com",
    projectId:         "YOUR_PROJECT_ID",
    storageBucket:     "YOUR_PROJECT_ID.firebasestorage.app",
    messagingSenderId: "YOUR_MESSAGING_SENDER_ID"
};

const app = initializeApp(firebaseConfig);
export const auth    = getAuth(app);
export const db      = getFirestore(app);
export const storage = getStorage(app);

/** Redirect to login if not authenticated, otherwise resolve with the user. */
export function requireAuth(redirectTo = 'index.html') {
    return new Promise(resolve => {
        onAuthStateChanged(auth, user => {
            if (!user) window.location.href = redirectTo;
            else resolve(user);
        });
    });
}

/** Show a bottom-right toast notification. */
export function showToast(message, type = 'success') {
    const t = document.getElementById('toast');
    if (!t) return;
    t.textContent  = message;
    t.className    = `toast toast-${type} show`;
    setTimeout(() => t.classList.remove('show'), 3200);
}

export {
    onAuthStateChanged, signInWithEmailAndPassword, signOut,
    collection, collectionGroup, getDocs, addDoc, updateDoc,
    deleteDoc, doc, setDoc, query, orderBy, limit, where,
    getCountFromServer, ref, uploadBytes, getDownloadURL
};
