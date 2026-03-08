package de.sodium.highlighter.render;

import com.mojang.blaze3d.systems.RenderSystem;
import de.sodium.highlighter.config.HighlightConfig;
import de.sodium.highlighter.config.HighlightConfig.HighlightEntry;
import de.sodium.highlighter.config.HighlightConfig.HighlightType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.*;

public class HighlightRenderer {

    public static void renderHighlights(MatrixStack matrices, Camera camera, float tickDelta) {
        HighlightConfig cfg = HighlightConfig.get();
        if (!cfg.globalEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc.world;
        if (world == null) return;

        Vec3d camPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        Tessellator tessellator = Tessellator.getInstance();

        // --- Render Block Highlights ---
        List<HighlightEntry> blockEntries = cfg.getByType(HighlightType.BLOCK);
        if (!blockEntries.isEmpty()) {
            BlockPos center = mc.player != null ? mc.player.getBlockPos() : BlockPos.ORIGIN;
            int radius = 64;

            for (BlockPos pos : BlockPos.iterate(
                    center.add(-radius, -radius, -radius),
                    center.add(radius, radius, radius))) {
                BlockState state = world.getBlockState(pos);
                String blockId = state.getBlock().getTranslationKey()
                        .replace("block.", "").replace(".", ":");

                for (HighlightEntry entry : blockEntries) {
                    String entryId = entry.id.replace("minecraft:", "minecraft:");
                    if (matches(blockId, entry.id)) {
                        float[] c = unpackColor(entry.color, entry.opacity);
                        matrices.push();
                        matrices.translate(
                                pos.getX() - camPos.x,
                                pos.getY() - camPos.y,
                                pos.getZ() - camPos.z);
                        renderOutlinedBox(matrices, tessellator, 0, 0, 0, 1, 1, 1,
                                c[0], c[1], c[2], c[3], entry.throughWalls);
                        matrices.pop();
                    }
                }
            }
        }

        // --- Render Entity & Player Highlights ---
        List<HighlightEntry> entityEntries = cfg.getByType(HighlightType.ENTITY);
        List<HighlightEntry> playerEntries = cfg.getByType(HighlightType.PLAYER);

        for (Entity entity : world.getEntities()) {
            String entityId = entity.getType().getTranslationKey()
                    .replace("entity.", "").replace(".", ":");

            boolean isPlayer = entity instanceof PlayerEntity;

            // Skip self
            if (entity == mc.player) continue;

            List<HighlightEntry> candidates = isPlayer ? playerEntries : entityEntries;

            for (HighlightEntry entry : candidates) {
                boolean nameMatch = isPlayer
                        ? (entry.id.equals("minecraft:player") || entity.getName().getString().equalsIgnoreCase(entry.id))
                        : matches(entityId, entry.id);

                if (nameMatch) {
                    float[] c = unpackColor(entry.color, entry.opacity);
                    Box box = entity.getBoundingBox();
                    double x1 = box.minX - camPos.x - entity.getX() + entity.getX();
                    double y1 = box.minY - camPos.y - entity.getY() + entity.getY();
                    double z1 = box.minZ - camPos.z - entity.getZ() + entity.getZ();

                    double lerpX = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()) - camPos.x;
                    double lerpY = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()) - camPos.y;
                    double lerpZ = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ()) - camPos.z;

                    double w = (box.maxX - box.minX) / 2.0;
                    double h = box.maxY - box.minY;
                    double d = (box.maxZ - box.minZ) / 2.0;

                    matrices.push();
                    matrices.translate(lerpX, lerpY, lerpZ);
                    renderOutlinedBox(matrices, tessellator,
                            -w, 0, -d, w, h, d,
                            c[0], c[1], c[2], c[3], entry.throughWalls);
                    matrices.pop();
                    break;
                }
            }
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void renderOutlinedBox(MatrixStack matrices, Tessellator tess,
                                           double x1, double y1, double z1,
                                           double x2, double y2, double z2,
                                           float r, float g, float b, float a,
                                           boolean throughWalls) {
        if (throughWalls) RenderSystem.disableDepthTest();
        else RenderSystem.enableDepthTest();

        Matrix4f mat = matrices.peek().getPositionMatrix();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        float fx1 = (float) x1, fy1 = (float) y1, fz1 = (float) z1;
        float fx2 = (float) x2, fy2 = (float) y2, fz2 = (float) z2;

        // Bottom face
        line(buf, mat, fx1,fy1,fz1, fx2,fy1,fz1, r,g,b,a);
        line(buf, mat, fx2,fy1,fz1, fx2,fy1,fz2, r,g,b,a);
        line(buf, mat, fx2,fy1,fz2, fx1,fy1,fz2, r,g,b,a);
        line(buf, mat, fx1,fy1,fz2, fx1,fy1,fz1, r,g,b,a);
        // Top face
        line(buf, mat, fx1,fy2,fz1, fx2,fy2,fz1, r,g,b,a);
        line(buf, mat, fx2,fy2,fz1, fx2,fy2,fz2, r,g,b,a);
        line(buf, mat, fx2,fy2,fz2, fx1,fy2,fz2, r,g,b,a);
        line(buf, mat, fx1,fy2,fz2, fx1,fy2,fz1, r,g,b,a);
        // Verticals
        line(buf, mat, fx1,fy1,fz1, fx1,fy2,fz1, r,g,b,a);
        line(buf, mat, fx2,fy1,fz1, fx2,fy2,fz1, r,g,b,a);
        line(buf, mat, fx2,fy1,fz2, fx2,fy2,fz2, r,g,b,a);
        line(buf, mat, fx1,fy1,fz2, fx1,fy2,fz2, r,g,b,a);

        // Fill (translucent)
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder fill = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        float fa = a * 0.25f;
        quad(fill, mat, fx1,fy1,fz1, fx2,fy1,fz1, fx2,fy2,fz1, fx1,fy2,fz1, r,g,b,fa);
        quad(fill, mat, fx1,fy1,fz2, fx1,fy2,fz2, fx2,fy2,fz2, fx2,fy1,fz2, r,g,b,fa);
        quad(fill, mat, fx1,fy1,fz1, fx1,fy1,fz2, fx1,fy2,fz2, fx1,fy2,fz1, r,g,b,fa);
        quad(fill, mat, fx2,fy1,fz1, fx2,fy2,fz1, fx2,fy2,fz2, fx2,fy1,fz2, r,g,b,fa);
        quad(fill, mat, fx1,fy1,fz1, fx2,fy1,fz1, fx2,fy1,fz2, fx1,fy1,fz2, r,g,b,fa);
        quad(fill, mat, fx1,fy2,fz1, fx1,fy2,fz2, fx2,fy2,fz2, fx2,fy2,fz1, r,g,b,fa);
        tess.draw();

        RenderSystem.enableDepthTest();
    }

    private static void line(BufferBuilder buf, Matrix4f mat,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float r, float g, float b, float a) {
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
    }

    private static void quad(BufferBuilder buf, Matrix4f mat,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float x3, float y3, float z3,
                              float x4, float y4, float z4,
                              float r, float g, float b, float a) {
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
        buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
        buf.vertex(mat, x3, y3, z3).color(r, g, b, a);
        buf.vertex(mat, x4, y4, z4).color(r, g, b, a);
    }

    private static float[] unpackColor(int argb, float opacity) {
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8)  & 0xFF) / 255f;
        float b = ( argb        & 0xFF) / 255f;
        float a = opacity;
        return new float[]{r, g, b, a};
    }

    private static boolean matches(String entityId, String entryId) {
        String normalized = entryId.replace("minecraft:", "").toLowerCase();
        String normalized2 = entityId.replace("minecraft:", "").toLowerCase();
        return normalized2.contains(normalized) || normalized.contains(normalized2);
    }
}
