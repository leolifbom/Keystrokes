# Keystrokes
A highly customizable Keystrokes Mod for Minecraft Forge 1.16.4.

Settings are accessible with the `/keystrokes` command, where users may change colors, add in custom key overlays and much more.

## Built With
* [Minecraft Forge](https://mcforge.readthedocs.io/en/latest/gettingstarted/) 

### Compiling from source
```
git clone https://github.com/arrayofc/Keystrokes.git
gradlew build
```
The output jar can be found in the `build/libs` directory.

To use the mod, place the built jar in your `.minecraft/mods` directory, and launch Minecraft with a Forge 1.16.4 installation.

### When fetching a new update
Make sure to delete the previous build inside the `.minecraft/mods` folder, and place the new compiled jar. If your game unexpectedly crashes on startup or during world loading, delete the mod data found in `.minecraft/config/keystroke-overlays` and reattempt.

**Requirements**

* Java 8+ JDK
* Git
* [Forge 1.16.4 Installation](http://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.16.4.html)

## Author
* [arrayofc (Leo)](https://github.com/arrayofc)