package fr.jarven.minitools.commands.arguments;

import org.bukkit.DyeColor;
import org.bukkit.command.CommandSender;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

public class DyeColorArgument extends CustomArgument<DyeColor, String> {
	public DyeColorArgument(String nodeName) {
		super(new StringArgument(nodeName), DyeColorArgument.parser);
		replaceSuggestions(DyeColorArgument.suggestions);
	}

	private static ArgumentSuggestions<CommandSender> suggestions = (info, builder) -> {
		String current = info.currentArg().toUpperCase();
		for (DyeColor color : DyeColor.values()) {
			if (color.name().startsWith(current)) {
				builder.suggest(color.name());
			}
		}
		return builder.buildFuture();
	};

	private static CustomArgumentInfoParser<DyeColor, String> parser = info -> {
		String colorName = info.currentInput();
		try {
			return DyeColor.valueOf(colorName.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw CustomArgumentException.fromString("Couleur invalide");
		}
	};
}
