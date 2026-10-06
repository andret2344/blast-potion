package eu.andret.blastpotion;

import eu.andret.blastpotion.helper.PluginTest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class BlastPotionCommandTest extends PluginTest {
	private PlayerMock player;

	@BeforeEach
	void setUpPlayer() {
		player = server.addPlayer();
		player.setOp(true);
	}

	@Test
	void get() {
		// when
		player.performCommand("bpot get throwable-tnt");

		// then
		assertThat(player.nextMessage()).isEqualTo("Blast potion: \"throwable-tnt\"");
		assertThat(inventory()).singleElement()
				.matches(item -> item.isSimilar(potion("throwable-tnt").itemStack()));
	}

	@Test
	void getAmount() {
		// when
		player.performCommand("bpot get throwable-tnt 3");

		// then
		assertThat(inventory()).hasSize(3)
				.allMatch(item -> item.getAmount() == 1 && item.isSimilar(potion("throwable-tnt").itemStack()));
	}

	@Test
	void getWithFullName() {
		// when
		player.performCommand("blastpotion get throwable-tnt");

		// then
		assertThat(inventory()).singleElement().extracting(ItemStack::getType).isEqualTo(Material.SPLASH_POTION);
	}

	@Test
	void unknownPotion() {
		// when
		player.performCommand("bpot get nothing");

		// then
		assertThat(nextMessages()).containsExactly("No blast potion found: \"nothing\"");
		assertThat(inventory()).isEmpty();
	}

	@Test
	void unknownSubcommand() {
		// when
		player.performCommand("bpot nothing");

		// then
		assertThat(nextMessages()).first().asString().startsWith("Failed to find a suitable command");
	}

	@Test
	void suggestions() {
		// when
		final List<String> potions = server.getCommandTabComplete(player, "bpot get ");

		// then
		assertThat(potions).containsExactly("throwable-tnt");
	}

	@Test
	void help() {
		// when
		player.performCommand("bpot");

		// then
		assertThat(nextMessages()).containsExactlyInAnyOrder(
				"/bpot get <potion> [amount] - Puts a blast potion into the inventory",
				"/bpot give <player> <potion> [amount] - Puts a blast potion into the inventory of a player",
				"/bpot reload - Loads the config again");
	}

	@Test
	void giveFromConsole() {
		// given
		final ConsoleCommandSenderMock console = server.getConsoleSender();

		// when
		server.dispatchCommand(console, "bpot give " + player.getName() + " throwable-tnt");

		// then
		assertThat(console.nextMessage()).isEqualTo("Gave blast potion \"throwable-tnt\" to " + player.getName());
		assertThat(inventory()).singleElement()
				.matches(item -> item.isSimilar(potion("throwable-tnt").itemStack()));
	}

	@Test
	void giveAmount() {
		// given
		final ConsoleCommandSenderMock console = server.getConsoleSender();

		// when
		server.dispatchCommand(console, "bpot give " + player.getName() + " throwable-tnt 3");

		// then
		assertThat(console.nextMessage()).isEqualTo("Gave 3 blast potions \"throwable-tnt\" to " + player.getName());
		assertThat(inventory()).hasSize(3)
				.allMatch(item -> item.getAmount() == 1 && item.isSimilar(potion("throwable-tnt").itemStack()));
	}

	@Test
	void giveWithFullInventory() {
		// given
		// All slots, as MockBukkit's addItem also fills the armour slots, unlike the server
		final ItemStack[] full = new ItemStack[player.getInventory().getContents().length];
		Arrays.fill(full, new ItemStack(Material.DIRT, 64));
		player.getInventory().setContents(full);

		// when
		server.dispatchCommand(server.getConsoleSender(), "bpot give " + player.getName() + " throwable-tnt 2");

		// then
		assertThat(player.getWorld().getEntitiesByClass(Item.class))
				.hasSize(2)
				.allMatch(item -> item.getItemStack().getAmount() == 1
						&& item.getItemStack().isSimilar(potion("throwable-tnt").itemStack()));
	}

	@Test
	void giveAmountOutOfRange() {
		// when
		server.dispatchCommand(server.getConsoleSender(), "bpot give " + player.getName() + " throwable-tnt 0");
		server.dispatchCommand(server.getConsoleSender(), "bpot give " + player.getName() + " throwable-tnt 37");

		// then
		assertThat(inventory()).isEmpty();
	}

	@Test
	void giveToUnknownPlayer() {
		// given
		final ConsoleCommandSenderMock console = server.getConsoleSender();

		// when
		server.dispatchCommand(console, "bpot give Nobody throwable-tnt");

		// then
		assertThat(console.nextMessage()).doesNotStartWith("Gave");
		assertThat(inventory()).isEmpty();
	}

	@Test
	void giveWithoutPermission() {
		// given
		final PlayerMock other = server.addPlayer();

		// when
		other.performCommand("bpot give " + player.getName() + " throwable-tnt");

		// then
		assertThat(inventory()).isEmpty();
	}

	@Test
	void reload() throws IOException {
		// given
		writeConfig("""
				potions:
				  first:
				    explosion-power: 1
				  second:
				    explosion-power: 2
				""");

		// when
		player.performCommand("bpot reload");

		// then
		assertThat(nextMessages()).containsExactly("Reloaded 2 potions");
	}

	@Test
	void reloadInvalidConfig() throws IOException {
		// given
		writeConfig("""
				potions:
				  weak:
				    explosion-power: 0
				""");

		// when
		player.performCommand("bpot reload");

		// then
		assertThat(nextMessages()).containsExactly("The config is invalid and was not reloaded: "
				+ "Missing or not positive explosion power at 'potions.weak.explosion-power'.");
		assertThat(plugin.getPotionList()).hasSize(1);
	}

	@Test
	void withoutPermission() {
		// given
		player.setOp(false);

		// when
		player.performCommand("bpot get throwable-tnt");

		// then
		assertThat(inventory()).isEmpty();
	}

	private void writeConfig(@NotNull final String yaml) throws IOException {
		Files.writeString(new File(plugin.getDataFolder(), "config.yml").toPath(), yaml);
	}

	@NotNull
	private List<ItemStack> inventory() {
		return Arrays.stream(player.getInventory().getContents())
				.filter(Objects::nonNull)
				.toList();
	}

	// The messages as plain text, without colours
	@NotNull
	private List<String> nextMessages() {
		final List<String> messages = new ArrayList<>();
		Component message;
		while ((message = player.nextComponentMessage()) != null) {
			messages.add(PlainTextComponentSerializer.plainText().serialize(message));
		}
		return messages;
	}
}
