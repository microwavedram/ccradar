package uk.cloudmc.microwavedram.ccradar.registry.blocks;

import dan200.computercraft.api.peripheral.IComputerAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.io.input.ProxyInputStream;
import uk.cloudmc.microwavedram.ccradar.registry.ModBlocks;

import java.util.*;

public class RadarBlockEntity extends BlockEntity {

    private static final double TARGET_MAX_RANGE = 10000;
    private static final double TARGET_FALLOFF = 100;

    private static final double NOISE_STRENGTH = 0.2;
    private static final double NOISE_PITCH = 0.1;
    private static final double NOISE_YAW = 0.1;
    private static final double NOISE_CLEAR = 0.5;
    private static final double NOISE_RAIN = 10;
    private static final double NOISE_THUNDER = 20;

    private static final double MIN_FREQUENCY = 300;
    private static final double MAX_FREQUENCY = 5000;

    private static final double MIN_FOV = 0;
    private static final double MAX_FOV = Math.PI;

    public enum RadarMode {
        STANDBY,
        PASSIVE,
        ACTIVE
    }

    public record RadarTarget(Vec3d location) {}

    private final Random random = new Random();

    public double yaw = 0f;
    public double fov = 10f;
    public double frequency = MIN_FREQUENCY;
    public RadarMode mode = RadarMode.STANDBY;

    private final Set<IComputerAccess> computers = new HashSet<>();

    public RadarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.RADAR_BLOCK_ENTITY, pos, state);
    }

    public void addComputer(IComputerAccess computer) {
        computers.add(computer);
    }

    public void removeComputer(IComputerAccess computer) {
        computers.remove(computer);
    }

    public void setFov(double fov) {
        this.fov = clamp(fov, MIN_FOV, MAX_FOV);
    }

    private double clamp(double x, double mi, double ma) {

        if (x > ma) return ma;
        if (x < mi) return mi;

        return x;
    }

    public void ping() {
        Vec3d radar_location = this.getPos().toCenterPos();
        ServerWorld world = (ServerWorld) this.getWorld();

        Vec3d facing = new Vec3d(-Math.sin(this.yaw), 0, Math.cos(this.yaw));

        assert world != null;

        List<ServerPlayerEntity> players = world.getPlayers();

        List<RadarBlockEntity.RadarTarget> targets = new ArrayList<>();

        for (ServerPlayerEntity player : players) targets.add(new RadarBlockEntity.RadarTarget(player.getPos()));

        for (RadarBlockEntity.RadarTarget target : targets) {
            Vec3d delta = target.location().subtract(radar_location);

            double distance = delta.length();

            if (distance > TARGET_MAX_RANGE) continue;

            // θ = cos-1 [ (a · b) / (|a| |b|) ]
            double yaw_difference = Math.acos((delta.x * facing.x + delta.z * facing.z) / distance);

            if (yaw_difference > this.fov / 2) continue;

            double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            double yaw = Math.atan2(delta.z, delta.x) - Math.PI / 2;
            double pitch = Math.atan2(delta.y, horizontalDistance);

            double range_factor = distance / TARGET_MAX_RANGE;
            double frequency_factor = (frequency - MIN_FREQUENCY) / (MAX_FREQUENCY - MIN_FREQUENCY);
            double fov_factor = (fov - MIN_FOV) / (MAX_FOV - MIN_FOV);

            // \frac{1}{d^{x}}
            double strength = 1 / Math.pow(TARGET_FALLOFF, range_factor);

            double weather_attenuation = NOISE_CLEAR;

            if (world.isRaining()) {
                weather_attenuation = NOISE_RAIN;
            }
            if (world.isThundering()) {
                weather_attenuation = NOISE_THUNDER;
            }

            weather_attenuation *= Math.pow(0.01, 1 - frequency_factor);

            double noise_base = Math.pow(10, -frequency_factor)
                    * Math.pow(3, 5 * frequency_factor * fov_factor - 1) // FOV impact
                    * weather_attenuation;

            noise_base = clamp(noise_base, 0, 1);

            double noisyYaw = yaw + random.nextGaussian() * NOISE_YAW * noise_base;
            double noisyPitch = pitch + random.nextGaussian() * NOISE_PITCH * noise_base;
            double noisyStrength = strength * (1 + clamp(random.nextGaussian() * NOISE_STRENGTH, -0.9, 0.9) * noise_base)

            for (IComputerAccess computerAccess : computers) {
                computerAccess.queueEvent("radar", noisyYaw, noisyPitch, noisyStrength);
            }
        }
    }
}