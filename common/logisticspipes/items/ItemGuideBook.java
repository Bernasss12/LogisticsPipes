package logisticspipes.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.SetCurrentPagePacket;
import logisticspipes.proxy.MainProxy;

public class ItemGuideBook extends LogisticsItem {

	public ItemGuideBook() {
		this.setMaxStackSize(1);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("page", 1);
		nbt.setFloat("sliderProgress", 0.0F);
	}

	public static void setCurrentPage(int slot, EntityPlayer player, NBTTagCompound nbt){
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SetCurrentPagePacket.class).setNbt(nbt).setSlot(slot), player);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (world.isRemote) MainProxy.proxy.openGuideBookGui(hand);
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}
}
