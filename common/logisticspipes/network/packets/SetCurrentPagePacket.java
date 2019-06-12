package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
<<<<<<< feature/custom-guide-book
<<<<<<< feature/custom-guide-book
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameRegistry;
=======
>>>>>>> Remade some key parts
=======
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.GameRegistry;
>>>>>>> Formatting fixes

import lombok.Getter;
import lombok.Setter;
import sun.rmi.runtime.Log;

<<<<<<< feature/custom-guide-book
<<<<<<< feature/custom-guide-book
import logisticspipes.LPConstants;
import logisticspipes.LPItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemGuideBook;
import logisticspipes.items.LogisticsItem;
=======
import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemGuideBook;
>>>>>>> Remade some key parts
=======
import logisticspipes.LPConstants;
import logisticspipes.LPItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemGuideBook;
import logisticspipes.items.LogisticsItem;
>>>>>>> Formatting fixes
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SetCurrentPagePacket extends ModernPacket {

	@Getter
	@Setter
	private float sliderProgress;

	@Getter
	@Setter
	private int page;

	@Getter
	@Setter
	private EnumHand hand;

	public SetCurrentPagePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ItemStack book;
<<<<<<< feature/custom-guide-book
<<<<<<< feature/custom-guide-book
		book = player.getHeldItem(hand);
		if (book.getItem() != LPItems.itemGuideBook) return;
		NBTTagCompound nbt = book.getTagCompound();
		if(nbt == null) nbt = new NBTTagCompound();
		nbt.setFloat("sliderProgress", sliderProgress);
		nbt.setInteger("page", page);
		book.setTagCompound(nbt);
=======
		if (hand == EnumHand.MAIN_HAND) {
			if (!(player.getHeldItemMainhand().getItem().getClass() == ItemGuideBook.class)) return;
			book = player.getHeldItemMainhand();
		} else {
			if (!(player.getHeldItemOffhand().getItem().getClass() == ItemGuideBook.class)) return;
			book = player.getHeldItemOffhand();
		}
=======
		book = player.getHeldItem(hand);
		if (book.getItem() != LPItems.itemGuideBook) return;
>>>>>>> Formatting fixes
		NBTTagCompound nbt = book.getTagCompound();
		nbt.setFloat("sliderProgress", sliderProgress);
		nbt.setInteger("page", page);
>>>>>>> Remade some key parts
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		hand = input.readEnum(EnumHand.class);
		sliderProgress = input.readFloat();
		page = input.readInt();
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeEnum(hand);
		output.writeFloat(sliderProgress);
		output.writeInt(page);
	}

	@Override
	public ModernPacket template() {
		return new SetCurrentPagePacket(getId());
	}
}
