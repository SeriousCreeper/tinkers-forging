/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.common.recipe.AnvilRecipe;
import com.alcatrazescapee.tinkersforging.util.TickTimer;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeStep;
import com.alcatrazescapee.tinkersforging.util.forge.ForgeSteps;

public class ForgeItem implements IForgeItem, ICapabilitySerializable<NBTTagCompound>
{
    private final ForgeSteps steps;
    private int work;
    private String recipeName;

    private final float meltingTemperature;
    private final float workingTemperature;

    // These are the values from last point of update. They are updated when read from NBT, or when the temperature is set manually.
    private float temperature;
    private long lastUpdateTick;

    public ForgeItem(@Nullable NBTTagCompound nbt)
    {
        steps = new ForgeSteps();
        this.meltingTemperature = 1500f;
        this.workingTemperature = 1100f;
        deserializeNBT(nbt);
    }

    @Override
    public int getWork()
    {
        return work;
    }

    @Override
    public void setWork(int work)
    {
        this.work = work;
    }

    @Override
    @Nullable
    public String getRecipeName()
    {
        return recipeName;
    }

    @Override
    public void setRecipe(@Nullable AnvilRecipe recipe)
    {
        recipeName = (recipe == null ? null : recipe.getName());
    }

    @Override
    @Nonnull
    public ForgeSteps getSteps()
    {
        return steps;
    }

    @Override
    public void addStep(ForgeStep step)
    {
        steps.addStep(step);
        work += step.getStepAmount();
    }

    @Override
    public void reset()
    {
        steps.reset();
        recipeName = null;
        work = 0;
    }

    @Override
    public float getTemperature()
    {
        final float newTemp = temperature - (float) (TickTimer.getTicks() - lastUpdateTick) * (float) ModConfig.GENERAL.temperatureModifier;
        return newTemp < 0 ? 0 : newTemp;
    }

    @Override
    public void setTemperature(float temperature)
    {
        this.temperature = temperature;
        this.lastUpdateTick = TickTimer.getTicks();
    }

    @Override
    public float getMeltingTemperature()
    {
        return meltingTemperature;
    }

    @Override
    public float getWorkableTemperature()
    {
        return workingTemperature;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityForgeItem.CAPABILITY;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return hasCapability(capability, facing) ? (T) this : null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();

        nbt.setInteger("work", work);
        nbt.setTag("steps", steps.serializeNBT());
        if (recipeName != null)
        {
            nbt.setString("recipe", recipeName);
        }

        nbt.setFloat("temp", getTemperature());
        nbt.setLong("tick", TickTimer.getTicks());

        return nbt;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound nbt)
    {
        if (nbt != null)
        {
            if (nbt.hasKey(CapabilityForgeItem.NBT_KEY))
            {
                nbt = nbt.getCompoundTag(CapabilityForgeItem.NBT_KEY);
            }

            work = nbt.getInteger("work");
            recipeName = nbt.hasKey("recipe") ? nbt.getString("recipe") : null; // stops defaulting to empty string
            steps.deserializeNBT(nbt.getCompoundTag("steps"));

            temperature = nbt.getFloat("temp");
            lastUpdateTick = nbt.getLong("tick");
        }
    }

}
