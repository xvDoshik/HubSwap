EN | [RU](docs/README_RU.md)

# HubSwap

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)


Fabric mod for quick switching between Anarchy / Lite-Anarchy servers on **HolyWorld**.

> Original mod - [Heldyy90/HubSwap](https://github.com/Heldyy90/HubSwap).  
> **Heldyy did great work** on the original base. This fork is a rewritten version for **Minecraft 1.21.11** with updated API and build.

## Requirements

| Component | Version |
|-----------|--------|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.141.6+1.21.11 |
| Java (for build) | 21+ |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft **1.21.11**
2. Download **Fabric API** (`0.141.6+1.21.11`) and place it in `mods/`
3. Download `HubSwap-1.1.1.jar` from [Releases](https://github.com/xvDoshik/HubSwap/releases)
4. Put the JAR in the `mods/` folder
5. Launch the game

```
.minecraft/
  mods/
    fabric-api-0.141.6+1.21.11.jar
    HubSwap-1.1.1.jar
```

## Controls

| Action | Key / command |
|----------|-------------------|
| Settings menu | **F6** |
| Classic anarchy | `/cn <1-5>` |
| Lite anarchy | `/ln <1-74>` |
| Lite 1.20 | `/ln120 <1-3>` |
| Prime anarchy | `/pr <1-9>` |

Commands work on Russian and English keyboard layouts. Server names in chat are clickable.

## Features

- Auto-switch: hub -> menu -> server selection
- Configurable `/hub` delay and delay between clicks
- Smart auto-tuning of delays based on ping
- Hotkeys for 8 slots
- Switch notifications
- UI themes and chat link color
- Switch statistics

## Config

File: `config/hubswap.json`

- Delays and commands
- Hotkeys
- Theme and link color
- Statistics: `config/hubswap_stats.json`

## Build from source

```bash
git clone https://github.com/xvDoshik/HubSwap.git
cd HubSwap
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew build
```

Output JAR:

```
build/libs/HubSwap-1.1.1.jar
```

## Credits

- **[Heldyy](https://github.com/Heldyy90)** - author of the original HubSwap; this mod would not exist without that work
- Original: https://github.com/Heldyy90/HubSwap

## License

MIT - see [LICENSE](LICENSE)

<!-- HUBSWAP-STATS:START -->
## 📊 Статистика проекта

| Показатель | Значение |
|---|---:|
| 📥 Всего скачиваний файлов | **662** |
| 🧩 Скачиваний `.jar` модов | **633** |
| 🚀 Всего релизов | **9** |
| 📦 Всего файлов в релизах | **13** |
| 🆕 Последняя версия | **[`v.1.0.8`](https://github.com/Heldyy90/HubSwap/releases/tag/v.1.0.8)** |
| 📅 Дата последнего релиза | **2026-08-19** |

[➡️ Подробная статистика по всем релизам](./STATS.md)

_Автообновление: 2026-09-05 03:58 UTC_
<!-- HUBSWAP-STATS:END -->
