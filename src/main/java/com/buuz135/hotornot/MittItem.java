package com.buuz135.hotornot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MittItem extends Item {
	public MittItem(Properties properties) {
		super(properties);
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return HotOrNotConfig.COMMON.MITTS_DURABILITY.get();
	}
}
