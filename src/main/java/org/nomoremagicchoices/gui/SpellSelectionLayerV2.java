package org.nomoremagicchoices.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.gui.overlays.SpellBarOverlay;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import org.nomoremagicchoices.Nomoremagicchoices;
import org.nomoremagicchoices.api.selection.ClientScrollData;
import org.nomoremagicchoices.api.selection.ILayerState;
import org.nomoremagicchoices.api.selection.SpellSelectionState;
import org.nomoremagicchoices.gui.component.IMoveWight;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;

import java.util.HashMap;
import java.util.List;

public class SpellSelectionLayerV2 implements ILayerState {

    private SpellSelectionState state = SpellSelectionState.EmptyHand;
    private int screenWidth;
    private int screenHeight;



    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Nomoremagicchoices.MODID, "textures/gui/icons.png");

    /**
     * 法术选中状态映射表
     * <p>
     * 用于记录每个法术是否为当前被选中的4个法术之一。
     * 该映射在每次渲染时更新，用于区分已选中和未选中的法术显示效果。
     * </p>
     *
     * @key {@link SpellData} 法术数据对象
     * @value {@link Boolean} 选中状态：{@code true} 表示该法术已被选中，{@code false} 表示未被选中
     */
    private static HashMap<SpellData, Boolean> selectSpell = new HashMap<>();

//    @Override
//    public SpellSelectionState getSpellSelectionState() {
//        return state;
//    }
//
//    @Override
//    public void setSpellSelectionState(SpellSelectionState spellSelectionState) {
//        this.state = spellSelectionState;
//    }


    @Override
    public void render(GuiGraphics context, DeltaTracker partialTick) {

        init(context);




        ClientScrollData.getSpellWightList().forEach(spell -> {
            IMoveWight wight = spell.getFirst();
            wight.render(context, partialTick);
        });








    }


    /**
     * 每tick刷新
     * 计算屏幕宽高
     * 计算centerX，Y坐标
     *
     *
     *
     */
    public void init(GuiGraphics guiGraphics){
        this.screenHeight = guiGraphics.guiHeight();
        this.screenWidth = guiGraphics.guiWidth();
    }






}
