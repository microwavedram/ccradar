package uk.cloudmc.microwavedram.ccradar.peripherals;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import uk.cloudmc.microwavedram.ccradar.registry.blocks.RadarBlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RadarPeripheral implements IPeripheral {

    private static final double TAU = Math.PI * 2;

    private final RadarBlockEntity blockEntity;

    public RadarPeripheral(BlockEntity blockEntity, Direction direction) {
        this.blockEntity = (RadarBlockEntity) blockEntity;
    }

    private double clamp(double x, double mi, double ma) {

        if (x > ma) return ma;
        if (x < mi) return mi;

        return x;
    }

    @Override
    public void attach(IComputerAccess computer) {
        blockEntity.addComputer(computer);
    }

    @Override
    public void detach(IComputerAccess computer) {
        blockEntity.removeComputer(computer);
    }

    @Override
    public String getType() {
        return "radar";
    }

    @Override
    public boolean equals(IPeripheral iPeripheral) {
        return iPeripheral instanceof RadarPeripheral;
    }

    @LuaFunction
    public final void setYaw(double yaw) {
        blockEntity.yaw = (Math.toRadians(yaw) + TAU) % TAU;
    }

    @LuaFunction
    public final double getYaw() {
        return Math.toDegrees(blockEntity.yaw);
    }

    @LuaFunction
    public final void setFov(double fov) {
        blockEntity.setFov(Math.toRadians(fov));
    }

    @LuaFunction
    public final double getFov() {
        return Math.toDegrees(blockEntity.fov);
    }

    @LuaFunction
    public final void setMode(String mode) throws LuaException {
        try {
            blockEntity.mode = RadarBlockEntity.RadarMode.valueOf(mode);
        } catch (IllegalArgumentException e) {
            throw new LuaException(mode + " is not a valid radar mode");
        }
    }

    @LuaFunction
    public final String getMode() {
        return blockEntity.mode.name();
    }

    @LuaFunction
    public final void setFrequency(double frequency) {
        blockEntity.frequency = clamp(frequency, 300, 5000);
    }

    @LuaFunction
    public final double getFrequency() {
        return blockEntity.frequency;
    }

    @LuaFunction
    public final String ping() {
        blockEntity.ping();

        return this.blockEntity.getPos().toShortString();
    }
}
