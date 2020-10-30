package com.buuz135.hotornot;

import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = HotOrNot.MOD_ID, bus = Bus.MOD)
public class HotOrNotConfig {
	public static class Common {
		
		public final IntValue MITTS_DURABILITY;

		public final IntValue HOT_TEMPERATURE;
		public final IntValue COLD_TEMPERATURE;
		public final BooleanValue GASEOUS;
		public final BooleanValue TOOLTIP;

		public final ConfigValue<ArrayList<String>> GLOVE_ITEMS;
		public final ConfigValue<ArrayList<String>> BLACKLISTED_ITEMS;
		public final ConfigValue<ArrayList<String>> COLD_WHITELISTED_ITEMS;
		public final ConfigValue<ArrayList<String>> GASEOUS_WHITELISTED_ITEMS;
		public final ConfigValue<ArrayList<String>> HOT_WHITELISTED_ITEMS;


		public final ArrayList<String> defaultgloveItems = new ArrayList<String>();
		public final ArrayList<String> defaultBlacklist = new ArrayList<String>();
		public final ArrayList<String> defaultColdWhitelist = new ArrayList<String>();
		public final ArrayList<String> defaultGaseousWhitelist = new ArrayList<String>();
		public final ArrayList<String> defaultHotWhitelist = new ArrayList<String>();


		private void addToStringArrayList(ArrayList<String> list, Item item) {
			list.add(item.getRegistryName().toString());
		}


		public Common(ForgeConfigSpec.Builder builder) {
			addToStringArrayList(defaultBlacklist, Items.BUCKET);
			
			builder.comment("HotOrNot Config").push("general");
			
			MITTS_DURABILITY = builder.comment(
					"This sets the maximum durability for the mitts.")
					.defineInRange("mitts_durability", 20 * 60 * 10, 1, Integer.MAX_VALUE);

			HOT_TEMPERATURE = builder.comment(
					"This sets the temperature when a hot fluid should start burning the player (in kelvin).")
					.defineInRange("hot_temperature", 1300, 1, Integer.MAX_VALUE);

			COLD_TEMPERATURE = builder.comment(
					"This sets the temperature when a cold fluid should add effects to the player (in kelvin).")
					.defineInRange("cold_temperature", 273, 1, Integer.MAX_VALUE);

			GASEOUS = builder.comment("This sets whether gaseos effect for a fluid should be enabled.")
					.define("gaseous", false);

			TOOLTIP = builder.comment(
					"This sets whether an item that contains a fluid will have a tooltip that they are gaseous, too hot or too cold.")
					.define("tooltips", true);
			
			GLOVE_ITEMS = builder.comment(
					"This sets an item as a glove item. It prevents all effects.")
					.define("glove_items", defaultgloveItems);

			BLACKLISTED_ITEMS = builder.comment(
					"This sets an item on a fluid blacklist. It won't be affected.")
					.define("blacklisted_items", defaultBlacklist);
			
			COLD_WHITELISTED_ITEMS = builder.comment(
					"This sets an items on a cold fluid whitelist. It will have the cold fluid effect.")
					.define("cold_whitelisted_items", defaultColdWhitelist);
			
			GASEOUS_WHITELISTED_ITEMS = builder.comment(
					"This sets an item on a gaseous fluid whitelist. It will have the gaseous fluid effect.")
					.define("gaseous_whitelisted_items", defaultGaseousWhitelist);

			HOT_WHITELISTED_ITEMS = builder.comment(
					"This sets an item on a hot fluid whitelist. It will have the hot fluid effect.")
					.define("hot_whitelisted_items", defaultHotWhitelist);

			builder.pop();
		}
	}

	public static final ForgeConfigSpec COMMON_SPEC;
	public static final Common COMMON;
	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading event) {
		HotOrNot.LOGGER.info("Loaded {} config file {}", HotOrNot.MOD_ID, event.getConfig().getFileName());
		loopArrayList(HotOrNotConfig.COMMON.GLOVE_ITEMS.get(), HotOrNot.gloveItemList);
		loopArrayList(HotOrNotConfig.COMMON.BLACKLISTED_ITEMS.get(), HotOrNot.blacklist);
		loopArrayList(HotOrNotConfig.COMMON.COLD_WHITELISTED_ITEMS.get(), HotOrNot.coldWhitelist);
		loopArrayList(HotOrNotConfig.COMMON.GASEOUS_WHITELISTED_ITEMS.get(), HotOrNot.gaseousWhitelist);
		loopArrayList(HotOrNotConfig.COMMON.HOT_WHITELISTED_ITEMS.get(), HotOrNot.hotWhitelist);
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.Reloading event) {
		HotOrNot.LOGGER.debug(HotOrNot.MOD_ID, "Config just got changed on the file system!");
	}
	
	public static void loopArrayList(ArrayList<String> list, Set<Item> set) {
		for (String string : list) {
			set.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(string)));
		}
	}
}
