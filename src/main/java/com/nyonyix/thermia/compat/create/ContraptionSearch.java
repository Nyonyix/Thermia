//package com.nyonyix.thermia.compat.create;
//
//import com.nyonyix.thermia.data.datamap.BlockTemperatureDataMap;
//import com.nyonyix.thermia.data.datamap.FluidTemperatureDataMap;
//import com.nyonyix.thermia.data.datamap.ThermiaDataMaps;
//import com.nyonyix.thermia.data.records.BlockSearchResult;
//import com.nyonyix.thermia.util.BlockSearch;
//import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
//import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
//import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
//import com.simibubi.create.content.contraptions.Contraption;
//import com.simibubi.create.content.contraptions.ContraptionHandler;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.registries.BuiltInRegistries;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.level.ClipContext;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.block.state.properties.Property;
//import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
//import net.minecraft.world.level.material.Fluid;
//import net.minecraft.world.phys.AABB;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.HitResult;
//import net.minecraft.world.phys.Vec3;
//import net.minecraft.world.phys.shapes.CollisionContext;
//import net.neoforged.fml.ModList;
//import net.neoforged.neoforge.fluids.FluidStack;
//
//import javax.annotation.Nullable;
//import java.lang.ref.WeakReference;
//import java.util.*;
//
/*


Complete and utter mess, Create does some funky shit with it's contraptions,
Making Async and local/world conversions weird.


 */
//public class ContraptionSearch
//{
//    public static final String MOD_ID = "create";
//
//    private static final boolean IS_LOADED = ModList.get().isLoaded(MOD_ID);
//    private static final Map<Level, CacheEntry> CACHE = new WeakHashMap<>();
//    private record ActiveContraption(AbstractContraptionEntity entity, Contraption contraption, Vec3 offset) {}
//    private record FluidSource(Fluid fluid, float temperature) {}
//    private record SourceAtPos(Object key, float temperature, boolean tfc) {}
//    private record CacheEntry(long tick, List<ActiveContraption> contraptions) {}
//
//    public record WindRayResult(double hitDist, BlockPos blockPos) {}
//    public record ShadeRayResult(double hitDist, BlockPos blockPos) {}
//
//    private static List<ActiveContraption> getActiveContraptions(Level level)
//    {
//        CacheEntry cached = CACHE.get(level);
//        if (cached != null && cached.tick() == level.getGameTime()) return cached.contraptions();
//
//        List<ActiveContraption> fresh = new ArrayList<>();
//        CACHE.put(level, new CacheEntry(level.getGameTime(), fresh));
//        return fresh;
//    }
//
//    private static List<ActiveContraption> getCachedContraptions(Level level)
//    {
//        CacheEntry cached = CACHE.get(level);
//        return cached != null ? cached.contraptions() : List.of();
//    }
//
//    private static List<ActiveContraption> findNearby(Level level, Vec3 origin, int radius)
//    {
//        List<ActiveContraption> found = new ArrayList<>();
//        if (!IS_LOADED) return found;
//
//        Map<Integer, WeakReference<AbstractContraptionEntity>> loaded = ContraptionHandler.loadedContraptions.get(level);
//        if (loaded.isEmpty()) return found;
//
//        AABB searchBox = new AABB(origin, origin).inflate(radius);
//
//        for (WeakReference<AbstractContraptionEntity> ref : loaded.values())
//        {
//            AbstractContraptionEntity entity = ref.get();
//            if (entity == null || entity.isRemoved()) continue;
//
//            Contraption contraption = entity.getContraption();
//            if (contraption == null || contraption.disassembled) continue;
//
//            if (!searchBox.intersects(entity.getBoundingBox())) continue;
//
//            Vec3 offset = entity.getAnchorVec().subtract(Vec3.atCenterOf(contraption.anchor));
//            found.add(new ActiveContraption(entity, contraption, offset));
//        }
//
//        return found;
//    }
//
//    private static Vec3 localToWorld(ActiveContraption ac, BlockPos localPos)
//    {
//        return Vec3.atCenterOf(localPos).add(ac.offset());
//    }
//
//    private static float resolveStateTemperature(BlockState state, BlockTemperatureDataMap dataMap)
//    {
//        float temp = dataMap.temperature();
//
//        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet())
//        {
//            String key = entry.getKey().getName() + "=" + entry.getValue().toString();
//            if (dataMap.stateTemps().containsKey(key)) temp = Math.max(temp, dataMap.stateTemps().get(key));
//        }
//
//        return temp;
//    }
//
//    private static float resolveWorldBlockTemperature(Level level, BlockPos pos, Block block)
//    {
//        BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
//        if (dataMap == null) return 0f;
//        return resolveStateTemperature(level.getBlockState(pos), dataMap);
//    }
//
//    private static float fluidTemperature(Fluid fluid)
//    {
//        FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(fluid).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
//        return dataMap != null ? dataMap.temperature() : 0.0f;
//    }
//
//    private static void removeSource(Map<Block, List<BlockPos>> mergedBlocks, Map<Fluid, List<BlockPos>> mergedFluid, BlockPos pos, Object key)
//    {
//        if (key instanceof Block block)
//        {
//            List<BlockPos> positions = mergedBlocks.get(block);
//            if (positions != null)
//            {
//                positions.remove(pos);
//                if (positions.isEmpty()) mergedBlocks.remove(block);
//            }
//        }
//        else if (key instanceof Fluid fluid)
//        {
//            List<BlockPos> positions = mergedFluid.get(fluid);
//            if (positions != null)
//            {
//                positions.remove(pos);
//                if (positions.isEmpty()) mergedFluid.remove(fluid);
//            }
//        }
//    }
//
//    @Nullable
//    private static FluidSource mountedFluidSource(ActiveContraption ac, BlockPos localPos)
//    {
//        MountedFluidStorageWrapper wrapper = ac.contraption().getStorage().getFluids();
//        if (wrapper == null) return null;
//
//        MountedFluidStorage storage = wrapper.storages.get(localPos);
//        if (storage == null) return null;
//
//        Fluid hottestFluid = null;
//        float hottestTemp = Float.NEGATIVE_INFINITY;
//
//        for (int i = 0; i < storage.getTanks(); i++)
//        {
//            FluidStack stack = storage.getFluidInTank(i);
//            if (stack.isEmpty()) continue;
//
//            FluidTemperatureDataMap dataMap = BuiltInRegistries.FLUID.wrapAsHolder(stack.getFluid()).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
//
//            if (dataMap != null && dataMap.isRadiative() && dataMap.temperature() > hottestTemp)
//            {
//                hottestFluid = stack.getFluid();
//                hottestTemp = dataMap.temperature();
//            }
//        }
//
//        return hottestFluid != null ? new FluidSource(hottestFluid, hottestTemp) : null;
//    }
//
//    @Nullable
//    public static Float resolveSource(Level level, BlockPos pos, Block expectedBlock)
//    {
//        if (!IS_LOADED) return null;
//
//        Map<Integer, WeakReference<AbstractContraptionEntity>> loaded = ContraptionHandler.loadedContraptions.get(level);
//        if (loaded.isEmpty()) return null;
//
//        Vec3 postVec = Vec3.atCenterOf(pos);
//
//        for (WeakReference<AbstractContraptionEntity> ref : loaded.values())
//        {
//            AbstractContraptionEntity entity = ref.get();
//            if (entity == null || entity.isRemoved()) continue;
//
//            Contraption contraption = entity.getContraption();
//            if (contraption == null || contraption.disassembled) continue;
//
//            BlockPos localPos = BlockPos.containing(entity.toLocalVector(postVec, 1f));
//            StructureTemplate.StructureBlockInfo info = contraption.getBlocks().get(localPos);
//            if (info == null || info.state().getBlock() != expectedBlock) continue;
//
//            BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(info.state().getBlock()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
//            if (dataMap == null) return null;
//
//            return resolveStateTemperature(info.state(), dataMap);
//        }
//
//        return null;
//    }
//
//    public static float getContactTemperature(Level level, Entity entity, BlockSearchResult result)
//    {
//        if (!IS_LOADED) return 0f;
//
//        float totalContactTemp = 0f;
//        AABB bb = entity.getBoundingBox().inflate(0.001);
//
//        for (Map.Entry<Block, List<BlockPos>> entry : result.allPositions().entrySet())
//        {
//            BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
//            if (dataMap == null || dataMap.isRadiative()) continue;
//
//            for (BlockPos pos : entry.getValue())
//            {
//                if (!bb.intersects(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) continue;
//
//                Float temp = resolveSource(level, pos, entry.getKey());
//                if (temp != null) totalContactTemp += temp;
//            }
//        }
//
//        return totalContactTemp;
//    }
//
//    @Nullable
//    public static WindRayResult getWindRayOcclusion(Level level, Vec3 start, Vec3 end)
//    {
//        if (!IS_LOADED) return null;
//
//        WindRayResult nearest = null;
//        for (ActiveContraption ac : findNearby(level, start, (int) Math.ceil(start.distanceTo(end)) + 2))
//        {
//            Vec3 localStart = ac.entity().toLocalVector(start, 1f);
//            Vec3 localEnd = ac.entity().toLocalVector(end, 1f);
//
//            BlockHitResult hit = ac.contraption().getContraptionWorld().clip(new ClipContext(localStart, localEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty()));
//
//            if (hit.getType() != HitResult.Type.MISS)
//            {
//                double hitDist = localStart.distanceTo(hit.getLocation());
//                if (nearest == null || hitDist < nearest.hitDist()) nearest = new WindRayResult(hitDist, hit.getBlockPos());
//            }
//        }
//
//        return nearest;
//    }
//
//    @Nullable
//    public static ShadeRayResult getShadeRayOcclusion(Level level, Vec3 start, Vec3 end)
//    {
//        if (!IS_LOADED) return null;
//
//        ShadeRayResult nearest = null;
//        for (ActiveContraption ac : findNearby(level, start, (int) Math.ceil(start.distanceTo(end)) + 2))
//        {
//            Vec3 localStart = ac.entity().toLocalVector(start, 1f);
//            Vec3 localEnd = ac.entity().toLocalVector(end, 1f);
//
//            BlockHitResult hit = ac.contraption().getContraptionWorld().clip(new ClipContext(localStart, localEnd, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
//
//            if (hit.getType() != HitResult.Type.MISS)
//            {
//                double hitDist = localStart.distanceTo(hit.getLocation());
//                if (nearest == null || hitDist < nearest.hitDist()) nearest = new ShadeRayResult(hitDist, hit.getBlockPos());
//            }
//        }
//
//        return nearest;
//    }
//
//    public static BlockSearchResult collectSources(Level level, Vec3 origin, int radius, BlockSearchResult result)
//    {
//        if (!IS_LOADED) return result;
//
//        double radiusSq = (double) radius * radius;
//
//        Map<Block, List<BlockPos>> mergedBlocks = new HashMap<>(result.allPositions());
//        Map<Fluid, List<BlockPos>> mergedFluids = new HashMap<>(result.allFluidPositions());
//        Map<Block, Integer> blockCounts = new HashMap<>();
//        Map<Fluid, Integer> fluidCounts = new HashMap<>();
//
//        Map<Long, SourceAtPos> positionIndex = new HashMap<>();
//
//        for (Map.Entry<Block, List<BlockPos>> entry : mergedBlocks.entrySet())
//        {
//            BlockTemperatureDataMap dataMap = BuiltInRegistries.BLOCK.wrapAsHolder(entry.getKey()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
//            boolean tfc = dataMap != null && dataMap.hasTFCHeat();
//
//            blockCounts.put(entry.getKey(), entry.getValue().size());
//            for (BlockPos pos : entry.getValue()) positionIndex.put(pos.asLong(), new SourceAtPos(entry.getKey(), resolveWorldBlockTemperature(level, pos, entry.getKey()), tfc));
//        }
//        for (Map.Entry<Fluid, List<BlockPos>> entry : mergedFluids.entrySet())
//        {
//            fluidCounts.put(entry.getKey(), entry.getValue().size());
//            for (BlockPos pos : entry.getValue()) positionIndex.put(pos.asLong(), new SourceAtPos(entry.getKey(), fluidTemperature(entry.getKey()), false));
//        }
//
//        for (ActiveContraption ac : findNearby(level, origin, radius))
//        {
//            for (Map.Entry<BlockPos, StructureTemplate.StructureBlockInfo> entry : ac.contraption().getBlocks().entrySet())
//            {
//                BlockPos localPos = entry.getKey();
//                BlockState state = entry.getValue().state();
//
//                BlockTemperatureDataMap blockDataMap = BuiltInRegistries.BLOCK.wrapAsHolder(state.getBlock()).getData(ThermiaDataMaps.BLOCK_TEMPERATURE_DATA_MAP);
//                if (blockDataMap == null) continue;
//
//                Vec3 worldPos = localToWorld(ac, localPos);
//                if (origin.distanceToSqr(worldPos) > radiusSq) continue;
//                BlockPos worldBlockPos = BlockPos.containing(worldPos);
//
//                FluidSource fluidSource = mountedFluidSource(ac, localPos);
//                Object key;
//                float sourceTemp;
//
//                if (fluidSource != null)
//                {
//                    key = fluidSource.fluid();
//                    sourceTemp = fluidSource.temperature();
//                }
//                else if (blockDataMap.isRadiative() || blockDataMap.temperature() < 0f)
//                {
//                    key = state.getBlock();
//                    sourceTemp = resolveStateTemperature(state, blockDataMap);
//                }
//                else continue;
//
//                SourceAtPos existing = positionIndex.get(worldBlockPos.asLong());
//                if (existing != null)
//                {
//                    if (existing.tfc()) continue;
//                    if (blockDataMap.hasTFCHeat())
//                    {
//                        removeSource(mergedBlocks, mergedFluids, worldBlockPos, existing.key());
//                    }
//                    if (existing.temperature() >= sourceTemp) continue;
//                    removeSource(mergedBlocks, mergedFluids, worldBlockPos, existing.key());
//                }
//
//                int cap;
//                if (key instanceof Block block)
//                {
//                    cap = blockDataMap.searchCap();
//                    if (blockCounts.getOrDefault(block, 0) >= cap) continue;
//                    mergedBlocks.computeIfAbsent(block, k -> new ArrayList<>()).add(worldBlockPos);
//                    blockCounts.put(block, blockCounts.getOrDefault(block, 0) + 1);
//                }
//                else
//                {
//                    Fluid fluid = (Fluid) key;
//                    FluidTemperatureDataMap fluidDataMap = BuiltInRegistries.FLUID.wrapAsHolder(fluid).getData(ThermiaDataMaps.FLUID_TEMPERATURE_DATA_MAP);
//                    if (fluidDataMap == null) continue;
//                    cap = fluidDataMap.searchCap();
//                    if (fluidCounts.getOrDefault(fluid, 0) >= cap) continue;
//                    mergedFluids.computeIfAbsent(fluid, k -> new ArrayList<>()).add(worldBlockPos);
//                    fluidCounts.put(fluid, fluidCounts.getOrDefault(fluid, 0) + 1);
//                }
//
//                positionIndex.put(worldBlockPos.asLong(), new SourceAtPos(key, sourceTemp, false));
//            }
//        }
//
//        return result.withAllPositions(mergedBlocks).withAllFluidPositions(mergedFluids);
//    }
//}
