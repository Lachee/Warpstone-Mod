package com.lenin.warpstonemod.common.mutations.effect_mutations;

import com.lenin.warpstonemod.common.mutations.WarpMutations;
import net.minecraft.entity.LivingEntity;

public class PpMutation extends EffectMutation {
	protected PpMutation(LivingEntity _parentPlayer, int _id) {
		super(_parentPlayer, _id,
				WarpMutations.nameConst + "effect.largepp",
				WarpMutations.nameConst + "effect.smallpp",
				"pp_icon.png",
				"ba2f092b-76d6-4d71-85ba-51becadb4d19");
	}

	@Override
	protected void applyMutation() {}

	@Override
	public void clearMutation() {}

	public static class EffectFactory implements IEffectFactory {
		private int id;

		public EffectFactory (){}

		@Override
		public int getID() {
			return id;
		}

		@Override
		public void setID(int value) {
			id = value;
		}

		@Override
		public EffectMutation factory(LivingEntity parent) {
			return new PpMutation(parent, id);
		}
	}
}