# ⚡ Schnellanleitung: JAR in 3 Minuten

## Option 1 – GitHub (empfohlen, kein Setup nötig)

1. Gehe zu https://github.com/new → Repository erstellen (z.B. `sodium-highlighter`)
2. ZIP entpacken → Alle Dateien in das leere Repo hochladen
3. Gehe zu **Actions** → **Build Sodium Highlighter** → **Run workflow**
4. Nach ~2 Minuten: **Releases** → JAR herunterladen ✅

## Option 2 – Lokal bauen (JDK 21 erforderlich)

```bash
cd sodium-highlighter
# Windows:
gradlew.bat build
# Linux/Mac:
chmod +x gradlew && ./gradlew build

# JAR liegt dann in:
# build/libs/sodium-highlighter-1.0.0.jar
```

## Installation

1. [Fabric Installer](https://fabricmc.net/use/installer/) → 1.21.4 installieren
2. [Fabric API JAR](https://modrinth.com/mod/fabric-api/versions?g=1.21.4) herunterladen
3. Beide JARs nach `.minecraft/mods/` kopieren
4. Minecraft starten → fertig!

## Tasten (Standard)

| Taste | Funktion |
|---|---|
| H | Highlighter GUI öffnen |
| J | Highlighter ein/ausschalten |
| Right Shift | Keybind-Einstellungen öffnen |
