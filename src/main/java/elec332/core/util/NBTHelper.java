package elec332.core.util;

import com.google.common.base.Function;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created by Elec332 on 28-5-2015.
 */
public class NBTHelper implements INBTSerializable<NBTTagCompound> {

    public NBTHelper(){
        this(new NBTTagCompound());
    }

    public NBTHelper(NBTHelper mainTag){
        this(mainTag.tagCompound);
    }

    public NBTHelper(NBTTagCompound tagCompound){
        this.tagCompound = tagCompound;
    }

    private NBTTagCompound tagCompound;


    /*
     * Add methods
     */

    public NBTHelper addToTag(List<String> list, String s){
        if (list.size() > 0){
            NBTTagList tagList = new NBTTagList();
            for (String string : list){
                tagList.appendTag(new NBTHelper().addToTag(string, s).serializeNBT());
            }
            tagCompound.setTag(s, tagList);
        }
        return this;
    }

    public NBTHelper addToTag(int i, String s){
        tagCompound.setInteger(s, i);
        return this;
    }

    public NBTHelper addToTag(UUID uuid, String s){
        tagCompound.setUniqueId(s, uuid);
        return this;
    }

    public NBTHelper addToTag(String data, String saveName){
        tagCompound.setString(saveName, data);
        return this;
    }

    public NBTHelper addToTag(boolean b, String s){
        tagCompound.setBoolean(s, b);
        return this;
    }

    public NBTHelper addToTag(float f, String s){
        tagCompound.setFloat(s, f);
        return this;
    }

    public NBTHelper addToTag(BlockPos pos){
        return addToTag(pos, "blockLoc");
    }

    public NBTHelper addToTag(BlockPos pos, String s){
        return addToTag(new NBTHelper().addToTag(pos.getX(), "x").addToTag(pos.getY(), "y").addToTag(pos.getZ(), "z"), s);
    }

    public NBTHelper addToTag(Long l, String s){
        tagCompound.setLong(s, l);
        return this;
    }

    public NBTHelper addToTag(NBTHelper nbt, String s){
        return addToTag(nbt.tagCompound, s);
    }

    public NBTHelper addToTag(NBTBase nbtBase, String s){
        tagCompound.setTag(s, nbtBase);
        return this;
    }

    public NBTHelper addToTag(Enum e, String s){
        return addToTag(EnumHelper.getName(e), s);
    }

    /*
     * Readers
     */

    public BlockPos getPos(){
        return getPos("blockLoc");
    }

    public BlockPos getPos(String s){
        NBTTagCompound tag = getCompoundTag(s);
        return new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
    }

    public long getLong(String s){
        return tagCompound.getLong(s);
    }

    public int getInteger(String s){
        return tagCompound.getInteger(s);
    }

    public NBTTagCompound getCompoundTag(String s){
        return tagCompound.getCompoundTag(s);
    }

    public String getString(String s){
        return tagCompound.getString(s);
    }

    public UUID getUUID(String s){
        return tagCompound.getUniqueId(s);
    }

    public <E extends Enum> E getEnum(String s, Class<E> c){
        return EnumHelper.fromString(getString(s), c);
    }


    @Override
    public NBTTagCompound serializeNBT() {
        return this.tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.tagCompound = nbt;
    }

    public static class DefaultCallable implements Callable<NBTHelper> {

        @Override
        public NBTHelper call() throws Exception {
            return new NBTHelper();
        }

    }

    public static class DefaultFunction<F> implements Function<F, NBTHelper>{

        @Override
        public NBTHelper apply(F input) {
            return new NBTHelper();
        }

    }

}
