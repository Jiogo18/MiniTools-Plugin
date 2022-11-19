package fr.jarven.minitools.commands;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.jorel.commandapi.ArgumentTree;
import dev.jorel.commandapi.arguments.ChatArgument;
import dev.jorel.commandapi.arguments.ChatColorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.NativeResultingCommandExecutor;
import dev.jorel.commandapi.executors.ResultingCommandExecutor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CommandSign extends Base {
	public static ArgumentTree lineArgument(String nodeName) {
		return new IntegerArgument(nodeName, 1, 4)
			.replaceSuggestions((info, builder) -> builder.suggest(1).suggest(2).suggest(3).suggest(4).buildFuture());
	}

	public static boolean isSign(Location location) {
		return location.getBlock().getState() instanceof Sign;
	}

	public static int executeWithSign(CommandSender sender, Location location, Function<Sign, Integer> callback) {
		if (isSign(location)) {
			Sign sign = (Sign) location.getBlock().getState();
			return callback.apply(sign);
		}
		sender.sendMessage(ChatColor.RED + "Pas un panneau");
		return 0;
	}

	public static final Material[] signMaterials =
		Stream
			.of(Material.values())
			.filter(material -> material.data == org.bukkit.block.data.type.Sign.class)
			.toArray(Material[] ::new);

	public static String encodeTextColor(String text) {
		if (text == null) return null;
		if (text == "") return "";
		char altColorChar = '&';

		char[] b = text.toCharArray();
		int length = b.length;
		for (int i = 0; i < length - 1; i++) {
			if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
				b[i] = ChatColor.COLOR_CHAR;
				b[i + 1] = Character.toLowerCase(b[i + 1]);
			} else if (b[i] == altColorChar && b[i + 1] == altColorChar) {
				// Escaped
				for (int j = i + 1; j < b.length - 1; j++) {
					b[j] = b[j + 1];
				}
				length--;
			}
		}
		return new String(b).substring(0, length);
	}

	public static String decodeTextColor(String text) {
		if (text == null || text == "") return "";
		char altColorChar = '&';

		for (int i = 0; i < text.length() - 1; i++) {
			char c = text.charAt(i);
			char next = text.charAt(i + 1);
			if (c == ChatColor.COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(next) > -1) {
				text = text.substring(0, i) + altColorChar + next + text.substring(i + 2);
			} else if (c == altColorChar) {
				// Escaped => double
				text = text.substring(0, i) + altColorChar + text.substring(i);
				i++;
			}
		}
		return text;
	}

	public static String[] convertChatTextToSignText(String text) {
		String lines[] = new String[4];
		for (int i = 0; i < 4; i++) {
			lines[i] = "";
		}
		int line = 0;
		for (int i = 0; i < text.length() - 1; i++) {
			char c = text.charAt(i);
			char next = text.charAt(i + 1);
			if (c == '\\') {
				if (next == 'n') {
					line++;
					i++; // Skip the next character
				} else if (next == '\\') {
					lines[line] += '\\';
					i++; // Skip the next character
				} else {
					lines[line] += c;
				}
				if (line >= 4) {
					break;
				}
			} else {
				lines[line] += c;
			}
		}
		return lines;
	}

	public static String convertSignTextToChatText(String[] lines) {
		// Convert the color codes and escape all backslashes
		for (int i = 0; i < lines.length; i++) {
			lines[i] = decodeTextColor(lines[i]).replace("\\", "\\\\");
		}
		// Join the lines with \\n
		return String.join("\\n", lines);
	}

	public static ArgumentTree getSubCommand() {
		ArgumentTree lineEdit =
			new ChatArgument("text")
				.replaceSuggestions((info, builder) -> {
					Location location = (Location) info.previousArgs()[0];
					if (isSign(location)) {
						Sign sign = (Sign) location.getBlock().getState();
						int line = (int) info.previousArgs()[1];
						builder.suggest(decodeTextColor(sign.getLine(line - 1)));
					}

					return builder.buildFuture();
				})
				.executes((sender, args) -> {
					return executeWithSign(sender, (Location) args[0], sign -> {
						int line = (int) args[1];
						BaseComponent[] chat = (BaseComponent[]) args[2];
						String text = encodeTextColor(chat[0].toPlainText());
						sign.setLine(line - 1, text);
						sign.update();
						sender.sendMessage("Ligne " + line + " : " + text);
						return 1;
					});
				});

		ArgumentTree lineColor =
			new ChatColorArgument("color")
				.executes((sender, args) -> {
					return executeWithSign(sender, (Location) args[0], sign -> {
						int line = (int) args[1];
						org.bukkit.ChatColor color = (org.bukkit.ChatColor) args[2];
						String text = sign.getLine(line - 1);
						// Remove the color in the text
						text = ChatColor.stripColor(text);
						text = color + text;
						sign.setLine(line - 1, text);
						sign.update();
						sender.sendMessage("Ligne " + line + " : " + text);
						return 1;
					});
				});

		ResultingCommandExecutor lineClear = (sender, args) -> {
			return executeWithSign(sender, (Location) args[0], sign -> {
				int line = (int) args[1];
				sign.setLine(line - 1, "");
				sign.update();
				sender.sendMessage("Ligne effacée");
				return 1;
			});
		};

		NativeResultingCommandExecutor lineEditor = (proxy, args) -> {
			return executeWithSign(proxy, (Location) args[0], sign -> {
				if (proxy.getCallee() instanceof Player) {
					sendLineChatEditor((Player) proxy.getCallee(), sign, (int) args[1]);
					return 1;
				} else {
					proxy.sendMessage(ChatColor.RED + "Pas un joueur");
					return 0;
				}
			});
		};

		// Example: /minitools sign -79 69 253 set-text ab&&cde\n&cfghij\nk&3lm
		ArgumentTree signSetText =
			new ChatArgument("text")
				.replaceSuggestions((info, builder) -> {
					Location location = (Location) info.previousArgs()[0];
					if (isSign(location)) {
						Sign sign = (Sign) location.getBlock().getState();
						builder.suggest(convertSignTextToChatText(sign.getLines()));
					}
					return builder.buildFuture();
				})
				.executes((sender, args) -> {
					return executeWithSign(sender, (Location) args[0], sign -> {
						BaseComponent[] chat = (BaseComponent[]) args[1];

						// Convert to string
						String textComponent = chat[0].toPlainText();
						String[] lines = convertChatTextToSignText(textComponent);

						for (int i = 0; i < 4; i++) {
							if (i < lines.length) {
								sign.setLine(i, encodeTextColor(lines[i]));
							} else {
								sign.setLine(i, "");
							}
						}

						sign.update();
						sender.sendMessage("Texte du panneu mis à " + String.join("\n", lines));
						return 1;
					});
				});

		ArgumentTree signSetColor =
			new StringArgument("color")
				.replaceSuggestions((info, builder) -> {
					String current = (String) info.currentArg().toUpperCase();
					for (DyeColor color : DyeColor.values()) {
						if (color.name().startsWith(current)) {
							builder.suggest(color.name());
						}
					}
					return builder.buildFuture();
				})
				.executes((sender, args) -> {
					return executeWithSign(sender, (Location) args[0], sign -> {
						String colorName = (String) args[1];
						DyeColor color;
						try {
							color = DyeColor.valueOf(colorName.toUpperCase());
						} catch (IllegalArgumentException e) {
							sender.sendMessage(ChatColor.RED + "Couleur invalide");
							return 0;
						}
						sign.setColor(color);
						sign.update();
						sender.sendMessage("Couleur du panneau mise à " + color);
						return 1;
					});
				});

		ArgumentTree signSetWood =
			new StringArgument("wood")
				.replaceSuggestions((info, builder) -> {
					String current = (String) info.currentArg().toUpperCase();
					for (Material material : signMaterials) {
						String woodType = material.name().replace("_SIGN", "");
						if (woodType.startsWith(current)) {
							builder.suggest(woodType);
						}
					}
					return builder.buildFuture();
				})
				.executes((sender, args) -> {
					return executeWithSign(sender, (Location) args[0], sign -> {
						String woodType = (String) args[1];
						Block block = sign.getBlock();
						BlockData blockData = block.getBlockData();

						String lines[] = sign.getLines();

						if (blockData instanceof WallSign) {
							Material material = Material.getMaterial(woodType + "_WALL_SIGN");
							if (material == null) {
								sender.sendMessage(ChatColor.RED + "Type de bois invalide");
								return 0;
							}

							WallSign wallSignData = (WallSign) blockData;
							block.setType(material);
							WallSign newWallSignData = (WallSign) block.getBlockData();
							newWallSignData.setFacing(wallSignData.getFacing());
							block.setBlockData(newWallSignData);

						} else {
							Material material = Material.getMaterial(woodType + "_SIGN");
							if (material == null) {
								sender.sendMessage(ChatColor.RED + "Type de bois invalide");
								return 0;
							}

							org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) blockData;
							block.setType(material);
							org.bukkit.block.data.type.Sign newSignData = (org.bukkit.block.data.type.Sign) block.getBlockData();
							newSignData.setRotation(signData.getRotation());
							block.setBlockData(newSignData);
						}

						sign = (Sign) block.getState();
						for (int i = 0; i < lines.length; i++) {
							sign.setLine(i, lines[i]);
						}
						sign.update();
						sender.sendMessage("Type de bois du panneau changé pour " + woodType);
						return 1;
					});
				});

		NativeResultingCommandExecutor signEditor = (proxy, args) -> {
			if (!(proxy.getCallee() instanceof Player)) {
				proxy.sendMessage(ChatColor.RED + "Pas un joueur");
				return 0;
			}
			return executeWithSign(proxy, (Location) args[0], sign -> {
				Player puppet = (Player) proxy.getCallee();
				sendSignChatEditor(puppet, sign);
				return 1;
			});
		};

		return literal("sign")
			.then(new LocationArgument("location", LocationType.BLOCK_POSITION)
					.then(literal("set-text").then(signSetText))
					.then(literal("set-color").then(signSetColor))
					.then(literal("set-wood").then(signSetWood))
					.then(literal("line")
							.then(lineArgument("line")
									.then(literal("set").then(lineEdit))
									.then(literal("color").then(lineColor))
									.then(literal("clear").executes(lineClear))
									.then(literal("editor").executesNative(lineEditor))
									.executesNative(lineEditor)))
					.then(literal("editor").executesNative(signEditor))
					.executesNative(signEditor));
	}

	public static void sendLineChatEditor(Player player, Sign sign, int line) {
		// Afficher la ligne et des boutons pour la modifier
		// Boutons : edit, color, clear, done
		String signCommand = "/minitools sign " + sign.getX() + " " + sign.getY() + " " + sign.getZ();
		ComponentBuilder builder =
			new ComponentBuilder(ChatColor.GRAY + "Ligne " + line + " : " + ChatColor.RESET + sign.getLine(line - 1))
				.append(new TextComponent("\n "))
				.append(
					new ComponentBuilder("[Éditer]")
						.color(ChatColor.GOLD)
						.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, signCommand + " line " + line + " set " + decodeTextColor(sign.getLine(line - 1))))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Éditer le ligne " + line)))
						.create())
				.append(new TextComponent(" "))
				.append(
					new ComponentBuilder("[Colorer]")
						.color(ChatColor.YELLOW)
						.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, signCommand + " line " + line + " color "))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Colorer la ligne " + line)))
						.create())
				.append(new TextComponent(" "))
				.append(
					new ComponentBuilder("[Effacer]")
						.color(ChatColor.RED)
						.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, signCommand + " line " + line + " clear"))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Effacer la ligne " + line)))
						.create())
				.append(new TextComponent(" "))
				.append(
					new ComponentBuilder("[Terminé]")
						.color(ChatColor.GREEN)
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, signCommand + " editor"))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Voir le résultat")))
						.create());
		player.spigot().sendMessage(builder.create());
	}

	public static void sendSignChatEditor(Player player, Sign sign) {
		// Afficher les lignes pour les éditer
		String signCommand = "/minitools sign " + sign.getX() + " " + sign.getY() + " " + sign.getZ();
		ComponentBuilder builder = new ComponentBuilder("Édition du panneau en " + sign.getX() + " " + sign.getY() + " " + sign.getZ() + " (" + sign.getWorld().getName() + ")");
		for (int line = 1; line <= 4; line++) {
			builder
				.append(new TextComponent("\n"))
				.append(
					new ComponentBuilder(ChatColor.GRAY + "Ligne " + line + " : " + ChatColor.RESET + sign.getLine(line - 1))
						.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, signCommand + " line " + line + " editor"))
						.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Cliquez pour éditer la ligne " + line)))
						.create());
		}
		// Boutons : Color, Wood, Copy
		String signText = convertSignTextToChatText(sign.getLines());
		builder
			.append(new TextComponent("\n "))
			.append(
				new ComponentBuilder("[Couleur]")
					.color(ChatColor.YELLOW)
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, signCommand + " set-color "))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Changer la couleur par défaut")))
					.create())
			.append(new TextComponent(" "))
			.append(
				new ComponentBuilder("[Bois]")
					.color(ChatColor.GOLD)
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, signCommand + " set-wood "))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Changer le type de bois")))
					.create())
			.append(new TextComponent(" "))
			.append(
				new ComponentBuilder("[Copier]")
					.color(ChatColor.GREEN)
					.event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, signText))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Copier le texte du panneau")))
					.create());
		player.spigot().sendMessage(builder.create());
	}
}
