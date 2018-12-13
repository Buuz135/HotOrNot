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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
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
    public static final String VERSION = "1.0";

    public enum FluidEffect {
        HOT(fluidStack -> fluidStack.getFluid().getTemperature(fluidStack) >= HotConfig.HOT, entityPlayerMP -> entityPlayerMP.setFire(1), TextFormatting.RED, "tooltip.hotornot.toohot"),
        COLD(fluidStack -> fluidStack.getFluid().getTemperature(fluidStack) <= HotConfig.COLD, entityPlayerMP -> {
            entityPlayerMP.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 2, 1));
            entityPlayerMP.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 2, 1));
        }, TextFormatting.AQUA, "tooltip.hotornot.toocold"),
        GAS(fluidStack -> fluidStack.getFluid().isGaseous(fluidStack) && HotConfig.GASEOUS, entityPlayerMP -> entityPlayerMP.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 2, 1)), TextFormatting.YELLOW, "tooltip.hotornot.toolight")
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

        @Nullable
        public static FluidEffect getFirstEffect(FluidStack fluidStack) {
            for (FluidEffect value : FluidEffect.values()) {
                if (value.isValid.test(fluidStack)) {
                    return value;
                }
            }
            return null;
        }
    }

    @Mod.EventBusSubscriber
    public static class ServerTick {

        @SubscribeEvent
        public static void onTick(TickEvent.ServerTickEvent event) {
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
                                    FluidEffect effect = FluidEffect.getFirstEffect(fluidStack);
                                    if (effect != null) {
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
                    FluidEffect effect = FluidEffect.getFirstEffect(fluidStack);
                    if (effect != null)
                        event.getToolTip().add(effect.color + new TextComponentTranslation(effect.tooltip).getUnformattedText());
                }
            }
        }
    }
}
