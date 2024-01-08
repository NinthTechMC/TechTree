package pistonmc.techtree.mc7.gui;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class RenderHacks {
    public static void renderItemDarkenedForMultiplePasses(RenderItem render, TextureManager textureManager, ItemStack stack, int x, int y) {
        {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            textureManager.bindTexture(TextureMap.locationItemsTexture);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(0, 0, 0, 0);
            GL11.glColorMask(false, false, false, true);

            // hack: always draw black
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 1.0F);

            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setColorOpaque_I(-1);
            tessellator.addVertex((double)(x - 2), (double)(y + 18), (double)render.zLevel);
            tessellator.addVertex((double)(x + 18), (double)(y + 18), (double)render.zLevel);
            tessellator.addVertex((double)(x + 18), (double)(y - 2), (double)render.zLevel);
            tessellator.addVertex((double)(x - 2), (double)(y - 2), (double)render.zLevel);
            tessellator.draw();
            GL11.glColorMask(true, true, true, true);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);

            Item item = stack.getItem();
            int meta = stack.getItemDamage();
            for (int l = 0; l < item.getRenderPasses(meta); ++l)
            {
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                textureManager.bindTexture(item.getSpriteNumber() == 0 ? TextureMap.locationBlocksTexture : TextureMap.locationItemsTexture);
                IIcon iicon = item.getIcon(stack, l);

                // hack: always draw black
                GL11.glColor3f(0, 0, 0);

                GL11.glDisable(GL11.GL_LIGHTING); //Forge: Make sure that render states are reset, ad renderEffect can derp them up.
                GL11.glEnable(GL11.GL_ALPHA_TEST);

                render.renderIcon(x, y, iicon, 16, 16);

                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_LIGHTING);

                // if (renderEffect && stack.hasEffect(l))
                // {
                //     renderEffect(p_77015_2_, p_77015_4_, p_77015_5_);
                // }
            }

            GL11.glEnable(GL11.GL_LIGHTING);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
