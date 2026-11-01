// Adds release signing + auto version code to the generated Android project
const fs = require('fs');
const p = 'android/app/build.gradle';
let s = fs.readFileSync(p, 'utf8');
if (!s.includes('signingConfigs')) {
  s = s.replace(/buildTypes \{\s*release \{/, m => `signingConfigs {
        release {
            storeFile file('../../keystore/release.jks')
            storePassword 'sijil2026'
            keyAlias 'sijil'
            keyPassword 'sijil2026'
        }
    }
    ${m}
            signingConfig signingConfigs.release`);
  s = s.replace(/versionCode \d+/, 'versionCode Integer.parseInt(System.getenv("GITHUB_RUN_NUMBER") ?: "1")');
  fs.writeFileSync(p, s);
}
if (!s.includes('signingConfig signingConfigs.release')) { console.error('patch failed'); process.exit(1); }
// native WhatsApp direct-send plugin
const jdir = 'android/app/src/main/java/com/hayder/sijilduyun/';
for (const f of ['WaSharePlugin.java', 'MainActivity.java']) fs.copyFileSync('native/' + f, jdir + f);
// let the app see whether WhatsApp / WhatsApp Business are installed (Android 11+)
const mp = 'android/app/src/main/AndroidManifest.xml';
let m = fs.readFileSync(mp, 'utf8');
if (!m.includes('<queries>')) {
  m = m.replace('<application', '<queries>\n        <package android:name="com.whatsapp" />\n        <package android:name="com.whatsapp.w4b" />\n    </queries>\n\n    <application');
  fs.writeFileSync(mp, m);
}
console.log('android patched');
