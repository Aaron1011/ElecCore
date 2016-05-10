package elec332.core.multiblock;

import com.google.common.collect.ImmutableList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

/**
 * Created by Elec332 on 27-7-2015.
 */
public abstract class IMultiBlock {

    public IMultiBlock(){
        this.identifier = UUID.randomUUID();
    }

    public final void initMain(BlockPos location, EnumFacing facing, List<BlockPos> allMultiBlockLocations, MultiBlockRegistry.MultiBlockWorldRegistry registry, String structureID){
        if (this.loc != null)
            throw new RuntimeException();
        this.loc = location;
        this.facing = facing;
        this.allMultiBlockLocations = ImmutableList.copyOf(allMultiBlockLocations);
        this.multiBlockRegistry = registry;
        this.structureID = structureID;
        init();
        this.multiBlockState = State.active;
    }

    private final UUID identifier;
    private BlockPos loc;
    private EnumFacing facing;
    private List<BlockPos> allMultiBlockLocations;
    private MultiBlockRegistry.MultiBlockWorldRegistry multiBlockRegistry;
    private State multiBlockState;
    private String structureID;

    public final String getStructureID() {
        return this.structureID;
    }

    public final BlockPos getLocation() {
        return this.loc;
    }

    public final EnumFacing getMultiBlockFacing(){
        return this.facing;
    }

    public final List<BlockPos> getAllMultiBlockLocations() {
        return this.allMultiBlockLocations;
    }

    /**
     * Initialise your multiblock here, all fields provided by @link IMultiblock have already been given a value
     */
    public abstract void init();

    /**
     * This gets run server-side only
     */
    public abstract void onTick();

    /**
     * Invalidate your multiblock here
     */
    public abstract void invalidate();

    public World getWorldObj(){
        return this.multiBlockRegistry.getWorldObj();
    }

    /**
     * Make sure this gets called when any of the tiles in this multiblock invalidate
     */
    public void tileEntityInvalidate(){
        multiBlockRegistry.invalidateMultiBlock(this);
    }

    /**
     * Make sure this gets called when any of the tiles in this multiblock validate
     */
    public void tileEntityValidate(){
        if (this.multiBlockState == State.paused) {
            System.out.println("reactivating...");
            this.multiBlockRegistry.reactivateMultiBlock(this);
            this.multiBlockState = State.active;
            System.out.println("reactivated");
        }
    }

    /**
     * Make sure this gets called when any of the tiles in this multiblock unload
     */
    public void tileEntityChunkUnload(IMultiBlockTile tile){
        //multiBlockRegistry.tileChunkUnloaded(this, tile);
        multiBlockRegistry.deactivateMultiBlock(this);
        this.multiBlockState = State.paused;
    }

    public static void tileEntityInvalidate(IMultiBlock multiBlock){
        if (multiBlock == null)
            return;
        multiBlock.tileEntityInvalidate();
    }

    public static void tileEntityChunkUnload(IMultiBlock multiBlock, IMultiBlockTile tile){
        if (multiBlock == null)
            return;
        multiBlock.tileEntityChunkUnload(tile);
    }

    public static void tileEntityValidate(IMultiBlockTile tile, IMultiBlock multiBlock, MultiBlockRegistry multiBlockRegistry) {
        if (!tile.isValidMultiBlock())
            return;
        if (multiBlock != null) {
            multiBlock.tileEntityValidate();
        } else {
            multiBlockRegistry.getStructureRegistry().attemptReCreate(tile.getStructureIdentifier(), (TileEntity) tile, tile.getMultiBlockFacing());
        }
    }

    @Override
    public int hashCode() {
        return loc.hashCode()*allMultiBlockLocations.size();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IMultiBlock && ((IMultiBlock) obj).identifier.equals(identifier) && obj.hashCode() == hashCode();
    }

    protected enum State{
        active, paused
    }
}
