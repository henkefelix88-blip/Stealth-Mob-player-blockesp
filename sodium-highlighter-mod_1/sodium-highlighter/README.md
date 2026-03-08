# Sodium Highlighter Mod
### Minecraft 1.21.4 | Fabric | Mod ID: `sodium`

A **client-side** highlighter mod that draws colored outlines and fills around
blocks, entities, and players — similar to the vanilla glowing effect, but fully
customizable per-target with your own colors.

---

## Features

| Feature | Details |
|---|---|
| **Block highlights** | Scan nearby blocks by ID and draw colored outlines |
| **Entity highlights** | Highlight any mob by registry ID |
| **Player highlights** | Highlight all players or specific names |
| **Through-walls** | Optional X-ray style outline (toggle per entry) |
| **Custom colors** | 10 preset colors, cycle per entry |
| **Opacity control** | Per-entry opacity |
| **Persistent config** | Saved to `.minecraft/config/sodium-highlighter.json` |
| **Custom GUI** | Clean dark UI with tabs and scroll |

---

## Keybinds (configurable in Options → Controls)

| Key | Action |
|---|---|
| `H` | Open Highlighter GUI |
| `J` | Toggle highlighter on/off (HUD message) |

---

## GUI Usage

1. Press **H** to open the GUI
2. Select a tab: **Entities / Blocks / Players**
3. Existing entries are shown as rows:
   - **●** = toggle this entry on/off
   - **W** = toggle through-walls
   - **C** = cycle to next color
   - **✕** = delete entry
4. Click **+ Add Entry** at the bottom
5. Type the block/entity/player ID or name
6. Click a color swatch to pick a color
7. Click **Add**

### Example IDs

```
minecraft:diamond_ore
minecraft:deepslate_diamond_ore
minecraft:ancient_debris
minecraft:creeper
minecraft:skeleton
minecraft:wither_skeleton
minecraft:player           ← highlights ALL players
Notch                      ← highlights specific player
```

---

## Installation

### Requirements
- Java 21+
- Minecraft 1.21.4
- [Fabric Loader](https://fabricmc.net/use/installer/) ≥ 0.16.0
- [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.4

### Build from source

```bash
# Clone / extract the project
cd sodium-highlighter

# Build (requires JDK 21)
./gradlew build

# Output JAR will be at:
# build/libs/sodium-highlighter-1.0.0.jar
```

Copy the JAR to your `.minecraft/mods/` folder.

---

## Config File

Located at: `.minecraft/config/sodium-highlighter.json`

```json
{
  "globalEnabled": true,
  "entries": [
    {
      "id": "minecraft:diamond_ore",
      "type": "BLOCK",
      "color": -16733185,
      "opacity": 0.8,
      "throughWalls": true,
      "enabled": true
    }
  ]
}
```

Colors are stored as packed ARGB integers.

---

## Notes

- Block scanning radius: **64 blocks** from the player
- All rendering is **client-side only** — no server interaction
- Compatible with OptiFine alternatives (Iris, Sodium-the-performance-mod)
- The mod ID `sodium` is used as specified; if you also use the performance mod
  "Sodium", rename the mod ID in `fabric.mod.json` to avoid conflicts

---

## License
MIT
