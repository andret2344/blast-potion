package eu.andret.blastpotion;

import eu.andret.blastpotion.config.PotionPattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.help.Help;

import java.io.IOException;

@Command({"blastpotion", "bpot"})
public class BlastPotionCommand {
	private static final String PERMISSION_GET = "blastpotion.get";
	private static final String PERMISSION_GIVE = "blastpotion.give";
	private static final String PERMISSION_RELOAD = "blastpotion.reload";
	// A full inventory, as potions do not stack, so a typo cannot drop hundreds of potions on the ground
	private static final int MAX_AMOUNT = 36;

	@NotNull
	private final BlastPotionPlugin plugin;

	public BlastPotionCommand(@NotNull final BlastPotionPlugin plugin) {
		this.plugin = plugin;
	}

	@CommandPlaceholder
	public void help(@NotNull final CommandSender sender,
			@NotNull final Help.ChildrenCommands<BukkitCommandActor> commands) {
		for (final ExecutableCommand<BukkitCommandActor> command : commands) {
			if (command.description() == null) {
				sender.sendMessage("/" + command.usage());
			} else {
				sender.sendMessage("/" + command.usage() + " - " + command.description());
			}
		}
	}

	@Subcommand("get")
	@Description("Puts a blast potion into the inventory")
	@CommandPermission(PERMISSION_GET)
	public void get(@NotNull final Player sender, @NotNull final PotionPattern potion,
			@Default("1") @Range(min = 1, max = MAX_AMOUNT) final int amount) {
		give(sender, potion, amount);
		sender.sendMessage("Blast potion: \"" + potion.name() + "\"");
	}

	// Works from the console, e.g. for shop plugins selling potions
	@Subcommand("give")
	@Description("Puts a blast potion into the inventory of a player")
	@CommandPermission(PERMISSION_GIVE)
	public void give(@NotNull final CommandSender sender, @NotNull final Player player,
			@NotNull final PotionPattern potion, @Default("1") @Range(min = 1, max = MAX_AMOUNT) final int amount) {
		give(player, potion, amount);
		final String potions = amount == 1 ? "blast potion" : amount + " blast potions";
		sender.sendMessage("Gave " + potions + " \"" + potion.name() + "\" to " + player.getName());
	}

	@Subcommand("reload")
	@Description("Loads the config again")
	@CommandPermission(PERMISSION_RELOAD)
	public void reload(@NotNull final CommandSender sender) {
		try {
			sender.sendMessage("Reloaded " + plugin.reload() + " potions");
		} catch (final IOException | InvalidConfigurationException | IllegalArgumentException e) {
			sender.sendMessage(Component.text("The config is invalid and was not reloaded: " + e.getMessage(),
					NamedTextColor.RED));
		}
	}

	// What does not fit into the inventory lands at the player's feet instead of disappearing
	private void give(@NotNull final Player player, @NotNull final PotionPattern potion, final int amount) {
		// A copy, as adding to an inventory may change the amount of the given item
		player.getInventory().addItem(potion.itemStack().asQuantity(amount))
				.values()
				.forEach(left -> drop(player, left));
	}

	// In stacks no bigger than the game allows
	private void drop(@NotNull final Player player, @NotNull final ItemStack itemStack) {
		for (int left = itemStack.getAmount(); left > 0; left -= itemStack.getMaxStackSize()) {
			player.getWorld().dropItemNaturally(player.getLocation(),
					itemStack.asQuantity(Math.min(left, itemStack.getMaxStackSize())));
		}
	}
}
