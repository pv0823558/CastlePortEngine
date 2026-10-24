# CastlePortEngine v0.1 — technical demonstration

**Not Castle Crashers playable.** This project contains no proprietary game code, graphics, music, animation or original gameplay implementation. It is an independent Android prototype for testing a rendering/update loop, movement, touch attack indicator, simple world boundaries and a ZIP-backed `.pak` listing.

## Build without Android Studio

Create a private GitHub repository, upload the **contents** of this folder preserving `.github/workflows` and `app/src/main`, then choose Actions > Build Android APK > Run workflow. Download `CastlePortEngine-demo-APK` from workflow artifacts, unzip and install `app-debug.apk`.

## Test checklist

- [ ] App launches landscape on an ARM64 phone
- [ ] FPS counter and moving blue test figure appear
- [ ] LEFT/RIGHT/UP/DOWN move figure; figure stays inside boundaries
- [ ] ATTACK displays yellow effect
- [ ] OPEN PAK selects legally obtained `bsp1.pak` and displays `BSP1.PDAG.NREC`

## Known limitations

This is a Java Android application that works on 64-bit Android; it **does not yet contain an ARM64 native library** or a native game engine. Graphics use Android Canvas, not OpenGL ES. The ZIP parser enumerates entries and does not interpret NREC files. Controls are designed for landscape testing and may need refinement. There is no Castle Crashers gameplay, original assets, audio, enemies, levels or native port. 
