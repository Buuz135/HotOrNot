package com.buuz135.hotornot;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class DataGenerators {
    public static final class Languages extends LanguageProvider {
        public Languages(PackOutput output, String locale) {
            super(output, HotOrNot.MOD_ID, locale);
        }

        @Override
        protected void addTranslations() {
            String locale = this.getName().replace("Languages: ", "");
            switch (locale) {
                case "de_de" -> {
                    add("_comment", "Translation (de_de) by Affehund");
                    add(HotOrNot.MITTS.get(), "Handschuhe");
                    add(HotOrNot.TOOLTIP_TOO_COLD, "Zu kalt zum Anfassen!");
                    add(HotOrNot.TOOLTIP_TOO_HOT, "Zu heiß zum Anfassen!");
                    add(HotOrNot.TOOLTIP_TOO_LIGHT, "Zu leicht zum Anfassen!");
                    add(HotOrNot.TOOLTIP_MITTS, "In die Zweithand nehmen um schlechte Effekte zu vermeiden.");
                }
                case "en_us" -> {
                    add("_comment", "Translation (en_us) by Affehund");
                    add(HotOrNot.MITTS.get(), "Mitts");
                    add(HotOrNot.TOOLTIP_TOO_COLD, "Too cold to handle!");
                    add(HotOrNot.TOOLTIP_TOO_HOT, "Too hot to handle!");
                    add(HotOrNot.TOOLTIP_TOO_LIGHT, "Too light to handle!");
                    add(HotOrNot.TOOLTIP_MITTS, "Wear in the offhand to avoid bad effects.");
                }
            }
        }
    }

    public static final class ItemModels extends ItemModelProvider {

        public static final ResourceLocation GENERATED = new ResourceLocation("item/generated");

        public ItemModels(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            for (ResourceLocation id : ForgeRegistries.ITEMS.getKeys()) {
                Item item = ForgeRegistries.ITEMS.getValue(id);
                if (item != null && HotOrNot.MOD_ID.equals(id.getNamespace())) {
                    if (!(item instanceof BlockItem)) {
                        this.defaultItem(id);
                    }
                }
            }
        }

        private void defaultItem(ResourceLocation id) {
            this.withExistingParent(id.getPath(), GENERATED).texture("layer0",
                    new ResourceLocation(id.getNamespace(), "item/" + id.getPath()));
        }
    }

    public static final class Recipes extends RecipeProvider implements IConditionBuilder {

        public Recipes(PackOutput output) {
            super(output);
        }

        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HotOrNot.MITTS.get())
                    .pattern(" w ").pattern("wlw").pattern("iw ")
                    .define('l', Ingredient.of(Tags.Items.LEATHER))
                    .define('i', Ingredient.of(Tags.Items.INGOTS_IRON))
                    .define('w', Ingredient.of(Items.BLACK_WOOL, Items.BLUE_WOOL, Items.BROWN_WOOL, Items.CYAN_WOOL,
                            Items.GRAY_WOOL, Items.GREEN_WOOL, Items.LIGHT_BLUE_WOOL, Items.LIGHT_GRAY_WOOL,
                            Items.LIME_WOOL, Items.MAGENTA_WOOL, Items.ORANGE_WOOL, Items.PINK_WOOL,
                            Items.PURPLE_WOOL, Items.RED_WOOL, Items.WHITE_WOOL, Items.YELLOW_WOOL))
                    .unlockedBy("has_item", has(Tags.Items.LEATHER)).save(consumer);
        }
    }
}
