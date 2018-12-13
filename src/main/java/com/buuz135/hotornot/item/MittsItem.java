package com.buuz135.hotornot.item;

import com.buuz135.hotornot.HotOrNot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MittsItem extends Item {

    public MittsItem() {
        setRegistryName(HotOrNot.MOD_ID, "mitts");
        setMaxStackSize(1);
        setMaxDamage(HotOrNot.HotConfig.MITTS_DURABILITY);
        setUnlocalizedName(HotOrNot.MOD_ID + ".mitts");
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }
}
