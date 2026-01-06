package org.nomoremagicchoices.mixin;


import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Minecraft.class, priority = 999)
public class MixinA {
}
