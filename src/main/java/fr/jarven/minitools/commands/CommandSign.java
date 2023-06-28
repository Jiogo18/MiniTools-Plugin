package fr.jarven.minitools.commands;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ChatArgument;
import dev.jorel.commandapi.arguments.ChatColorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.NativeResultingCommandExecutor;
import dev.jorel.commandapi.executors.ResultingCommandExecutor;
import fr.jarven.minitools.commands.arguments.DyeColorArgument;
import fr.jarven.minitools.commands.arguments.SignBlockArgument;
import fr.jarven.minitools.commands.arguments.SignWoodArgument;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CommandSign extends Base {
	public static Argument<Integer> lineArgument(String nodeName) {
		return new IntegerArgument(nodeName, 1, 4)
			.replaceSuggestions((info, builder) -> builder.suggest(1).suggest(2).suggest(3).suggest(4).buildFuture());
	}

	public static String encodeTextColor(String text) {
		if (text == null) return null;
		if (text.isEmpty()) return "";
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
		if (text == null || text.isEmpty()) return "";
		char altColorChar = '&';
		boolean skipNext = false;

		for (int i = 0; i < text.length() - 1; i++) {
			if (skipNext) {
				skipNext = false;
				continue;
			}
			char c = text.charAt(i);
			char next = text.charAt(i + 1);
			if (c == ChatColor.COLOR_CHAR && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(next) > -1) {
				text = text.substring(0, i) + altColorChar + next + text.substring(i + 2);
			} else if (c == altColorChar) {
				// Escaped => double
				text = text.substring(0, i) + altColorChar + text.substring(i);
				skipNext = true;
			}
		}
		return text;
	}

	public static String[] convertChatTextToSignText(String text) {
		String[] lines = new String[4];
		for (int i = 0; i < 4; i++) {
			lines[i] = "";
		}
		int line = 0;
		boolean skipNext = false;
		for (int i = 0; i < text.length() - 1; i++) {
			if (skipNext) {
				skipNext = false;
				continue;
			}
			char c = text.charAt(i);
			char next = text.charAt(i + 1);
			if (c == '\\') {
				if (next == 'n') {
					line++;
					skipNext = true;
				} else if (next == '\\') {
					lines[line] += '\\';
					skipNext = true;
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

	private static Sign extractSign(CommandArguments args) {
		return (Sign) args.get("location");
	}

	public Argument<String> getSubCommand() {
		ChatArgument lineEdit =
			(ChatArgument) new ChatArgument("text")
				.replaceSuggestions((info, builder) -> {
					Sign sign = extractSign(info.previousArgs());
					int line = (int) info.previousArgs().get("line");
					builder.suggest(decodeTextColor(sign.getLine(line - 1)));
					return builder.buildFuture();
				})
				.executes((sender, args) -> {
					Sign sign = extractSign(args);
					int line = (int) args.get("line");
					BaseComponent[] chat = (BaseComponent[]) args.get("text");
					String text = encodeTextColor(chat[0].toPlainText());
					sign.setLine(line - 1, text);
					sign.update();
					sender.sendMessage("Ligne " + line + " : " + text);
					return 1;
				});

		ChatColorArgument lineColor =
			(ChatColorArgument) new ChatColorArgument("color")
				.executes((sender, args) -> {
					Sign sign = extractSign(args);
					int line = (int) args.get("line");
					org.bukkit.ChatColor color = (org.bukkit.ChatColor) args.get("color");
					String text = sign.getLine(line - 1);
					// Remove the color in the text
					text = ChatColor.stripColor(text);
					text = color + text;
					sign.setLine(line - 1, text);
					sign.update();
					sender.sendMessage("Ligne " + line + " : " + text);
					return 1;
				});

		ResultingCommandExecutor lineClear = (sender, args) -> {
			Sign sign = extractSign(args);
			int line = (int) args.get("line");
			sign.setLine(line - 1, "");
			sign.update();
			sender.sendMessage("Ligne effacée");
			return 1;
		};

		NativeResultingCommandExecutor lineEditor = (proxy, args) -> {
			Sign sign = extractSign(args);
			if (proxy.getCallee() instanceof Player) {
				sendLineChatEditor((Player) proxy.getCallee(), sign, (int) args.get("line"));
				return 1;
			} else {
				proxy.sendMessage(ChatColor.RED + "Pas un joueur");
				return 0;
			}
		};

		// Example: /minitools sign -79 69 253 set-text ab&&cde\n&cfghij\nk&3lm
		ChatArgument signSetText =
			(ChatArgument) new ChatArgument("text")
				.replaceSuggestions((info, builder) -> {
					Sign sign = extractSign(info.previousArgs());
					builder.suggest(convertSignTextToChatText(sign.getLines()));
					return builder.buildFuture();
				})
				.executes((sender, args) -> {
					Sign sign = extractSign(args);
					BaseComponent[] chat = (BaseComponent[]) args.get("text");

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

		Argument<DyeColor> signSetColor =
			new DyeColorArgument("color")
				.executes((sender, args) -> {
					Sign sign = extractSign(args);
					DyeColor color = (DyeColor) args.get("color");
					sign.setColor(color);
					sign.update();
					sender.sendMessage("Couleur du panneau mise à " + color);
					return 1;
				});

		Argument<Material> signSetWood =
			new SignWoodArgument("wood")
				.executes((sender, args) -> {
					Material material;
					Sign sign = extractSign(args);
					Block block = sign.getBlock();
					BlockData blockData = block.getBlockData();
					boolean wallSign = blockData instanceof WallSign;

					String[] lines = sign.getLines();
					DyeColor color = sign.getColor();

					material = SignWoodArgument.getMaterial(args, "wood", wallSign);
					block.setType(material);

					// Restore the sign data
					if (wallSign) {
						WallSign wallSignData = (WallSign) blockData;
						WallSign newWallSignData = (WallSign) block.getBlockData();
						newWallSignData.setFacing(wallSignData.getFacing());
						block.setBlockData(newWallSignData);
					} else {
						org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) blockData;
						org.bukkit.block.data.type.Sign newSignData = (org.bukkit.block.data.type.Sign) block.getBlockData();
						newSignData.setRotation(signData.getRotation());
						block.setBlockData(newSignData);
					}

					// Restore the text
					sign = (Sign) block.getState();
					for (int i = 0; i < lines.length; i++) {
						sign.setLine(i, lines[i]);
					}
					sign.setColor(color);

					sign.update();
					sender.sendMessage("Type de bois du panneau changé pour " + material);
					return 1;
				});

		NativeResultingCommandExecutor signEditor = (proxy, args) -> {
			Sign sign = extractSign(args);
			if (!(proxy.getCallee() instanceof Player)) {
				proxy.sendMessage(ChatColor.RED + "Pas un joueur");
				return 0;
			}
			Player puppet = (Player) proxy.getCallee();
			sendSignChatEditor(puppet, sign);
			return 1;
		};

		return literal("sign")
			.then(new SignBlockArgument("location")
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
