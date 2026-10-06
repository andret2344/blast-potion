package eu.andret.blastpotion.command;

import eu.andret.blastpotion.config.PotionPattern;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.stream.MutableStringStream;

import java.util.Collection;
import java.util.function.Supplier;

public final class PotionParameterType implements ParameterType<BukkitCommandActor, PotionPattern> {
	@NotNull
	private final Supplier<Collection<PotionPattern>> potions;

	public PotionParameterType(@NotNull final Supplier<Collection<PotionPattern>> potions) {
		this.potions = potions;
	}

	@Override
	public PotionPattern parse(@NotNull final MutableStringStream input,
			@NotNull final ExecutionContext<BukkitCommandActor> context) {
		final String name = input.readString();
		return potions.get()
				.stream()
				.filter(potion -> potion.name().equals(name))
				.findAny()
				.orElseThrow(() -> new CommandErrorException("No blast potion found: \"" + name + "\""));
	}

	@NotNull
	@Override
	public SuggestionProvider<BukkitCommandActor> defaultSuggestions() {
		return context -> potions.get()
				.stream()
				.map(PotionPattern::name)
				.toList();
	}
}
