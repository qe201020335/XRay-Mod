package fgtXray.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import fgtXray.BlockInfo;
import fgtXray.FgtXRay;
import fgtXray.OreInfo;

public class OresSearch {
	public static List<OreInfo> searchList = new ArrayList();

	public OresSearch(){}
	
	private static boolean checkList( List<OreInfo> temp, OreInfo value, ItemStack stack ){
		for( OreInfo oreCheck : temp ){
			if( (oreCheck.oreName == value.oreName) && (oreCheck.id == stack.itemID) && (oreCheck.meta == stack.getItemDamage()) ){
				return true; // This ore already exists in the temp list. (Sometimes the OreDict returns duplicate entries, like gold twice) 
			}
		}
		return false;
	}
	
	public static List<OreInfo> get(){
		if( OresSearch.searchList.isEmpty() ){
			System.out.println( "[FindIt] --- Populating the searchList with the ore dictionary --- ");
			List<OreInfo> temp = new ArrayList();
			
			for( Map.Entry<String, OreInfo> entry : FgtXRay.defaultOres.entrySet() ){
				String key = entry.getKey();
				OreInfo value = entry.getValue();
				
				int lastId = 0;
				int lastMeta = 0;
				for( ItemStack stack : OreDictionary.getOres( OreDictionary.getOreID( key ) ) ){
					if( checkList( temp, value, stack ) ){ // Check if ore already added to temp
						System.out.println("[FindIt] Duplicate ore found in Ore Dictionary!!! ("+key+")");
						continue;
					} 
					temp.add( new OreInfo( value.oreName, stack.itemID, stack.getItemDamage(), value.color, value.draw) );
					System.out.println( String.format("[FindIt] Adding OreInfo( %s, %d, %d, 0x%x, %b ) ", value.oreName, stack.itemID, stack.getItemDamage(), value.color, value.draw ) );
				}
			}
			System.out.println( "[FindIt] --- Done populating searchList! --- ");
			System.out.println( "[FindIt] --- Adding custom blocks --- ");
			
			for( OreInfo ore : FgtXRay.customOres ){ //TODO: Check if custom already exists
				System.out.println( String.format( "[FindIt] Adding OreInfo( %s, %d, %d, 0x%x, %b ) ", ore.oreName, ore.id, ore.meta, ore.color, ore.draw ) );
				temp.add( ore );
			}
			System.out.println( "[FindIt] --- Done adding custom blocks --- ");
			
			OresSearch.searchList.clear();
			OresSearch.searchList.addAll( temp );
			
		}
		return OresSearch.searchList;
	}
}
