package com.lenin.warpstonemod.common.mutations;

import com.lenin.warpstonemod.common.WarpstoneMain;
import com.lenin.warpstonemod.common.items.IWarpstoneConsumable;
import com.lenin.warpstonemod.common.mutations.effect_mutations.EffectMutation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

import java.util.ArrayList;
import java.util.List;

public class MutateManager {
    protected final LivingEntity parentEntity;
    protected final List<AttributeMutation> attributeMutations = new ArrayList<>();
    protected List<Integer> effectMutations = new ArrayList<>();

    protected CompoundNBT mutData;

    protected int instability;
    protected int corruption;

    public MutateManager (LivingEntity _parentEntity){
        parentEntity = _parentEntity;
        instability = 0;
        corruption = 0;

        if (_parentEntity == null) return;

        //On the Manager's creation we create the Attribute Mutations classList
        WarpMutations[] array = WarpMutations.values();
        for (WarpMutations warpMutations : array) {
            attributeMutations.add(WarpMutations.constructAttributeMutation(warpMutations, parentEntity));
        }

        mutData = serialize();
    }

    public void mutate(IWarpstoneConsumable item){
        boolean hasEffectBeenCreated = false;

        //Loop over every point of instablity and apply levels, no negatives if no instablity
        for (int i = 0; i < getInstabilityLevel() + 1; i++) {
            boolean canMutateAttribute = true;

            if (!hasEffectBeenCreated && WarpstoneMain.getRandom().nextInt(100) > 85) {
                EffectMutation mut = getRandomEffectMut();

                if (mut != null) {
                    effectMutations.add(mut.getMutationID());
                    mut.applyMutation(parentEntity);

                    hasEffectBeenCreated = true;
                    canMutateAttribute = false;
                }
            }

            if (canMutateAttribute) attributeMutations.get(WarpstoneMain.getRandom().nextInt(attributeMutations.size())).changeLevel(5);

            if (i > 0) {
                attributeMutations.get(WarpstoneMain.getRandom().nextInt(attributeMutations.size())).changeLevel(-5);
            }
        }

        double witherRisk = getWitherRisk(item.getCorruptionValue());
        if (Math.random() > 1f - witherRisk) {
            int duration = WarpstoneMain.getRandom().nextInt((int) Math.round(800 * witherRisk));
            parentEntity.addPotionEffect(new EffectInstance(Effects.WITHER, duration));
        }

        int instabilityValue = item.getCorruptionValue() + (int) Math.round(item.getCorruptionValue() * (
                (double)getInstability() / 100) * (double)(WarpstoneMain.getRandom().nextInt((getCorruptionLevel() + 2) * 10) / 100)
        );
        int corruptionValue = Math.round(instabilityValue * (getInstabilityLevel() /10f));

        instability += instabilityValue;
        corruption += corruptionValue;
        mutData = serialize();

        MutateHelper.pushMutDataToClient(parentEntity.getUniqueID(), getMutData());
    }

    protected EffectMutation getRandomEffectMut () {
        if (effectMutations.size() >= WarpstoneMain.getEffectsMap().getMapSize()) return null;
        List<EffectMutation> legalList = new ArrayList<>();

        for (EffectMutation e : WarpstoneMain.getEffectsMap().getMap().values()) {
            if (!containsEffect(e) && e.isLegalMutation(this)) legalList.add(e);
        }

        if (legalList.isEmpty()) return null;

        int i = WarpstoneMain.getRandom().nextInt(legalList.size());
        return WarpstoneMain.getEffectsMap().constructInstance(legalList.get(i).getMutationID(), parentEntity);
    }

    protected CompoundNBT serialize (){
        CompoundNBT out = new CompoundNBT();
        out.putUniqueId("player", parentEntity.getUniqueID());
        out.putInt("instability", getInstability());
        out.putInt("corruption", getCorruption());

        for (AttributeMutation mut : getAttributeMutations()) {
            out.putInt(mut.getMutationType(), mut.getMutationLevel());
        }

        List<Integer> classList = new ArrayList<>(effectMutations);

        out.putIntArray("effect_mutations", classList);

        return out;
    }

    public void loadFromNBT (CompoundNBT nbt) {
        instability = nbt.getInt("instability");
        corruption = nbt.getInt("corruption");
        mutData = nbt;

        for (AttributeMutation mut : getAttributeMutations()) {
            mut.setLevel(nbt.getInt(mut.getMutationType()));
        }

        int[] array = nbt.getIntArray("effect_mutations");
        List<Integer> deletion = new ArrayList<>(effectMutations);

        for (int i : array) {
            if (containsEffect(i)) { deletion.remove((Integer) i); continue; }
            effectMutations.add(i);

            EffectMutation mut = WarpstoneMain.getEffectsMap().constructInstance(i, parentEntity);

            if (!parentEntity.world.isRemote()) {
                mut.applyMutation(parentEntity);
            }
        }

        for (int i : deletion) {
            effectMutations.remove((Integer) i);
        }
    }

    public void resetMutations (boolean death) {
        for (AttributeMutation m : attributeMutations) { m.setLevel(0); }

        for (int i : effectMutations) { getEffect(i).clearInstance(this.parentEntity); }
        effectMutations.clear();

        if (!death) corruption = 0;
        instability = 0;
        mutData = serialize();

        MutateHelper.pushMutDataToClient(parentEntity.getUniqueID(), getMutData());
    }

    public List<AttributeMutation> getAttributeMutations (){
        return attributeMutations;
    }

    public List<Integer> getEffectMutations (){
            return new ArrayList<>(effectMutations);
    }

    public LivingEntity getParentEntity (){
        return this.parentEntity;
    }

    public int getInstability(){
        return instability;
    }

    public int getInstabilityLevel (){
        return (int) Math.floor((double) (instability) / 100);
    }

    public int getCorruption () {
        return corruption;
    }

    public int getCorruptionLevel (){
        int threshold = 0;

        for (int i = 0; i < 10; i++) {
            threshold +=  i * (125 * (i + 1)) + 750;

            if (threshold > corruption) {
                if (i < 1) return 0;
                return 1;
            }
        }

        return 0;
    }

    public double getWitherRisk (int corruptionValue) {
        //System.out.println(getInstabilityLevel() + "   " + (double)getInstabilityLevel() / 10);
        //System.out.println(getInstabilityLevel() + "   " + (double)getCorruptionLevel() / 10);

        double value =  (((double) corruptionValue / 100f) * (((double) getInstabilityLevel() / 10 - 0.3) - (double) getCorruptionLevel() / 10));

        if (value < 0) return 0;
        return value;
    }

    public CompoundNBT getMutData () {
        if (mutData == null) System.out.println("Getting the NBT from Manager returns null");
        return mutData;
    }

    public void unload() {
        saveData();

        for (int i : effectMutations) {
            getEffect(i).clearInstance(this.parentEntity);
        }

        effectMutations.clear();
        attributeMutations.clear();
        MutateHelper.managers.remove(this);
    }

    public void saveData (){
        mutData = serialize();
        MutateHelper.savePlayerData(parentEntity.getUniqueID(), getMutData());
    }

    private EffectMutation getEffect (int id) {
        return WarpstoneMain.getEffectsMap().getEffectMutation(id);
    }

    public boolean containsEffect (EffectMutation mut) {
        return containsEffect(mut.getMutationID());
    }

    public boolean containsEffect (int id) {
        for (int i : effectMutations) {
            if (i == id) return true;
        }
        return false;
    }
}