package net.tigereye.hellishmaterials.items;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.tigereye.hellishmaterials.mechanics.BatetDeferment;
import net.tigereye.hellishmaterials.mechanics.LussLuck;

import java.util.Collection;
import java.util.Iterator;

public class ExplodingDice extends DiceItem {
    private static final int LOW_DURATION = 45*20;
    private static final int MID_DURATION = 90*20;
    private static final int HIGH_DURATION = 180*20;
    private static final int FOUR_DURATION = (4*60*20)+(44*20)+4;
    private static final int BLOOD_COST = 6;

    public ExplodingDice() {
        super(new Settings().maxCount(1).group(ItemGroup.TOOLS));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        BatetDeferment.deferDamage(user, BLOOD_COST,1);
        return super.use(world, user, hand);
    }
    @Override
    protected TypedActionResult<ItemStack> onFourOfAKind(World world, PlayerEntity user, Hand hand, int die1, int die2, int die3, int die4, int kind){
        String out = "Rolled Four of a Kind!! " + die1 +
                ", " + die2 +
                ", " + die3 +
                ", and " + die4 + ".";
        user.sendMessage(new LiteralText(out), true);

        createShockwave(user,10);
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(kind),HIGH_DURATION,3));
        spawnEffectCloud(user,new StatusEffectInstance(getPrizeStatusEffectType(kind),HIGH_DURATION,3));
        return TypedActionResult.success(user.getStackInHand(hand));
    }
    @Override
    protected TypedActionResult<ItemStack> onThreeOfAKind(World world, PlayerEntity user, Hand hand, int die1, int die2, int die3, int die4, int kind){
        String out = "Rolled Three of a Kind! " + die1 +
                ", " + die2 +
                ", " + die3 +
                ", and " + die4 + ".";
        user.sendMessage(new LiteralText(out), true);

        int leftover = die1+die2+die3+die4-(kind*3);
        createShockwave(user,4);
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(kind),HIGH_DURATION,1));
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(leftover),LOW_DURATION,2));
        return TypedActionResult.success(user.getStackInHand(hand));
    }
    @Override
    protected TypedActionResult<ItemStack> onTwoPair(World world, PlayerEntity user, Hand hand, int die1, int die2, int die3, int die4, int pair1, int pair2){
        String out = "Rolled Two Pair! " + die1 +
                ", " + die2 +
                ", " + die3 +
                ", and " + die4 + ".";
        user.sendMessage(new LiteralText(out), true);

        createShockwave(user,4);
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(pair1),MID_DURATION,1));
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(pair2),MID_DURATION,1));
        spawnEffectCloud(user,new StatusEffectInstance(getPrizeStatusEffectType(pair1),LOW_DURATION,1));
        spawnEffectCloud(user,new StatusEffectInstance(getPrizeStatusEffectType(pair2),LOW_DURATION,1));
        return TypedActionResult.success(user.getStackInHand(hand));
    }
    @Override
    protected TypedActionResult<ItemStack> onPair(World world, PlayerEntity user, Hand hand, int die1, int die2, int die3, int die4, int pair){
        String out = "Rolled a Pair of "+pair+"s. " + die1 +
                ", " + die2 +
                ", " + die3 +
                ", and " + die4 + ".";
        user.sendMessage(new LiteralText(out), true);

        int leftovers = die1+die2+die3+die4-(pair*2);
        int amplifier = 0;
        if(leftovers >= 13){
            amplifier = 2;
        }
        else if(leftovers >= 11){
            amplifier = 1;
        }
        createShockwave(user,amplifier+1);
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(pair),LOW_DURATION,amplifier));
        return TypedActionResult.success(user.getStackInHand(hand));
    }
    @Override
    protected TypedActionResult<ItemStack> onStraight(World world, PlayerEntity user, Hand hand, int die1, int die2, int die3, int die4, int lowest){
        String out = "Rolled a Straight! " + die1 +
                ", " + die2 +
                ", " + die3 +
                ", and " + die4 + ".";

        createShockwave(user,5);
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(lowest),HIGH_DURATION,0));
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(lowest+1),HIGH_DURATION,0));
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(lowest+2),HIGH_DURATION,0));
        user.applyStatusEffect(new StatusEffectInstance(getPrizeStatusEffectType(lowest+3),HIGH_DURATION,0));
        user.sendMessage(new LiteralText(out), true);
        return TypedActionResult.success(user.getStackInHand(hand));
    }
    @Override
    protected TypedActionResult<ItemStack> onNoScore(World world, PlayerEntity user, Hand hand, int die1, int die2, int die3, int die4){
        String out = "Rolled " + die1 +
                ", " + die2 +
                ", " + die3 +
                ", and " + die4 + ".";
        user.sendMessage(new LiteralText(out), true);
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private StatusEffect getPrizeStatusEffectType(int die1){
        switch (die1) {
            case 1:
                return StatusEffects.FIRE_RESISTANCE;
            case 2:
                return StatusEffects.WATER_BREATHING;
            case 3:
                return StatusEffects.SPEED;
            case 4:
                return StatusEffects.LUCK;
            case 5:
                return StatusEffects.ABSORPTION;
            case 6:
                return StatusEffects.STRENGTH;
            case 7:
                return StatusEffects.REGENERATION;
            default:
                return StatusEffects.RESISTANCE;
        }
    }

    private void createShockwave(PlayerEntity player,float blastPower) {
        if (!player.world.isClient) {
            player.world.createExplosion(player, player.getX(), player.getY(), player.getZ(), blastPower, Explosion.DestructionType.NONE);
        }

    }

    private static void spawnEffectCloud(PlayerEntity player,StatusEffectInstance appliedEffect) {
        AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(player.world, player.getX(), player.getY(), player.getZ());
        areaEffectCloudEntity.setRadius(2.5F);
        areaEffectCloudEntity.setRadiusOnUse(-0.5F);
        areaEffectCloudEntity.setWaitTime(10);
        areaEffectCloudEntity.setDuration(areaEffectCloudEntity.getDuration() / 2);
        areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
        areaEffectCloudEntity.addEffect(new StatusEffectInstance(appliedEffect));
        player.world.spawnEntity(areaEffectCloudEntity);
    }
}