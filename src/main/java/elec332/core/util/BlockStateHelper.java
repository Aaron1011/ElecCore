package elec332.core.util;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

/**
 * Created by Elec332 on 7-12-2015.
 */
public interface BlockStateHelper<M extends Comparable<M>> {

    public static final BlockStateHelper<EnumFacing> FACING_NORMAL = new BlockStateHelper<EnumFacing>() {

        private final PropertyEnum<EnumFacing> TYPE = PropertyEnum.create("facing", EnumFacing.class);

        @Override
        public BlockState createMetaBlockState(Block block) {
            return new BlockState(block, TYPE);
        }

        @Override
        public IBlockState setDefaultMetaState(Block block) {
            return block.getBlockState().getBaseState().withProperty(TYPE, EnumFacing.NORTH);
        }

        @Override
        public IBlockState getStateForMeta(Block block, int meta) {
            return block.getBlockState().getBaseState().withProperty(TYPE, EnumFacing.values()[meta]);
        }

        @Override
        public int getMetaForState(IBlockState state) {
            return state.getValue(TYPE).ordinal();
        }

        @Override
        public IProperty<EnumFacing> getProperty() {
            return TYPE;
        }
    };

    public BlockState createMetaBlockState(Block block);

    public IBlockState setDefaultMetaState(Block block);

    public IBlockState getStateForMeta(Block block, int meta);

    public int getMetaForState(IBlockState state);

    public IProperty<M> getProperty();

}
