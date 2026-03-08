package de.sodium.highlighter.mixin;

import de.sodium.highlighter.render.HighlightRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/WorldRenderer;renderEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;FLjava/util/List;)V",
            shift = At.Shift.AFTER
        )
    )
    private void onRenderAfterEntities(
            MatrixStack matrices,
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            Matrix4f positionMatrix,
            CallbackInfo ci) {

        HighlightRenderer.renderHighlights(matrices, camera, tickCounter.getTickDelta(true));
    }
}
