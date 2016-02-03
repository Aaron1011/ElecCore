package elec332.core.multiblock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import elec332.core.network.NetworkHandler;
import elec332.core.registry.AbstractWorldRegistryHolder;
import elec332.core.registry.IWorldRegistry;
import elec332.core.world.WorldHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Elec332 on 27-7-2015.
 */
public final class MultiBlockRegistry extends AbstractWorldRegistryHolder<MultiBlockRegistry.MultiBlockWorldRegistry>{

    public MultiBlockRegistry(){
        this(new NetworkHandler(Loader.instance().activeModContainer().getModId()+"|MultiBlocks"));
    }

    public MultiBlockRegistry(NetworkHandler networkHandler){
        this.registry = Maps.newHashMap();
        this.networkHandler = networkHandler;
        this.structureRegistry = new MultiBlockStructureRegistry(this);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
     public boolean serverOnly() {
        return false;
    }

    @Override
    public MultiBlockWorldRegistry newRegistry(World world) {
        return new MultiBlockWorldRegistry(world);
    }

    private HashMap<Class<? extends IMultiBlockStructure>, Class<? extends IMultiBlock>> registry;
    private final MultiBlockStructureRegistry structureRegistry;
    protected final NetworkHandler networkHandler;


    public void registerMultiBlock(IMultiBlockStructure multiBlockStructure, String name, Class<? extends IMultiBlock> multiBlock){
        registry.put(multiBlockStructure.getClass(), multiBlock);
        structureRegistry.registerMultiBlockStructure(multiBlockStructure, name);
    }

    public MultiBlockStructureRegistry getStructureRegistry() {
        return this.structureRegistry;
    }

    public class MultiBlockWorldRegistry implements IWorldRegistry{

        protected MultiBlockWorldRegistry(World world){
            this.world = world;
            this.activeMultiBlocks = Lists.newArrayList();
            this.pausedMultiBlocks = Lists.newArrayList();
        }

        private final World world;
        private List<IMultiBlock> activeMultiBlocks;
        private List<IMultiBlock> pausedMultiBlocks;

        protected World getWorldObj(){
            return world;
        }

        protected void invalidateMultiBlock(IMultiBlock multiBlock){
            for (BlockPos loc : multiBlock.getAllMultiBlockLocations()){
                TileEntity tile = WorldHelper.getTileAt(world, loc);
                if (tile instanceof IMultiBlockTile){
                    ((IMultiBlockTile)tile).invalidateMultiBlock();
                }
            }
            this.activeMultiBlocks.remove(multiBlock);
            multiBlock.invalidate();
        }

        protected void deactivateMultiBlock(IMultiBlock multiBlock){
            this.activeMultiBlocks.remove(multiBlock);
            this.pausedMultiBlocks.add(multiBlock);
        }

        protected void reactivateMultiBlock(IMultiBlock multiBlock){
            this.pausedMultiBlocks.remove(multiBlock);
            this.activeMultiBlocks.add(multiBlock);
        }

        protected void createNewMultiBlock(IMultiBlockStructure multiBlockStructure, BlockPos bottomLeft, List<BlockPos> allLocations, World world, EnumFacing facing){
            Class<? extends IMultiBlock> clazz = registry.get(multiBlockStructure.getClass());
            IMultiBlock multiBlock;
            try {
                multiBlock = clazz.getConstructor().newInstance();
            } catch (Exception e){
                throw new RuntimeException("Error invoking class: "+clazz.getName()+" Please make sure the constructor has no arguments!", e);
            }
            boolean one = false;
            for (BlockPos loc : allLocations){
                TileEntity tile = WorldHelper.getTileAt(world, loc);
                if (tile instanceof IMultiBlockTile){
                    ((IMultiBlockTile)tile).setMultiBlock(multiBlock, facing, structureRegistry.getIdentifier(multiBlockStructure));
                    one = true;
                }
            }
            if (!one)
                throw new IllegalArgumentException("A multiblock must contain at LEAST 1 IMultiBlockTile");
            multiBlock.initMain(bottomLeft, facing, allLocations, this, structureRegistry.getIdentifier(multiBlockStructure));
            activeMultiBlocks.add(multiBlock);
        }

        /**
         * Gets called every tick
         */
        @Override
        public void tick() {
            for (IMultiBlock multiBlock : activeMultiBlocks){
                multiBlock.onTick();
            }
        }

        /**
         * Gets called when the world unloads, just before it is removed from the registry and made ready for the GC
         */
        @Override
        public void onWorldUnload() {
        }
    }

}
