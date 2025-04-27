package com.buuz135.hotornot;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.concurrent.CompletableFuture;

public class DataGenerators {
    public static final class Languages extends LanguageProvider {
        private final String locale;
        public Languages(PackOutput output, String locale) {
            super(output, HotOrNot.MOD_ID, locale);
            this.locale = locale;
        }

        @Override
        protected void addTranslations() {
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

        public static final ResourceLocation GENERATED = ResourceLocation.withDefaultNamespace("item/generated");

        public ItemModels(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
            super(output, modid, existingFileHelper);
        }

        @Override
        protected void registerModels() {
            for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item != null && HotOrNot.MOD_ID.equals(id.getNamespace())) {
                    if (!(item instanceof BlockItem)) {
                        this.defaultItem(id);
                    }
                }
            }
        }

        private void defaultItem(ResourceLocation id) {
            this.withExistingParent(id.getPath(), GENERATED).texture("layer0",
                    ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "item/" + id.getPath()));
        }
    }

    public static final class Recipes extends RecipeProvider {

        public Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void buildRecipes(RecipeOutput recipeOutput) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HotOrNot.MITTS.get())
                    .pattern(" w ").pattern("wlw").pattern("iw ")
                    .define('l', Ingredient.of(Tags.Items.LEATHERS))
                    .define('i', Ingredient.of(Tags.Items.INGOTS_IRON))
                    .define('w', Ingredient.of(ItemTags.WOOL))
                    .unlockedBy("has_item", has(Tags.Items.LEATHERS)).save(recipeOutput);
        }
    }
}
