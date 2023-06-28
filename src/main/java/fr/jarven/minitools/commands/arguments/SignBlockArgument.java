package fr.jarven.minitools.commands.arguments;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;

public class SignBlockArgument extends CustomArgument<Sign, Location> {
	public SignBlockArgument(String nodeName) {
		super(new LocationArgument(nodeName, LocationType.BLOCK_POSITION), SignBlockArgument.parser);
	}

	private static CustomArgumentInfoParser<Sign, Location> parser = info -> {
		Location location = info.currentInput();
		BlockState blockState = location.getBlock().getState();
		if (blockState instanceof Sign) {
			return (Sign) blockState;
		} else {
			throw CustomArgumentException.fromString("Not a sign");
		}
	};
}
