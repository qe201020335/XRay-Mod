package com.xray.client.gui;

import com.xray.common.XRay;
import com.xray.common.reference.OreButtons;
import com.xray.client.OresSearch;
import com.xray.common.config.ConfigHandler;
import com.xray.common.reference.OreInfo;
import com.xray.common.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiSettings extends GuiScreen
{
	private Map<String, OreButtons> buttons = new HashMap<>();
    private List<GuiPage> pageIndex = new ArrayList<>();
    private List<GuiList> listInfo = new ArrayList<>();

	private int pageCurrent, pageMax = 0;

	@Override
	public void initGui()
    {
        // Called when the gui should be (re)created.
		if( OresSearch.searchList.isEmpty() )
        {
            // This shouldnt happen. But return if it does.
			System.out.println( "[XRay] Error: searchList is empty inside initGui call!" );
			return;
		}

		this.buttons = new HashMap<>(); // String id for the button. Same as the button text. (Diamond / Iron ect.)
		this.buttonList.clear();
		this.listInfo.clear();
        pageIndex.clear();

        int x = width / 2 - 100, y = height / 2 - 106;
        int Count = 0, Page = 0;

		for( OreInfo ore : OresSearch.searchList )
        {
			if( buttons.get( ore.oreName ) != null )
			{
                // Button already created for this ore.
				buttons.get( ore.oreName ).ores.add( ore ); // Add this new OreInfo to the internal ArrayList for this button
			}
            else
            {
                // Create the new button for this ore.
				int id = Integer.parseInt( Integer.toString( ore.id ) + Integer.toString( ore.meta) );                     // Unique button id. int( str(id) + str(meta) )
                // very hacky... Need to keep an eye on it.
                if( Count % 9 == 0 && Count != 0 )
                {
                    Page++;
                    if( Page > pageMax )
                        pageMax++;

                    x = width / 2 - 100;
                    y = height / 2 - 106;
				}
                GuiButton tmpButton = new GuiButton(id, x+25, y, 160, 20, ore.oreName + ": " + (ore.draw ? "On" : "Off"));
                pageIndex.add(new GuiPage(x, y, Page, tmpButton, ore)); // create new button and set the text to Name: On||Off
                buttons.put( ore.oreName, new OreButtons( ore.oreName, id,  ore ) ); // Add this new button to the buttons hashmap.
				y += 21.8; // Next button should be placed down from this one.

                Count++;
			}
		}

        // only draws the current page
		for (GuiPage page : pageIndex) {
			if (page.getPage() != pageCurrent)
				continue; // skip the ones that are not on this page.

			this.buttonList.add(page.getButton());
			this.listInfo.add( new GuiList( page.x, page.y, page.ore, page.getButton(), page.getButton()) );
		}

		GuiButton aNextButton, aPrevButton;
		this.buttonList.add( new GuiButton(97, (width / 2) - 67, height / 2 + 86, 55, 20, "Add Ore" ) );
		this.buttonList.add( new GuiButton(98, (width / 2) - 10, height / 2 + 86, 82, 20, "Distance: "+ XRay.distStrings[ XRay.currentDist]) ); // Static button for printing the ore dictionary / searchList.
		this.buttonList.add( aNextButton = new GuiButton(-150, width / 2 + 75, height / 2 + 86, 30, 20, ">") );
		this.buttonList.add( aPrevButton = new GuiButton(-151, width / 2 - 100, height / 2 + 86, 30, 20, "<") );

        if( pageMax < 1 )
        {
            aNextButton.enabled = false;
            aPrevButton.enabled = false;
        }

        if( pageCurrent == 0 )
        	aPrevButton.enabled = false;
        
        if( pageCurrent == pageMax )
            aNextButton.enabled = false;
    }
	
	@Override
	public void actionPerformed( GuiButton button )
	{
			// Called on left click of GuiButton
		switch(button.id)
		{
			case 99: // Print OreDict
				for ( String name : OreDictionary.getOreNames() ) // Print the ore dictionary.
				{
					List<ItemStack> oreStack = OreDictionary.getOres( name);
					System.out.print( String.format("[OreDict] %-40.40s [%d types] ( ", name, oreStack.size() ) );
					StringBuilder idMetaCsv = new StringBuilder();
					if( oreStack.size() < 1 )
					{
						idMetaCsv.append( " )" );
					}

					for( ItemStack stack : oreStack )
					{
						if( stack == oreStack.get( oreStack.size() - 1 ) )
						{
							idMetaCsv.append( String.format( "%d:%d )", Item.getIdFromItem( stack.getItem() ), stack.getItemDamage() ) );
						}
						else
						{
							idMetaCsv.append( String.format( "%d:%d, ", Item.getIdFromItem( stack.getItem() ), stack.getItemDamage() ) );
						}
					}
					System.out.println( idMetaCsv.toString() );
				}

				if (!OresSearch.searchList.isEmpty()) // Print out the searchList.
				{
					for (OreInfo ore : OresSearch.searchList)
					{
						System.out.println(String.format("[XRay] OreInfo( %s, %d, %d, 0x%x, %b )", ore.oreName, ore.id, ore.meta, ore.color[0], ore.draw));
					}
				}
				break;

			case 98: // Distance Button
				if (XRay.currentDist < XRay.distNumbers.length - 1)
					XRay.currentDist++;
				else
					XRay.currentDist = 0;
				ConfigHandler.update("searchdist", false);
				break;

			case 97: // New Ore button
				mc.player.closeScreen();
				mc.displayGuiScreen( new GuiNewOre() );
				break;

		  case -150:
			  if( pageCurrent < pageMax )
			  {
				  pageCurrent ++;
			  }
			  break;

		  case -151:
			  if( pageCurrent > 0 )
			  {
				  pageCurrent --;
			  }
			  break;

		default:
			for( Map.Entry<String, OreButtons> entry : buttons.entrySet() )
			{
				// Iterate through the buttons map and check what ores need to be toggled
				String key = entry.getKey();            // Block name (Diamond)
				OreButtons value = entry.getValue();    // OreButtons structure

				if( value.id == button.id )
				{
					// Matched the buttons unique id.
					for( OreInfo tempOre : value.ores )
					{
						// Iterate through the ores that this button should toggle.
						for( OreInfo ore : OresSearch.searchList )
						{
							// Match this ore with the one in the searchList.
							if( (tempOre.id == ore.id) && (tempOre.meta == ore.meta) )
							{
								ore.draw = !ore.draw; // Invert searchList.ore.draw
								ConfigHandler.update( ore.oreName, ore.draw );
							}
						}
					}
				}
			}
			break;
		}

		this.initGui();
	}
	
	@Override
	protected void keyTyped( char par1, int par2 )
    {
		try {
			super.keyTyped( par1, par2 );
		} catch (IOException e) {
			e.printStackTrace();
		}
		if( (par2 == 1) || (par2 == mc.gameSettings.keyBindInventory.getKeyCode()) || par2 == XRay.keyBind_keys[ XRay.keyIndex_showXrayMenu ].getKeyCode() )
        {
            // Close on esc, inventory key or keybind
			mc.player.closeScreen();
		}
	}
	
	@Override
	public boolean doesGuiPauseGame()
    {
		return false;
	}

    // this should be moved to some sort of utility package but fuck it :).
    // this removes the stupid power of 2 rule that comes with minecraft.
    public static void drawTexturedQuadFit(double x, double y, double width, double height, double zLevel)
    {
    	Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder tessellate = tessellator.getBuffer();
		tessellate.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        tessellate.pos(x + 0, y + height, zLevel).tex( 0,1).endVertex();
        tessellate.pos(x + width, y + height, zLevel).tex( 1, 1).endVertex();
        tessellate.pos(x + width, y + 0, zLevel).tex( 1,0).endVertex();
        tessellate.pos(x + 0, y + 0, zLevel).tex( 0, 0).endVertex();
		Tessellator.getInstance().draw();
    }

	@Override
	public void drawScreen( int x, int y, float f ) {
		drawDefaultBackground();

		mc.renderEngine.bindTexture(new ResourceLocation(Reference.PREFIX_GUI + "bg.png"));
		drawTexturedQuadFit(width / 2 - 110, height / 2 - 118, 229, 235, 0);

		super.drawScreen(x, y, f);

		RenderHelper.enableGUIStandardItemLighting();

		for ( GuiList item : this.listInfo ) {
			ItemStack items = new ItemStack(Block.getBlockById( item.ore.getId() ), 64);
			this.itemRender.renderItemAndEffectIntoGUI(items, item.x+2, item.y+2);
		}

		RenderHelper.disableStandardItemLighting();
	}
	
	@Override
	public void mouseClicked( int x, int y, int mouse )
    {
		try {
			super.mouseClicked( x, y, mouse );
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}