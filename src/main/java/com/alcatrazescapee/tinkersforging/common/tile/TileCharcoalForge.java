/*
 * Part of the Tinkers Forging Mod by alcatrazEscapee
 * Work under Copyright. Licensed under the GPL-3.0.
 * See the project LICENSE.md for more information.
 */

package com.alcatrazescapee.tinkersforging.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.alcatrazescapee.alcatrazcore.tile.ITileFields;
import com.alcatrazescapee.alcatrazcore.tile.TileInventory;
import com.alcatrazescapee.alcatrazcore.util.CoreHelpers;
import com.alcatrazescapee.tinkersforging.ModConfig;
import com.alcatrazescapee.tinkersforging.TinkersForging;
import com.alcatrazescapee.tinkersforging.common.blocks.ModBlocks;
import com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem;
import com.alcatrazescapee.tinkersforging.common.capability.IForgeItem;

import static com.alcatrazescapee.tinkersforging.common.capability.CapabilityForgeItem.MAX_TEMPERATURE;
import static com.alcatrazescapee.tinkersforging.util.property.IBurnBlock.LIT;
import static com.alcatrazescapee.tinkersforging.util.property.IPileBlock.LAYERS;

@ParametersAreNonnullByDefault
public class TileCharcoalForge extends TileInventory implements ITickable, ITileFields
{
    public static final int SLOT_INPUT_MIN = 0;
    public static final int SLOT_INPUT_MAX = 5;

    public static final int FIELD_FUEL = 0;
    public static final int FIELD_TEMPERATURE = 1;
    public static final int FUEL_TICKS_MAX = 1600;

    public static void lightNearbyForges(World world, BlockPos pos)
    {
        if (tryLight(world, pos))
        {
            for (EnumFacing face : EnumFacing.HORIZONTALS)
            {
                BlockPos pos1 = pos.offset(face);
                if (tryLight(world, pos1))
                {
                    lightNearbyForges(world, pos1);
                }
            }
        }
    }

    public static boolean updateSideBlocks(World world, BlockPos pos)
    {
        if (!world.isRemote)
        {
            for (EnumFacing face : EnumFacing.HORIZONTALS)
            {
                IBlockState state = world.getBlockState(pos.offset(face));
                if (!isValidSideBlock(state))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean tryLight(World world, BlockPos pos)
    {
        // Replace the block
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == ModBlocks.CHARCOAL_PILE)
        {
            int layers = state.getValue(LAYERS);
            world.setBlockState(pos, ModBlocks.CHARCOAL_FORGE.getStateWithLayers(layers).withProperty(LIT, true));
        }
        else if (state.getBlock() == ModBlocks.CHARCOAL_FORGE && !state.getValue(LIT))
        {
            int layers = state.getValue(LAYERS);
            world.setBlockState(pos, ModBlocks.CHARCOAL_FORGE.getStateWithLayers(layers).withProperty(LIT, true));
        }
        else
        {
            return false;
        }
        // Light the TE
        TileCharcoalForge tile = CoreHelpers.getTE(world, pos, TileCharcoalForge.class);
        if (tile != null)
        {
            tile.consumeFuel();
        }
        return true;
    }

    private static boolean isValidSideBlock(IBlockState state)
    {
        return (state.isNormalCube() && !state.getMaterial().getCanBurn()) ||
                state.getBlock() == ModBlocks.CHARCOAL_FORGE ||
                state.getBlock() == ModBlocks.CHARCOAL_PILE;
    }

    private int fuelTicksRemaining;
    private float temperature;
    private boolean isClosed;

    public TileCharcoalForge()
    {
        super(5);
    }

    public void updateClosedState(World world, BlockPos pos)
    {
        for (EnumFacing face : EnumFacing.HORIZONTALS)
        {
            if (isValidSideBlock(world.getBlockState(pos.offset(face))))
                continue;

            isClosed = false;
            return;
        }
        isClosed = true;
    }

    @Override
    public void update()
    {
        // todo: remove
        if (world.getTotalWorldTime() % 20 == 0)
            TinkersForging.getLog().info("Charcoal Forge Tick: Fuel {} | Temp: {}", fuelTicksRemaining, temperature);
        if (fuelTicksRemaining > 0)
        {
            // Consume fuel ticks
            fuelTicksRemaining--;

            if (fuelTicksRemaining == 0)
            {
                consumeFuel();

                if (fuelTicksRemaining == 0)
                {
                    // Couldn't consume any more fuel
                    world.setBlockState(pos, world.getBlockState(pos).withProperty(LIT, false));
                }
            }

            // Update temperature
            float actualMaxTemp = isClosed ? MAX_TEMPERATURE : MAX_TEMPERATURE * 0.6f;
            if (temperature < actualMaxTemp)
            {
                temperature += (float) ModConfig.GENERAL.temperatureModifierCharcoalForge;
                if (temperature > actualMaxTemp)
                    temperature = actualMaxTemp;
            }

            for (int i = SLOT_INPUT_MIN; i < SLOT_INPUT_MAX; i++)
            {
                ItemStack stack = inventory.getStackInSlot(i);
                IForgeItem cap = stack.getCapability(CapabilityForgeItem.CAPABILITY, null);

                if (cap != null)
                {
                    // Add temperature
                    if (cap.getTemperature() < temperature)
                    {
                        CapabilityForgeItem.addTemp(stack, cap, 2.0f);
                    }

                    if (cap.isMolten())
                    {
                        // The thing melted!
                        inventory.setStackInSlot(i, ItemStack.EMPTY);
                        world.playSound(null, pos, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                }
            }
        }
        else if (temperature > 0)
        {
            // When it is not burning fuel, then decrease the temperature until it reaches zero
            temperature -= (float) ModConfig.GENERAL.temperatureModifierCharcoalForge;
            if (temperature < 0)
                temperature = 0;
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack)
    {
        return stack.hasCapability(CapabilityForgeItem.CAPABILITY, null);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        temperature = nbt.getFloat("temp");
        fuelTicksRemaining = nbt.getInteger("ticks");

        super.readFromNBT(nbt);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setFloat("temp", temperature);
        nbt.setInteger("ticks", fuelTicksRemaining);

        return super.writeToNBT(nbt);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public int getFieldCount()
    {
        return 2;
    }

    @Override
    public int getField(int index)
    {
        switch (index)
        {
            case FIELD_FUEL:
                return fuelTicksRemaining;
            case FIELD_TEMPERATURE:
                return (int) temperature;
            default:
                TinkersForging.getLog().warn("Invalid field ID!");
                return 0;
        }
    }

    @Override
    public void setField(int index, int value)
    {
        switch (index)
        {
            case FIELD_FUEL:
                fuelTicksRemaining = value;
                break;
            case FIELD_TEMPERATURE:
                temperature = (float) value;
                break;
            default:
                TinkersForging.getLog().warn("Invalid field ID!");

        }
    }

    private void consumeFuel()
    {
        // Consume fuel
        IBlockState state = world.getBlockState(pos);
        if (state.getValue(LAYERS) == 1)
        {
            world.setBlockToAir(pos);
        }
        else
        {
            world.setBlockState(pos, state.withProperty(LAYERS, state.getValue(LAYERS) - 1));
            fuelTicksRemaining = FUEL_TICKS_MAX;
        }
    }
}
