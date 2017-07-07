package com.xray.client.gui;

import com.xray.common.reference.BlockContainer;
import com.xray.common.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by MiKeY on 07/07/17.
 */
public class GuiBlocks extends GuiScreen {
    private RenderItem render;
    private GuiBlocksList blockList;
    private ArrayList<BlockContainer> blocks = new ArrayList<>();
    private GuiTextField search;

    GuiBlocks() {
        for ( Block block : ForgeRegistries.BLOCKS ) {
            NonNullList<ItemStack> subBlocks = NonNullList.create();
            block.getSubBlocks( block.getCreativeTabToDisplayOn(), subBlocks );
            for( ItemStack subBlock : subBlocks ) {
                if (subBlock.isEmpty())
                    continue;

                Block tmpBlock = Block.getBlockFromItem( subBlock.getItem() );
                blocks.add( new BlockContainer( subBlock.getDisplayName(), tmpBlock, subBlock, subBlock.getItem(), subBlock.getItem().getRegistryName() ));
            }
        }
    }

    @Override
    public void initGui() {
        this.render = this.itemRender;
        this.blockList = new GuiBlocksList( this, this.blocks );

        search = new GuiTextField(0, getFontRender(), width / 2 -96, height / 2 + 85, 135, 18);
        search.setFocused(true);
        search.setCanLoseFocus(true);

        this.buttonList.add( new GuiButton( 0, width / 2 +43, height / 2 + 84, 60, 20, "Cancel" ) );
    }

    @Override
    public void actionPerformed( GuiButton button )
    {
        switch(button.id)
        {
            case 0: // Cancel
                mc.player.closeScreen();
                mc.displayGuiScreen( new GuiNewOre() );
                break;

            default:
                break;
        }
    }

    @Override
    protected void keyTyped( char charTyped, int hex ) throws IOException
    {

    }

    @Override
    public boolean doesGuiPauseGame() // Dont pause the game in single player.
    {
        return false;
    }

    @Override
    public void updateScreen()
    {
        search.updateCursorCounter();
    }

    @Override
    public void drawScreen( int x, int y, float f )
    {
        drawDefaultBackground();
        mc.renderEngine.bindTexture( new ResourceLocation(Reference.PREFIX_GUI+"bg.png") );
        GuiSettings.drawTexturedQuadFit(width / 2 - 110, height / 2 - 118, 229, 235, 0);

        super.drawScreen(x, y, f);
        search.drawTextBox();
        this.blockList.drawScreen( x,  y,  f );
    }

    @Override
    public void mouseClicked( int x, int y, int mouse ) throws IOException
    {
        super.mouseClicked( x, y, mouse );
        this.blockList.handleMouseInput(x, y);
    }

    FontRenderer getFontRender() {
        return mc.fontRenderer;
    }

    Minecraft getMinecraftInstance() {
        return this.mc;
    }

    RenderItem getRender() {
        return this.render;
    }
}
