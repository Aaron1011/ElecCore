package elec332.core.baseclasses.tileentity;

import com.google.common.collect.Lists;
import elec332.core.main.ElecCore;
import elec332.core.util.BlockLoc;
import elec332.core.util.DirectionHelper;
import elec332.core.util.IRunOnce;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;

/**
 * Created by Elec332 on 8-4-2015.
 */
public class TileBase extends TileEntity {

    @Override
    public void validate() {
        super.validate();
        ElecCore.tickHandler.registerCall(new IRunOnce() {
            @Override
            public void run() {
                if (getWorldObj().blockExists(xCoord, yCoord, zCoord)) {
                    onTileLoaded();
                }
            }
        });
    }


    public void invalidate() {
        if (!isInvalid()){
            super.invalidate();
            onTileUnloaded();
        }
    }

    public void onChunkUnload() {
        if (!isInvalid()) {
            super.onChunkUnload();
            super.invalidate();
            onTileUnloaded();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        readItemStackNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        writeToItemStack(tagCompound);
    }

    /**
     * Reads NBT data from either the NBTTagCompound from readFromNBT when a tile loads,
     * or the ItemStack, if the block is placed.
     *
     * @param tagCompound The NBT Tag compound
     */
    public void readItemStackNBT(NBTTagCompound tagCompound){
    }


    /**
     * Writes the NBT data, gets run when the tile unloads and when the block is broken,
     * when the block is broken, this data will be stored on the ItemStack, if the tile unloads,
     * this gets treated as the writeToNBT method.
     *
     * @param tagCompound The NBT Tag compound
     */
    public void writeToItemStack(NBTTagCompound tagCompound){
    }

    public void onTileLoaded(){
    }

    public void onTileUnloaded(){
    }

    public void notifyNeighboursOfDataChange(){
        this.markDirty();
        this.worldObj.notifyBlockChange(xCoord, yCoord, zCoord, blockType);
    }

    public BlockLoc myLocation(){
        return new BlockLoc(this.xCoord, this.yCoord, this.zCoord);
    }

    public boolean timeCheck() {
        return this.worldObj.getTotalWorldTime() % 32L == 0L;
    }

    protected void setBlockMetadataWithNotify(int meta){
        this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta, 2);
        notifyNeighboursOfDataChange();
    }

    public int getLightOpacity() {
        return 0;
    }

    public int getLightValue() {
        return 0;
    }

    public void onBlockRemoved() {

    }

    public void onBlockAdded() {

    }

    public void onBlockPlacedBy(EntityLivingBase entityLiving, ItemStack stack) {
        if (stack.hasTagCompound())
            readItemStackNBT(stack.getTagCompound());
        setMetaForFacingOnPlacement(entityLiving);
    }

    public void onNeighborBlockChange(Block block) {

    }

    public boolean onBlockActivated(EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public ArrayList<ItemStack> getDrops(int fortune){
        return Lists.newArrayList(itemStackFromNBTTile());
    }

    public void onWrenched(ForgeDirection forgeDirection) {
        DirectionHelper.setFacing_YAW(worldObj, xCoord, yCoord, zCoord, forgeDirection);
    }

    protected ItemStack itemStackFromNBTTile(){
        NBTTagCompound compound = new NBTTagCompound();
        ItemStack ret = new ItemStack(getBlockType(), 1, getBlockMetadata());
        writeToItemStack(compound);
        ret.setTagCompound(compound);
        return ret;
    }

    protected void setMetaForFacingOnPlacement(EntityLivingBase entityLivingBase){
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, DirectionHelper.getDirectionNumberOnPlacement(entityLivingBase), 2);
    }
}
