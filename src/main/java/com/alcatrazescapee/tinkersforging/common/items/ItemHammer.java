/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.items;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;

import com.alcatrazescapee.alcatrazcore.item.tool.ItemToolCore;
import com.alcatrazescapee.alcatrazcore.util.OreDictionaryHelper;
import com.alcatrazescapee.tinkersforging.util.Metal;

@ParametersAreNonnullByDefault
public class ItemHammer extends ItemToolCore
{
    @Nullable
    public static ItemHammer get(Metal metal)
    {
        return MAP.get(metal);
    }

    private static final Map<Metal, ItemHammer> MAP = new HashMap<>();

    @Nonnull
    public static ItemStack get(Metal metal, int amount)
    {
        ItemHammer item = get(metal);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, amount);
    }

    @Nonnull
    public static Collection<ItemHammer> getAll()
    {
        return MAP.values();
    }

    private final Metal metal;

    public ItemHammer(Metal metal, ToolMaterial material)
    {
        super(material, 2.0f, -3.0f);

        this.efficiency = Math.min(efficiency - 2.0f, 1.0f);
        addToolClass(ToolClass.PICKAXE);

        this.metal = metal;
        MAP.put(metal, this);

        OreDictionaryHelper.register(this, "hammer");
        OreDictionaryHelper.register(this, "hammer");
    }

    public ItemHammer(ToolMaterial material)
    {
        super(material, 2.0f, -3.0f);

        this.efficiency = Math.min(efficiency - 2.0f, 1.0f);
        addToolClass(ToolClass.PICKAXE);

        this.metal = null;

        OreDictionaryHelper.register(this, "hammer");
        OreDictionaryHelper.register(this, "hammer", material.name());
    }

    @Nullable
    public Metal getMetal()
    {
        return metal;
    }
}
