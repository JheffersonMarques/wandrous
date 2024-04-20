package com.hakimen.wandrous.common.utils;

import com.hakimen.wandrous.common.spell.SpellContext;
import com.hakimen.wandrous.common.spell.SpellStatus;
import com.hakimen.wandrous.common.spell.effects.modifiers.MulticastEffect;
import com.hakimen.wandrous.common.spell.SpellEffect;
import com.hakimen.wandrous.common.utils.data.Node;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CastingUtils {
    public int idx;

    public Node<SpellEffect> makeCastingTree(List<SpellEffect> effects) {
        if (idx > effects.size() - 1) {
            return new Node<>(null);
        }
        Node<SpellEffect> tree = new Node<>(effects.get(idx++));
        if (tree.getData() instanceof MulticastEffect effect) {
            int casts = Math.min(effect.getCastCount(), effects.size());
            for (int i = 0; i < casts; i++) {
                Node<SpellEffect> cast = makeCastingTree(effects);
                if (cast.getData() != null) {
                    tree.addChild(cast);
                }
            }
        } else if (tree.getData().hasKind(SpellEffect.TRIGGER) || tree.getData().hasKind(SpellEffect.MODIFIER)) {
            Node<SpellEffect> cast = makeCastingTree(effects);
            if (cast.getData() != null) {
                tree.addChild(cast);
            }
        }

        return tree;
    }

    public static int calculateManaCost(int cost, Node<SpellEffect> castTree) {

        cost += castTree.getData().getStatus().getManaDrain();

        for (Node<SpellEffect> child : castTree.getChildren()) {
            cost += calculateManaCost(0, child);
        }
        return cost;
    }

    public static void castSpells(Entity entity, ItemStack wand, Level pLevel, Vec3 location, Node<SpellEffect> toCast) {

        SpellContext context = new SpellContext()
                .setCaster(entity)
                .setWand(wand)
                .setLevel(pLevel)
                .setLocation(location)
                .setStatus(new SpellStatus())
                .setNode(toCast)
                .setOriginalCaster(entity);

        toCast.getData().cast(context);
    }

    public static SpellStatus mergeStatus(SpellStatus first, SpellStatus second) {

        SpellStatus status = new SpellStatus();

        status.setDamage(first.getRawDamage());
        status.setLifeTime(first.getLifeTime());

        status.setCastDelayMod(first.getCastDelayMod() + second.getCastDelayMod());
        status.setDamageMod(first.getDamageMod() + second.getDamageMod());
        status.setSpeedMod(first.getSpeedMod() + second.getSpeedMod());
        status.setCritChance(first.getCritChance() + second.getCritChance());
        status.setSpreadMod(first.getSpreadMod() + second.getSpreadMod());

        status.setSpeed(first.getSpeed() + second.getSpeed());
        status.setSpread(first.getSpread() + second.getSpread());

        return status;
    }
}