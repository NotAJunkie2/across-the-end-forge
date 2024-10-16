package net.notajunkie.acrosstheend.entity.custom;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.notajunkie.acrosstheend.entity.ModEntities;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public class Enderfly extends Animal implements FlyingAnimal {
    public static final List<Block> LIGHT_SOURCES = Arrays.asList(
            Blocks.OCHRE_FROGLIGHT, Blocks.VERDANT_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT, Blocks.FIRE,
            Blocks.LANTERN, Blocks.SEA_LANTERN, Blocks.CANDLE, Blocks.JACK_O_LANTERN,
            Blocks.END_ROD, Blocks.GLOWSTONE, Blocks.SHROOMLIGHT, Blocks.REDSTONE_LAMP
    );

    public final AnimationState flyAnimationState = new AnimationState();
    private static final float maxAnimationSpeed = 2.25f;

    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Enderfly.class, EntityDataSerializers.BYTE);
    public static final int TICKS_BEFORE_LOCATING_NEW_LIGHT_SOURCE = 200;
    public static final String TAG_LIGHT_SOURCE_POS = "LightSourcePos";
    public static final String TAG_CANNOT_GO_TO_LIGHT_SOURCE_TICKS = "CannotGoToLightSourceTicks";
    private int stayOutOfLightSourceCountdown;
    int remainingCooldownBeforeLocatingNewLightSource;
    BlockPos savedLightSourceBlockPos;
    @Nullable
    EnderflyGoToLightSourceGoal goToLightSourceGoal;
    private int underWaterTicks;

    public Enderfly(EntityType<? extends Enderfly> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.lookControl = new EnderflyLookControl(this);

        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
        return pLevel.getBlockState(pPos).isAir() ? 10.0F : 0.0F;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(Items.POPPED_CHORUS_FRUIT), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(5, new EnderflyLocateLightSourceGoal());
        this.goToLightSourceGoal = new EnderflyGoToLightSourceGoal();
        this.goalSelector.addGoal(4, this.goToLightSourceGoal);
        this.goalSelector.addGoal(8, new EnderflyWanderGoal());
        this.goalSelector.addGoal(9, new FloatGoal(this));
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.hasLightSource()) {
            pCompound.put(TAG_LIGHT_SOURCE_POS, NbtUtils.writeBlockPos(this.getLightSourcePos()));
        }

        pCompound.putInt(TAG_CANNOT_GO_TO_LIGHT_SOURCE_TICKS, this.stayOutOfLightSourceCountdown);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.savedLightSourceBlockPos = null;
        if (pCompound.contains(TAG_LIGHT_SOURCE_POS)) {
            this.savedLightSourceBlockPos = NbtUtils.readBlockPos(pCompound.getCompound(TAG_LIGHT_SOURCE_POS));
        }

        super.readAdditionalSaveData(pCompound);
        this.stayOutOfLightSourceCountdown = pCompound.getInt(TAG_CANNOT_GO_TO_LIGHT_SOURCE_TICKS);
    }

    public void tick() {
        super.tick();
        if (this.random.nextFloat() < 0.05F) {
            for(int i = 0; i < this.random.nextInt(2) + 1; ++i) {
                this.spawnPortalParticle(this.level(), this.getX() - (double)0.3F,
                        this.getX() + (double)0.3F, this.getZ() - (double)0.3F,
                        this.getZ() + (double)0.3F, this.getY(0.25D));
            }
        }

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        this.flyAnimationState.startIfStopped(this.tickCount);
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f = Math.min(pPartialTick * 6f, maxAnimationSpeed);
        this.flyAnimationState.updateTime(f, maxAnimationSpeed);
    }

    private void spawnPortalParticle(Level pLevel, double pStartX, double pEndX, double pStartZ, double pEndZ, double pPosY) {
        pLevel.addParticle(ParticleTypes.PORTAL, Mth.lerp(pLevel.random.nextDouble(), pStartX, pEndX), pPosY, Mth.lerp(pLevel.random.nextDouble(), pStartZ, pEndZ), 0.0D, 0.0D, 0.0D);
    }

    void pathfindRandomlyTowards(BlockPos pPos) {
        Vec3 vec3 = Vec3.atBottomCenterOf(pPos);
        int i = 0;
        BlockPos blockpos = this.blockPosition();
        int j = (int)vec3.y - blockpos.getY();

        if (j > 2) {
            i = 4;
        } else if (j < -2) {
            i = -4;
        }

        int k = 6;
        int l = 8;
        int i1 = blockpos.distManhattan(pPos);
        if (i1 < 15) {
            k = i1 / 2;
            l = i1 / 2;
        }

        Vec3 vec31 = AirRandomPos.getPosTowards(this, k, l, i, vec3, (double)((float)Math.PI / 10F));
        if (vec31 != null) {
            this.navigation.setMaxVisitedNodesMultiplier(0.5F);
            this.navigation.moveTo(vec31.x, vec31.y, vec31.z, 1.0D);
        }
    }

    @VisibleForDebug
    public int getTravellingTicks() {
        return this.goToLightSourceGoal.travellingTicks;
    }

    boolean wantsToGoToALightSource() {
        return this.stayOutOfLightSourceCountdown <= 0 && this.getTarget() == null;
    }

    protected void customServerAiStep() {
        if (this.isInWaterOrBubble()) {
            ++this.underWaterTicks;
        } else {
            this.underWaterTicks = 0;
        }

        if (this.underWaterTicks > 10) {
            this.hurt(this.damageSources().drown(), 0.5F);
        }
    }

    @VisibleForDebug
    public boolean hasLightSource() {
        return this.savedLightSourceBlockPos != null;
    }

    @Nullable
    @VisibleForDebug
    public BlockPos getLightSourcePos() {
        return this.savedLightSourceBlockPos;
    }

    @VisibleForDebug
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    protected void sendDebugPackets() {
        super.sendDebugPackets();
    }

    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.stayOutOfLightSourceCountdown > 0) {
                --this.stayOutOfLightSourceCountdown;
            }

            if (this.remainingCooldownBeforeLocatingNewLightSource > 0) {
                --this.remainingCooldownBeforeLocatingNewLightSource;
            }

            boolean flag = this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0D;
            if (this.tickCount % 20 == 0 && !this.isLightSourceValid()) {
                this.savedLightSourceBlockPos = null;
            }
        }

    }

    boolean isLightSourceValid() {
        if (!this.hasLightSource()) {
            return false;
        } else if (this.isTooFarAway(this.savedLightSourceBlockPos)) {
            return false;
        } else {
            assert this.savedLightSourceBlockPos != null;
            BlockState blockState = this.level().getBlockState(this.savedLightSourceBlockPos);

            return LIGHT_SOURCES.contains(blockState.getBlock()) && (blockState.getLightEmission(Enderfly.this.level(), this.savedLightSourceBlockPos) == 15);
        }
    }

    boolean isTooFarAway(BlockPos pPos) {
        return !this.closerThan(pPos, 32);
    }

    private void setFlag(int pFlagId, boolean pValue) {
        if (pValue) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | pFlagId));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~pFlagId));
        }

    }

    private boolean getFlag(int pFlagId) {
        return (this.entityData.get(DATA_FLAGS_ID) & pFlagId) != 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6D)
                .add(Attributes.FLYING_SPEED, 0.6F)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel) {
            public boolean isStableDestination(BlockPos p_27947_) {
                return !this.level.getBlockState(p_27947_.below()).isAir();
            }
            
            public void tick() {
                super.tick();
            }
        };
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(false);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    public boolean isFood(ItemStack pStack) {
        return pStack.is(Items.POPPED_CHORUS_FRUIT);
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMITE_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.ENDERMAN_TELEPORT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMITE_DEATH;
    }

    protected float getSoundVolume() {
        return 0.4F;
    }

    @Nullable
    public Enderfly getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return ModEntities.ENDERFLY.get().create(pLevel);
    }

    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height * 0.5F;
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
    }

    public boolean isFlying() {
        return !this.onGround();
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            return super.hurt(pSource, pAmount);
        }
    }

    public @NotNull MobType getMobType() {
        return MobType.UNDEFINED;
    }

    boolean closerThan(BlockPos pPos, int pDistance) {
        return pPos.closerThan(this.blockPosition(), pDistance);
    }

    abstract static class BaseEnderflyGoal extends Goal {
        public abstract boolean canEnderflyUse();

        public abstract boolean canEnderflyContinueToUse();

        public boolean canUse() {
            return this.canEnderflyUse();
        }

        public boolean canContinueToUse() {
            return this.canEnderflyContinueToUse();
        }
    }

    @VisibleForDebug
    public class EnderflyGoToLightSourceGoal extends BaseEnderflyGoal {
        public static final int MAX_TRAVELLING_TICKS = 100;
        public static final int LIGHT_SOURCE_CLOSE_ENOUGH_DISTANCE = 2;

        int travellingTicks = Enderfly.this.level().random.nextIntBetweenInclusive(40, MAX_TRAVELLING_TICKS);
        @Nullable
        private Path lastPath;
        private int ticksStuck;

        EnderflyGoToLightSourceGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canEnderflyUse() {
            return Enderfly.this.savedLightSourceBlockPos != null && !Enderfly.this.hasRestriction()
                    && Enderfly.this.wantsToGoToALightSource() && !this.hasReachedTarget(Enderfly.this.savedLightSourceBlockPos)
                    && LIGHT_SOURCES.contains(Enderfly.this.level().getBlockState(Enderfly.this.savedLightSourceBlockPos).getBlock());
        }

        public boolean canEnderflyContinueToUse() {
            return this.canEnderflyUse();
        }

        public void start() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            super.start();
        }

        public void stop() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            Enderfly.this.navigation.stop();
            Enderfly.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        public void tick() {
            if (Enderfly.this.savedLightSourceBlockPos != null) {
                ++this.travellingTicks;
                if (Enderfly.this.navigation.isInProgress()) {
                    return;
                }

                if (!Enderfly.this.closerThan(Enderfly.this.savedLightSourceBlockPos, 16)) {
                    if (Enderfly.this.isTooFarAway(Enderfly.this.savedLightSourceBlockPos)) {
                        this.dropLightSource();
                    } else {
                        Enderfly.this.pathfindRandomlyTowards(Enderfly.this.savedLightSourceBlockPos);
                    }
                    return;
                }

                boolean flag = this.pathfindDirectlyTowards(Enderfly.this.savedLightSourceBlockPos);
                if (!flag) {
                    this.dropLightSource();
                } else if (this.lastPath != null && Enderfly.this.navigation.getPath().sameAs(this.lastPath)) {
                    ++this.ticksStuck;
                    if (this.ticksStuck > 60) {
                        this.dropLightSource();
                        this.ticksStuck = 0;
                    }
                } else {
                    this.lastPath = Enderfly.this.navigation.getPath();
                }
            }
        }

        private boolean pathfindDirectlyTowards(BlockPos pPos) {
            Enderfly.this.navigation.setMaxVisitedNodesMultiplier(10.0F);
            Enderfly.this.navigation.moveTo(pPos.getX(), pPos.getY(), pPos.getZ(), 1.0D);
            return Enderfly.this.navigation.getPath() != null && Enderfly.this.navigation.getPath().canReach();
        }

        private void dropLightSource() {
            Enderfly.this.savedLightSourceBlockPos = null;
            Enderfly.this.remainingCooldownBeforeLocatingNewLightSource = Enderfly.TICKS_BEFORE_LOCATING_NEW_LIGHT_SOURCE;
        }

        private boolean hasReachedTarget(BlockPos pPos) {
            if (Enderfly.this.closerThan(pPos, LIGHT_SOURCE_CLOSE_ENOUGH_DISTANCE)) {
                return true;
            } else {
                Path path = Enderfly.this.navigation.getPath();
                return path != null && path.getTarget().equals(pPos) && path.canReach() && path.isDone();
            }
        }
    }

    class EnderflyLocateLightSourceGoal extends BaseEnderflyGoal {
        private static final int MAX_LOCATE_RADIUS = 16;

        public boolean canEnderflyUse() {
            return Enderfly.this.remainingCooldownBeforeLocatingNewLightSource == 0 && !Enderfly.this.hasLightSource() && Enderfly.this.wantsToGoToALightSource();
        }

        public boolean canEnderflyContinueToUse() {
            return false;
        }

        public void start() {
            int validLightSourceIndex = -1;

            Enderfly.this.remainingCooldownBeforeLocatingNewLightSource = Enderfly.TICKS_BEFORE_LOCATING_NEW_LIGHT_SOURCE;
            List<BlockPos> validLightSources = this.findValidLightSources();

            if (validLightSources.isEmpty()) {
                return;
            }

            // Check if block can be reached
            for (BlockPos lightSource : validLightSources) {
                if (validLightSourceIndex == -1) {
                    validLightSourceIndex = validLightSources.indexOf(lightSource);
                    continue;
                }
                if (Enderfly.this.blockPosition().distSqr(lightSource) < Enderfly.this.blockPosition().distSqr(validLightSources.get(validLightSourceIndex))) {
                    validLightSourceIndex = validLightSources.indexOf(lightSource);
                }

            }

            if (validLightSourceIndex != -1) {
                Enderfly.this.savedLightSourceBlockPos = validLightSources.get(validLightSourceIndex);
                Enderfly.this.navigation.moveTo(
                        Enderfly.this.savedLightSourceBlockPos.getX(),
                        Enderfly.this.savedLightSourceBlockPos.getY(),
                        Enderfly.this.savedLightSourceBlockPos.getZ(), 1.0D
                );
            }
        }

        private List<BlockPos> findValidLightSources() {
            BlockPos enderflyPos = Enderfly.this.blockPosition();
            List<BlockPos> posList = Lists.newArrayList();
            BlockPos pBlockPos = null;
            BlockState pBlockState = null;

            for (int x = -MAX_LOCATE_RADIUS; x <= MAX_LOCATE_RADIUS; ++x) {
                for (int y = -MAX_LOCATE_RADIUS; y <= MAX_LOCATE_RADIUS; ++y) {
                    for (int z = -MAX_LOCATE_RADIUS; z <= MAX_LOCATE_RADIUS; ++z) {
                        pBlockPos = enderflyPos.offset(x, y, z);
                        pBlockState = Enderfly.this.level().getBlockState(pBlockPos);

                        // Skip if block is not in LIGHT_SOURCES or has no Air around
                        if (!LIGHT_SOURCES.contains(pBlockState.getBlock()) || !hasNoSolidRendererBlocksAround(Enderfly.this.level(), pBlockPos)) {
                            continue;
                        }
                        posList.add(pBlockPos);
                    }
                }
            }
            return posList;
        }

        public static boolean hasNoSolidRendererBlocksAround(Level level, BlockPos blockPos) {
            BlockState above = level.getBlockState(blockPos.above());
            BlockState below = level.getBlockState(blockPos.below());
            BlockState north = level.getBlockState(blockPos.north());
            BlockState south = level.getBlockState(blockPos.south());
            BlockState east = level.getBlockState(blockPos.east());
            BlockState west = level.getBlockState(blockPos.west());

            boolean above_below = (above.isSolidRender(level, blockPos.above()) && below.isSolidRender(level, blockPos.below()));
            boolean north_south = (north.isSolidRender(level, blockPos.north()) && south.isSolidRender(level, blockPos.south()));
            boolean west_east = (east.isSolidRender(level, blockPos.east()) && west.isSolidRender(level, blockPos.west()));

            return !(above_below && north_south && west_east);
        }
    }

    static class EnderflyLookControl extends LookControl {
        EnderflyLookControl(Mob pMob) {
            super(pMob);
        }

        public void tick() {
            super.tick();
        }
    }

    class EnderflyWanderGoal extends Goal {
        private static final int WANDER_THRESHOLD = 18;

        EnderflyWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canUse() {
            return Enderfly.this.navigation.isDone() && Enderfly.this.random.nextInt(10) == 0;
        }

        public boolean canContinueToUse() {
            return Enderfly.this.navigation.isInProgress();
        }

        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                Enderfly.this.navigation.moveTo(Enderfly.this.navigation.createPath(BlockPos.containing(vec3), 1), 1.0D);
            }

        }

        @Nullable
        private Vec3 findPos() {
            final int pRadius = 4;
            final int pYRange = 4;
            final int pMaxDistance = 4;

            Vec3 vec3;
            if (Enderfly.this.isLightSourceValid() && !Enderfly.this.closerThan(Enderfly.this.savedLightSourceBlockPos, WANDER_THRESHOLD)) {
                Vec3 vec31 = Vec3.atCenterOf(Enderfly.this.savedLightSourceBlockPos);
                vec3 = vec31.subtract(Enderfly.this.position()).normalize();
            } else {
                vec3 = Enderfly.this.getViewVector(0.0F);
            }

            Vec3 vec32 = HoverRandomPos.getPos(Enderfly.this, pRadius, pYRange, vec3.x, vec3.z, ((float)Math.PI / 2F), 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(Enderfly.this, pMaxDistance, pYRange/2, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
        }
    }
}
