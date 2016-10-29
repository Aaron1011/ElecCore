package elec332.core.main;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import elec332.core.api.APIHandlerInject;
import elec332.core.api.discovery.ASMDataProcessor;
import elec332.core.api.discovery.IASMDataHelper;
import elec332.core.api.discovery.IASMDataProcessor;
import elec332.core.api.discovery.IAdvancedASMData;
import elec332.core.api.world.IWorldGenManager;
import elec332.core.java.ReflectionHelper;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.LoaderState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

/**
 * Created by Elec332 on 29-10-2016.
 */
@ASMDataProcessor(LoaderState.CONSTRUCTING)
public enum APIHandler implements IASMDataProcessor {

    INSTANCE;

    APIHandler(){
        callBacks = HashMultimap.create();
    }

    private final SetMultimap<Class<?>, Consumer<?>> callBacks;

    @Override
    public void processASMData(IASMDataHelper asmData, LoaderState state) {
        for (IAdvancedASMData data : asmData.getAdvancedAnnotationList(APIHandlerInject.class)){

            Consumer<?> ret;
            Class<?> type;

            if (data.isMethod()){
                Class[] params = data.getMethodParameters();
                if (params.length > 1 || params.length < 0){
                    ElecCore.logger.error("Skipping invalid API method: "+data.getClassName() + " "+ data.getMethodName());
                }
                type = params[0];
                ret = new Consumer<Object>() {

                    @Override
                    public void accept(Object o) {
                        if (!ReflectionHelper.isStatic(data.getMethod())){
                            ElecCore.logger.error("Field "+data.getClassName() + " "+ data.getMethodName()+" is not static! it will be skipped...");
                            return;
                        }
                        try {
                            data.getMethod().invoke(null, o);
                        } catch (Exception e){
                            throw new RuntimeException(e);
                        }
                    }

                };
            } else {
                type = data.getFieldType();
                ret = new Consumer<Object>() {

                    @Override
                    public void accept(Object o) {
                        if (!ReflectionHelper.isStatic(data.getField())){
                            ElecCore.logger.error("Field "+data.getClassName() + " "+ data.getFieldName()+" is not static! it will be skipped...");
                            return;
                        }
                        try {
                            EnumHelper.setFailsafeFieldValue(data.getField(), null, o);
                        } catch (Exception e){
                            throw new RuntimeException(e);
                        }
                    }

                };
            }

            callBacks.put(Preconditions.checkNotNull(type), Preconditions.checkNotNull(ret));

        }

        inject(ElecCore.instance.asmDataProcessor.asmDataHelper, IASMDataHelper.class);

        for (IAdvancedASMData data : asmData.getAdvancedAnnotationList(StaticLoad.class)){
            data.loadClass();
        }

    }

    @SuppressWarnings("unchecked")
    public void inject(Object o, Class<?>... classes){
        for (Class<?> clazz : classes){
            if (!clazz.isAssignableFrom(o.getClass())){
                throw new IllegalArgumentException();
            }
            for (Consumer consumer : callBacks.removeAll(clazz)){
                consumer.accept(o);
            }
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StaticLoad {
    }

    @APIHandlerInject
    static IWorldGenManager worldGenManager;

}
