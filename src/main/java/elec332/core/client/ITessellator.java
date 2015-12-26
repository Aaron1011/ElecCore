package elec332.core.client;

import net.minecraft.client.renderer.Tessellator;

/**
 * Created by Elec332 on 25-11-2015.
 */
public interface ITessellator {

    public void setBrightness(int brightness);

    public void setColorOpaque_F(float red, float green, float blue);

    public void setColorOpaque(int red, int green, int blue);

    public void setColorRGBA_F(float red, float green, float blue, float alpha);

    public void setColorRGBA_I(int color, int alpha);

    public void setColorRGBA(int red, int green, int blue, int alpha);

    public void addVertexWithUV(double x, double y, double z, double u, double v);

    public Tessellator getMCTessellator();

}
