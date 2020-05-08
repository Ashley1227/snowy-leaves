package io.github.ashley1227.snowyleaves.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block {

	private static BooleanProperty SNOWY;
	private static BooleanProperty PERSISTENT_SNOW;

	public LeavesBlockMixin(Settings settings) {
		super(settings);
	}

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/block/LeavesBlock;<init>(Lnet/minecraft/block/Block$Settings;)V")
	public void constructor(CallbackInfo info) {
		setDefaultState(stateManager.getDefaultState().with(LeavesBlock.DISTANCE, 7).with(LeavesBlock.PERSISTENT, false).with(SNOWY, false).with(PERSISTENT_SNOW, false));
	}

	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/block/LeavesBlock;hasRandomTicks(Lnet/minecraft/block/BlockState;)Z", cancellable = true)
	public void hasRandomTicks(BlockState state, CallbackInfoReturnable<Boolean> info) {
		if (!state.get(PERSISTENT_SNOW))
			info.setReturnValue(true);
	}

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/block/LeavesBlock;randomTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V")
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info) {
		if (world.isRaining() && world.getBiome(pos).getPrecipitation() == Biome.Precipitation.SNOW) {
			if(state.get(SNOWY).equals(false)) {
				BlockPos p = pos.up();
				for (int i = 0; i < 16; i++) {
					Block block = world.getBlockState(p).getBlock();
					if (
							!block.equals(Blocks.AIR)
									&& !block.equals(Blocks.SNOW)
									&& !BlockTags.LEAVES.contains(block)
					) {
						return;
					}
					p = p.up();
				}
				float chance = Math.max(-world.getBiome(pos).getTemperature(), 0) + 1f;
				chance -= world.getLightLevel(LightType.BLOCK, pos) * 0.2f;
				chance -= world.getLightLevel(LightType.SKY, pos) * 0.01f;

				if (chance > Math.random())
					world.setBlockState(pos, state.with(SNOWY, true));
			}
		} else if (state.get(SNOWY) && !world.getBlockState(pos.up()).getBlock().equals(Blocks.SNOW)) {
			if (world.getBiome(pos).getPrecipitation() == Biome.Precipitation.SNOW && world.isRaining())
				return;
			float chance = Math.max(world.getBiome(pos).getTemperature(), 0) + 0.2f;
			if (world.isRaining())
				chance *= 4;
			chance += world.getLightLevel(LightType.BLOCK, pos) * 0.2f;
			chance += world.getLightLevel(LightType.SKY, pos) * 0.01f;

			if (chance > Math.random())
				world.setBlockState(pos, state.with(SNOWY, false));
		}
	}

	@Override
	public void onProjectileHit(World world, BlockState state, BlockHitResult hitResult, Entity entity) {
		super.onProjectileHit(world, state, hitResult, entity);
		if (entity instanceof SnowballEntity) {
			BlockState stateOwO = state.with(LeavesBlockMixin.SNOWY, true).with(LeavesBlockMixin.PERSISTENT_SNOW, true);
			world.setBlockState(hitResult.getBlockPos(), stateOwO);
//			BlockStateParticleEffect particleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.getDefaultState());
			ItemStackParticleEffect particleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(Items.SNOWBALL));

			for (PlayerEntity p : world.getPlayers()) {
				world.playSound(p, hitResult.getBlockPos(), SoundEvents.BLOCK_SNOW_FALL, SoundCategory.BLOCKS, 1f, 1f);
			}
			for (int i = 0; i < 32; ++i) {
				world.addParticle(particleEffect, hitResult.getPos().getX(), hitResult.getPos().getY(), hitResult.getPos().getZ(), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/block/LeavesBlock;appendProperties(Lnet/minecraft/state/StateManager$Builder;)V")
	public void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo info) {
		builder.add(SNOWY);
		builder.add(PERSISTENT_SNOW);
	}

	static {
		SNOWY = Properties.SNOWY;
		PERSISTENT_SNOW = BooleanProperty.of("persistent_snow");
	}

}
