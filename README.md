# DominionSAR

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/LunaDeerMC/DominionSAR)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://minecraft.net/)
[![Folia Support](https://img.shields.io/badge/Folia-Supported-brightgreen.svg)](https://papermc.io/software/folia)

DominionSAR is a Bukkit/Spigot plugin that integrates [Dominion](https://github.com/LunaDeerMC/Dominion) claim areas with various web map plugins. It automatically displays dominion boundaries and information on web maps, making it easy for players to visualize claimed territories.

## Features

- 🗺️ **Multi-Map Support**: Works with multiple web map plugins
- 🎨 **Customizable Visualization**: Uses dominion colors for consistent representation
- ⚙️ **Configurable Display**: Players can control whether their dominions appear on web maps
- 🔄 **Real-time Updates**: Automatically updates markers when dominions are created, modified, or deleted
- 📊 **Information Rich**: Shows dominion name, owner, and boundaries
- 🌐 **Folia Compatible**: Full support for Folia server software

## Supported Map Plugins

DominionSAR supports integration with the following web map plugins:

- **[BlueMap](https://bluemap.bluecolored.de/)** - Modern 3D web map
- **[Dynmap](https://www.spigotmc.org/resources/dynmap%C2%AE.274/)** - Classic 2D web map
- **[SquareMap](https://modrinth.com/plugin/squaremap)** - Lightweight alternative to Dynmap
- **[Pl3xMap](https://modrinth.com/plugin/pl3xmap)** - Fast and efficient web map

## Requirements

- **Minecraft**: 1.20+
- **Java**: 21+
- **Server Software**: Bukkit, Spigot, Paper, or Folia
- **Dependencies**:
  - [Dominion](https://github.com/LunaDeerMC/Dominion) (**4.7.0+** Required)
  - At least one supported map plugin (Optional but recommended)

## Installation

1. **Download** the latest release from the [releases page](https://github.com/LunaDeerMC/DominionSAR/releases)
2. **Place** the JAR file in your server's `plugins/` directory
3. **Install** Dominion plugin if not already installed
4. **Install** at least one supported map plugin
5. **Restart** your server
6. **Configure** the plugin (see Configuration section)

## Configuration

The plugin creates a `config.yml` file in the `plugins/DominionSAR/` directory:

```yaml
mapProvider:
  # https://bluemap.bluecolored.de/
  blueMap: false
  
  # https://www.spigotmc.org/resources/dynmap%C2%AE.274/
  dynmap: false
  
  # https://modrinth.com/plugin/squaremap
  squareMap: false
  
  # https://modrinth.com/plugin/pl3xmap
  pl3xMap: false
```

### Configuration Options

- **`mapProvider.blueMap`**: Enable BlueMap integration
- **`mapProvider.dynmap`**: Enable Dynmap integration
- **`mapProvider.squareMap`**: Enable SquareMap integration
- **`mapProvider.pl3xMap`**: Enable Pl3xMap integration

Set the corresponding option to `true` for each map plugin you want to integrate with.

## Usage

### For Server Administrators

1. **Enable Map Providers**: Edit the config.yml to enable the map plugins you have installed
2. **Restart Server**: Restart the server to apply configuration changes
3. **Check Web Maps**: Visit your web map to see dominion boundaries displayed

### For Players

DominionSAR adds a new dominion flag called `show_on_web` that players can use to control whether their dominions appear on web maps.

**Commands** (via Dominion plugin):
```
/dominion set_flag show_on_web true   # Show dominion on web map
/dominion set_flag show_on_web false  # Hide dominion from web map
```

By default, dominions are visible on web maps (`show_on_web: true`).

## How It Works

1. **Event Listening**: The plugin listens for dominion creation, modification, and deletion events
2. **Flag Checking**: Before rendering, it checks if the dominion has the `show_on_web` flag enabled
3. **Marker Creation**: Creates colored markers on supported web maps using the dominion's configured colors
4. **Real-time Updates**: Automatically updates or removes markers when dominions change

### Marker Information

Each dominion marker displays:
- **Name**: The dominion's name
- **Owner**: The dominion owner's username
- **Boundaries**: Visual representation of the claim area
- **Colors**: Uses the dominion's custom colors for inner fill and border

## Building from Source

```bash
# Clone the repository
git clone https://github.com/LunaDeerMC/DominionSAR.git
cd DominionSAR

# Build with Maven
mvn clean package

# The compiled JAR will be in the target/ directory
```

## Dependencies

The project uses the following dependencies:

- **Spigot API** (1.20.1) - Bukkit/Spigot server API
- **Dominion API** (4.7.1) - Integration with Dominion plugin
- **BlueMap API** (2.7.4) - BlueMap integration
- **Dynmap Core API** (3.7-beta-6) - Dynmap integration
- **SquareMap API** (1.3.8) - SquareMap integration

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- **Issues**: Report bugs or request features on the [GitHub Issues](https://github.com/LunaDeerMC/DominionSAR/issues) page
- **Dominion Plugin**: For Dominion-related questions, visit the [Dominion repository](https://github.com/LunaDeerMC/Dominion)

## Related Projects

- **[Dominion](https://github.com/LunaDeerMC/Dominion)** - The main land claiming plugin
- **[BlueMap](https://github.com/BlueMap-Minecraft/BlueMap)** - 3D web map for Minecraft
- **[Dynmap](https://github.com/webbukkit/dynmap)** - Real-time map for Minecraft
- **[SquareMap](https://github.com/jpenilla/squaremap)** - Minimalistic web map
- **[Pl3xMap](https://github.com/pl3xgaming/Pl3xMap)** - Fast web map for Paper servers

---

Made with ❤️ by [LunaDeer](https://github.com/LunaDeerMC)