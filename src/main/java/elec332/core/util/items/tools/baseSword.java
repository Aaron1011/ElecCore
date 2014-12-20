package elec332.core.util.items.tools;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import elec332.core.helper.modInfoHelper;
import elec332.core.helper.registerHelper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemSword;

/**
 * Created by Elec332 on 20-12-2014.
 */
public class baseSword extends ItemSword{
    public baseSword(ToolMaterial mat, String name, CreativeTabs creativetab, FMLPreInitializationEvent event){
        super(mat);
        setCreativeTab(creativetab);
        setUnlocalizedName(modInfoHelper.getModID(event) + "." + name);
        setTextureName(name);
        registerHelper.registerItem(this, name);
    }
}
