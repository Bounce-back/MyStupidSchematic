package io.github.bounceback.tutorialplugin;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;


public class TutorialPlugin extends JavaPlugin implements Listener {
	boolean ignoreAir=false;
	boolean includeBlocks=false;
	Hashtable<List<String>, List<Object[]>> schematic=new Hashtable<List<String>, List<Object[]>>();
	Hashtable<String, String[]> playerConfig=new Hashtable<String, String[]>();
	
	@Override
	public void onEnable() {
		getLogger().info("onEnable has been invoked");
		getServer().getPluginManager().registerEvents(
				new MyPlayerListener(), this);
		getServer().getPluginManager().registerEvents(this, this);
		
		File schematicFile=new File("schematics.ser");
		File playerConfigFile=new File("playerConfig.ser");
		try {
			if (!schematicFile.exists()) {
				schematicFile.createNewFile();
			
				FileOutputStream fileOut=new FileOutputStream("schematics.ser");
				ObjectOutputStream out=new ObjectOutputStream(fileOut);
				out.writeObject(schematic);
				out.flush();
				out.close();
			} else {
				FileInputStream fileIn=new FileInputStream("schematics.ser");
				ObjectInputStream in=new ObjectInputStream(fileIn);
				schematic=(Hashtable<List<String>, List<Object[]>>) in.readObject();
				in.close();
			}
			
//			if (!playerConfigFile.exists()) {
//				playerConfigFile.createNewFile();
//			
//				FileOutputStream fileOut=new FileOutputStream("playerConfig.ser");
//				ObjectOutputStream out=new ObjectOutputStream(fileOut);
//				out.writeObject(playerConfig);
//				out.flush();
//				out.close();
//			} else {
//				FileInputStream fileIn=new FileInputStream("playerConfig.ser");
//				ObjectInputStream in=new ObjectInputStream(fileIn);
//				playerConfig=(Hashtable<String, String[]>) in.readObject();
//				in.close();
//			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDisable() {
		getLogger().info("onDisable has been invoked");
	}
	
	@Override
	public boolean onCommand(CommandSender sender,
			Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("delallsch")) {
			schematic=new Hashtable<List<String>, List<Object[]>>();
			try {
				FileOutputStream fileOut=new FileOutputStream("schematics.ser");
				ObjectOutputStream out=new ObjectOutputStream(fileOut);
				out.writeObject(schematic);
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("copyme")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player");
			} else if (args.length!=6) {
				sender.sendMessage("plz fix num args");
			} else if (this.schematic.containsKey(Arrays.asList(args[3],args[4],args[5]))) {
				sender.sendMessage("this schematic already exists");
			} else {
				List<String> key=Arrays.asList(args[3],args[4],args[5]);
				int w;
				int l;
				int h;
				try {
					w=Integer.parseInt(args[0]); 
					l=Integer.parseInt(args[1]);
					h=Integer.parseInt(args[2]);
				} catch (NumberFormatException e) {
					sender.sendMessage("incorrect width/length/height");
					return false;
				}
				this.schematic.put(key, Arrays.asList(new String[w*l*h],new int[w*l*h][3]));
				Player player=(Player) sender;
				Location targetLoc=player.getTargetBlock(null,100).getLocation();
//				player.getTargetBlock(null, 100).getType().
//				player.getTargetBlock(null, 100).getBlockData().
				int count=0;
				for (int i=0;i<w;i++) {
					for (int j=0;j<h;j++) {
						for (int k=0;k<l;k++) {
							Location curLoc=targetLoc.clone();
							curLoc.add(i,j,k);
							this.schematic.get(key).get(0)[count]=curLoc.getBlock().getBlockData().getAsString();
							this.schematic.get(key).get(1)[count]=new int[] {i,j,k};

							count++;
						}
					}
				}
				
				try {
					FileOutputStream fileOut=new FileOutputStream("schematics.ser");
					ObjectOutputStream out=new ObjectOutputStream(fileOut);
					out.writeObject(schematic);
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("pasteme")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player");
			} else if (args.length>3||args.length<1) {
				sender.sendMessage("plz fix num args");
			} else {
				Player player=(Player) sender;
				if (!playerConfig.containsKey(player.getUniqueId().toString())) {
					playerConfig.put(player.getUniqueId().toString(), new String[5]);
				}
				String[] categories=playerConfig.get(player.getUniqueId().toString());
				for (int i=0;i<3;i++) {
					if (i<args.length) {categories[i]=args[i];}
					else {categories[i]=null;}
				}
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("listkeys")) {
			if (args.length>2) {
				sender.sendMessage("plz fix num args");
			} else {
				Set<String> categories=new HashSet<String>();
				Set<List<String>> validKeys=this.schematic.keySet();
				String rtnMsg="";
				if (args.length==0) {
					for (List<String> key:validKeys) {
						categories.add(key.get(0));
					}
				} else if (args.length==1) {
					for (List<String> key:validKeys) {
						if (key.get(0).equals(args[0])) {categories.add(key.get(1));}
					}
				} else if (args.length==2) {
					for (List<String> key:validKeys) {
						if (key.get(0).equals(args[0])&&key.get(1).equals(args[1])) {categories.add(key.get(2));}
					}
				}
				for (String category:categories) {rtnMsg+=category+" ";}
				sender.sendMessage("Valid keys: "+rtnMsg);
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("copypasteone")) {
			Player player=(Player) sender;
			Location loc=player.getTargetBlock(null,100).getLocation();
			sender.sendMessage(((Directional) loc.getBlock().getBlockData()).getFacing().toString());
//			Location newLoc=loc.clone().add(1,0,0);
//			newLoc.getBlock().setType(loc.getBlock().getType());
//			newLoc.getBlock().setBlockData(loc.getBlock().getBlockData());
			player.getTargetBlock(null,100).getBlockData();
		}
		return false;
	}
	
	@EventHandler(priority=EventPriority.LOW)
    public void onLogin(PlayerInteractEvent event) {
        if (event.getAction().toString()=="RIGHT_CLICK_AIR"||event.getAction().toString()=="RIGHT_CLICK_BLOCK") {
        	if (event.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.FEATHER)) {
        		Player player=event.getPlayer();
        		if (!playerConfig.containsKey(player.getUniqueId().toString())) {
        			player.sendMessage("No command bound");
        			return;
        		}
        		String[] playerSettings=playerConfig.get(event.getPlayer().getUniqueId().toString());
        		String[] args=new String[] {playerSettings[0],playerSettings[1],playerSettings[2]};
        		int numArgs=args[2]==null ? (args[1]==null ? (args[0]==null ? 0:1):2):3;
        		
        		List<List<String>> validKeys=new ArrayList<List<String>>();
				for (List<String> keyTemp:this.schematic.keySet()) {
					for (int i=0;i<numArgs;i++) {
						if (!keyTemp.get(i).equals(args[i])) {break;}
						if (i==numArgs-1) {validKeys.add(keyTemp);}
					}
				}
				if (validKeys.size()==0) {
					player.sendMessage("schematic doesn't exist");
					return;
				}
				
				List<String> key=validKeys.get(new Random().nextInt(validKeys.size()));
				int[][] locList=(int[][]) this.schematic.get(key).get(1);
				String[] blockDataStrings=(String[]) this.schematic.get(key).get(0);
				BlockData[] matList=new BlockData[blockDataStrings.length];
				for (int i=0;i<matList.length;i++) {matList[i]=Bukkit.createBlockData(blockDataStrings[i]);}
				
				Location targetLoc=player.getTargetBlock(null,100).getLocation();
				
				int maxW=0;
				int maxL=0;
				for (int[] loc:locList) {
					maxW=Math.max(loc[0], maxW);
					maxL=Math.max(loc[2], maxL);
				}
				
				int rotate=new Random().nextInt(2);
				int flipW=new Random().nextInt(2);
				int flipL=new Random().nextInt(2);
				
				for (int i=0;i<locList.length;i++) {
					Location curLoc=targetLoc.clone();
					if (rotate==1) {curLoc.add(
							(maxW-2*locList[i][2])*flipW+locList[i][2]-(int) maxW/2,
							locList[i][1],
							(maxL-2*locList[i][0])*flipL+locList[i][0]-(int) maxL/2
						);
					}
					else {curLoc.add(
							(maxW-2*locList[i][0])*flipW+locList[i][0]-(int) maxW/2,
							locList[i][1],
							(maxL-2*locList[i][2])*flipL+locList[i][2]-(int) maxL/2
					);}
					Hashtable<String,BlockFace> convert=new Hashtable<String,BlockFace>(){{
						put("EAST",BlockFace.EAST); put("EAST_NORTH_EAST",BlockFace.EAST_NORTH_EAST);
						put("NORTH_EAST",BlockFace.NORTH_EAST); put("NORTH_NORTH_EAST",BlockFace.NORTH_NORTH_EAST);
						put("NORTH",BlockFace.NORTH); put("NORTH_NORTH_WEST",BlockFace.NORTH_NORTH_WEST);
						put("NORTH_WEST",BlockFace.NORTH_WEST); put("WEST_NORTH_WEST",BlockFace.WEST_NORTH_WEST);
						put("WEST",BlockFace.WEST); put("WEST_SOUTH_WEST",BlockFace.WEST_SOUTH_WEST);
						put("SOUTH_WEST",BlockFace.SOUTH_WEST); put("SOUTH_SOUTH_WEST",BlockFace.SOUTH_SOUTH_WEST);
						put("SOUTH",BlockFace.SOUTH); put("SOUTH_SOUTH_EAST",BlockFace.SOUTH_SOUTH_EAST);
						put("SOUTH_EAST",BlockFace.SOUTH_EAST); put("EAST_SOUTH_EAST",BlockFace.EAST_SOUTH_EAST);
						put("DOWN",BlockFace.DOWN); put("UP",BlockFace.UP); put("SELF",BlockFace.SELF);
					}};
					
					Hashtable<String,String> rotateDict=new Hashtable<String,String>() {{
						put("EAST","SOUTH"); put("EAST_NORTH_EAST","SOUTH_SOUTH_WEST");
						put("NORTH_EAST","SOUTH_WEST"); put("NORTH_NORTH_EAST","WEST_SOUTH_WEST");
						put("NORTH","WEST"); put("NORTH_NORTH_WEST","WEST_NORTH_WEST");
						put("NORTH_WEST","NORTH_WEST"); put("WEST_NORTH_WEST","NORTH_NORTH_WEST");
						put("WEST","NORTH"); put("WEST_SOUTH_WEST","NORTH_NORTH_EAST");
						put("SOUTH_WEST","NORTH_EAST"); put("SOUTH_SOUTH_WEST","EAST_NORTH_EAST");
						put("SOUTH","EAST"); put("SOUTH_SOUTH_EAST","WEST_SOUTH_WEST");
						put("SOUTH_EAST","SOUTH_EAST"); put("EAST_SOUTH_EAST","SOUTH_SOUTH_EAST");
						put("DOWN","DOWN"); put("UP","UP"); put("SELF","SELF");
					}};
					Hashtable<String,String> flipWDict=new Hashtable<String,String>() {{
						put("EAST","WEST"); put("EAST_NORTH_EAST","WEST_NORTH_WEST");
						put("NORTH_EAST","NORTH_WEST"); put("NORTH_NORTH_EAST","NORTH_NORTH_WEST");
						put("NORTH","NORTH"); put("NORTH_NORTH_WEST","NORTH_NORTH_EAST");
						put("NORTH_WEST","NORTH_EAST"); put("WEST_NORTH_WEST","EAST_NORTH_EAST");
						put("WEST","EAST"); put("WEST_SOUTH_WEST","EAST_SOUTH_EAST");
						put("SOUTH_WEST","SOUTH_EAST"); put("SOUTH_SOUTH_WEST","SOUTH_SOUTH_EAST");
						put("SOUTH","SOUTH"); put("SOUTH_SOUTH_EAST","SOUTH_SOUTH_WEST");
						put("SOUTH_EAST","SOUTH_WEST"); put("EAST_SOUTH_EAST","WEST_SOUTH_WEST");
						put("DOWN","DOWN"); put("UP","UP"); put("SELF","SELF");
					}};
					Hashtable<String,String> flipLDict=new Hashtable<String,String>() {{
						put("EAST","EAST"); put("EAST_NORTH_EAST","EAST_SOUTH_EAST");
						put("NORTH_EAST","SOUTH_EAST"); put("NORTH_NORTH_EAST","SOUTH_SOUTH_EAST");
						put("NORTH","SOUTH"); put("NORTH_NORTH_WEST","SOUTH_SOUTH_WEST");
						put("NORTH_WEST","SOUTH_WEST"); put("WEST_NORTH_WEST","WEST_SOUTH_WEST");
						put("WEST","WEST"); put("WEST_SOUTH_WEST","WEST_NORTH_WEST");
						put("SOUTH_WEST","NORTH_WEST"); put("SOUTH_SOUTH_WEST","NORTH_NORTH_WEST");
						put("SOUTH","NORTH"); put("SOUTH_SOUTH_EAST","NORTH_NORTH_EAST");
						put("SOUTH_EAST","NORTH_EAST"); put("EAST_SOUTH_EAST","EAST_NORTH_EAST");
						put("DOWN","DOWN"); put("UP","UP"); put("SELF","SELF");
					}};
					
					curLoc.getBlock().setBlockData(matList[i],false);
					if (matList[i] instanceof Rotatable) {
						Rotatable rotation=(Rotatable) matList[i].clone();
						String dir=rotation.getRotation().toString();
						if (rotate==1) {dir=rotateDict.get(dir);}
						if (flipL==1) {dir=flipLDict.get(dir);}
						if (flipW==1) {dir=flipWDict.get(dir);}
						rotation.setRotation(convert.get(dir));
						curLoc.getBlock().setBlockData(rotation,false);
					} else if (matList[i] instanceof Directional) {
						Directional direction = (Directional) matList[i].clone();
						String dir=direction.getFacing().toString();
						if (rotate==1) {dir=rotateDict.get(dir);}
						if (flipL==1) {dir=flipLDict.get(dir);}
						if (flipW==1) {dir=flipWDict.get(dir);}
						direction.setFacing(convert.get(dir));
						curLoc.getBlock().setBlockData(direction,false);
					}
				}
        	}
        }
    }
	
}
