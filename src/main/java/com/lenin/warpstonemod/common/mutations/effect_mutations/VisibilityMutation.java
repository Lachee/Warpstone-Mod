package com.lenin.warpstonemod.common.mutations.effect_mutations;

import com.lenin.warpstonemod.common.mutations.WarpMutations;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

public class VisibilityMutation extends EffectMutation{
	protected VisibilityMutation(LivingEntity _parentPlayer, int _mutationLevel) {
		super(_parentPlayer, EffectFactory.id, _mutationLevel,
				WarpMutations.nameConst + "effect.invisibility",
				WarpMutations.nameConst + "effect.glowing",
				"visibility_icon.png",
				"a2361e8f-1be0-478f-9742-a873400e9b6d");
	}

	@Override
	public void applyMutation() {
		super.applyMutation();

		switch (mutationLevel) {
			case -1:
				parentPlayer.setGlowing(true);
				break;
			case 0:
				break;
			case 1:
				parentPlayer.setInvisible(true);
				break;
		}
	}

	@Override
	public void clearMutation() {
		super.clearMutation();

		parentPlayer.setInvisible(false);
		parentPlayer.setGlowing(false);
	}

	public static class EffectFactory implements IEffectFactory {
		public EffectFactory() { }

		protected static int id;

		@Override
		public int getID() {
			return id;
		}

		@Override
		public void setID(int value){
			id = value;
		}

		@Override
		public EffectMutation factory(LivingEntity parent, int _mutationLevel) {
			return new VisibilityMutation(parent, _mutationLevel);
		}
	}
}