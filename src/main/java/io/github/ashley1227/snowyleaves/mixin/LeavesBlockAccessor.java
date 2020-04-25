package io.github.ashley1227.snowyleaves.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LeavesBlock.class)
public interface LeavesBlockAccessor {
	@Accessor BlockState updateDistanceFromLogs(BlockState state, IWorld world, BlockPos pos);
}
