package pvp;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PvpScreen extends Screen {
    public PvpScreen() { super(Component.literal("PvP Mod")); }

    @Override
    protected void init() {
        int x = width / 2 - 100, y = height / 2 - 60;
        toggle(x, y, "ESP", () -> PvpMod.esp, v -> PvpMod.esp = v);
        toggle(x, y + 24, "Aura", () -> PvpMod.aura, v -> PvpMod.aura = v);
        toggle(x, y + 48, "Target players", () -> PvpMod.players, v -> PvpMod.players = v);
        toggle(x, y + 72, "Target mobs", () -> PvpMod.mobs, v -> PvpMod.mobs = v);
        addRenderableWidget(new AbstractSliderButton(x, y + 96, 200, 20,
                Component.literal("Range: " + PvpMod.range), (PvpMod.range - 1f) / 5f) {
            @Override protected void updateMessage() { setMessage(Component.literal("Range: " + PvpMod.range)); }
            @Override protected void applyValue() { PvpMod.range = Math.round((1f + 5f * (float) value) * 10f) / 10f; }
        });
    }

    private void toggle(int x, int y, String name, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
        }).bounds(x, y, 200, 20).build());
    }

    private static Component label(String n, boolean on) { return Component.literal(n + ": " + (on ? "ON" : "OFF")); }

    @Override public boolean isPauseScreen() { return false; }
}
