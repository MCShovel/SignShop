package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.World;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Arrays;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.specialops.*;

public class signshopUtil {
    public static String getOperation(String sSignOperation){
        if(sSignOperation.length() < 4){
            return "";
        }        
        sSignOperation = ChatColor.stripColor(sSignOperation);
        return sSignOperation.substring(1,sSignOperation.length()-1);
    }
    
    public static void generateInteractEvent(Block bLever, Player player, BlockFace bfBlockface) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getItemInHand(), bLever, bfBlockface);
        Bukkit.getServer().getPluginManager().callEvent(event);        
    }  
    
    public static List<String> getParameters(String sOperation) {
        List<String> parts = new LinkedList();
        if(sOperation.contains("{") && sOperation.contains("}")) {
            String[] bits = sOperation.split("\\{");
            if(bits.length == 2) {
                parts.add(bits[0]);
                String parameters = bits[1].replace("}", "");                
                String[] parbits = parameters.split(",");                
                if(parbits.length > 1)
                    parts.addAll(Arrays.asList(parbits));                    
                else
                    parts.add(parameters);
            }            
        }
        if(parts.isEmpty())
            parts.add(sOperation);        
        return parts;
    }
        
    public static Map<SignShopOperation, List> getSignShopOps(List<String> operation) {
        Map<SignShopOperation, List> SignShopOperations = new LinkedHashMap<SignShopOperation, List>();
        for(String sSignShopOp : operation) {            
            List<String> bits = getParameters(sSignShopOp);
            String op = bits.get(0);
            bits.remove(0);            
            try {
                Class<Object> fc = (Class<Object>)Class.forName("org.wargamer2010.signshop.operations."+op);
                SignShopOperations.put((SignShopOperation)fc.newInstance(), bits);
            } catch(ClassNotFoundException notfoundex) {                
                return null;
            } catch(InstantiationException instex) {                
                return null;
            } catch(IllegalAccessException illex) {                
                return null;
            }
        }
        return SignShopOperations;
    }
    
    public static List getSignShopSpecialOps() {
        List<SignShopSpecialOp> SignShopOperations = new LinkedList();
        for(String sSignShopOp : SignShop.SpecialsOps) {            
            try {
                Class<Object> fc = (Class<Object>)Class.forName("org.wargamer2010.signshop.specialops."+sSignShopOp);
                SignShopOperations.add((SignShopSpecialOp)fc.newInstance());
            } catch(ClassNotFoundException notfoundex) {                
                return null;
            } catch(InstantiationException instex) {                
                return null;
            } catch(IllegalAccessException illex) {                
                return null;
            }
        }
        return SignShopOperations;
    }
    
    public static Map<Enchantment, Integer> convertStringToEnchantments(String sEnchantments) {
        Map<Enchantment, Integer> mEnchantments = new HashMap<Enchantment, Integer>();
        String saEnchantments[] = sEnchantments.split(";");
        if(saEnchantments.length == 0)
            return mEnchantments;
        for(int i = 0; i < saEnchantments.length; i++) {
            String sEnchantment[] = saEnchantments[i].split("\\|");
            int iEnchantment; int iEnchantmentLevel;
            if(sEnchantment.length < 2)
                continue;
            else {
                try {
                    iEnchantment = Integer.parseInt(sEnchantment[0]);
                    iEnchantmentLevel = Integer.parseInt(sEnchantment[1]);
                } catch(NumberFormatException ex) {
                    continue;
                }
                Enchantment eTemp = Enchantment.getById(iEnchantment);
                if(eTemp != null)
                    mEnchantments.put(eTemp, iEnchantmentLevel);
            }
        }
        return mEnchantments;
    }
    
    public static String convertEnchantmentsToString(Map<Enchantment, Integer> aEnchantments) {
        String sEnchantments = "";
        Boolean first = true;
        for(Map.Entry<Enchantment, Integer> entry : aEnchantments.entrySet()) {
            if(first) first = false;
            else sEnchantments += ";";
            sEnchantments += (entry.getKey().getId() + "|" + entry.getValue());
        }
        return sEnchantments;
    }
    
    public static String convertLocationToString(Location loc) {
        return (loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ());
    }
    
    public static Location convertStringToLocation(String sLoc, World world) {
        String[] sCoords = sLoc.split("/");
        if(sCoords.length < 3)
            return null;
        try {
            Location loc = new Location(world, Double.parseDouble(sCoords[0]), Double.parseDouble(sCoords[1]), Double.parseDouble(sCoords[2]));
            return loc;
        } catch(NumberFormatException ex) {
            return null;
        }
    }
    
    public static Float getNumberFromThirdLine(Block bSign) {
        Sign sign = (Sign)bSign.getState();
        String XPline = sign.getLines()[2];
        return economyUtil.parsePrice(XPline);
    }
    
    public static String getError(String sType, Map<String, String> messageParts) {
        if(!SignShop.Errors.containsKey(sType) || SignShop.Errors.get(sType) == null)
            return "";
        return fillInBlanks(SignShop.Errors.get(sType), messageParts);
    }
    
    public static String getMessage(String sType, String sOperation, Map<String, String> messageParts) {
        if(!SignShop.Messages.get(sType).containsKey(sOperation) || SignShop.Messages.get(sType).get(sOperation) == null){
            return "";
        }
        return fillInBlanks(SignShop.Messages.get(sType).get(sOperation), messageParts);
    }
    
    public static String fillInBlanks(String message, Map<String, String> messageParts) {
        for(Map.Entry<String, String> part : messageParts.entrySet()) {            
            message = message.replace(part.getKey(), part.getValue());
        }        
        message = message.replace("\\", "");
        return message;
    }
   
}