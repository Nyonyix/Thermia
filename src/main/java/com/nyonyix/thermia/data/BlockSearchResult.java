package com.nyonyix.thermia.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nyonyix.thermia.util.BlockSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.*;

public record BlockSearchResult(
        BlockPos searchOrigin,
        ResourceKey<Level> levelID,
        Map<Block, List<BlockPos>> allPositions,
        Map<BlockPos, Float> blockOcclusions
)
{
    public static final Codec<BlockSearchResult> CODEC = RecordCodecBuilder.create(blockSearchResultInstance -> blockSearchResultInstance.group(
            BlockPos.CODEC.fieldOf("search_origin").forGetter(BlockSearchResult::searchOrigin),
            ResourceKey.codec(Registries.DIMENSION).fieldOf("level_id").forGetter(BlockSearchResult::levelID),
            Codec.unboundedMap(BuiltInRegistries.BLOCK.byNameCodec(), Codec.list(BlockPos.CODEC)).fieldOf("all_positions").forGetter(BlockSearchResult::allPositions),
            Codec.unboundedMap(BlockPos.CODEC, Codec.FLOAT).fieldOf("block_occlusions").forGetter(BlockSearchResult::blockOcclusions)
    ).apply(blockSearchResultInstance, BlockSearchResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockSearchResult> STREAM_CODEC = StreamCodec.of(
            (buf, result) ->
            {
                BlockPos.STREAM_CODEC.encode(buf, result.searchOrigin);
                buf.writeResourceKey(result.levelID);

                buf.writeInt(result.allPositions.size());
                for (Map.Entry<Block, List<BlockPos>> entry : result.allPositions.entrySet())
                {
                    buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(entry.getKey()));
                    buf.writeInt(entry.getValue().size());
                    for (BlockPos pos : entry.getValue())
                    {
                        BlockPos.STREAM_CODEC.encode(buf, pos);
                    }
                }

                buf.writeInt(result.blockOcclusions.size());
                for (Map.Entry<BlockPos, Float> entry : result.blockOcclusions.entrySet())
                {
                    BlockPos.STREAM_CODEC.encode(buf, entry.getKey());
                    buf.writeFloat(entry.getValue());
                }
            },
            (buf) ->
            {
                BlockPos searchOrigin = BlockPos.STREAM_CODEC.decode(buf);
                ResourceKey<Level> levelID = buf.readResourceKey(Registries.DIMENSION);

                int allPositionSize = buf.readInt();
                Map<Block, List<BlockPos>> allPositions = new HashMap<>();
                for (int i = 0; i < allPositionSize; i++)
                {
                    Block block = BuiltInRegistries.BLOCK.get(buf.readResourceLocation());
                    int listSize = buf.readInt();
                    List<BlockPos> positions = new ArrayList<>();
                    for (int o = 0; o < listSize; o++)
                    {
                        positions.add(BlockPos.STREAM_CODEC.decode(buf));
                    }
                    allPositions.put(block, positions);
                }

                int blockOcclusionSize = buf.readInt();
                Map<BlockPos, Float> blockOcclusions = new HashMap<>();
                for (int i = 0; i < blockOcclusionSize; i++)
                {
                    blockOcclusions.put(BlockPos.STREAM_CODEC.decode(buf), buf.readFloat());
                }

                return new BlockSearchResult(searchOrigin, levelID, allPositions, blockOcclusions);
            }
    );

    public static BlockSearchResult createDefault() {return new BlockSearchResult(BlockPos.ZERO, Level.OVERWORLD, new HashMap<>(), new HashMap<>());}

    public BlockSearchResult withSearchOrigin(BlockPos searchOrigin) {return new BlockSearchResult(searchOrigin, this.levelID, this.allPositions, this.blockOcclusions);}

    public BlockSearchResult withLevelID(ResourceKey<Level> levelID) {return new BlockSearchResult(this.searchOrigin, levelID, this.allPositions, this.blockOcclusions);}

    public BlockSearchResult withAllPositions(Map<Block, List<BlockPos>> allPositions) {return new BlockSearchResult(this.searchOrigin, this.levelID, allPositions, this.blockOcclusions);}

    public BlockSearchResult withBlockOcclusions(Map<BlockPos, Float> blockOcclusions) {return new BlockSearchResult(this.searchOrigin, this.levelID, this.allPositions, blockOcclusions);}

    public BlockPos getNearest()
    {
        return BlockPos.ZERO;
    }
}