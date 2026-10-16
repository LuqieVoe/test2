package pvp;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public class PvpMod implements ClientModInitializer {
    public static boolean esp = true, aura = false, players = true, mobs = false;
    public static float range = 3.5f;

    public static boolean valid(Entity e) {
        return e instanceof Player ? players : (e instanceof Enemy && mobs);
    }

    static KeyMapping menuKey;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category cat = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("pvp", "main"));
        menuKey = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.pvp.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.consumeClick()) if (mc.screen == null) mc.setScreen(new PvpScreen());
        });

        // Aura: attacks nearest valid target in range, only when weapon cooldown is full
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            LocalPlayer p = mc.player;
            if (!aura || p == null || mc.level == null || mc.gameMode == null) return;
            if (p.getAttackStrengthScale(0.5f) < 1f) return; // cooldown differs per weapon automatically
            Entity best = null;
            double bd = range * range;
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e == p || !(e instanceof LivingEntity le) || !le.isAlive() || !valid(e)) continue;
                double d = p.distanceToSqr(e);
                if (d < bd && p.hasLineOfSight(e)) { best = e; bd = d; }
            }
            if (best != null) {
                mc.gameMode.attack(p, best);
                p.swing(InteractionHand.MAIN_HAND);
            }
        });

        // Commands: /pvp esp|aura|players|mobs  and  /pvp range <1-6>
        ClientCommandRegistrationCallback.EVENT.register((d, reg) -> d.register(
            ClientCommandManager.literal("pvp")
                .then(ClientCommandManager.literal("esp").executes(c -> { esp = !esp; return msg(c.getSource(), "ESP " + esp); }))
                .then(ClientCommandManager.literal("aura").executes(c -> { aura = !aura; return msg(c.getSource(), "Aura " + aura); }))
                .then(ClientCommandManager.literal("players").executes(c -> { players = !players; return msg(c.getSource(), "Players " + players); }))
                .then(ClientCommandManager.literal("mobs").executes(c -> { mobs = !mobs; return msg(c.getSource(), "Mobs " + mobs); }))
                .then(ClientCommandManager.literal("range").then(ClientCommandManager.argument("v", FloatArgumentType.floatArg(1f, 6f))
                    .executes(c -> { range = FloatArgumentType.getFloat(c, "v"); return msg(c.getSource(), "Range " + range); })))
        ));
    }

    private static int msg(FabricClientCommandSource s, String t) {
        s.sendFeedback(Component.literal(t));
        return 1;
    }
}
