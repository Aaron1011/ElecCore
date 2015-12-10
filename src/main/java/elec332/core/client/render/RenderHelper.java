package elec332.core.client.render;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import elec332.core.client.ElecTessellator;
import elec332.core.client.ITessellator;
import elec332.core.client.RenderBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;

/**
 * Created by Elec332 on 31-7-2015.
 */
@SideOnly(Side.CLIENT)
public class RenderHelper {

    public static final float renderUnit = 1/16f;
    private static List<Block> itemRenders = Lists.newArrayList();
    private static List<Block> blockRenders = Lists.newArrayList();
    private static List<Class<? extends TileEntity>> tileRenders = Lists.newArrayList();
    private static Map<Block, Integer> renderData = Maps.newHashMap();
    private static Tessellator mcTessellator = Tessellator.getInstance();
    private static ITessellator tessellator;
    private static Minecraft mc;
    private static RenderBlocks renderBlocks;

    public static ITessellator forWorldRenderer(WorldRenderer renderer){
        return new ElecTessellator(renderer);
    }

    public static RenderBlocks getBlockRenderer(){
        return renderBlocks;
    }

    public static FontRenderer getMinecraftFontrenderer(){
        return mc.fontRendererObj;
    }

    public static ITessellator getTessellator(){
        return tessellator;
    }

    @Deprecated
    public static <A extends AbstractBlockRenderer> A registerBlockRenderer(A renderer){
        return null;} /*
        if (renderer == null)
            throw new IllegalArgumentException("Cannot register a null renderer!");
        if (!renderer.isISBRH() && !renderer.isItemRenderer() && !renderer.isTESR())
            throw new IllegalArgumentException("Useless render handler detected: "+renderer.getClass());
        if ((renderer.isItemRenderer() || renderer.isISBRH())){
            if (renderer.block == null)
                throw new IllegalArgumentException("Cannot register IItemRenderer or ISBRH for a null Block!");
            if (renderer.isItemRenderer() && itemRenders.contains(renderer.block) || renderer.isISBRH() && blockRenders.contains(renderer.block))
                throw new IllegalArgumentException("Renderer already registered for "+renderer.block.getClass());
        }
        if (renderer.isTESR()) {
            if (renderer.tileClass == null)
                throw new IllegalArgumentException("Cannot register TESR for a null TileEntity class!");
            if (tileRenders.contains(renderer.tileClass))
                throw new IllegalArgumentException("Renderer already registered for "+renderer.tileClass);
        }
        if (renderer.isISBRH()){
            if (renderer.renderID != -1){
                throw new UnsupportedOperationException();
            }
            int r = -1;// RenderingRegistry.getNextAvailableRenderId();
            renderer.renderID = r;
            //RenderingRegistry.registerBlockHandler(r, renderer);
            blockRenders.add(renderer.block);
            renderData.put(renderer.block, r);
        }
        if (renderer.isItemRenderer()){
            MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(renderer.block), renderer);
            itemRenders.add(renderer.block);
        }
        if (renderer.isTESR()){
            ClientRegistry.bindTileEntitySpecialRenderer(renderer.tileClass, renderer);
            tileRenders.add(renderer.tileClass);
        }
        return renderer;
    }*/

    public static int getRenderID(Block block){
        return renderData.get(block);
    }

    public static Vec3 getPlayerVec(float partialTicks){
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        return new Vec3(dX, dY, dZ);
    }

    public static Vec3 getPlayerVec(){
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        return new Vec3(player.posX, player.posY, player.posZ);
    }

    public static void drawLine(Vec3 from, Vec3 to, Vec3 player, float thickness){
        drawQuad(from, from.addVector(thickness, thickness, thickness), to, to.addVector(thickness, thickness, thickness));
    }

    public static void drawQuad(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4){
        tessellator.addVertexWithUV(v1.xCoord, v1.yCoord, v1.zCoord, 0, 0);
        tessellator.addVertexWithUV(v2.xCoord, v2.yCoord, v2.zCoord, 1, 0);
        tessellator.addVertexWithUV(v3.xCoord, v3.yCoord, v3.zCoord, 1, 1);
        tessellator.addVertexWithUV(v4.xCoord, v4.yCoord, v4.zCoord, 0, 1);
    }

    public static Vec3 multiply(Vec3 original, double m){
        return new Vec3(original.xCoord * m, original.yCoord * m, original.zCoord * m);
    }

    public static void bindBlockTextures(){
        bindTexture(getBlocksResourceLocation());
    }

    public static void bindTexture(ResourceLocation rl){
        Minecraft.getMinecraft().renderEngine.bindTexture(rl);
    }

    public static TextureAtlasSprite checkIcon(TextureAtlasSprite icon) {
        if (icon == null)
            return getMissingTextureIcon();
        return icon;
    }

    public static TextureAtlasSprite getFluidTexture(Fluid fluid, boolean flowing) {
        if (fluid == null)
            return getMissingTextureIcon();
        return checkIcon(flowing ? getIconFrom(fluid.getFlowing()) : getIconFrom(fluid.getStill()));
    }

    public static TextureAtlasSprite getMissingTextureIcon(){
        return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();//((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(getBlocksResourceLocation())).getAtlasSprite("missingno");
    }

    public static TextureAtlasSprite getIconFrom(ResourceLocation rl){
        return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString());
    }

    public static ResourceLocation getBlocksResourceLocation(){
        return TextureMap.locationBlocksTexture;
    }

    public void spawnParticle(EntityFX particle){
        Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }

    static {
        tessellator = new ElecTessellator();
        mc = Minecraft.getMinecraft();
        renderBlocks = new RenderBlocks();
    }

}
