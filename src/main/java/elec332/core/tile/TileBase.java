package elec332.core.tile;

import com.google.common.collect.Lists;
import elec332.core.main.ElecCore;
import elec332.core.network.IElecCoreNetworkTile;
import elec332.core.network.PacketReRenderBlock;
import elec332.core.network.PacketTileDataToServer;
import elec332.core.server.ServerHelper;
import elec332.core.util.BlockLoc;
import elec332.core.util.DirectionHelper;
import elec332.core.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

/**
 * Created by Elec332 on 8-4-2015.
 */
public class TileBase extends TileEntity implements IElecCoreNetworkTile, ITickable {

    @Override
    public void validate() {
        super.validate();
        ElecCore.tickHandler.registerCall(new Runnable() {
            @Override
            public void run() {
                if (WorldHelper.chunkExists(worldObj, getPos())) {
                    onTileLoaded();
                }
            }
        }, getWorld());
    }


    public void invalidate() {
        if (!isInvalid()){
            super.invalidate();
            onTileUnloaded();
        }
    }

    public void onChunkUnload() {
        //if (!isInvalid()) {
            super.onChunkUnload();
            //super.invalidate();
            onTileUnloaded();
        //}
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



    @SuppressWarnings("deprecation")
    public final void update(){
        if (canUpdate())
            updateEntity();
    }

    @Deprecated
    public void updateEntity() {
    }

    @Deprecated
    public boolean canUpdate() {
        return true;
    }

    public void notifyNeighboursOfDataChange(){
        this.markDirty();
        this.worldObj.notifyNeighborsOfStateChange(getPos(), blockType);
    }

    public EnumFacing getTileFacing(){
        return DirectionHelper.getDirectionFromNumber(getBlockMetadata());
    }

    @Deprecated
    public BlockLoc myLocation(){
        return new BlockLoc(getPos());
    }

    public boolean timeCheck() {
        return this.worldObj.getTotalWorldTime() % 32L == 0L;
    }

    protected void setBlockMetadataWithNotify(int meta){
        this.worldObj.setBlockState(getPos(), getBlockType().getStateFromMeta(meta), 2);
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

    public boolean onBlockActivated(EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public ArrayList<ItemStack> getDrops(int fortune){
        return Lists.newArrayList(itemStackFromNBTTile());
    }

    public void onWrenched(EnumFacing forgeDirection) {
        DirectionHelper.setFacing_YAW(worldObj, getPos(), forgeDirection);
    }

    public boolean openGui(EntityPlayer player, Object mod, int guiID){
        player.openGui(mod, guiID, worldObj, getPos().getX(), getPos().getY(), getPos().getZ());
        return true;
    }

    protected ItemStack itemStackFromNBTTile(){
        NBTTagCompound compound = new NBTTagCompound();
        ItemStack ret = new ItemStack(getBlockType(), 1, getBlockMetadata());
        writeToItemStack(compound);
        ret.setTagCompound(compound);
        return ret;
    }

    protected void setMetaForFacingOnPlacement(EntityLivingBase entityLivingBase){
        setBlockMetadataWithNotify(DirectionHelper.getDirectionNumberOnPlacement(entityLivingBase));
    }

    public void reRenderBlock(){
        if (!worldObj.isRemote){
            ServerHelper.instance.sendMessageToAllPlayersWatchingBlock(worldObj, getPos(), new PacketReRenderBlock(this), ElecCore.networkHandler);
        } else {
            WorldHelper.markBlockForRenderUpdate(worldObj, getPos());
        }
    }

    //NETWORK///////////////////////

    public void syncData(){
        this.worldObj.markBlockForUpdate(getPos());
    }

    @SideOnly(Side.CLIENT)
    public void sendPacketToServer(int ID, NBTTagCompound data){
        ElecCore.networkHandler.getNetworkWrapper().sendToServer(new PacketTileDataToServer(this, ID, data));
    }

    public void onPacketReceivedFromClient(EntityPlayerMP sender, int ID, NBTTagCompound data){
    }

    public void sendPacket(int ID, NBTTagCompound data){
        for (EntityPlayerMP player : ServerHelper.instance.getAllPlayersWatchingBlock(worldObj, getPos()))
            sendPacketTo(player, ID, data);
    }

    public void sendPacketTo(EntityPlayerMP player, int ID, NBTTagCompound data){
        player.playerNetServerHandler.sendPacket(new S35PacketUpdateTileEntity(getPos(), ID, data));
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBT(nbtTag);
        return new S35PacketUpdateTileEntity(getPos(), 0, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        if (packet.getTileEntityType() == 0)
            readFromNBT(packet.getNbtCompound());
        else onDataPacket(packet.getTileEntityType(), packet.getNbtCompound());
    }

    public void onDataPacket(int id, NBTTagCompound tag){
    }
}
