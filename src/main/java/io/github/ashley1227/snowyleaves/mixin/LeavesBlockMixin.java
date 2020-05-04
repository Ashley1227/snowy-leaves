package io.github.ashley1227.snowyleaves.mixin;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
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
		info.setReturnValue(!state.get(PERSISTENT_SNOW));
	}

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/block/LeavesBlock;randomTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;)V")
	public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info) {
		if (world.getBiome(pos).getPrecipitation() == Biome.Precipitation.SNOW && state.get(SNOWY) == false) {
			if (world.isRaining())
				world.setBlockState(pos, state.with(SNOWY, true));
			return;
		}
		if (state.get(SNOWY) && !world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.SNOW)) {
			if (world.getBiome(pos).getPrecipitation() == Biome.Precipitation.SNOW && world.isRaining())
				return;
			float chance = Math.max(world.getBiome(pos).getTemperature(), 0) + 0.3f;
			System.out.println(chance);
			if (world.isRaining())
				chance *= 3;
			if (Math.random() < chance)
				world.setBlockState(pos, state.with(SNOWY, false));
		}
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		System.out.println("epic");
	}

	@Override
	public void onProjectileHit(World world, BlockState state, BlockHitResult hitResult, Entity entity) {
		super.onProjectileHit(world, state, hitResult, entity);
		if(entity instanceof SnowballEntity) {
			BlockState stateOwO = state.with(LeavesBlockMixin.SNOWY, true).with(LeavesBlockMixin.PERSISTENT_SNOW, true);
			world.setBlockState(hitResult.getBlockPos(), stateOwO);
//			BlockStateParticleEffect particleEffect = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.getDefaultState());
			ItemStackParticleEffect particleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM,new ItemStack(Items.SNOWBALL));

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
