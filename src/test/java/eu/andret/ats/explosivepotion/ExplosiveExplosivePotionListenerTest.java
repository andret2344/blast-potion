package eu.andret.ats.explosivepotion;

import eu.andret.ats.explosivepotion.entity.ExplosivePotion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExplosiveExplosivePotionListenerTest {
	@Test
	void thrownExplosivePotion() {
		// given
		final ExplosivePotionPlugin explosivePotionPlugin = mock(ExplosivePotionPlugin.class);
		final ExplosivePotionListener explosivePotionListener = new ExplosivePotionListener(explosivePotionPlugin);
		final PotionSplashEvent potionSplashEvent = mock(PotionSplashEvent.class);
		final ThrownPotion thrownPotion = mock(ThrownPotion.class);
		final ExplosivePotion explosivePotion = new ExplosivePotion("test", new ItemStack(Material.SPLASH_POTION), 10);
		final Location location = mock(Location.class);
		final World world = mock(World.class);

		when(potionSplashEvent.getPotion()).thenReturn(thrownPotion);
		when(explosivePotionPlugin.getExplosivePotion(thrownPotion)).thenReturn(Optional.of(explosivePotion));
		when(thrownPotion.getLocation()).thenReturn(location);
		when(location.getWorld()).thenReturn(world);

		// when 
		explosivePotionListener.onPotionSplash(potionSplashEvent);

		// then
		verify(potionSplashEvent, times(1)).setCancelled(true);
		verify(world, times(1)).createExplosion(location, 10f);
	}

	@Test
	void thrownNonExplosivePotion() {
		// given
		final ExplosivePotionPlugin explosivePotionPlugin = mock(ExplosivePotionPlugin.class);
		final ExplosivePotionListener explosivePotionListener = new ExplosivePotionListener(explosivePotionPlugin);
		final PotionSplashEvent potionSplashEvent = mock(PotionSplashEvent.class);
		final ThrownPotion thrownPotion = mock(ThrownPotion.class);

		when(potionSplashEvent.getPotion()).thenReturn(thrownPotion);
		when(explosivePotionPlugin.getExplosivePotion(thrownPotion)).thenReturn(Optional.empty());

		// when
		explosivePotionListener.onPotionSplash(potionSplashEvent);

		// then
		verify(potionSplashEvent, times(0)).setCancelled(true);
		verify(thrownPotion, times(0)).getLocation();
	}
}
