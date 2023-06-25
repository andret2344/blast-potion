package eu.andret.ats.explosivepotion;

import eu.andret.arguments.AnnotatedCommandExecutor;
import eu.andret.arguments.api.annotation.Argument;
import eu.andret.arguments.api.annotation.BaseCommand;
import eu.andret.arguments.api.annotation.TypeFallback;
import eu.andret.arguments.api.entity.ExecutorType;
import eu.andret.ats.explosivepotion.entity.ExplosivePotion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@BaseCommand("explosivepotion")
public class ExplosivePotionCommand extends AnnotatedCommandExecutor<ExplosivePotionPlugin> {
	public ExplosivePotionCommand(final CommandSender sender, final ExplosivePotionPlugin plugin) {
		super(sender, plugin);
	}

	@Argument(executorType = ExecutorType.PLAYER, permission = "ats.explosivepotion.get")
	public String get(final ExplosivePotion explosivePotion) {
		((Player) sender).getInventory().addItem(explosivePotion.itemStack());
		return "&4Gave " + explosivePotion.name();
	}

	@TypeFallback(ExplosivePotion.class)
	public String potionFallback(final String potion) {
		return "&cNo \"" + potion + "\" exists!";
	}
}
