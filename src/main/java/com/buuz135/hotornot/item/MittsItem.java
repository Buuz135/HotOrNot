package com.buuz135.hotornot.item;

import com.buuz135.hotornot.HotOrNot;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

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

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TextComponentTranslation("item.hotornot.mitts.tooltip").getUnformattedComponentText());
    }
}
