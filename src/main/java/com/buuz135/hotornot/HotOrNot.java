package com.buuz135.hotornot;

import net.minecraft.ChatFormatting;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mod(HotOrNot.MOD_ID)
public class HotOrNot {
    public static final String MOD_ID = "hotornot";
    public static final String NAME = "HotOrNot";
    public static final String COMMON_CONFIG_NAME = "hotornot.toml";
    public static final Logger LOGGER = LogManager.getLogger();
    final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

    // HashSet Lists
    public static final Set<Item> blacklist = new HashSet<>();
    public static final Set<Item> hotWhitelist = new HashSet<>();
    public static final Set<Item> coldWhitelist = new HashSet<>();
    public static final Set<Item> gaseousWhitelist = new HashSet<>();
    public static final Set<Item> mittsItemList = new HashSet<>();

    public HotOrNot() {
        LOGGER.debug("Loading up " + NAME);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HotOrNotConfig.COMMON_SPEC, COMMON_CONFIG_NAME);
        modEventBus.addListener(this::gatherData);
        MinecraftForge.EVENT_BUS.register(this);
        ITEMS.register(modEventBus);
    }

    public static final String TOOLTIP_TOO_HOT = "tooltip.hotornot.toohot";
    public static final String TOOLTIP_TOO_COLD = "tooltip.hotornot.toocold";
    public static final String TOOLTIP_TOO_LIGHT = "tooltip.hotornot.toolight";
    public static final String TOOLTIP_MITTS = "tooltip.hotornot.mitts";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final RegistryObject<Item> MITTS = ITEMS.register("mitts", () -> new Item(new Item.Properties()
            .stacksTo(1).durability(HotOrNotConfig.COMMON.MITTS_DURABILITY.get()).tab(CreativeModeTab.TAB_MISC)));

    public void gatherData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        if (event.includeServer()) {
            generator.addProvider(new DataGenerators.Recipes(generator));
        }
        if (event.includeClient()) {
            generator.addProvider(new DataGenerators.Languages(generator, "en_us"));
            generator.addProvider(new DataGenerators.Languages(generator, "de_de"));
            generator.addProvider(new DataGenerators.ItemModels(generator, MOD_ID, existingFileHelper));
        }
    }

    @SubscribeEvent
    public void onTick(final TickEvent.WorldTickEvent event) {
        if (!event.world.isClientSide) {
            if (event.phase == TickEvent.Phase.START) {
                for (Player player : event.world.players()) {
                    if (player instanceof ServerPlayer) {
                        if (!player.isOnFire() && !player.isCreative()
                                && !player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                            LazyOptional<IItemHandler> handler = player
                                    .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);

                            handler.ifPresent(h -> {
                                for (int i = 0; i < h.getSlots(); i++) {
                                    ItemStack stack = h.getStackInSlot(i);
                                    if (!stack.isEmpty()) {
                                        LazyOptional<IFluidHandlerItem> fluidHandlerItem = stack
                                                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);

                                        if (fluidHandlerItem.isPresent()) {
                                            if (blacklist.contains(stack.getItem()))
                                                return;
                                            fluidHandlerItem.ifPresent(fh -> {
                                                FluidStack fluidStack = fh.drain(1000,
                                                        IFluidHandler.FluidAction.SIMULATE);

                                                if (fluidStack != FluidStack.EMPTY) {
                                                    for (FluidEffect effect : FluidEffect.values()) {
                                                        if (effect.isValid.test(fluidStack)) {
                                                            applyEffectAndDamageMitts(player, effect, event);
                                                        }
                                                    }
                                                }
                                            });
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
                            });
                        }
                    }
                }
            }
        }
    }

    public void applyEffectAndDamageMitts(Player player, FluidEffect effect, TickEvent.WorldTickEvent event) {
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem().equals(MITTS.get()) || mittsItemList.contains(offHand.getItem())) {
            offHand.hurtAndBreak(1, player, consumer -> {
            });
        } else if (event.world.getGameTime() % 20 == 0) {
            effect.interactPlayer.accept(player);
        }
    }

    public enum FluidEffect {
        HOT(fluidStack -> fluidStack.getFluid().getAttributes()
                .getTemperature(fluidStack) >= HotOrNotConfig.COMMON.HOT_TEMPERATURE.get(),
                entityPlayerMP -> entityPlayerMP.setSecondsOnFire(1),
                new TranslatableComponent(TOOLTIP_TOO_HOT).withStyle(ChatFormatting.RED)),
        COLD(fluidStack -> fluidStack.getFluid().getAttributes()
                .getTemperature(fluidStack) <= HotOrNotConfig.COMMON.COLD_TEMPERATURE.get(), entityPlayerMP -> {
            entityPlayerMP.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 21, 1));
            entityPlayerMP.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 21, 1));
        }, new TranslatableComponent(TOOLTIP_TOO_COLD).withStyle(ChatFormatting.AQUA)),

        GAS(fluidStack -> fluidStack.getFluid().getAttributes().isGaseous(fluidStack)
                && HotOrNotConfig.COMMON.GASEOUS.get(),
                entityPlayerMP -> entityPlayerMP.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 21, 1)),
                new TranslatableComponent(TOOLTIP_TOO_LIGHT).withStyle(ChatFormatting.YELLOW));

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
                event.getToolTip().add(new TranslatableComponent(TOOLTIP_MITTS).withStyle(ChatFormatting.GREEN));
            }
            if (blacklist.contains(stack.getItem()))
                return;
            if (stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent()) {
                LazyOptional<IFluidHandlerItem> iFluidHandler = stack
                        .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
                iFluidHandler.ifPresent(h -> {
                    FluidStack fluidStack = h.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                    if (fluidStack != FluidStack.EMPTY) {
                        for (FluidEffect effect : FluidEffect.values()) {
                            if (effect.isValid.test(fluidStack)) {
                                event.getToolTip().add(effect.tooltip);
                            }
                        }
                    }
                });
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
