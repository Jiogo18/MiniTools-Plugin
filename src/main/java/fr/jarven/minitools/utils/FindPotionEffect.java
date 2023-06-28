package fr.jarven.minitools.utils;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import fr.jarven.minitools.Main;

public class FindPotionEffect {
	private FindPotionEffect() {}

	public static PotionEffectType findType(Object object) {
		@SuppressWarnings("deprecation")
		PotionEffectType type = object instanceof Integer ? PotionEffectType.getById((Integer) object) : null;
		if (type != null) return type; // Found it with type id

		if (object instanceof String) {
			String string = (String) object;

			@SuppressWarnings("deprecation")
			PotionEffectType type2 = Integer.getInteger(string) != null ? PotionEffectType.getById(Integer.getInteger(string)) : null;
			if (type2 != null) return type2; // Found it with type id
			type = PotionEffectType.getByName(string);
			if (type != null) return type; // Found it in PotionEffectType (i.e. DAMAGE_RESISTANCE)

			// Not found, try with MobEffects (not in Spigot-api)
			switch (string.toLowerCase()) {
				case "faster_movement":
					return PotionEffectType.SPEED;
				case "slowness":
				case "slower_movement":
					return PotionEffectType.SLOW;
				case "haste":
				case "faster_digging":
					return PotionEffectType.FAST_DIGGING;
				case "mining_fatigue":
				case "slower_digging":
					return PotionEffectType.SLOW_DIGGING;
				case "strength":
					return PotionEffectType.INCREASE_DAMAGE;
				case "instant_health":
					return PotionEffectType.HEAL;
				case "instant_damage":
					return PotionEffectType.HARM;
				case "jump_boost":
					return PotionEffectType.JUMP;
				case "nausea":
					return PotionEffectType.CONFUSION;
				case "resistance":
					return PotionEffectType.DAMAGE_RESISTANCE;
				case "absorbtion": // absorPtion and absorBtion...
					return PotionEffectType.ABSORPTION;
			}
		}

		return null;
	}

	public static PotionEffect loadPotionEffect(Object object) {
		if (object instanceof PotionEffect) {
			return (PotionEffect) object;
		} else if (object instanceof LinkedHashMap) {
			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) object;

			PotionEffectType type = findType(map.get("type"));
			if (type == null) {
				Main.LOGGER.warning("[Give] Invalid potion effect type: " + map.get("type"));
				return null;
			}
			int duration = (int) map.getOrDefault("duration", 1);
			int amplifier = (int) map.getOrDefault("amplifier", 0);
			return new PotionEffect(type, duration, amplifier);
		} else {
			Main.LOGGER.warning("[Give] Invalid potion effect: " + object.getClass() + " " + object);
			return null;
		}
	}

	public static List<PotionEffect> loadPotionEffects(Object objectEffects) {
		List<PotionEffect> effects = new ArrayList<>();
		if (objectEffects instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) objectEffects;
			for (Object effectMap : list) {
				PotionEffect effect = loadPotionEffect(effectMap);
				if (effect != null) effects.add(effect);
			}
		}
		return effects;
	}
}
