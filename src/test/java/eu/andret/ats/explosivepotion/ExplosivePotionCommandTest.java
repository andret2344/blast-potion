package eu.andret.ats.explosivepotion;

import eu.andret.ats.explosivepotion.entity.ExplosivePotion;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ExplosivePotionCommandTest {
	@Test
	void getPotion() {
		// given
		final ExplosivePotionPlugin explosivePotionPlugin = mock(ExplosivePotionPlugin.class);
		final Player sender = mock(Player.class);
		final ExplosivePotionCommand explosivePotionCommand = new ExplosivePotionCommand(sender, explosivePotionPlugin);
		final ItemStack itemStack = new ItemStack(Material.SPLASH_POTION);
		final ExplosivePotion explosivePotion = new ExplosivePotion("test", itemStack, 1);
		final PlayerInventory inventory = mock(PlayerInventory.class);
		when(sender.getInventory()).thenReturn(inventory);

		// when
		final String result = explosivePotionCommand.get(explosivePotion);

		// then
		assertEquals("&4Gave test", result);
		verify(inventory, times(1)).addItem(itemStack);
		verifyNoMoreInteractions(inventory);
	}

	@Test
	void fallbackPotion() {
		// given
		final ExplosivePotionPlugin explosivePotionPlugin = mock(ExplosivePotionPlugin.class);
		final Player sender = mock(Player.class);
		final ExplosivePotionCommand explosivePotionCommand = new ExplosivePotionCommand(sender, explosivePotionPlugin);
		final PlayerInventory inventory = mock(PlayerInventory.class);
		when(sender.getInventory()).thenReturn(inventory);

		// when
		final String result = explosivePotionCommand.potionFallback("test");

		// then
		assertEquals("&cNo \"test\" exists!", result);
		verifyNoInteractions(inventory);
	}
}
