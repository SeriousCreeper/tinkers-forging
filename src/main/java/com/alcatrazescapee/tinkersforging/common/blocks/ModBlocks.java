/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.blocks;

import net.minecraftforge.fml.common.registry.GameRegistry;

import com.alcatrazescapee.alcatrazcore.util.RegistryHelper;
import com.alcatrazescapee.tinkersforging.common.tile.TileTinkersAnvil;
import com.alcatrazescapee.tinkersforging.util.Metal;

import static com.alcatrazescapee.alcatrazcore.util.CoreHelpers.getNull;
import static com.alcatrazescapee.tinkersforging.ModConstants.MOD_ID;
import static com.alcatrazescapee.tinkersforging.client.ModCreativeTabs.TAB_ITEMS;

@GameRegistry.ObjectHolder(value = MOD_ID)
public final class ModBlocks
{
    @GameRegistry.ObjectHolder("tinkers_anvil/iron")
    public static final BlockTinkersAnvil IRON_ANVIL = getNull();

    public static void preInit()
    {
        RegistryHelper r = RegistryHelper.get(MOD_ID);

        for (Metal metal : Metal.values())
        {
            r.registerBlock(new BlockTinkersAnvil(metal), "tinkers_anvil/" + metal.name());
        }

        r.registerTile(TileTinkersAnvil.class, "tinkers_anvil");
    }

    public static void init()
    {
        // Tinkers Anvil creative tab
        for (BlockTinkersAnvil block : BlockTinkersAnvil.getAll())
        {
            if (block.getMetal().isEnabled())
                block.setCreativeTab(TAB_ITEMS);
        }
    }
}
