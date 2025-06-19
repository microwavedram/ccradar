package uk.cloudmc.microwavedram.ccradar;

import dan200.computercraft.api.peripheral.PeripheralLookup;
import net.fabricmc.api.ModInitializer;
import uk.cloudmc.microwavedram.ccradar.peripherals.RadarPeripheral;
import uk.cloudmc.microwavedram.ccradar.registry.ModBlocks;

public class CCradar implements ModInitializer {

    public static final String NAMESPACE = "ccradar";

    @Override
    public void onInitialize() {
        ModBlocks.init();

        PeripheralLookup.get().registerForBlockEntity(
                RadarPeripheral::new,
                ModBlocks.RADAR_BLOCK_ENTITY
        );
    }
}
