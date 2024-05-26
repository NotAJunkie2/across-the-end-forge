package net.notajunkie.acrosstheend.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.notajunkie.acrosstheend.AcrossTheEnd;
import net.notajunkie.acrosstheend.item.ModItems;
import net.notajunkie.acrosstheend.util.ModTags;

import java.util.List;

@Mod.EventBusSubscriber(modid = AcrossTheEnd.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void anvilEvent(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.is(ModItems.DORMANT_VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get()) && right.is(ModItems.ENDERFLY_ESSENCE.get())
                && right.getCount() >= 4) {
            ItemStack output = new ItemStack(ModItems.VOID_GRASP_UPGRADE_SMITHING_TEMPLATE.get(), 1);
            event.setOutput(output);
            event.setMaterialCost(4);
            event.setCost(2);
        }
    }

    @SubscribeEvent
    public static void blockBreakEvent(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getMainHandItem();
        BlockState blockState = event.getState();
        Level level = event.getPlayer().level();
        ServerLevel serverLevel = (ServerLevel) player.level();
        int fortuneModifier = handItem.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        int silkTouchModifier = handItem.getEnchantmentLevel(Enchantments.SILK_TOUCH);

        // Exit if player is in creative mode
        if (player.isCreative()) {
            return;
        }
        // Exit if player is not holding an amethyst infused tool
        if (!handItem.is(ModTags.Items.AMETHYST_INFUSED_DIAMOND_TOOLS) && !handItem.is(ModTags.Items.AMETHYST_INFUSED_NETHERITE_TOOLS)) {
            return;
        }
        // Exit if block requires correct tool for drops and player is not holding correct tool
        if (blockState.requiresCorrectToolForDrops() && !handItem.isCorrectToolForDrops(blockState)) {
            return;
        }

        // Add drops to player inventory or spawn them if inventory is full
        List<ItemStack> drops = Block.getDrops(blockState, serverLevel, event.getPos(), null, player, handItem);
        for (ItemStack drop : drops) {
            if (!player.addItem(drop)) { player.spawnAtLocation(drop); }
        }

        // Add xp
        int xpDrop = blockState.getExpDrop(level, RandomSource.create(), event.getPos(), fortuneModifier, silkTouchModifier);
        // Spawn xp orb if xp drop is greater than 0
        if (xpDrop > 0) {
            event.getLevel().addFreshEntity(new ExperienceOrb(serverLevel, player.getX(), player.getY(), player.getZ(), xpDrop));
        }

        // Remove block
        event.getLevel().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 1);
    }

    @SubscribeEvent
    public static void onLootGenerated(LivingDropsEvent event) {
        Entity killer = event.getSource().getEntity();

        // Exit if killer is not a player
        if (!(killer instanceof Player)) {
            return;
        }
        // Exit if killer is in creative mode
        if (((Player) killer).isCreative()) {
            return;
        }
        // Exit if killer is not holding an amethyst infused weapon
        if (!(((Player) killer).getMainHandItem().is(ModTags.Items.AMETHYST_INFUSED_WEAPONS))) {
            return;
        }
        // Add drops to player inventory or spawn them if inventory is full
        for (ItemEntity drop : event.getDrops()) {
            ItemStack itemStack = drop.getItem();
            if (!((Player) killer).addItem(itemStack)) {
                killer.spawnAtLocation(itemStack);
            }
        }
        // Clear drops
        event.getDrops().clear();
    }
}
