# BlastPotion changelog

## Unreleased

BlastPotion is the successor of atsExplosivePotion 2.4.4. The changes below are relative to it.

### Added

- The `crafting` section of a potion is optional. Without it the potion has no recipe and can only be given with the
  command, e.g. for servers that sell potions in a shop.
- `/bpot get <potion> [amount]` takes an optional amount, 1 by default and at most 36.
- `/bpot give <player> <potion> [amount]` gives potions to another player and works from the console, e.g. for shop
  plugins. The amount is 1 by default and at most 36. Permission `blastpotion.give`.
- `/bpot reload` loads the config again without restarting the server. An invalid config is not applied and the command
  says what is wrong with it. Permission `blastpotion.reload`.

### Changed

- Renamed from atsExplosivePotion to BlastPotion. The plugin folder is `plugins/BlastPotion`, the command is
  `/blastpotion` with the `/bpot` alias (`/explosivepotion` and `/ep` are gone), and the permissions are
  `blastpotion.get`, `blastpotion.give` and `blastpotion.reload` (all part of `blastpotion.*`), given to operators by
  default.
- Potions made by atsExplosivePotion are not recognized and splash like regular potions without effects.
- Requires Paper (or a fork such as Purpur) 26.2 or newer and Java 25. Spigot is no longer supported.
- Names and lore in `config.yml` use the [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) format,
  e.g. `<dark_purple>Throwable TNT` instead of `&5Throwable TNT`. Names are no longer italic. A potion without a name
  keeps the name of the game instead of "unnamed potion".
- The explosion counts as caused by the player who threw the potion.
- Potions glow without carrying a hidden Infinity enchantment.
- The recipe of a potion is named after the potion in the config instead of its display name.
- Commands are handled by Lamp. `/bpot` alone lists the subcommands with a description for each.
- The plugin is licensed under the Apache License 2.0. Redistributions have to keep the `NOTICE` file, which the jar
  carries as `META-INF/NOTICE-blast-potion`.
- Usage statistics are sent to the new [BlastPotion page on bStats](https://bstats.org/plugin/bukkit/BlastPotion/34537)
  instead of the one of atsExplosivePotion.

### Removed

- The server no longer downloads the `org.jetbrains:annotations` library when loading the plugin.

### Fixed

- A potion thrown where another plugin, such as a region protection, cancels the splash no longer explodes.
- A potion without `explosion-power`, or with one that is not positive, stops the plugin with a message naming where
  it is. Before, it was loaded with a power of 0.
- An unknown material in a crafting mapping stops the plugin with a message naming the material and where it is.
  Before, the ingredient was skipped without a word.
- A potion without a `crafting` section no longer stops the whole plugin from loading.
- Two potions with the same display name no longer clash over the recipe name.
- Potions given with a command no longer disappear when the inventory is full - they are dropped at the player's feet.
- Recipes are removed when the plugin is disabled, so enabling it again does not register them twice.
