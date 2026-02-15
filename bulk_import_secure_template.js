#!/usr/bin/env node
// bulk_import_secure_template.js - secure template (dry-run default). Review before executing.
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const child_process = require('child_process');
const os = require('os');
let axios; try { axios = require('axios'); } catch (e) { axios = null; }
const camelCase = (() => { try { return require('camelcase'); } catch (e) { return s => s.replace(/[^a-zA-Z0-9]/g, ' ').split(/\s+/).map(w=>w.charAt(0).toUpperCase()+w.slice(1)).join(''); } })();
const franc = (() => { try { return require('franc'); } catch (e) { return null; } })();
const translate = (() => { try { return require('@vitalets/google-translate-api'); } catch (e) { return null; } })();
const pinyin = (() => { try { return require('pinyin'); } catch (e) { return null; } })();
const SRC_DIR = path.resolve(__dirname, '3dProject_to_add');
const TARGET_DIR = path.resolve(__dirname, 'app_home', 'projects');
const LOG_FILE = path.join(SRC_DIR, 'upload.log');
const INDEX_FILE = path.join(TARGET_DIR, '.upload_index.json');
const IMPORT_URL = process.env.IMPORT_URL || 'http://localhost:8080/api/import';
const DRY_RUN = !(process.env.RUN_REAL === 'true'); // set RUN_REAL=true to disable dry-run

function log(level, msg) {
  const line = new Date().toISOString() + ' [' + level.toUpperCase() + '] ' + msg + '\n';
  try { fs.appendFileSync(LOG_FILE, line); } catch (e) { console.error('Failed to write log:', e && e.message); }
  console.log(line.trim());
}

function sha256File(p) {
  const data = fs.readFileSync(p);
  return crypto.createHash('sha256').update(data).digest('hex');
}

function dirContentChecksum(dir) {
  // Compute checksums for all files in dir, produce a combined hash
  const list = [];
  (function walk(d) {
    for (const it of fs.readdirSync(d, { withFileTypes: true })) {
      const p = path.join(d, it.name);
      if (it.isFile()) list.push({ p: path.relative(dir, p), h: sha256File(p) });
      else if (it.isDirectory()) walk(p);
    }
  })(dir);
  list.sort((a,b)=>a.p.localeCompare(b.p));
  const combined = list.map(x=>x.p+":"+x.h).join('|');
  return crypto.createHash('sha256').update(combined).digest('hex');
}

function loadIndex() {
  try { return JSON.parse(fs.readFileSync(INDEX_FILE, 'utf8')); } catch (e) { return {}; }
}

function saveIndex(idx) {
  try { fs.mkdirSync(path.dirname(INDEX_FILE), { recursive: true }); fs.writeFileSync(INDEX_FILE, JSON.stringify(idx, null, 2)); } catch (e) { log('error', 'Failed to save index: '+(e && e.message)); }
}

function getDBEndpointsViaJava() {
  // Attempt to call Database.java helper to print endpoints. Adjust classpath and class name to your project.
  try {
    // Example: `java -cp build/classes/java/main:lib/* com.example.Database --print-endpoints`
    const cp = process.env.JAVA_CLASSPATH || 'build/classes/java/main:lib/*';
    const classname = process.env.DATABASE_CLASS || 'com.example.Database';
    const out = child_process.spawnSync('java', ['-cp', cp, classname, '--print-endpoints'], { encoding: 'utf8', timeout: 20000 });
    if (out.error) { log('warn', 'Java execution failed: '+out.error.message); return null; }
    if (out.status !== 0) { log('warn', 'Java helper returned non-zero status. stdout: '+out.stdout+' stderr: '+out.stderr); return null; }
    try { return JSON.parse(out.stdout); } catch (e) { log('warn', 'Could not parse endpoints JSON from Database.java output'); return { raw: out.stdout }; }
  } catch (e) { log('warn', 'Java interop skipped: '+(e && e.message)); return null; }
}

function safeCopyDir(src, dst) {
  fs.mkdirSync(dst, { recursive: true });
  for (const it of fs.readdirSync(src, { withFileTypes: true })) {
    const s = path.join(src, it.name); const d = path.join(dst, it.name);
    if (it.isDirectory()) safeCopyDir(s, d); else fs.copyFileSync(s, d);
  }
}

function backupDatabaseFile() {
  try {
    const dbPath = path.resolve(__dirname, 'app_home', 'print_jobs.mv.db');
    if (!fs.existsSync(dbPath)) { log('warn', 'Database file not found for backup: '+dbPath); return false; }
    const backupsDir = path.resolve(__dirname, 'app_home', 'backups');
    fs.mkdirSync(backupsDir, { recursive: true });
    const timestamp = new Date().toISOString().replace(/[:.]/g,'').replace(/T/,'_').split('Z')[0];
    const target = path.join(backupsDir, `backup_${timestamp}.mv.db`);
    fs.copyFileSync(dbPath, target);
    log('info', 'Database backup created at: '+target);
    return true;
  } catch (e) {
    log('error', 'Database backup failed: '+(e && e.message));
    return false;
  }
}

async function processFile(fileEntry) {
  const originalName = fileEntry;
  const originalPath = path.join(SRC_DIR, originalName);
  log('info', 'Processing single file: '+originalPath);
  const checksum = sha256File(originalPath);
  const idx = loadIndex();
  if (idx[checksum]) { log('info', 'Duplicate detected; skipping upload for file '+originalName); return; }
  // create temp dir and copy file into it
  const tempBase = fs.mkdtempSync(path.join(os.tmpdir(), 'bulk-import-'));
  const baseName = path.parse(originalName).name;
  const newName = camelCase(baseName.replace(/[^\p{L}\p{N}]+/gu, ' '), { pascalCase: true });
  const tempDir = path.join(tempBase, newName);
  fs.mkdirSync(tempDir, { recursive: true });
  fs.copyFileSync(originalPath, path.join(tempDir, path.basename(originalPath)));
  log('info', 'Copied file to temp dir: '+tempDir);
  const metadata = { recipient: 'Ryan Hunsaker', projectType: 'Prototype', originalName, folderName: newName, checksum };
  if (DRY_RUN) { log('info', 'Dry-run enabled — skipping actual submission for '+newName); log('info', 'Would submit metadata: '+JSON.stringify(metadata)); fs.rmSync(tempBase, { recursive: true, force: true }); return; }
  const ok = await submitTempDir(tempDir, metadata);
  if (ok) {
    idx[checksum] = { originalName, folderName: newName, uploadedAt: new Date().toISOString() };
    saveIndex(idx);
    const finalPath = path.join(TARGET_DIR, newName);
    if (!fs.existsSync(finalPath)) fs.mkdirSync(finalPath, { recursive: true });
    safeCopyDir(tempDir, finalPath);
    log('info', 'Import succeeded and file placed in: '+finalPath);
  } else { log('error', 'Submission failed for file '+newName); }
  fs.rmSync(tempBase, { recursive: true, force: true });
}

async function submitTempDir(tempDir, metadata) {
  // Prefer calling Database.java submit helper or project's import CLI for safe insertion into H2.
  const javaHelper = process.env.DATABASE_CLASS || 'com.example.Database';
  const cp = process.env.JAVA_CLASSPATH || 'build/classes/java/main:lib/*';
  try {
    // If the project provides a helper that can accept a folder path, call it safely.
    const out = child_process.spawnSync('java', ['-cp', cp, javaHelper, '--import-folder', tempDir, '--recipient', 'Ryan Hunsaker', '--projectType', 'Prototype'], { encoding: 'utf8', timeout: 120000 });
    log('info', 'Java import helper stdout: '+(out.stdout||'') );
    if (out.status !== 0) { log('error', 'Java import helper failed: '+(out.stderr||'exit '+out.status)); return false; }
    return true;
  } catch (e) {
    log('warn', 'Java submit attempt failed: '+(e && e.message));
  }
  // Fallback: HTTP POST metadata only (not file contents). Real import may need multipart uploads.
  if (axios) {
    try {
      await axios.post(IMPORT_URL, metadata, { timeout: 120000 });
      return true;
    } catch (e) { log('warn', 'HTTP POST failed: '+(e && e.message)); }
  } else log('info', 'Axios not available; skipped HTTP POST');
  return false;
}

async function processFolder(dirEntry) {
  const originalName = dirEntry;
  const originalPath = path.join(SRC_DIR, originalName);
  log('info', 'Processing: '+originalPath);
  const checksum = dirContentChecksum(originalPath);
  const idx = loadIndex();
  if (idx[checksum]) {
    log('info', 'Duplicate detected; skipping upload for '+originalName+' (keeps existing)');
    return;
  }
  // robust translation/transliteration with fallback to preserve meaning
  async function betterTranslate(name) {
    // Normalize and strip common file-type / suffix tokens first (underscores, dashes, spaces)
    const suffixRegex = /[._\- ]*(?:stl|stls|stl_files|stls_files|stl-files|model_files|model-files|model_files|files|parts|models|3mf|gcode|zip|compressed|archive)$/i;
    const trimmed = String(name).trim();
    let base = trimmed.replace(suffixRegex, '').trim();
    if (!base) base = trimmed; // fallback if stripping removed everything

    // Attempt language detection + translation on the cleaned base name
    let candidate = base;
    try {
      if (franc) {
        const lang = franc(base, { minLength: 1 });
        if (lang && lang !== 'eng' && lang !== 'und' && translate) {
          try {
            const res = await translate(base, { to: 'en' });
            if (res && res.text) candidate = res.text;
          } catch (e) { log('warn', 'translation failed, will fallback: '+(e && e.message)); }
        }
      }
    } catch (e) { log('warn', 'language detection/translation skipped: '+(e && e.message)); }

    // Clean candidate: remove punctuation but keep letters/numbers/spaces
    const cleaned = String(candidate).trim().replace(/[^^\p{L}\p{N}\s]+/gu, '').trim();

    // If translation produced a very short/generic token (e.g., "Stls", "Files"), prefer transliteration
    const genericTokenRegex = /^(?:stl|stls|files|models|parts|3mf|gcode|zip)$/i;
    if (!cleaned || cleaned.length < 3 || genericTokenRegex.test(cleaned)) {
      // Try pinyin transliteration for CJK names
      if (pinyin) {
        try {
          const py = pinyin(base, { style: pinyin.STYLE_NORMAL }).flat().join(' ');
          if (py && py.trim().length > 0) return py;
        } catch (e) { log('warn', 'pinyin transliteration failed, using cleaned base'); }
      }
      // Fallback: strip non-ASCII and use a cleaned base
      const ascii = base.normalize('NFKD').replace(/[^\x00-\x7F]/g, '').replace(/[^a-zA-Z0-9\s]+/g, ' ').trim();
      if (ascii && ascii.length >= 1) return ascii;
      return base;
    }

    return cleaned;
  }

  const translated = await betterTranslate(originalName);
  const newName = camelCase(translated.replace(/[^\p{L}\p{N}]+/gu, ' '), { pascalCase: true });
  log('info', 'Rename suggestion: '+originalName+' -> '+newName);
  const tempBase = fs.mkdtempSync(path.join(os.tmpdir(), 'bulk-import-'));
  const tempDir = path.join(tempBase, newName);
  safeCopyDir(originalPath, tempDir);
  log('info', 'Copied to temp dir: '+tempDir);
  const metadata = { recipient: 'Ryan Hunsaker', projectType: 'Prototype', originalName, folderName: newName, checksum };
  if (DRY_RUN) { log('info', 'Dry-run enabled — skipping actual submission for '+newName); log('info', 'Would submit metadata: '+JSON.stringify(metadata)); fs.rmSync(tempBase, { recursive: true, force: true }); return; }
  const ok = await submitTempDir(tempDir, metadata);
  if (ok) {
    idx[checksum] = { originalName, folderName: newName, uploadedAt: new Date().toISOString() };
    saveIndex(idx);
    // Move to final target atomically
    const finalPath = path.join(TARGET_DIR, newName);
    if (!fs.existsSync(finalPath)) fs.mkdirSync(finalPath, { recursive: true });
    // Move temp contents to final (non-destructive if already exists)
    safeCopyDir(tempDir, finalPath);
    log('info', 'Import succeeded and files placed in: '+finalPath);
  } else {
    log('error', 'Submission failed for '+newName);
  }
  fs.rmSync(tempBase, { recursive: true, force: true });
}

async function main() {
  log('info', 'Template started (dry-run='+DRY_RUN+')');
  if (!fs.existsSync(SRC_DIR)) { log('error', 'Source directory missing: '+SRC_DIR); return; }
  fs.mkdirSync(path.dirname(LOG_FILE), { recursive: true });
  const endpoints = getDBEndpointsViaJava();
  if (endpoints) log('info', 'Discovered endpoints via Database.java: '+JSON.stringify(endpoints));
  // If running for real, create a single backup before the batch to avoid per-upload backups
  if (!DRY_RUN) {
    log('info', 'RUN_REAL detected — creating single database backup before batch');
    const ok = backupDatabaseFile();
    if (!ok) { log('warn', 'Database backup failed or not found — proceeding with caution'); }
  }

  const entries = fs.readdirSync(SRC_DIR, { withFileTypes: true });
  for (const e of entries) { if (!e.isDirectory()) continue; await processFolder(e.name); }
  log('info', 'Template finished. Review upload.log and .upload_index.json for results.');
}

// Do not call main automatically. Export for controlled invocation after review.
module.exports = { main, processFolder, dirContentChecksum, getDBEndpointsViaJava, backupDatabaseFile };
