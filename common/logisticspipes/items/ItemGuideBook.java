package logisticspipes.items;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import logisticspipes.gui.guidebook.GuiGuideBook;
import logisticspipes.gui.guidebook.book.SavedTab;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.SetCurrentPagePacket;
import logisticspipes.proxy.MainProxy;

public class ItemGuideBook extends LogisticsItem {

	public ItemGuideBook() {
		this.setMaxStackSize(1);
	}

	public static void setCurrentPage(SavedTab page, ArrayList<SavedTab> tabs, EnumHand hand) {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(SetCurrentPagePacket.class)
				.setHand(hand)
				.setSliderProgress(page.getProgress())
				.setPage(page.getPage())
				.setChapter(page.getChapter())
				.setDivision(page.getDivision())
				.setSavedTabs(tabs));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack itemstack = player.getHeldItem(hand);
		if (world.isRemote) MainProxy.proxy.openGuideBookGui(hand);
		return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
	}
}
