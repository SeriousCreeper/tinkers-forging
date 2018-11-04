/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.capability;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.oredict.OreDictionary;

import com.alcatrazescapee.tinkersforging.ModConfig;

import static com.alcatrazescapee.alcatrazcore.util.CoreHelpers.getNull;
import static com.alcatrazescapee.tinkersforging.ModConstants.MOD_ID;

@ParametersAreNonnullByDefault
public final class CapabilityForgeItem
{
    @CapabilityInject(IForgeItem.class)
    public static final Capability<IForgeItem> CAPABILITY = getNull();
    public static final String NBT_KEY = MOD_ID + ":forge_item";
    public static final ResourceLocation KEY = new ResourceLocation(MOD_ID, "forge_item");

    public static final float MAX_TEMPERATURE = 1500f;

    private static final float DEFAULT_MELT_TEMPERATURE = 1400f;
    private static final float DEFAULT_WORK_TEMPERATURE = 1000f;

    public static void preInit()
    {
        CapabilityManager.INSTANCE.register(IForgeItem.class, new DumbStorage(), () -> new ForgeItem(null, null));
    }

    public static void clearStackCheckRecipe(ItemStack stack)
    {
        IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap != null)
        {
            if (cap.getTemperature() < 1f && (cap.getRecipeName() == null || cap.getWork() == 0))
            {
                clearStack(stack, cap);
            }
        }
    }

    /**
     * Use this to increase the heat on an item stack
     */
    public static void addTemp(ItemStack stack, IForgeItem cap, float modifier)
    {
        final float temp = cap.getTemperature() + modifier * (float) ModConfig.GENERAL.temperatureModifier;
        cap.setTemperature(temp > MAX_TEMPERATURE ? MAX_TEMPERATURE : temp);
        stack.setTagInfo(NBT_KEY, cap.serializeNBT());
    }

    static float getMeltingTemperature(@Nullable ItemStack stack)
    {
        if (stack != null)
        {
            int[] ids = OreDictionary.getOreIDs(stack);
            for (int id : ids)
            {
                // Try and catch all common metal items
                String name = OreDictionary.getOreName(id);
                if (name.startsWith("ingot") || name.startsWith("block"))
                {
                    return getMetalTemperature(name.substring(5).toLowerCase());
                }
                else if (name.startsWith("nugget"))
                {
                    return getMetalTemperature(name.substring(6).toLowerCase());
                }
            }
        }
        return DEFAULT_MELT_TEMPERATURE;
    }

    public static void clearStack(ItemStack stack)
    {
        IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);
        if (cap != null)
        {
            clearStack(stack, cap);
        }
    }

    static float getWorkingTemperature(@Nullable ItemStack stack)
    {
        if (stack != null)
        {
            float meltTemp = getMeltingTemperature(stack);
            if (meltTemp == DEFAULT_MELT_TEMPERATURE)
                return DEFAULT_WORK_TEMPERATURE;
            else if (meltTemp > 1500)
                return 1400;
            else if (meltTemp >= 1000)
                return meltTemp - 400;
            else if (meltTemp >= 500)
                return meltTemp - 250;
            else if (meltTemp >= 200)
                return 100;
            else
                return 0;
        }
        return DEFAULT_WORK_TEMPERATURE;
    }

    private static void clearStack(ItemStack stack, IForgeItem cap)
    {
        cap.reset();
        stack.removeSubCompound(CapabilityForgeItem.NBT_KEY);
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null && nbt.isEmpty())
        {
            stack.setTagCompound(null);
        }
    }

    private static float getMetalTemperature(String name)
    {
        // This is roughly based on real melting points of metals
        switch (name)
        {
            case "tin": // 231.9
                return 300;
            case "lead": // 327.5
                return 350;
            case "zinc": // 419.5
            case "pigiron": // ???
                return 400;
            case "aluminium": // 660.3
                return 700;
            case "electrum": // 912.8
            case "brass": // 920.0
                return 900;
            case "bronze": // 950.0
            case "silver": // 961.8
                return 950;
            case "copper": // 1085.0
                return 1000;
            case "gold": // 1064.0
                return 1100;
            case "steel": // 1370.0
                return 1350;
            case "nickel": // 1455.0
                return 1450;
            case "invar": // 1427.0
                return 1450;
            case "cobalt": // 1495.0
            case "iron": // 1538.0
                return 1500;
            case "ardite": // ???
            case "manyullyn": // ???
                return 2100;
            case "mithril": // ???
                return 3200;
            case "diamond": // 4726.9
                return 4700;
            default:
                return DEFAULT_MELT_TEMPERATURE;
        }
    }

    // This is not for usage; it will not do anything.
    static class DumbStorage implements Capability.IStorage<IForgeItem>
    {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IForgeItem> capability, IForgeItem instance, EnumFacing side)
        {
            return null;
        }

        @Override
        public void readNBT(Capability<IForgeItem> capability, IForgeItem instance, EnumFacing side, NBTBase nbtBase)
        {
        }
    }
}
