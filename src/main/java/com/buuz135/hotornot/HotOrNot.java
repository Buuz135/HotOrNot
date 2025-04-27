package com.buuz135.hotornot;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mod(HotOrNot.MOD_ID)
public class HotOrNot {
    public static final String MOD_ID = "hotornot";
    public static final String NAME = "HotOrNot";
    public static final String COMMON_CONFIG_NAME = "hotornot.toml";
    public static final Logger LOGGER = LogManager.getLogger();

    // HashSet Lists
    public static final Set<Item> blacklist = new HashSet<>();
    public static final Set<Item> hotWhitelist = new HashSet<>();
    public static final Set<Item> coldWhitelist = new HashSet<>();
    public static final Set<Item> gaseousWhitelist = new HashSet<>();
    public static final Set<Item> mittsItemList = new HashSet<>();

    public HotOrNot(IEventBus modEventBus, ModContainer container) {
        LOGGER.debug("Loading up " + NAME);
        container.registerConfig(ModConfig.Type.COMMON, HotOrNotConfig.COMMON_SPEC, COMMON_CONFIG_NAME);
        modEventBus.addListener(this::onBuildTabContents);
        modEventBus.addListener(this::gatherData);
        NeoForge.EVENT_BUS.register(this);
        ITEMS.register(modEventBus);
    }

    public static final String TOOLTIP_TOO_HOT = "tooltip.hotornot.toohot";
    public static final String TOOLTIP_TOO_COLD = "tooltip.hotornot.toocold";
    public static final String TOOLTIP_TOO_LIGHT = "tooltip.hotornot.toolight";
    public static final String TOOLTIP_MITTS = "tooltip.hotornot.mitts";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredItem<MittItem> MITTS = ITEMS.register("mitts", () -> new MittItem(new Item.Properties()
            .stacksTo(1).durability(12000)));

    private void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey().equals(CreativeModeTabs.SEARCH)) {
            event.accept(MITTS.get());
        }
    }

    public void gatherData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        if (event.includeServer()) {
            generator.addProvider(true, new DataGenerators.Recipes(output, lookupProvider));
        }
        if (event.includeClient()) {
            generator.addProvider(true, new DataGenerators.Languages(output, "en_us"));
            generator.addProvider(true, new DataGenerators.Languages(output, "de_de"));
            generator.addProvider(true, new DataGenerators.ItemModels(output, MOD_ID, existingFileHelper));
        }
    }

    @SubscribeEvent
    public void onTick(final LevelTickEvent.Pre event) {
        if (!event.getLevel().isClientSide) {
            for (Player player : event.getLevel().players()) {
                if (player instanceof ServerPlayer) {
                    if (!player.isOnFire() && !player.isCreative()
                            && !player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                        IItemHandler itemHandler = player.getCapability(Capabilities.ItemHandler.ENTITY);
                        if (itemHandler != null) {
                            for (int i = 0; i < itemHandler.getSlots(); i++) {
                                ItemStack stack = itemHandler.getStackInSlot(i);
                                if (!stack.isEmpty()) {
                                    IFluidHandler fluidHandlerItem = stack.getCapability(Capabilities.FluidHandler.ITEM);
                                    if (fluidHandlerItem != null) {
                                        if (blacklist.contains(stack.getItem()))
                                            return;
                                        FluidStack fluidStack = fluidHandlerItem.drain(1000,
                                                IFluidHandler.FluidAction.SIMULATE);

                                        if (fluidStack != FluidStack.EMPTY) {
                                            for (FluidEffect effect : FluidEffect.values()) {
                                                if (effect.isValid.test(fluidStack)) {
                                                    applyEffectAndDamageMitts(player, effect, event);
                                                }
                                            }
                                        }
                                    } else {
                                        if (coldWhitelist.contains(stack.getItem())) {
                                            applyEffectAndDamageMitts(player, FluidEffect.COLD, event);
                                        }
                                        if (gaseousWhitelist.contains(stack.getItem())
                                                && HotOrNotConfig.COMMON.GASEOUS.get()) {
                                            applyEffectAndDamageMitts(player, FluidEffect.GAS, event);
                                        }
                                        if (hotWhitelist.contains(stack.getItem())) {
                                            applyEffectAndDamageMitts(player, FluidEffect.HOT, event);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void applyEffectAndDamageMitts(Player player, FluidEffect effect, LevelTickEvent event) {
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem().equals(MITTS.get()) || mittsItemList.contains(offHand.getItem())) {
            offHand.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        } else if (event.getLevel().getGameTime() % 20 == 0) {
            effect.interactPlayer.accept(player);
        }
    }

    public enum FluidEffect {
        HOT(fluidStack -> fluidStack.getFluid().getFluidType()
                .getTemperature(fluidStack) >= HotOrNotConfig.COMMON.HOT_TEMPERATURE.get(),
                entityPlayerMP -> entityPlayerMP.igniteForSeconds(1),
                Component.translatable(TOOLTIP_TOO_HOT).withStyle(ChatFormatting.RED)),
        COLD(fluidStack -> fluidStack.getFluid().getFluidType()
                .getTemperature(fluidStack) <= HotOrNotConfig.COMMON.COLD_TEMPERATURE.get(), entityPlayerMP -> {
            entityPlayerMP.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 21, 1));
            entityPlayerMP.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 21, 1));
        }, Component.translatable(TOOLTIP_TOO_COLD).withStyle(ChatFormatting.AQUA)),

        GAS(fluidStack -> fluidStack.getFluid().is(Tags.Fluids.GASEOUS)
                && HotOrNotConfig.COMMON.GASEOUS.get(),
                entityPlayerMP -> entityPlayerMP.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 21, 1)),
                Component.translatable(TOOLTIP_TOO_LIGHT).withStyle(ChatFormatting.YELLOW));

        private final Predicate<FluidStack> isValid;
        private final Consumer<Player> interactPlayer;
        private final Component tooltip;

        FluidEffect(Predicate<FluidStack> isValid, Consumer<Player> interactPlayer,
                    Component component) {
            this.isValid = isValid;
            this.interactPlayer = interactPlayer;
            this.tooltip = component;
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onTooltip(final ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (HotOrNotConfig.COMMON.TOOLTIP.get() && !stack.isEmpty()) {
            if (stack.getItem() == HotOrNot.MITTS.get() || mittsItemList.contains(stack.getItem())) {
                event.getToolTip().add(Component.translatable(TOOLTIP_MITTS).withStyle(ChatFormatting.GREEN));
            }
            if (blacklist.contains(stack.getItem()))
                return;
            IFluidHandler fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (fluidHandler != null) {
                FluidStack fluidStack = fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                if (fluidStack != FluidStack.EMPTY) {
                    for (FluidEffect effect : FluidEffect.values()) {
                        if (effect.isValid.test(fluidStack)) {
                            event.getToolTip().add(effect.tooltip);
                        }
                    }
                }
            } else {
                if (coldWhitelist.contains(stack.getItem())) {
                    event.getToolTip().add(FluidEffect.COLD.tooltip);
                }
                if (gaseousWhitelist.contains(stack.getItem()) && HotOrNotConfig.COMMON.GASEOUS.get()) {
                    event.getToolTip().add(FluidEffect.GAS.tooltip);
                }
                if (hotWhitelist.contains(stack.getItem())) {
                    event.getToolTip().add(FluidEffect.HOT.tooltip);
                }
            }
        }
    }
}
