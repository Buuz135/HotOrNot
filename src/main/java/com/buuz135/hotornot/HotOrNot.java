/*
 * This file is part of Hot or Not.
 *
 * Copyright 2018, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.buuz135.hotornot;

import com.buuz135.hotornot.proxy.CommonProxy;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Mod(
        modid = HotOrNot.MOD_ID,
        name = HotOrNot.MOD_NAME,
        version = HotOrNot.VERSION
)
public class HotOrNot {

    public static final String MOD_ID = "hotornot";
    public static final String MOD_NAME = "HotOrNot";
    public static final String VERSION = "1.1.2";

    @SidedProxy(clientSide = "com.buuz135.hotornot.proxy.ClientProxy", serverSide = "com.buuz135.hotornot.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventBusSubscriber
    public static class ObjectRegistryHandler {

        @SubscribeEvent
        public static void addItems(RegistryEvent.Register<Item> event) {
            proxy.registerItems(event);
        }

        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void modelRegistryEvent(ModelRegistryEvent event) {
            proxy.modelRegistryEvent(event);
        }
    }

    public enum FluidEffect {
        HOT(fluidStack -> fluidStack.getFluid().getTemperature(fluidStack) >= HotConfig.HOT, entityPlayerMP -> entityPlayerMP.setFire(1), TextFormatting.RED, "tooltip.hotornot.toohot"),
        COLD(fluidStack -> fluidStack.getFluid().getTemperature(fluidStack) <= HotConfig.COLD, entityPlayerMP -> {
            entityPlayerMP.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 21, 1));
            entityPlayerMP.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 21, 1));
        }, TextFormatting.AQUA, "tooltip.hotornot.toocold"),
        GAS(fluidStack -> fluidStack.getFluid().isGaseous(fluidStack) && HotConfig.GASEOUS, entityPlayerMP -> entityPlayerMP.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 21, 1)), TextFormatting.YELLOW, "tooltip.hotornot.toolight")
        ;

        private final Predicate<FluidStack> isValid;
        private final Consumer<EntityPlayerMP> interactPlayer;
        private final TextFormatting color;
        private final String tooltip;

        FluidEffect(Predicate<FluidStack> isValid, Consumer<EntityPlayerMP> interactPlayer, TextFormatting color, String tooltip) {
            this.isValid = isValid;
            this.interactPlayer = interactPlayer;
            this.color = color;
            this.tooltip = tooltip;
        }

    }

    @Mod.EventBusSubscriber
    public static class ServerTick {

        @SubscribeEvent
        public static void onTick(TickEvent.WorldTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                for (EntityPlayerMP entityPlayerMP : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                    if (!entityPlayerMP.isBurning() && !entityPlayerMP.isCreative() && entityPlayerMP.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                        IItemHandler handler = entityPlayerMP.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack stack = handler.getStackInSlot(i);
                            if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                                IFluidHandlerItem fluidHandlerItem = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                                FluidStack fluidStack = fluidHandlerItem.drain(1000, false);
                                if (fluidStack != null) {
                                    for (FluidEffect effect : FluidEffect.values()) {
                                        if (effect.isValid.test(fluidStack)) {
                                            ItemStack offHand = entityPlayerMP.getHeldItemOffhand();
                                            if (offHand.getItem().equals(CommonProxy.MITTS)) {
                                                offHand.damageItem(1, entityPlayerMP);
                                            } else if (event.world.getTotalWorldTime() % 20 == 0) {
                                                effect.interactPlayer.accept(entityPlayerMP);
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
    }

    @Config(modid = MOD_ID)
    public static class HotConfig {

        @Config.Comment("How hot a fluid should be to start burning the player (in kelvin)")
        public static int HOT = 1300;

        @Config.Comment("How cold a fluid should be to start adding effects the player (in kelvin)")
        public static int COLD = 273;

        @Config.Comment("If true gaseous effects for the fluids will be enabled")
        public static boolean GASEOUS = true;

        @Config.Comment("If true, the items that contain hot fluid will have a tooltip that will show that they are too hot")
        public static boolean TOOLTIP = true;

        @Config.Comment("Max durability of the mitts")
        public static int MITTS_DURABILITY = 20 * 60 * 10;

        @Mod.EventBusSubscriber(modid = MOD_ID)
        private static class EventHandler {
            @SubscribeEvent
            public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
                if (event.getModID().equals(MOD_ID)) {
                    ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(value = Side.CLIENT)
    public static class HotTooltip {

        @SubscribeEvent
        public static void onTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (HotConfig.TOOLTIP && !stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidHandlerItem fluidHandlerItem = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                FluidStack fluidStack = fluidHandlerItem.drain(1000, false);
                if (fluidStack != null) {
                    for (FluidEffect effect : FluidEffect.values()) {
                        if (effect.isValid.test(fluidStack))
                            event.getToolTip().add(effect.color + new TextComponentTranslation(effect.tooltip).getUnformattedText());
                    }
                }
            }
        }
    }
}
