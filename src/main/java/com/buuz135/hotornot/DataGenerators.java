package com.buuz135.hotornot;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;

public class DataGenerators {
	public static final class Languages extends LanguageProvider {
		public Languages(DataGenerator gen, String modid, String locale) {
			super(gen, HotOrNot.MOD_ID, locale);
		}

		@Override
		protected void addTranslations() {
			String locale = this.getName().replace("Languages: ", "");
			switch (locale) {
			case "de_de":
				add("_comment", "Translation (de_de) by Affehund");
				add(HotOrNot.MITTS.get(), "Handschuhe");
				add(HotOrNot.TOOLTIP_TOO_COLD, "Zu kalt zum Anfassen!");
				add(HotOrNot.TOOLTIP_TOO_HOT, "Zu heiﬂ zum Anfassen!");
				add(HotOrNot.TOOLTIP_TOO_LIGHT, "Zu leicht zum Anfassen!");
				add(HotOrNot.TOOLTIP_MITTS, "In die Zweithand nehmen um schlechte Effekte zu vermeiden.");
				break;
			case "en_us":
				add("_comment", "Translation (en_us) by Affehund");
				add(HotOrNot.MITTS.get(), "Mitts");
				add(HotOrNot.TOOLTIP_TOO_COLD, "Too cold to handle!");
				add(HotOrNot.TOOLTIP_TOO_HOT, "Too hot to handle!");
				add(HotOrNot.TOOLTIP_TOO_LIGHT, "Too light to handle!");
				add(HotOrNot.TOOLTIP_MITTS, "Wear in the offhand to avoid bad effects.");
			}
		}
	}

	public static final class ItemModels extends ItemModelProvider {

		public static final ResourceLocation GENERATED = new ResourceLocation("item/generated");

		public ItemModels(DataGenerator gen, String modid, ExistingFileHelper existingFileHelper) {
			super(gen, modid, existingFileHelper);
		}

		@Override
		protected void registerModels() {
			for (ResourceLocation id : ForgeRegistries.ITEMS.getKeys()) {
				Item item = ForgeRegistries.ITEMS.getValue(id);
				if (item != null && HotOrNot.MOD_ID.equals(id.getNamespace())) {
					if (!(item instanceof BlockItem)) {
						this.defaultItem(id, item);
					}
				}
			}
		}

		protected void defaultItem(ResourceLocation id, Item item) {
			this.withExistingParent(id.getPath(), GENERATED).texture("layer0",
					new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
		}
	}

	public static final class Recipes extends RecipeProvider implements IConditionBuilder {

		public Recipes(DataGenerator gen) {
			super(gen);
		}

		@Override
		protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
			ShapedRecipeBuilder.shapedRecipe(HotOrNot.MITTS.get())
			.patternLine(" w ").patternLine("wlw").patternLine("iw ")
					.key('l', Ingredient.fromTag(Tags.Items.LEATHER))
					.key('i', Ingredient.fromTag(Tags.Items.INGOTS_IRON))
					.key('w', Ingredient.fromItems(Items.BLACK_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.CYAN_WOOL,
									Items.GRAY_WOOL, Items.GREEN_WOOL, Items.LIGHT_BLUE_WOOL, Items.LIGHT_GRAY_WOOL,
									Items.LIME_WOOL, Items.MAGENTA_WOOL, Items.ORANGE_WOOL, Items.PINK_WOOL,
									Items.PURPLE_WOOL, Items.RED_WOOL, Items.WHITE_WOOL, Items.YELLOW_WOOL))
					.addCriterion("has_item", hasItem(Tags.Items.LEATHER)).build(consumer);
		}
	}
}
