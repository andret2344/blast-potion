# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

BlastPotion (successor of atsExplosivePotion) - a Paper plugin. Players craft splash potions that explode where they
land instead of splashing. Every potion (name, lore, explosion power, recipe) is defined in
`src/main/resources/config.yml`. Paper-only on purpose: Spigot is not supported, Paper API is fine to use.

## Commands

```sh
./gradlew build                      # compile, test, coverage check, shadowJar -> build/libs/BlastPotion-<version>.jar
./gradlew test                       # JUnit tests, then jacoco coverage verification (min 80%)
./gradlew test --tests eu.andret.blastpotion.config.ConfigLoaderTest -x jacocoTestCoverageVerification
./gradlew test --tests "eu.andret.blastpotion.BlastPotionListenerTest.splashBlastPotion" -x jacocoTestCoverageVerification
```

- `test` is `finalizedBy` `jacocoTestCoverageVerification`, so running a subset of tests fails the 80% coverage rule
  unless that task is excluded with `-x`.
- Java toolchain, Paper API and all dependency versions live in `gradle/libs.versions.toml`. Project `version`,
  `group`, `artifact` and `minecraftVersions` (what releases are marked as supporting) live in `gradle.properties`;
  `${version}` is expanded into `plugin.yml`. MockBukkit (`mockbukkit-v26.2`) must match the Paper API version.
- The `jar` task is disabled on purpose: the shadow jar is the only jar. There is no Maven publishing.

## Architecture

Packages under `eu.andret.blastpotion`: the root holds the plugin, its command and its listener; `config` the loader
and what it produces (`PotionPattern`); `command` the Lamp argument type and condition.

- `BlastPotionPlugin` - entry point. `applyPotions` swaps the potion list and the registered recipes in one go; it is
  used on enable, by `reload` (which parses the file into a fresh `YamlConfiguration` first, so an invalid config -
  YAML or content - throws and changes nothing) and on disable (removes the recipes). On enable: saves default config,
  loads `PotionPattern`s via `ConfigLoader`, registers the listener and the command (Lamp), starts bStats (ID 10681,
  still registered as atsExplosivePotion). `findPotion` maps an item to its potion through the item's
  `PersistentDataContainer`: key `<plugin namespace>:potion`, value = potion name. The namespace is the plugin name,
  so renaming the plugin turns every existing potion into a regular one.
- `ConfigLoader` - parses `config.yml`. Each entry under `potions` has a required positive `explosion-power`, an
  optional `item` (MiniMessage `name` set as the custom name with italics turned off - the game names a potion with
  `potion_contents` after its potion type and ignores the item name - and MiniMessage `lore`) and an optional `crafting`. The item is an awkward `SPLASH_POTION` with the enchantment glint
  override and the `POTION_CONTENTS` component hidden through `TOOLTIP_DISPLAY` (hides "No Effects"). The recipe key is
  the sanitized potion name. It takes the config as a parameter and builds `PotionPattern.recipe` but never registers
  it - registering is the plugin's job. Config errors throw `IllegalArgumentException` with the config path in the
  message and stop the plugin from loading.
- `BlastPotionListener` - `PotionSplashEvent` at `HIGH` with `ignoreCancelled`, so region protections cancelling at
  `NORMAL` or lower stop the explosion. For a blast potion it cancels the splash and calls
  `World#createExplosion(source, ...)` with the thrown potion as the source, so the explosion's damage and the
  `EntityExplodeEvent` seen by protections are attributed to the thrower. No fire, blocks are broken.
- `command/` - Lamp (`revxrsal.commands`) command `/blastpotion` (alias `bpot`) is registered in code, not in
  `plugin.yml`. `PotionParameterType` resolves the potion argument. Lamp needs `-parameters` (set in
  `build.gradle.kts`). Lamp runs a command that matched only the start of the input, so `PlaceholderCondition` stops
  the `@CommandPlaceholder` help from swallowing the errors of the subcommands. `give` takes a `CommandSender` (the
  console, for shop plugins) and a `Player` argument; `get` and `give` take an optional amount (`@Default("1")
  @Range`, capped at a full inventory, as potions do not stack). Items are given as copies (`asQuantity`; `addItem`
  may change the amount of the stack it gets) and what does not fit is dropped at the player's feet in stacks of at
  most the max stack size.
  MockBukkit's `addItem` also fills the armour slots, so a "full inventory" test must fill all of `getContents()`.
- bStats and Lamp are shaded and relocated under `eu.andret.blastpotion.*` by `shadowJar`. bStats refuses to start
  unrelocated, so tests run with `bstats.relocatecheck=false`.

## Tests

- JUnit 6 + AssertJ + MockBukkit, `// given` / `// when` / `// then` structure. No Mockito.
- Test classes, their methods and test-only helpers are package-private. `public` stays only where Java needs it: the
  `helper` classes (used from other packages) and overridden API methods.
- Tests extend `helper/PluginTest`, which starts `MockBukkit.mock()`, loads the real plugin with the shipped
  `config.yml` and adds a `helper/ExplosionWorld` before every test. MockBukkit's `createExplosion` does nothing, so
  `ExplosionWorld` records the explosions for the tests to check.
- Events are built by hand and fired with `server.getPluginManager().callEvent`. Thrown potions are spawned with
  `world.spawn(location, ThrownPotion.class, ...)`.
- MockBukkit's `ItemStack` copies (`clone`, `asQuantity`) drop data components such as `TOOLTIP_DISPLAY`; check them on
  the configured item.
- `ConfigLoaderTest` loads other configs with `YamlConfiguration#loadFromString` and a new `ConfigLoader`; potion names
  must differ from the shipped ones, whose recipes are already registered.
- MockBukkit throws `UnimplementedOperationException`, a `TestAbortedException`, from what it does not implement, and
  JUnit reports that as **skipped**. The `test` task fails the build when any test is skipped, so a test never passes
  without running.

## Icon and banner

`.github/assets/` holds the icon (`icon.svg`/`.png`, 512x512) and the banner (`banner.svg`/`.png`, 1200x400, shown on
top of the README), all generated - never edit them by hand. `python .github/assets/generator/generate.py` draws them
as pure vector SVGs and renders the PNGs with headless Chrome (or `CHROME`), then copies the icon to `.idea/icon.png`.
The icon is the hand-made 39x39 pixel art kept as `PALETTE` and `PIXELS` in `icon.py` - to change it, edit those and
run the script. The pixel art is drawn at a whole number of pixels per art pixel (13 in the icon, 10 in the banner), so
every edge lands on a whole pixel. The banner adds the name and the tagline in the font, and a background of the
deepslate texture, of the Minecraft 26.2 client jar (`%APPDATA%/.minecraft/versions/26.2/26.2.jar`, or
`MINECRAFT_JAR`). Textures are read from the jar at run time on purpose: Mojang's files must not be committed to this
Apache 2.0 repository. Python 3 standard library only.

## Conventions

- Tabs for indentation, LF line endings (`.gitattributes`), no wildcard imports, no `var`, `final` on parameters and
  locals, `@NotNull`/`@Nullable` from `org.jetbrains.annotations` on everything.
- No `clone()` (`UseOfClone` is an error in the inspection profile) - copy an `ItemStack` with `asQuantity`.
- User-facing changes go under `## Unreleased` in `CHANGELOG.md` (`org.jetbrains.changelog` format); release notes
  are extracted from it.
- Apache 2.0: the jar's `META-INF` carries `LICENSE`/`NOTICE` renamed with the `-blast-potion` suffix so shaded
  libraries' files do not overwrite them.
- Build script reads project properties through `project.group`/`project.version` and
  `providers.gradleProperty(...)`, never `project.properties[...]` (deprecated, fails in Gradle 10).
- CI is GitHub Actions. `build.yml` builds every push and PR and uploads the JaCoCo XML report
  (`build/reports/jacoco/test/jacocoTestReport.xml`) to Codecov with the `CODECOV_TOKEN` secret. Releasing takes
  two workflows - releases are never made by pushing tags:
  - `release.yml` is run by hand from the Actions tab on `main` with a `version` input. It sets the version in
    `gradle.properties`, builds and tests, runs `patchChangelog` (fails when "Unreleased" is empty), commits both files
    as `Released <version>.` by github-actions, and creates a **draft** GitHub release of that commit with the jar.
  - `publish.yml` runs when that draft is published (which creates the `v<version>` tag). It downloads the jar from the
    release and uploads that same file, with the release description as the changelog, to Modrinth (`mc-publish`,
    project ID `V7ERMC6V` in the workflow, `MODRINTH_TOKEN` secret) and Hangar
    (`hangarPublish` in `build.gradle.kts` with `-PhangarJar`, project `andret2344/BlastPotion`, `HANGAR_API_TOKEN`
    secret).

  So user-facing changes only go under "Unreleased" - never bump the version or add version sections by hand. Each
  upload is skipped without its secret; a token is passed only to the step that needs it. A version with a `-suffix`
  is a pre-release (Modrinth beta, Hangar "Snapshot" channel).
