package com.buuz135.hotornot;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(
        modid = HotOrNot.MOD_ID,
        name = HotOrNot.MOD_NAME,
        version = HotOrNot.VERSION
)
public class HotOrNot {

    public static final String MOD_ID = "hotornot";
    public static final String MOD_NAME = "HotOrNot";
    public static final String VERSION = "1.0";

    @Mod.EventBusSubscriber
    public static class ServerTick {

        @SubscribeEvent
        public static void onTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                for (EntityPlayerMP entityPlayerMP : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                    if (!entityPlayerMP.isBurning()) {
                        for (ItemStack stack : entityPlayerMP.inventory.mainInventory) {
                            if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                                IFluidHandlerItem fluidHandlerItem = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                                FluidStack fluidStack = fluidHandlerItem.drain(1000, false);
                                if (fluidStack != null && fluidStack.getFluid().getTemperature(fluidStack) >= HotConfig.HOT) {
                                    entityPlayerMP.setFire(1);
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

        @Config.Comment("How hot a fluid should be to start burning the player")
        public static int HOT = 1300;

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
}
