package eu.andret.ats.explosivepotion;

import eu.andret.arguments.AnnotatedCommandExecutor;
import eu.andret.arguments.api.annotation.Argument;
import eu.andret.arguments.api.annotation.BaseCommand;
import eu.andret.arguments.api.annotation.Fallback;
import eu.andret.arguments.api.annotation.Param;
import eu.andret.arguments.api.entity.ExecutorType;
import eu.andret.ats.explosivepotion.entity.Potion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@BaseCommand("explosivepotion")
public class ExplosivePotionCommand extends AnnotatedCommandExecutor<ExplosivePotionPlugin> {
	public ExplosivePotionCommand(final CommandSender sender, final ExplosivePotionPlugin plugin) {
		super(sender, plugin);
	}

	@Argument(executorType = ExecutorType.PLAYER)
	public String get(@Param("potionMapper") final Potion potion) {
		((Player) sender).getInventory().addItem(potion.getItemStack());
		return "&4Gave" + potion.getName();
	}

	@Fallback
	public String get(final String ignored, final String potion) {
		return "&cNo \"" + potion + "\" exists!";
	}
}
