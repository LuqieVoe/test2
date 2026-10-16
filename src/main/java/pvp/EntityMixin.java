package pvp;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Makes valid targets glow (outline visible through walls)
@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true)
    private void pvp$glow(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (PvpMod.esp && self != Minecraft.getInstance().player && PvpMod.valid(self)
                && self.level().isClientSide()) cir.setReturnValue(true);
    }
}
