package org.nomoremagicchoices.api.selection;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import org.joml.Vector2i;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;

import java.util.ArrayList;
import java.util.List;

public class ScrollWightData {


    private static final int SCROLL_WIGHT_LIST_SIZE = 4;
    private static final int SCROLL_WIGHT_FOCUS_INDEX = 0;
    private static final int SCROLL_WIGHT_HEIGHT = 8;

    private static final int FOCUS_HEIGHT = 30;


    private static final int centerXOffset = -200;
    private static final int centerYOffset = -50;
    /**
     *
     *    □□□□ 0
     *           ===空着表示是focus渲染的==========   □□□□ 0
     *    □□□□ 1                                   □□□□ 1
     *    □□□□ 2                                   □□□□ 2
     *                                      center↑  (左下角)
     *  固定渲染index为0的位置,
     *
     *
     */
    private List<ScrollSpellWight> scrollWights;
    private Vector2i center;

    public ScrollWightData(SpellGroupData spellGroupData,ClientHandData clientHandData) {
        groupData = spellGroupData;
        handData = clientHandData;

        update();
    }

    private static SpellGroupData groupData;
    private static ClientHandData handData;

    /**
     * {@link SpellGroupData#update()} 这个方法触发时会触发update
     */
    public void update(){
        List<ScrollSpellWight> oldScrollWights = scrollWights;
        List<ScrollSpellWight> newScrollWights = getNewScrollWights();
        
        // 如果是第一次初始化，直接设置scrollWights
        if (oldScrollWights == null) {
            scrollWights = newScrollWights;
            return;
        }
        
        if (newScrollWights == null) return;

        //比较坐标差距，然后移动
        for(ScrollSpellWight oldScrollWight : oldScrollWights){
            //比较不变的法术，法术包含冷却
            if (oldScrollWight.compareEqualsSpell(ScrollSpellWight.EMPTY)) continue;
            for (ScrollSpellWight newScrollWight : newScrollWights){
                if (newScrollWight.compareEqualsSpell(ScrollSpellWight.EMPTY)) continue;

                var newCenter = newScrollWight.getCenter();
                var oldCenter = oldScrollWight.getCenter();
                if ((newScrollWight.getGroupIndex() == oldScrollWight.getGroupIndex()) &&
                        (Vector2i.distance(oldCenter.x(), oldCenter.y(), newCenter.x(), newCenter.y()) != 0)){
                    oldScrollWight.moveDown(newCenter);
                }

            }
        }




        //等待移动完成过后，替换原组件
        wightTickHook();

    }//=========================================================
    private static final int MOVE_TICKS = ScrollSpellWight.TOTAL_TICKS;
    private static int cTick = 0;
    private static boolean isTicking = false;

    /**
     * 通过独立内部计时器进行延迟替换
     */
    private  void wightTickHook() {
        isTicking = true;
    }

    public void tick(){
        if (isTicking){
            cTick++;
            if (cTick == MOVE_TICKS){
                isTicking = false;
                cTick = 0;
                scrollWights = getNewScrollWights();
            }
        }
        
        // 检查scrollWights是否为null
        if (scrollWights == null) {
            return;
        }
        
        for(ScrollSpellWight wight : scrollWights){
            if (wight != null && !wight.equals(ScrollSpellWight.EMPTY)){
                wight.tick();
            }
        }
    }

    public List<ScrollSpellWight> getNewScrollWights(){
        var window = Minecraft.getInstance().getWindow();
        center = new Vector2i(window.getGuiScaledWidth()/2 + centerXOffset, window.getGuiScaledHeight() + centerYOffset);

        var cIndex = SpellGroupData.getCurrentGroupIndex();
        var groupCount = SpellGroupData.getGroupCount();


        // 如果组数为0，返回一个空列表
        if (groupCount == 0) {
            return NonNullList.withSize(1, ScrollSpellWight.EMPTY);
        }

        // 创建正确大小的列表
        List<ScrollSpellWight> scrollWights = NonNullList.withSize(groupCount, ScrollSpellWight.EMPTY);

        // 填充当前组（放在索引0位置）
        scrollWights.set(0, ScrollSpellWight.create(
            calculateCenter(0),
            SpellGroupData.getIndexSpells(cIndex),
            cIndex,
            handData
        ));

        // 填充当前组之后的组
        int scrollIndex = 1;
        for (int afterGroupIndex = cIndex + 1; afterGroupIndex < groupCount; afterGroupIndex++) {
            if (scrollIndex >= groupCount) break;
            scrollWights.set(scrollIndex, ScrollSpellWight.create(
                calculateCenter(scrollIndex),
                SpellGroupData.getIndexSpells(afterGroupIndex),
                afterGroupIndex,
                handData
            ));
            scrollIndex++;
        }

        // 填充当前组之前的组
        for (int beforeGroupIndex = 0; beforeGroupIndex < cIndex; beforeGroupIndex++) {
            if (scrollIndex >= groupCount) break;
            scrollWights.set(scrollIndex, ScrollSpellWight.create(
                calculateCenter(scrollIndex),
                SpellGroupData.getIndexSpells(beforeGroupIndex),
                beforeGroupIndex,
                handData
            ));
            scrollIndex++;
        }



        return scrollWights;
    }


    public Vector2i calculateCenter(int wightIndex){
        int x = center.x();
        int y;

        int groupCount = SpellGroupData.getGroupCount();
        if (groupCount == 0) {
            return new Vector2i(x, center.y());
        }

        // 修改：当前组（wightIndex=0）在最上面（Y值最小）
        // wightIndex越大，位置越低（Y值越大）
        // 当前组在顶部：y = center.y() - (groupCount - 1) * SCROLL_WIGHT_HEIGHT
        // 其他组依次向下排列
        y = center.y() - (groupCount - 1 - wightIndex) * SCROLL_WIGHT_HEIGHT;

        // 对于index 0，如果是Focus状态，需要额外上移
        if (wightIndex == 0) {
            //TODO 后面完善Focus的变化
            if (false){
                 y = y - FOCUS_HEIGHT;
            }
        }
        
        return new Vector2i(x, y);
    }

    public List<ScrollSpellWight> getScrollWights() {
        return scrollWights;
    }

    public static boolean isTicking() {
        return isTicking;
    }
}
