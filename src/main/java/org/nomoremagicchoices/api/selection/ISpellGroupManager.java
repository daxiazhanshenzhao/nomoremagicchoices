package org.nomoremagicchoices.api.selection;

public interface ISpellGroupManager {


    void add();
    void less();


    /**
     * 移动一组
     * @param offset 偏移量 + 为index变大，-为index变小
     */
    void move(int offset);


}
