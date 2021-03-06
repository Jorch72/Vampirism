package de.teamlapen.vampirism.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import de.teamlapen.vampirism.ModItems;
import de.teamlapen.vampirism.entity.player.VampirePlayer;
import de.teamlapen.vampirism.tileEntity.TileEntityCoffin;
import de.teamlapen.vampirism.util.Logger;
import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * @author Moritz
 *
 *         Metadata: first two bits are the direction third bit determines whether it is occupied or not (true means it's occupied) therefore if occupied: meta & 4 != 0 fourth bit determines whether
 *         it is the primary block (true means it's primary) therefore if primary: meta & -8 != 0
 */
public class BlockCoffin extends BasicBlockContainer {
	public static final String name = "blockCoffin";
	public final static Material material = Material.rock;
	private final String TAG = "BlockCoffin";

	public BlockCoffin() {
		super(material, name);
		this.setCreativeTab(null);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par) {
		TileEntityCoffin te = (TileEntityCoffin) world.getTileEntity(x, y, z);
		if (te == null)
			return;
		world.setBlockToAir(te.otherX, te.otherY, te.otherZ);
		world.removeTileEntity(te.otherX, te.otherY, te.otherZ);
		if ((par & -8) != 0)
			world.spawnEntityInWorld(new EntityItem(world, x, y + 1, z, new ItemStack(ModItems.coffin, 1)));
		if ((par & 4) != 0)
			wakeSleepingPlayer(world, x, y, z);
		super.breakBlock(world, x, y, z, block, par);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCoffin();
	}

	public int getDirection(World world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z) & 3;
	}

	// Miscellaneous methods (rendertype etc.)
	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player) {
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par4, float f1, float f2, float f3) {
		if (world.isRemote) {
			return true;
		} else {
			// Gets the coordinates of the primary block
			if ((world.getBlockMetadata(x, y, z) & -8) == 0) {
				TileEntityCoffin te = (TileEntityCoffin) world.getTileEntity(x, y, z);
				x = te.otherX;
				y = te.otherY;
				z = te.otherZ;
			}
			if (player.isSneaking()) {
				if(player.getCurrentEquippedItem()!=null&&player.getCurrentEquippedItem().getItem() instanceof ItemDye){
					return false;
				}

			}

			if (world.provider.canRespawnHere() && world.getBiomeGenForCoords(x, z) != BiomeGenBase.hell) {
				if ((world.getBlockMetadata(x, y, z) & 4) != 0) {
					player.addChatComponentMessage(new ChatComponentTranslation("text.vampirism.coffin.occupied", new Object[0]));
					return true;
				}

				EntityPlayer.EnumStatus enumstatus = VampirePlayer.get(player).sleepInCoffinAt(x, y, z);

				if (enumstatus == EntityPlayer.EnumStatus.OK) {
					setCoffinOccupied(world, x, y, z, player, true);
					((TileEntityCoffin) world.getTileEntity(x, y, z)).markDirty();
					return true;
				} else {
					if (enumstatus == EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW) {
						player.addChatComponentMessage(new ChatComponentTranslation("text.vampirism.coffin.noSleep", new Object[0]));
					} else if (enumstatus == EntityPlayer.EnumStatus.NOT_SAFE) {
						player.addChatComponentMessage(new ChatComponentTranslation("tile.bed.notSafe", new Object[0]));
					}
					return true;
				}
			} else{
				player.addChatComponentMessage(new ChatComponentTranslation("text.vampirism.coffin.wrong_dimension"));
				return true;
			}
		}
	}

	@Override
	public void onBlockHarvested(World world, int par1, int par2, int par3, int par4, EntityPlayer player) {
		this.breakBlock(world, par1, par2, par3, this, 0);
	}

	/**
	 * Checks if the other block still exists
	 */
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		TileEntityCoffin tileEntity = (TileEntityCoffin) world.getTileEntity(x, y, z);
		if (tileEntity != null) {
			if (!(world.getBlock(tileEntity.otherX, tileEntity.otherY, tileEntity.otherZ) instanceof BlockCoffin)) {
				// Logger.d(TAG, "Other coffin block destroyed, removing this one");
				this.breakBlock(world, x, y, z, block, world.getBlockMetadata(x, y, z));
				// world.setBlockToAir(x, y, z);
				// world.removeTileEntity(x, y, z);
			}
		}
	}


	public void setCoffinOccupied(World world, int x, int y, int z, @Nullable EntityPlayer player, boolean flag) {
		setBedOccupied(world, x, y, z, player, flag);
		((TileEntityCoffin) world.getTileEntity(x, y, z)).occupied = flag;
		// if(!world.isRemote)
		// ((EntityPlayerMP)
		// player).playerNetServerHandler.sendPacket(world.getTileEntity(x, y,
		// z).getDescriptionPacket());
	}

	private void wakeSleepingPlayer(World world, int x, int y, int z) {
		if (world.isRemote)
			return;
		WorldServer w = (WorldServer) world;
		for (int i = 0; i < w.playerEntities.size(); i++) {
			EntityPlayer p = ((EntityPlayer) w.playerEntities.get(i));
			if (p.isPlayerSleeping()) {
				// Logger.d("BlockCoffin", String.format(
				// "Found sleeping player: x=%s, y=%s, z=%s",
				// p.playerLocation.posX, p.playerLocation.posY,
				// p.playerLocation.posZ));
				if (p.playerLocation.posX == x && p.playerLocation.posY == y && p.playerLocation.posZ == z) {
					VampirePlayer.get(p).wakeUpPlayer(false, true, false, false);
				}
			}
		}
	}
}