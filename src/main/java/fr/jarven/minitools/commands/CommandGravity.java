package fr.jarven.minitools.commands;

import org.bukkit.entity.Entity;

import dev.jorel.commandapi.ArgumentTree;

public class CommandGravity extends Base {
	public static ArgumentTree getSubCommand() {
		return executeEntity(literal("gravity")
					     .then(executeEntity(literal("on"), (entity, args) -> entity.setGravity(true)))
					     .then(executeEntity(literal("off"), (entity, args) -> entity.setGravity(false)))
					     .then(executeEntity(literal("toggle"), (entity, args) -> entity.setGravity(!entity.hasGravity())))
					     .then(executeEntityProxy(literal("info"), (proxy, args) -> {
						     Entity entity = (Entity) proxy.getCallee();
						     proxy.sendMessage("Gravity: " + entity.hasGravity());
					     })),
			(entity, args) -> entity.setGravity(!entity.hasGravity()));
	}
}
