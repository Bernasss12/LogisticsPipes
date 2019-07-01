package logisticspipes.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import logisticspipes.gui.guidebook.GuiGuideBook;
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

	public static void setCurrentPage(GuiGuideBook.PageInformation page, EnumHand hand) {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(SetCurrentPagePacket.class).setHand(hand).setSliderProgress(page.getProgress()).setPage(page.getIndex()).setChapter(page.getChapter()).setDivision(page.getDivision()));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (world.isRemote) MainProxy.proxy.openGuideBookGui(hand);
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}
}
