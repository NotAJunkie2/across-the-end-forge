package net.notajunkie.acrosstheend.entity.custom;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
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

import javax.annotation.Nullable;
import java.util.*;

public class Enderfly extends Animal implements FlyingAnimal {
    public static final List<Block> LIGHT_SOURCES = Arrays.asList(
            Blocks.OCHRE_FROGLIGHT, Blocks.VERDANT_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT, Blocks.FIRE,
            Blocks.SHROOMLIGHT, Blocks.GLOWSTONE, Blocks.LANTERN, Blocks.SEA_LANTERN, Blocks.END_ROD
    );
    public final AnimationState flyAnimationState = new AnimationState();
    private final float maxAnimationSpeed = 2.25f;
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Enderfly.class, EntityDataSerializers.BYTE);
    private static final int TOO_FAR_DISTANCE = 32;
    private static final int LIGHT_SOURCE_CLOSE_ENOUGH_DISTANCE = 2;
    private static final int PATHFIND_TO_LIGHT_SOURCE_WHEN_CLOSER_THAN = 16;
    private static final int LIGHT_SOURCE_SEARCH_DISTANCE = 20;
    public static final int TICKS_BEFORE_LOCATING_NEW_LIGHT_SOURCE = 200;
    public static final String TAG_CANNOT_ENTER_LIGHT_SOURCE_TICKS = "CannotEnterLightSourceTicks";
    public static final String TAG_LIGHT_SOURCE_POS = "LightSourcePos";
    int ticksWithoutNectarSinceExitingLightSource;
    private int stayOutOfLightSourceCountdown;
    int remainingCooldownBeforeLocatingNewLightSource;
    BlockPos savedFlowerPos;
    @Nullable
    BlockPos lightSourceBlockPos;
    Enderfly.EnderflyGoToLightSourceGoal goToLightSourceGoal;
    private int underWaterTicks;

    public Enderfly(EntityType<? extends Enderfly> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.lookControl = new EnderflyLookControl(this);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
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
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(Items.CHORUS_FRUIT), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(5, new Enderfly.EnderflyLocateLightSourceGoal());
        this.goToLightSourceGoal = new Enderfly.EnderflyGoToLightSourceGoal();
        this.goalSelector.addGoal(5, this.goToLightSourceGoal);
        this.goalSelector.addGoal(8, new Enderfly.EnderflyWanderGoal());
        this.goalSelector.addGoal(9, new FloatGoal(this));
    }

    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.hasLightSource()) {
            pCompound.put("LightSourcePos", NbtUtils.writeBlockPos(this.getLightSourcePos()));
        }

        pCompound.putInt("TicksSincePollination", this.ticksWithoutNectarSinceExitingLightSource);
        pCompound.putInt("CannotEnterLightSourceTicks", this.stayOutOfLightSourceCountdown);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        this.lightSourceBlockPos = null;
        if (pCompound.contains("LightSourcePos")) {
            this.lightSourceBlockPos = NbtUtils.readBlockPos(pCompound.getCompound("LightSourcePos"));
        }

        this.savedFlowerPos = null;
        if (pCompound.contains("FlowerPos")) {
            this.savedFlowerPos = NbtUtils.readBlockPos(pCompound.getCompound("FlowerPos"));
        }

        super.readAdditionalSaveData(pCompound);
        this.ticksWithoutNectarSinceExitingLightSource = pCompound.getInt("TicksSincePollination");
        this.stayOutOfLightSourceCountdown = pCompound.getInt("CannotEnterLightSourceTicks");
    }

    public void tick() {
        super.tick();
        if (this.random.nextFloat() < 0.05F) {
            for(int i = 0; i < this.random.nextInt(2) + 1; ++i) {
                this.spawnPortalParticle(this.level(), this.getX() - (double)0.3F,
                        this.getX() + (double)0.3F, this.getZ() - (double)0.3F,
                        this.getZ() + (double)0.3F, this.getY(0.25D), ParticleTypes.PORTAL);
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
        float f = Math.min(pPartialTick * 6f, this.maxAnimationSpeed);
        this.flyAnimationState.updateTime(f, this.maxAnimationSpeed);
    }

    private void spawnPortalParticle(Level pLevel, double pStartX, double pEndX, double pStartZ, double pEndZ, double pPosY, ParticleOptions pParticleOption) {
        pLevel.addParticle(pParticleOption, Mth.lerp(pLevel.random.nextDouble(), pStartX, pEndX), pPosY, Mth.lerp(pLevel.random.nextDouble(), pStartZ, pEndZ), 0.0D, 0.0D, 0.0D);
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

    private boolean isTiredOfLookingForNectar() {
        return this.ticksWithoutNectarSinceExitingLightSource > 3600;
    }

    boolean wantsToGoToALightSource() {
        if (this.stayOutOfLightSourceCountdown <= 0 && this.getTarget() == null) {
            return this.isTiredOfLookingForNectar() || this.level().isRaining() || this.level().isNight();
        } else {
            return false;
        }
    }

    public void setStayOutOfLightSourceCountdown(int pStayOutOfLightSourceCountdown) {
        this.stayOutOfLightSourceCountdown = pStayOutOfLightSourceCountdown;
    }

    protected void customServerAiStep() {
        if (this.isInWaterOrBubble()) {
            ++this.underWaterTicks;
        } else {
            this.underWaterTicks = 0;
        }

        if (this.underWaterTicks > 20) {
            this.hurt(this.damageSources().drown(), 1.0F);
        }
    }

    public void resetTicksWithoutNectarSinceExitingLightSource() {
        this.ticksWithoutNectarSinceExitingLightSource = 0;
    }

    @VisibleForDebug
    public boolean hasLightSource() {
        return this.lightSourceBlockPos != null;
    }

    @Nullable
    @VisibleForDebug
    public BlockPos getLightSourcePos() {
        return this.lightSourceBlockPos;
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
                this.lightSourceBlockPos = null;
            }
        }

    }

    boolean isLightSourceValid() {
        if (!this.hasLightSource()) {
            return false;
        } else if (this.isTooFarAway(this.lightSourceBlockPos)) {
            return false;
        } else {
            BlockState blockState = this.level().getBlockState(this.lightSourceBlockPos);
            // Is in LIGHT_SOURCES
            return LIGHT_SOURCES.contains(blockState);
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
                .add(Attributes.FLYING_SPEED, (double)0.6F)
                .add(Attributes.MOVEMENT_SPEED, (double)0.3F)
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
        return pStack.is(Items.CHORUS_FRUIT);
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
    }

    protected SoundEvent getAmbientSound() {
        return null;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.BEE_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
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

    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    boolean closerThan(BlockPos pPos, int pDistance) {
        return pPos.closerThan(this.blockPosition(), (double)pDistance);
    }

    abstract class BaseEnderflyGoal extends Goal {
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
    public class EnderflyGoToLightSourceGoal extends Enderfly.BaseEnderflyGoal {
        public static final int MAX_TRAVELLING_TICKS = 600;
        int travellingTicks = Enderfly.this.level().random.nextInt(10);
        @Nullable
        private Path lastPath;
        private static final int TICKS_BEFORE_LIGHT_SOURCE_DROP = 60;
        private int ticksStuck;

        EnderflyGoToLightSourceGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canEnderflyUse() {
            return Enderfly.this.lightSourceBlockPos != null && !Enderfly.this.hasRestriction() && Enderfly.this.wantsToGoToALightSource() && !this.hasReachedTarget(Enderfly.this.lightSourceBlockPos) && Enderfly.this.level().getBlockState(Enderfly.this.lightSourceBlockPos).is(BlockTags.BEEHIVES);
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
            if (Enderfly.this.lightSourceBlockPos != null) {
                ++this.travellingTicks;
                if (Enderfly.this.navigation.isInProgress()) {
                    return;
                }

                if (!Enderfly.this.closerThan(Enderfly.this.lightSourceBlockPos, 16)) {
                    if (Enderfly.this.isTooFarAway(Enderfly.this.lightSourceBlockPos)) {
                        this.dropLightSource();
                    } else {
                        Enderfly.this.pathfindRandomlyTowards(Enderfly.this.lightSourceBlockPos);
                    }
                    return;
                }

                boolean flag = this.pathfindDirectlyTowards(Enderfly.this.lightSourceBlockPos);
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
            Enderfly.this.navigation.moveTo((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), 1.0D);
            return Enderfly.this.navigation.getPath() != null && Enderfly.this.navigation.getPath().canReach();
        }

        private void dropLightSource() {
            Enderfly.this.lightSourceBlockPos = null;
            Enderfly.this.remainingCooldownBeforeLocatingNewLightSource = Enderfly.TICKS_BEFORE_LOCATING_NEW_LIGHT_SOURCE;
        }

        private boolean hasReachedTarget(BlockPos pPos) {
            if (Enderfly.this.closerThan(pPos, 2)) {
                System.getLogger("Enderfly").log(System.Logger.Level.INFO, "Reached target");
                return true;
            } else {
                Path path = Enderfly.this.navigation.getPath();
                return path != null && path.getTarget().equals(pPos) && path.canReach() && path.isDone();
            }
        }
    }

    class EnderflyLocateLightSourceGoal extends Enderfly.BaseEnderflyGoal {
        private static final int MAX_LOCATE_DISTANCE = 20;
        public boolean canEnderflyUse() {
            return Enderfly.this.remainingCooldownBeforeLocatingNewLightSource == 0 && !Enderfly.this.hasLightSource() && Enderfly.this.wantsToGoToALightSource();
        }

        public boolean canEnderflyContinueToUse() {
            return false;
        }

        public void start() {
            Enderfly.this.remainingCooldownBeforeLocatingNewLightSource = Enderfly.TICKS_BEFORE_LOCATING_NEW_LIGHT_SOURCE;
            List<BlockPos> list = this.findNearbyLightSourcesWithSpace();
            int index = -1;
            boolean canReach;
            Level level = Enderfly.this.level();

            if (list.isEmpty()) {
                return;
            }
            // Check if block can be reached
            for (int i = 0; i < list.size(); i++) {
                BlockPos pPos = list.get(i);

                canReach = level.getBlockState(pPos.above()).isAir() || level.getBlockState(pPos.below()).isAir()
                        || level.getBlockState(pPos.north()).isAir() || level.getBlockState(pPos.south()).isAir()
                        || level.getBlockState(pPos.east()).isAir() || level.getBlockState(pPos.west()).isAir();


                if (canReach) {
                    System.getLogger("Enderfly").log(System.Logger.Level.INFO, "Can reach source at " + pPos);
                    if (index != -1) {
                        // If there is more than one valid hive, choose the closest one
                        if (Enderfly.this.blockPosition().distSqr(list.get(i)) < Enderfly.this.blockPosition().distSqr(list.get(index))) {
                            index = i;
                        }
                    } else {
                        index = i;
                    }
                }

            }

            if (index != -1) {
                Enderfly.this.lightSourceBlockPos = list.get(index);
                System.getLogger("Enderfly").log(System.Logger.Level.INFO, "Source found at " + Enderfly.this.lightSourceBlockPos);
                Enderfly.this.navigation.moveTo(Enderfly.this.lightSourceBlockPos.getX(), Enderfly.this.lightSourceBlockPos.getY(), Enderfly.this.lightSourceBlockPos.getZ(), 1.0D);
            }
        }

        private List<BlockPos> findNearbyLightSourcesWithSpace() {
            BlockPos blockpos = Enderfly.this.blockPosition();
            List<BlockPos> posList = Lists.newArrayList();

            for (int x = -MAX_LOCATE_DISTANCE; x <= MAX_LOCATE_DISTANCE; ++x) {
                for (int y = -MAX_LOCATE_DISTANCE; y <= MAX_LOCATE_DISTANCE; ++y) {
                    for (int z = -MAX_LOCATE_DISTANCE; z <= MAX_LOCATE_DISTANCE; ++z) {
                        // Add if block is in LIGHT_SOURCES
                        if (LIGHT_SOURCES.contains(Enderfly.this.level().getBlockState(blockpos.offset(x, y, z)).getBlock())) {
                            posList.add(blockpos.offset(x, y, z));
                        }
                    }
                }
            }
            return posList;
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
        private static final int WANDER_THRESHOLD = 22;

        EnderflyWanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
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
            Vec3 vec3;
            if (Enderfly.this.isLightSourceValid() && !Enderfly.this.closerThan(Enderfly.this.lightSourceBlockPos, WANDER_THRESHOLD)) {
                Vec3 vec31 = Vec3.atCenterOf(Enderfly.this.lightSourceBlockPos);
                vec3 = vec31.subtract(Enderfly.this.position()).normalize();
            } else {
                vec3 = Enderfly.this.getViewVector(0.0F);
            }

            int i = 8;
            Vec3 vec32 = HoverRandomPos.getPos(Enderfly.this, 8, 7, vec3.x, vec3.z, ((float)Math.PI / 2F), 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(Enderfly.this, 8, 4, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
        }
    }
}
