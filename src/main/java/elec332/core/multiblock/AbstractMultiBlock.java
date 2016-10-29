package elec332.core.multiblock;

import elec332.core.api.info.IInfoDataAccessorBlock;
import elec332.core.api.info.IInfoProvider;
import elec332.core.api.info.IInformation;
import elec332.core.tile.IInventoryTile;
import elec332.core.tile.TileBase;
import elec332.core.world.WorldHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

/**
 * Created by Elec332 on 28-7-2015.
 */
public abstract class AbstractMultiBlock extends IMultiBlock implements IInventoryTile, IInfoProvider, IMultiBlockCapabilityProvider, ISpecialMultiBlockCapabilityProvider {

    public boolean onAnyBlockActivated(EntityPlayer player, EnumHand hand, ItemStack stack, BlockPos pos, IBlockState state){
        return false;
    }

    public boolean isSaveDelegate(AbstractMultiBlockTile tile){
        return tile.getPos().equals(getLocation());
    }

    public TileBase getSaveDelegate(){
        return (TileBase) getTileAt(getLocation());
    }

    public TileEntity getTileAt(BlockPos loc){
        return WorldHelper.getTileAt(getWorldObj(), loc);
    }

    public boolean isServer(){
        return !getWorldObj().isRemote;
    }

    public BlockPos getBlockLocAtTranslatedPos(int length, int width, int height){
        return MultiBlockStructureRegistry.getTranslated(getLocation(), getMultiBlockFacing(), length, width, height);
    }

    public TileEntity getTileAtTranslatedPos(int length, int width, int height){
        return getTileAt(getBlockLocAtTranslatedPos(length, width, height));
    }

    public void markDirty(){
        getSaveDelegate().markDirty();
    }

    public void writeToNBT(NBTTagCompound tagCompound){
    }

    public void readFromNBT(NBTTagCompound tagCompound){
    }

    @Override
    public void addInformation(@Nonnull IInformation information, @Nonnull IInfoDataAccessorBlock hitData) {
    }

    @Nonnull
    @Override
    public NBTTagCompound getInfoNBTData(@Nonnull NBTTagCompound tag, TileEntity tile, @Nonnull EntityPlayerMP player, @Nonnull IInfoDataAccessorBlock hitData) {
        return tag;
    }

    public final boolean openGui(EntityPlayer player, Object mod, int ID){
        player.openGui(mod, ID, getWorldObj(), getLocation().getX(), getLocation().getY(), getLocation().getZ());
        return true;
    }

    public final boolean openGui(EntityPlayer player, Object mod){
        return openGui(player, mod, 0);
    }

    @Override
    public final Container getGuiServer(EntityPlayer player){
        return (Container) getGui(player, false);
    }

    @Override
    public final Object getGuiClient(EntityPlayer player) {
        return getGui(player, true);
    }

    public Object getGui(EntityPlayer player, boolean client){
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing, @Nonnull BlockPos pos) {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing, @Nonnull BlockPos pos) {
        return null;
    }

    @Override
    public <T> T getSpecialCapability(Capability<T> capability, EnumFacing facing, @Nonnull BlockPos pos) {
        return null;
    }

}
