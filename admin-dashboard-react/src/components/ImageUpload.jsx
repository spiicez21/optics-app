import { useRef, useState } from 'react';
import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';
import { storage } from '../lib/firebase.js';
import { Upload, X, ImageIcon } from 'lucide-react';

/**
 * ImageUpload
 * Uploads a file to Firebase Storage and returns the download URL via onChange.
 *
 * Props:
 *   value    – current image URL (string or null)
 *   onChange – called with new URL after upload, or '' when cleared
 *   path     – Storage path prefix, e.g. 'admin/products'
 *   height   – preview area height in px (default 140)
 */
export default function ImageUpload({ value, onChange, path = 'admin/images', height = 140 }) {
  const inputRef             = useRef(null);
  const [uploading, setUploading] = useState(false);
  const [progress,  setProgress]  = useState(0);
  const [error,     setError]     = useState('');

  const handleFile = async file => {
    if (!file) return;
    if (!file.type.startsWith('image/')) { setError('Please select an image file.'); return; }
    if (file.size > 10 * 1024 * 1024)   { setError('Image must be under 10 MB.');    return; }

    setError('');
    setUploading(true);
    setProgress(0);

    try {
      const storageRef = ref(storage, `${path}/${Date.now()}_${file.name.replace(/\s+/g, '_')}`);
      const task       = uploadBytesResumable(storageRef, file);

      task.on(
        'state_changed',
        snap => setProgress(Math.round((snap.bytesTransferred / snap.totalBytes) * 100)),
        err  => { setError('Upload failed: ' + err.message); setUploading(false); },
        async () => {
          const url = await getDownloadURL(task.snapshot.ref);
          onChange(url);
          setUploading(false);
        },
      );
    } catch (e) {
      setError('Upload failed: ' + e.message);
      setUploading(false);
    }
  };

  const handleDrop = e => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (file) handleFile(file);
  };

  const clear = e => {
    e.stopPropagation();
    onChange('');
    if (inputRef.current) inputRef.current.value = '';
  };

  return (
    <div>
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        style={{ display: 'none' }}
        onChange={e => handleFile(e.target.files[0])}
      />

      {value ? (
        /* ── Preview with remove button ── */
        <div style={{ position: 'relative', display: 'inline-block', borderRadius: 10, overflow: 'hidden', border: '1px solid var(--border)' }}>
          <img
            src={value}
            alt="preview"
            style={{ display: 'block', maxWidth: 200, maxHeight: height, objectFit: 'cover' }}
          />
          <button
            type="button"
            onClick={clear}
            style={{
              position: 'absolute', top: 6, right: 6,
              width: 24, height: 24, borderRadius: '50%',
              background: 'rgba(0,0,0,.55)', border: 'none',
              cursor: 'pointer', display: 'grid', placeItems: 'center', color: '#fff',
            }}
          >
            <X size={13} />
          </button>
        </div>
      ) : (
        /* ── Drop zone ── */
        <div
          className="img-preview-wrap"
          style={{ height }}
          onClick={() => !uploading && inputRef.current?.click()}
          onDragOver={e => e.preventDefault()}
          onDrop={handleDrop}
        >
          {uploading ? (
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 8 }}>Uploading… {progress}%</div>
              <div style={{ width: 140, height: 6, background: 'var(--border)', borderRadius: 3 }}>
                <div style={{ width: `${progress}%`, height: '100%', background: 'var(--accent)', borderRadius: 3, transition: 'width .2s' }} />
              </div>
            </div>
          ) : (
            <>
              <Upload size={22} color="var(--text-muted)" />
              <span style={{ fontSize: 13 }}>Click or drag to upload image</span>
              <span style={{ fontSize: 11, color: 'var(--text-muted)' }}>PNG, JPG, WEBP — max 10 MB</span>
            </>
          )}
        </div>
      )}

      {error && <p style={{ color: 'var(--danger)', fontSize: 12, marginTop: 6 }}>{error}</p>}
    </div>
  );
}
