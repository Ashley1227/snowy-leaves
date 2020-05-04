package io.github.ashley1227.snowyleaves.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.thrown.SnowballEntity;
import net.minecraft.entity.thrown.ThrownItemEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
public abstract class SnowballEntityMixin extends ThrownItemEntity {
	public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> type, World world) {
		super(type, world);
	}
	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/thrown/SnowballEntity;onCollision(Lnet/minecraft/util/hit/HitResult;)V", cancellable = true)
	public void onCollision(HitResult hitResult, CallbackInfo info) {
		if(hitResult instanceof BlockHitResult) {
			BlockHitResult blockHitResult = (BlockHitResult)hitResult;
			BlockState state = this.world.getBlockState(blockHitResult.getBlockPos());
			if(BlockTags.LEAVES.contains(state.getBlock())) {
				state.onProjectileHit(this.world, state, blockHitResult, this);
				if(Math.random() > 0.3)
					info.cancel();
			}
		}
	}
}
