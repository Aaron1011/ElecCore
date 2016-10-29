package elec332.core.network.packets;

import elec332.core.inventory.IWidgetContainer;
import elec332.core.inventory.widget.Widget;
import elec332.core.util.NBTHelper;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created by Elec332 on 23-8-2015.
 */
@SuppressWarnings("unused")
public class PacketWidgetDataToServer extends AbstractPacket {

    public PacketWidgetDataToServer(){
    }

    public PacketWidgetDataToServer(NBTTagCompound tag, IWidgetContainer widgetContainer, Widget widget){
        super(new NBTHelper().addToTag(new NBTHelper().addToTag(widgetContainer.getWidgets().indexOf(widget), "widget").addToTag(((Container) widgetContainer).windowId, "window").serializeNBT(), "containerData").addToTag(tag, "data").serializeNBT());
    }

    @Override
    public IMessage onMessageThreadSafe(AbstractPacket message, MessageContext ctx) {
        NBTTagCompound data = message.networkPackageObject.getCompoundTag("data");
        NBTTagCompound containerData = message.networkPackageObject.getCompoundTag("containerData");
        Container openContainer = ctx.getServerHandler().playerEntity.openContainer;
        if (openContainer.windowId == containerData.getInteger("window")){
            ((IWidgetContainer)openContainer).getWidgets().get(containerData.getInteger("widget")).readNBTChangesFromPacket(data, Side.SERVER);
        }
        return null;
    }

}
