package org.nomoremagicchoices.api.selection;

import net.minecraft.client.Minecraft;
import org.joml.Vector2i;
import org.nomoremagicchoices.gui.component.ScrollSpellWight;

import java.util.ArrayList;
import java.util.List;

public class ScrollWightData {


    private static final int SCROLL_WIGHT_LIST_SIZE = 4;
    private static final int SCROLL_WIGHT_FOCUS_INDEX = 0;
    private static final int SCROLL_WIGHT_HEIGHT = 20;

    private static final int FOCUS_HEIGHT = 30;


    private static final int centerXOffset = -50;
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

    public ScrollWightData(SpellGroupData groupData,ClientHandData clientHandData) {
        this.groupData = groupData;
        this.clientHandData = clientHandData;

        update();
    }

    private SpellGroupData groupData;
    private ClientHandData clientHandData;

    /**
     * {@link SpellGroupData#update()} 这个方法触发时会触发update
     */
    public void update(){
        List<ScrollSpellWight> oldScrollWights = scrollWights;
        List<ScrollSpellWight> newScrollWights = getNewScrollWights();

        //比较坐标差距，然后移动
        for(ScrollSpellWight oldScrollWight : oldScrollWights){
            for (ScrollSpellWight newScrollWight : newScrollWights){
                var newCenter = newScrollWight.getCenter();
                var oldCenter = oldScrollWight.getCenter();

                if ((newScrollWight.getGroupIndex() == oldScrollWight.getGroupIndex()) &&
                        (Vector2i.distance(oldCenter.x(), oldCenter.y(), newCenter.x(), newCenter.y()) != 0)){
                    oldScrollWight.moveTo(newCenter);
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

    public  void tick(){
        if (isTicking){
            cTick++;
            if (cTick == MOVE_TICKS){
                isTicking = false;
                cTick = 0;
                scrollWights = getNewScrollWights();
            }
        }
    }

    public List<ScrollSpellWight> getNewScrollWights(){
        var window = Minecraft.getInstance().getWindow();
        center = new Vector2i(window.getWidth()/2 + centerXOffset, window.getHeight() + centerYOffset);

        var cIndex = groupData.getCurrentGroupIndex();

        var scrollIndex = 0;

        List<ScrollSpellWight> scrollWights = new ArrayList<>();
        //1.将currentGroupIndex 放在 0 号位
        scrollWights.set(scrollIndex,ScrollSpellWight.create(calculateCenter(scrollIndex),groupData.getIndexSpells(cIndex),cIndex,clientHandData));
        //2.遍历cIndex后面的
        if (cIndex < groupData.getGroupCount()-1){
            for (int afterGroupIndex = cIndex+1; afterGroupIndex < groupData.getGroupCount()-1; afterGroupIndex++){
                scrollIndex = scrollIndex +1;
                scrollWights.set(scrollIndex,ScrollSpellWight.create(calculateCenter(scrollIndex),groupData.getIndexSpells(afterGroupIndex),afterGroupIndex,clientHandData));
            }
        }
        //溢出index到0 - cIndex-1
        for (int firstGroupIndex = 0; firstGroupIndex < cIndex; firstGroupIndex++){
            scrollIndex = scrollIndex +1;
            scrollWights.set(scrollIndex,ScrollSpellWight.create(calculateCenter(scrollIndex),groupData.getIndexSpells(firstGroupIndex),firstGroupIndex,clientHandData));
        }
        return scrollWights;
    }


    public Vector2i calculateCenter(int wightIndex){

        int x = center.x();
        int y;

        if (wightIndex!=0){
            y = center.y() - (groupData.getGroupCount()- 1 - wightIndex) * SCROLL_WIGHT_HEIGHT;
        }else{
            //index 为0
            y = center.y() - (groupData.getGroupCount() - 1 - 0) * SCROLL_WIGHT_HEIGHT;
            if (clientHandData.getState().isFocus()){
                 y = y - FOCUS_HEIGHT;
            }
        }
        return new Vector2i(x,y);
    }

    public List<ScrollSpellWight> getScrollWights() {
        return scrollWights;
    }
}
