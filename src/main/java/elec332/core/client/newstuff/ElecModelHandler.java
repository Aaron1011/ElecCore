package elec332.core.client.newstuff;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import elec332.core.api.annotations.ASMDataProcessor;
import elec332.core.api.util.IASMDataHelper;
import elec332.core.api.util.IASMDataProcessor;
import elec332.core.main.ElecCore;
import elec332.core.util.RegistryHelper;
import mcmultipart.client.multipart.MultipartRegistryClient;
import mcmultipart.multipart.IMultipart;
import mcmultipart.multipart.MultipartRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Elec332 on 11-3-2016.
 */
@SideOnly(Side.CLIENT)
@ASMDataProcessor(LoaderState.PREINITIALIZATION)
public final class ElecModelHandler implements IASMDataProcessor {

    private static List<IBlockModelHandler> blockModelHandlers;
    private static List<IItemModelHandler> itemModelHandlers;
    private static List<IMultipartModelHandler> multipartModelHandlers;
    private static Map<ModelResourceLocation, IBakedModel> models;
    private static Map<Item, ModelResourceLocation> itemResourceLocations;
    private static Map<IBlockState, ModelResourceLocation> blockResourceLocations;
    private static Map<Pair<IBlockState, IMultipart>, ModelResourceLocation> multipartResourceLocations;

    private static boolean mcmp = Loader.isModLoaded("mcmultipart");

    @Override
    public void processASMData(IASMDataHelper asmData, LoaderState state) {
        for (ASMDataTable.ASMData data : asmData.getAnnotationList(ModelHandler.class)){
            String s = data.getClassName();
            try {
                Object instance = Class.forName(s).newInstance();
                if (instance instanceof IBlockModelHandler){
                    blockModelHandlers.add((IBlockModelHandler) instance);
                }
                if (instance instanceof IItemModelHandler){
                    itemModelHandlers.add((IItemModelHandler) instance);
                }
                if (instance instanceof IMultipartModelHandler){
                    multipartModelHandlers.add((IMultipartModelHandler) instance);
                }
            } catch (Exception e){
                throw new RuntimeException("Error registering ModelHandler class: "+s, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void registerItemModels(RenderItem renderItem){
        ElecCore.logger.info("Prehandling Items");
        /*IdentityHashMap<Item, TIntObjectHashMap<IBakedModel>> models = null;
        try {
            models = (IdentityHashMap<Item, TIntObjectHashMap<IBakedModel>>) ReflectionHelper.makeFieldAccessible(ItemModelMesherForge.class.getDeclaredField("models")).get(renderItem.getItemModelMesher());
        } catch (Exception e){
            e.printStackTrace();
        }
        if (models == null){
            throw new RuntimeException();
        }*/
        for (Item item : RegistryHelper.getItemRegistry().typeSafeIterable()){
            for (IItemModelHandler handler : itemModelHandlers){
                if (handler.handleItem(item)){
                    //models.put(item, new InternalItemMap<IBakedModel>());
                    String s = handler.getIdentifier(item);
                    final ModelResourceLocation mr = new ModelResourceLocation(item.delegate.name().toString(), s);
                    renderItem.getItemModelMesher().register(item, new ItemMeshDefinition() {
                        @Override
                        public ModelResourceLocation getModelLocation(ItemStack stack) {
                            return mr;
                        }
                    });
                    itemResourceLocations.put(item, mr);
                    break;
                }
            }
        }
    }

    public static void registerBlockModels(ModelManager modelManager){
        ElecCore.logger.info("Prehandling blocks");
        for (Block block : RegistryHelper.getBlockRegistry().typeSafeIterable()){
            for (final IBlockModelHandler handler : blockModelHandlers) {
                if (handler.handleBlock(block)) {
                    modelManager.getBlockModelShapes().getBlockStateMapper().registerBlockStateMapper(block, new StateMapperBase() {
                        @Override
                        protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                            ModelResourceLocation mrl = new ModelResourceLocation(state.getBlock().delegate.name().toString() + "#" + handler.getIdentifier(state));
                            blockResourceLocations.put(state, mrl);
                            return mrl;
                        }
                    });
                    break;
                }
            }
        }
    }

    public static void registerMultiPartModels(){
        if (mcmp) {
            ElecCore.logger.info("Prehandling MultiParts");
            for (final ResourceLocation part : MultipartRegistry.getRegisteredParts()) {
                for (final IMultipartModelHandler handler : multipartModelHandlers) {
                    final IMultipart mPart = MultipartRegistry.createPart(part, new NBTTagCompound());
                    if (handler.handlePart(mPart)) {
                        MultipartRegistryClient.registerSpecialPartStateMapper(part, new StateMapperBase() {
                            @Override
                            public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block blockIn) {
                                for (IBlockState iblockstate : MultipartRegistry.getDefaultState(part).getValidStates()) {
                                    this.mapStateModelLocations.put(iblockstate, this.getModelResourceLocation(iblockstate));
                                }
                                return this.mapStateModelLocations;
                            }

                            @Override
                            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                                ModelResourceLocation mrl = new ModelResourceLocation(part, handler.getIdentifier(state, mPart));
                                multipartResourceLocations.put(Pair.of(state, mPart), mrl);
                                return mrl;
                            }
                        });
                    }
                }
            }
        }
    }

    public static Set<ModelResourceLocation> registerBakedModels(IRegistry<ModelResourceLocation, IBakedModel> registry){
        ElecCore.logger.info("handling models");
        Set<ModelResourceLocation> ret = Sets.newHashSet();
        List<ModelResourceLocation> o = Lists.newArrayList();
        IBakedModel missingModel = registry.getObject(ModelBakery.MODEL_MISSING);
        for (Map.Entry<IBlockState, ModelResourceLocation> entry : blockResourceLocations.entrySet()){
            ModelResourceLocation mrl = entry.getValue();
            for (IBlockModelHandler handler : blockModelHandlers){
                if (handler.handleBlock(entry.getKey().getBlock())){
                    IBakedModel model = handler.getModelFor(entry.getKey(), mrl.getVariant(), mrl);
                    if (model == null){
                        if (models.get(mrl) == null){
                            o.add(mrl);
                        }
                        break;
                    }
                    models.put(mrl, model);
                    ret.add(mrl);
                    break;
                }
            }
        }
        for (Map.Entry<Item, ModelResourceLocation> entry : itemResourceLocations.entrySet()){
            ModelResourceLocation mrl = entry.getValue();
            for (IItemModelHandler handler : itemModelHandlers){
                if (handler.handleItem(entry.getKey())){
                    IBakedModel model = handler.getModelFor(entry.getKey(), mrl.getVariant(), mrl);
                    if (model == null){
                        if (models.get(mrl) == null){
                            o.add(mrl);
                        }
                        break;
                    }
                    models.put(mrl, model);
                    ret.add(mrl);
                    break;
                }
            }
        }
        if (mcmp) {
            for (Map.Entry<Pair<IBlockState, IMultipart>, ModelResourceLocation> entry : multipartResourceLocations.entrySet()) {
                ModelResourceLocation mrl = entry.getValue();
                for (IMultipartModelHandler handler : multipartModelHandlers) {
                    Pair<IBlockState, IMultipart> pair = entry.getKey();
                    IMultipart multipart = pair.getRight();
                    if (handler.handlePart(multipart)) {
                        IBakedModel model = handler.getModelFor(multipart, pair.getLeft(), mrl.getVariant(), mrl);
                        if (model == null) {
                            if (models.get(mrl) == null) {
                                o.add(mrl);
                            }
                            break;
                        }
                        models.put(mrl, model);
                        ret.add(mrl);
                        break;
                    }
                }
            }
        }
        for (ModelResourceLocation mrl : o){
            if (models.get(mrl) == null){
                models.put(mrl, missingModel);
            }
        }
        for (Map.Entry<ModelResourceLocation, IBakedModel> entry : models.entrySet()){
            registry.putObject(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    static {
        blockModelHandlers = Lists.newArrayList();
        itemModelHandlers = Lists.newArrayList();
        multipartModelHandlers = Lists.newArrayList();
        models = Maps.newHashMap();
        itemResourceLocations = Maps.newHashMap();
        blockResourceLocations = Maps.newHashMap();
        multipartResourceLocations = Maps.newHashMap();
    }

}
