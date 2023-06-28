package fr.jarven.minitools.commands.arguments;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.stream.Stream;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;

public class SignWoodArgument extends CustomArgument<Material, String> {
	public SignWoodArgument(String nodeName) {
		super(new StringArgument(nodeName), SignWoodArgument.parser);
		replaceSuggestions(SignWoodArgument.suggestions);
	}

	private static ArgumentSuggestions<CommandSender> suggestions = ArgumentSuggestions.strings(
		Stream
			.of(Material.values())
			.filter(material -> material.data == org.bukkit.block.data.type.Sign.class)
			.map(material -> material.name().replace("_SIGN", ""))
			.toArray(String[] ::new));

	private static CustomArgumentInfoParser<Material, String> parser = info -> {
		String woodType = info.currentInput();
		Material material = Material.getMaterial(woodType + "_SIGN");
		if (material == null) {
			throw CustomArgumentException.fromString("Invalid wood type: " + woodType);
		}
		return material;
	};

	public static Material getMaterial(CommandArguments args, String nodeName, boolean wallSign) {
		Material material = (Material) args.get(nodeName);
		if (wallSign) {
			Material wallMaterial = Material.getMaterial(material.name().replace("_SIGN", "_WALL_SIGN"));
			if (wallMaterial != null) {
				return wallMaterial;
			}
		}
		return material;
	}
}
