package uk.cloudmc.microwavedram.ccradar.registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import uk.cloudmc.microwavedram.ccradar.CCradar;
import uk.cloudmc.microwavedram.ccradar.registry.blocks.RadarBlock;
import uk.cloudmc.microwavedram.ccradar.registry.blocks.RadarBlockEntity;

import java.util.function.Function;

public class ModBlocks {

    public static final Block RADAR = register("radar", RadarBlock::new, AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE), true);
    public static final BlockEntityType<RadarBlockEntity> RADAR_BLOCK_ENTITY;

    static {
        RADAR_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(CCradar.NAMESPACE, "radar_block"),
                BlockEntityType.Builder.create(RadarBlockEntity::new, ModBlocks.RADAR).build(null)
        );
    }

    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings);

        if (shouldRegisterItem) {
            RegistryKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(CCradar.NAMESPACE, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(CCradar.NAMESPACE, name));
    }

    public static void init() {

    }
}
