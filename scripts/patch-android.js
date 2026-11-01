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
console.log('android patched');
