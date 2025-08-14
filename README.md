# StaffUtilsPlus

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptopenjdk.net/) [![Spigot](https://img.shields.io/badge/Spigot-1.21+-orange.svg)](https://www.spigotmc.org/) [![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

> **A comprehensive, modern, and highly configurable staff utilities plugin for Minecraft servers.**

### Note that versions 1.1.0+ of StaffUtilsPlus will require [ProtocolLib](https://github.com/dmulloy2/ProtocolLib/releases) to function.

---

## ✨ Features

- **Ban, Mute, and IP Ban**: Temporarily or permanently ban/mute players, including IP bans.
- **Player Notes & Warnings**: Add, view, and manage notes and warnings for players.
- **Advanced Staff Menu**: GUI-based staff menu for quick access to moderation tools (coming soon).
- **Database & Local Storage**: Supports MySQL, SQLite, or local YAML storage.
- **Auto-Expiration**: Automatic expiration for temporary punishments.
- **Notifications**: Broadcast and permission-based notifications for staff actions.
- **Performance & Logging**: Async operations, caching, and detailed logging.
- **Highly Configurable**: Tweak punishments, commands, storage, and more via YAML config.

---

## 📦 Installation

1. **Download** the latest release from [GitHub Releases](https://github.com/hexett/StaffUtilsPlus/releases) or [Modrinth](https://modrinth.com/plugin/staffutilsplus).
2. **Place** the `StaffUtilsPlus.jar` file into your server's `plugins` folder.
3. **Restart** or **reload** your server.
4. **Edit** the configuration files in `plugins/StaffUtilsPlus/` as needed.

---

## ⚙️ Configuration

- **config.yml**: Main plugin configuration (database, punishments, commands, storage, logging, performance).
- **messages.yml**: Customize all plugin messages, including color codes and placeholders.
- **plugin.yml**: Bukkit/Spigot plugin metadata and command registration.

Example `config.yml` snippet:
```yaml
# Debug mode - enables additional logging
debug-mode: false

# Database Configuration
database:
  # Whether to use a database (false = local storage only)
  enabled: false
  
  # Database type: mysql, sqlite
  type: "mysql"
  
  # MySQL Configuration (ignored for SQLite)
  host: "localhost"
  port: 3306
  name: "staffutils"
  user: "root"
  pass: "password"


vanish-fake-messages: true # When a player goes into vanish, broadcasts a fake join/leave message.
```

---

## 🚀 Usage

- **All commands** can be run from the console or in-game (unless noted).
- **Tab completion** and **aliases** are supported for all commands.
- **Permissions** are required for most commands (see below).

### Main Commands

| Command                | Description                        | Aliases         | Permission           |
|------------------------|------------------------------------|-----------------|----------------------|
| `/ban <player> [reason] [duration]`      | Ban a player                | `/tempban`      | `staffutils.ban`     |
| `/unban <player>`      | Unban a player                     |                 | `staffutils.unban`   |
| `/kick <player> [reason]`      | Kick a Player              |              | `staffutils.kick`  
| `/mute <player> [reason] [duration]`     | Mute a player               | `/tempmute`     | `staffutils.mute`    |
| `/unmute <player>`     | Unmute a player                    |                 | `staffutils.unmute`  |
| `/ipban <player> [reason] [duration]`    | IP ban a player             | `/tempipban`    | `staffutils.ipban`   |
| `/unbanip <ip-address>`| Unban an IP address                |                 | `staffutils.unbanip` |
| `/vanish`              | Makes the sender invisible to other players.    |    | `staffutils.vanish`  |
| `/notes <player> [add/remove] [content/id]` | Manage player notes      |                 | `staffutils.notes`   |
| `/warnings <player> [add/remove] [reason/severity/id]` | Manage player warnings | | `staffutils.warnings` |
| `/help [page]`         | Show help information              | `/h`, `/?`      | `staffutils.help`    |
| `/reload`              | Reload the plugin configuration    | `/staffreload`  | `staffutils.reload`  |

> **Note:** The advanced staff menu GUI is in development and will be enabled in a future update.

---

## 🛡️ Permissions

- `staffutils.*` — **All plugin permissions** (default: OP)
- `staffutils.ban` — Ban players
- `staffutils.unban` — Unban players
- `staffutils.mute` — Mute players
- `staffutils.unmute` — Unmute players
- `staffutils.ipban` — IP ban players
- `staffutils.unbanip` — Unban IP addresses
- `staffutils.vanish` — Allows players to enter vanish.
- `staffutils.notes` — Manage player notes
- `staffutils.warnings` — Manage player warnings
- `staffutils.menu` — Access staff menu (future)
- `staffutils.reload` — Reload plugin
- `staffutils.debug` — Debug commands
- `staffutils.notify.*` — Receive notifications for bans, mutes, IP bans

---

## 🧩 Dependencies

- [Spigot 1.21+](https://www.spigotmc.org/)
- [ProtocolLib](https://github.com/dmulloy2/ProtocolLib/releases)
- Java 21

---

## 🙏 Credits

- **Author:** Hexett
- **Contributors:** See [GitHub Contributors](https://github.com/hexett/StaffUtilsPlus/graphs/contributors)
- **License:** MIT

---

## 💬 Support & Feedback

- [GitHub Issues](https://github.com/hexett/StaffUtilsPlus/issues) for bug reports and suggestions
- [Modrinth Page](https://modrinth.com/plugin/staffutilsplus) for downloads and reviews

---

## 📜 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
