package eu.andret.blastpotion.helper;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.ArrayList;
import java.util.List;

/**
 * A MockBukkit world that remembers the explosions created in it, which MockBukkit ignores.
 */
public class ExplosionWorld extends WorldMock {
	@NotNull
	private final List<Explosion> explosions = new ArrayList<>();

	@Override
	public boolean createExplosion(@Nullable final Entity source, @NotNull final Location loc, final float power,
			final boolean setFire, final boolean breakBlocks) {
		explosions.add(new Explosion(source, loc, power, setFire, breakBlocks));
		return true;
	}

	@NotNull
	public List<Explosion> getExplosions() {
		return explosions;
	}

	public record Explosion(@Nullable Entity source, @NotNull Location location, float power, boolean setFire,
			boolean breakBlocks) {
	}
}
