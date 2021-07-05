package com.lenin.warpstonemod.items;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class WarpstoneItemGroup extends ItemGroup {
    public WarpstoneItemGroup (String label) {
        super(label);
    }

    @Override
    public ItemStack createIcon() {
        return ItemGroup.MATERIALS.getIcon();
    }
}