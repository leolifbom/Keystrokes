package me.arrayofc.keystrokes.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;

/**
 * Utility class for rendering.
 */
public class RenderUtil {

    /**
     * Draws a horizontal line.
     *
     * @see net.minecraft.client.gui.AbstractGui#hLine(MatrixStack, int, int, int, int)
     */
    public static void hLine(MatrixStack matrixStack, int minX, int maxX, int y, int color) {
        if (maxX < minX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }

        fill(matrixStack, minX, y, maxX + 1, y + 1, color);
    }

    /**
     * Draws a vertical line.
     *
     * @see net.minecraft.client.gui.AbstractGui#vLine(MatrixStack, int, int, int, int)
     */
    public static void vLine(MatrixStack matrixStack, int x, int minY, int maxY, int color) {
        if (maxY < minY) {
            int i = minY;
            minY = maxY;
            maxY = i;
        }

        fill(matrixStack, x, minY + 1, x + 1, maxY, color);
    }

    public static void fill(MatrixStack matrixStack, int minX, int minY, int maxX, int maxY, int color) {
        fill(matrixStack.getLast().getMatrix(), minX, minY, maxX, maxY, color);
    }

    /**
     * Fills an area.
     * <p>
     * This code snippet is taken from {@link net.minecraft.client.gui.AbstractGui#fill(Matrix4f, int, int, int, int, int)}
     * with the ability to change the alpha color value of the {@link BufferBuilder}.
     */
    private static void fill(Matrix4f matrix, int minX, int minY, int maxX, int maxY, int color, float alpha) {
        if (minX < maxX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            int j = minY;
            minY = maxY;
            maxY = j;
        }

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(matrix, (float) minX, (float) maxY, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(matrix, (float) maxX, (float) maxY, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(matrix, (float) maxX, (float) minY, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(matrix, (float) minX, (float) minY, 0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    /**
     * Fills an area with default alpha value of 0.5f.
     */
    private static void fill(Matrix4f matrix, int minX, int minY, int maxX, int maxY, int color) {
        fill(matrix, minX, minY, maxX, maxY, color, 0.5F);
    }
}