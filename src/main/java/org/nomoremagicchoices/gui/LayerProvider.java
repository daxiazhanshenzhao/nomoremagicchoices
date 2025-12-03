package org.nomoremagicchoices.gui;

import net.minecraft.client.gui.LayeredDraw;


@FunctionalInterface
public interface LayerProvider {


    LayeredDraw.Layer getInstance();

}
