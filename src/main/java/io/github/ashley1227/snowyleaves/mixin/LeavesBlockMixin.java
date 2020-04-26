package io.github.ashley1227.snowyleaves.mixin;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
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

	public LeavesBlockMixin(Settings settings) {
		super(settings);
	}

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/block/LeavesBlock;<init>(Lnet/minecraft/block/Block$Settings;)V")
	public void constructor(CallbackInfo info) {
		setDefaultState((BlockState) ((BlockState) ((BlockState) stateManager.getDefaultState()).with(LeavesBlock.DISTANCE, 7)).with(LeavesBlock.PERSISTENT, false).with(SNOWY, false));
	}

	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/block/LeavesBlock;hasRandomTicks(Lnet/minecraft/block/BlockState;)Z", cancellable = true)
	public void hasRandomTicks(BlockState state, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(true);
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

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/block/LeavesBlock;appendProperties(Lnet/minecraft/state/StateManager$Builder;)V")
	public void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo info) {
		builder.add(SNOWY);
	}

	static {
		SNOWY = Properties.SNOWY;
	}

}
