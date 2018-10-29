/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.items;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.alcatrazescapee.alcatrazcore.util.RegistryHelper;
import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.util.ItemType;
import com.alcatrazescapee.tinkersforging.util.Metal;

import static com.alcatrazescapee.alcatrazcore.util.CoreHelpers.getNull;
import static com.alcatrazescapee.tinkersforging.ModConstants.MOD_ID;
import static com.alcatrazescapee.tinkersforging.client.ModCreativeTabs.TAB_ITEMS;

@GameRegistry.ObjectHolder(value = MOD_ID)
public final class ModItems
{
    @GameRegistry.ObjectHolder("hammer/iron")
    public static final ItemHammer IRON_HAMMER = getNull();

    public static void preInit()
    {
        RegistryHelper r = RegistryHelper.get(MOD_ID);

        for (Metal metal : Metal.values())
        {
            Item.ToolMaterial tool = metal.getMaterial();
            if (tool != null)
                r.registerItem(new ItemHammer(metal, tool), "hammer/" + metal.name());

            if (Loader.isModLoaded("tconstruct") && ModConfig.GENERAL.useTinkersConstruct)
                continue;
            // Below this line is not loaded if using TiCon compat

            for (ItemType type : ItemType.values())
            {
                if (type.isItemType())
                    r.registerItem(new ItemToolHead(type, metal), type.name() + "/" + metal.name());
            }
        }

        r.registerItem(new ItemHammer(Item.ToolMaterial.WOOD), "hammer/wood", TAB_ITEMS);
        r.registerItem(new ItemHammer(Item.ToolMaterial.STONE), "hammer/stone", TAB_ITEMS);
    }

    public static void init()
    {
        // Add hammer creative tabs
        for (ItemHammer item : ItemHammer.getAll())
        {
            if (item.getMetal() == null || item.getMetal().isEnabled())
                item.setCreativeTab(TAB_ITEMS);
        }

        // Add tool part creative tabs
        for (ItemToolHead item : ItemToolHead.getAll())
        {
            if (item.getMetal().isEnabled())
                item.setCreativeTab(TAB_ITEMS);
        }
    }
}
