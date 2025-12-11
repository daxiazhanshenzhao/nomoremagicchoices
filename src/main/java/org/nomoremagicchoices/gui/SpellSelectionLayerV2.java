package org.nomoremagicchoices.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.event.RenderBgEvent;

import org.nomoremagicchoices.api.selection.ClientData;
import org.nomoremagicchoices.api.selection.ClientHandData;
import org.nomoremagicchoices.api.selection.ILayerState;
import org.nomoremagicchoices.api.selection.ScrollWightData;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;

import java.util.List;

/**
 * 法术选择层V2
 * 负责渲染法术选择界面，所有逻辑由ClientScrollData处理
 * 该层只从ClientScrollData获取数据并进行渲染
 */
public class SpellSelectionLayerV2 implements ILayerState {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Nomoremagicchoices.MODID, "textures/gui/bg.png");



    private int screenWidth;
    private int screenHeight;

    /**
     * 渲染方法
     * 从ClientScrollData获取Widget列表并渲染
     */
    @Override
    public void render(GuiGraphics context, DeltaTracker partialTick) {
        // 更新屏幕尺寸
        var screenWidth = context.guiWidth();
        var screenHeight = context.guiHeight();

        var x = screenWidth / 2 - 200;
        var y = screenHeight -36;

        renderBg(TEXTURE, context,x , y,0,0, 105, 36,105,36);
        updateScreenSize(context);



        // 获取Widget列表并渲染
        List<ScrollSpellWight> wightList = ClientData.getScrollWightData().getScrollWights();

        if (wightList == null || wightList.isEmpty()) {
            return;
        }

        // 渲染所有Widget - 从大到小倒序渲染，让小index的Widget显示在最上层
        for (int i = wightList.size() - 1; i >= 0; i--) {
            ScrollSpellWight wight = wightList.get(i);
            if (wight != null) {
                wight.render(context, partialTick);
            }
        }
//        for (int i =0;i < wightList.size();i++){
//            ScrollSpellWight wight = wightList.get(i);
//            if (wight != null) {
//                wight.render(context, partialTick);
//            }
//        }
    }

    /**
     * 更新屏幕尺寸
     */
    private void updateScreenSize(GuiGraphics guiGraphics) {
        this.screenHeight = guiGraphics.guiHeight();
        this.screenWidth = guiGraphics.guiWidth();
    }


    @Override
    public void renderBg(ResourceLocation texture, GuiGraphics context, int x, int y,int uOffset,int vOffset, int width, int height,int textureWidth,int textureHeight) {

        RenderBgEvent event = new RenderBgEvent(texture, context, x, y,uOffset, vOffset, width, height,textureWidth,textureHeight);
        NeoForge.EVENT_BUS.post(event);

        if (event.isCanceled()) return;


        context.blit(event.getTexture(), event.getX(), event.getY(),
                     event.getuOffset(), event.getvOffset(),
                     event.getWidth(), event.getHeight(),
                     event.getTextureWidth(), event.getTextureHeight());

    }


}
