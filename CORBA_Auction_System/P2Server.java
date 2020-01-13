import P2App.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.util.*;


class P2Impl extends P2POA{
	private ORB orb;
	
	public void setORB(ORB orb_val) {
		orb = orb_val;
	}
	
	// <key = usrName, value = [password, status]>
	private Map<String, List<String>> bankServer = new HashMap<>();

	// <key = itemID, value = [seller, description, price, bidder, status]>
	private Map<Integer, List<String>> itemServer = new HashMap<>();

	// <key = id, value = ref>
	private Map<String, P2CallBack> nameServer = new HashMap<>();	

	private int itemKey = 0;

	public boolean login(String usrName, String password, P2CallBack objref){
		//long startTime = System.nanoTime();
		if (bankServer.containsKey(usrName)){
			// Check if clients still alive
			//Set<String> exUsrSet = nameServer.keySet();
			List<String> delList = new ArrayList<>();

			for (String exUsr : nameServer.keySet()){
        		try{
					String ack = nameServer.get(exUsr).stillAlive();
					//System.out.println(exUsr + " " + ack);
				}
				catch(Exception e){
					e.printStackTrace(System.out);
					delList.add(exUsr);
					List<String> tmp = bankServer.get(exUsr);
					tmp.set(1, "off");
					bankServer.put(exUsr, tmp);
					//System.out.println(pair.getKey() + " deleted");
				}
			}

			if (!delList.isEmpty()){
				for (String x : delList){
					nameServer.remove(x);
					//System.out.println(x + " deleted");
				}
			}


			if ( bankServer.get(usrName).get(1).equals("off") && bankServer.get(usrName).get(0).equals(password) ){
				List<String> tmp = bankServer.get(usrName);
				tmp.set(1, "on");
				bankServer.put(usrName, tmp);
				nameServer.put(usrName, objref);
				//long endTime = System.nanoTime();
				//System.out.println(endTime - startTime);
				return true;
			}
			return false;
		}
		return false;
	}

	public void logout(String usrName){
		List<String> usrInfo = bankServer.get(usrName);
		usrInfo.set(1, "off");
		bankServer.put(usrName, usrInfo);
		nameServer.remove(usrName);
	}

	public boolean regAccount(String usrName){
		if (bankServer.containsKey(usrName)){ 
			return false;
		}
		return true;
	}

	public void regNew(String usrName, String password){
		List<String> tmp = new ArrayList<>();
		tmp.add(password);
		//tmp.add(Integer.toString(balance));
		tmp.add("off");
		bankServer.put(usrName, tmp);
	}

	public String getItems(String usrName){
		// <key = itemID, value = [seller, description, price, bidder, status]>
		//long startTime = System.nanoTime();
		String res = "";
		for (int itemID : itemServer.keySet()){
			if ( itemServer.get(itemID).get(0).equals(usrName) && itemServer.get(itemID).get(4).equals("on")){
				res = res + itemID + "\t" + itemServer.get(itemID).get(1) + "\t" + itemServer.get(itemID).get(2) + "\t" + itemServer.get(itemID).get(3) + ":";
			}
		}
		res = res + ";";
		for (int itemID : itemServer.keySet()){
			if ( (!itemServer.get(itemID).get(0).equals(usrName)) && itemServer.get(itemID).get(4).equals("on")){
				res = res + itemID + "\t" + itemServer.get(itemID).get(1) + "\t" + itemServer.get(itemID).get(2) + "\t" + itemServer.get(itemID).get(0) + ":";
			}
		}
		//long endTime = System.nanoTime();
		//System.out.println(endTime-startTime);
		return res;
	}
	
	public int postItem(String usrName, String description, int initial){
		// <key = itemID, value = [seller, description, price, bidder, status]>
		List<String> tmp = new ArrayList<>();
		tmp.add(usrName);
		tmp.add(description);
		tmp.add(Integer.toString(initial));
		tmp.add("-");
		tmp.add("on");
		itemServer.put(itemKey, tmp);
		itemKey = itemKey + 1;
		return (itemKey - 1);
	}

	public String lockItems(String usrName, int keyItem){
		// <key = itemID, value = [seller, description, price, bidder, status]>
		String res = "";
		if (itemServer.containsKey(keyItem)){
			if (!itemServer.get(keyItem).get(3).equals("-")){
				if (itemServer.get(keyItem).get(0).equals(usrName) && itemServer.get(keyItem).get(4).equals("on")){
					List<String> tmp = itemServer.get(keyItem);
					tmp.set(4, "off");
					itemServer.put(keyItem, tmp);
					res = "The current price of your item " + Integer.toString(keyItem) + " is " + itemServer.get(keyItem).get(2) + " given by " + itemServer.get(keyItem).get(3) + "\n";
				}
				else{
					res = "Failed \n";
				}
			}
			else{
				res = "No bidding on this now! Try again later \n";
			}
		}
		else{
			res = "Invalid Key \n";
		}
		return res;
	}

	public String sell(String usrName, int keyItem){
		// <key = itemID, value = [seller, description, price, bidder, status]>
		// <key = usrName, value = [password, status]>
		if (itemServer.get(keyItem).get(0).equals(usrName)){

			List<String> tmpItem = itemServer.get(keyItem);
			String res = tmpItem.get(2);
			String winner = tmpItem.get(3);
			itemServer.remove(keyItem);

			// Inform bidder
			if (nameServer.containsKey(winner)){
				String informInfo = "You win the auction " + Integer.toString(keyItem) + " with description " + tmpItem.get(1) + " and with price " + tmpItem.get(2) + "\n";
				nameServer.get(winner).stateChange(informInfo);
			}

			return res;
		}
		else{
			return "Failed";
		}
	}

	public void unlockItem(String usrName, int keyItem){
		// <key = itemID, value = [seller, description, price, bidder, status]>
		if (itemServer.get(keyItem).get(0).equals(usrName)){
			List<String> tmp = itemServer.get(keyItem);
			tmp.set(4, "on");
			itemServer.put(keyItem, tmp);
		}
	}

	public String bid(String usrName, int keyItem, int money){
		// Money is not enough
		// off state
		// No such keyItem
		// Small than current price
		// success
		// bankServer <key = usrName, value = [password, money, status]>
		// itemServer <key = itemID, value = [seller, description, price, bidder, status]>
		if (itemServer.containsKey(keyItem)){
			//if ( Integer.parseInt(bankServer.get(usrName).get(1)) < money ){
			//	return "No enough money \n";
			//}
			if (itemServer.get(keyItem).get(4).equals("on")){
				int curPrice = Integer.parseInt(itemServer.get(keyItem).get(2));
				if ( (itemServer.get(keyItem).get(3).equals("-") && curPrice <= money) || ((!itemServer.get(keyItem).get(3).equals("-")) && curPrice < money) ){
					String curBidder = itemServer.get(keyItem).get(3);

					if (!curBidder.equals("-")){
						// previous bidder get money back
						// If have previous bidder
						//List<String> curBankInfo = bankServer.get(curBidder);
						//curBankInfo.set(1, Integer.toString(Integer.parseInt(curBankInfo.get(1)) + curPrice));
						//bankServer.put(curBidder, curBankInfo);
						if (nameServer.containsKey(curBidder)){
							String notifyCurBidder = "Another higher bid to Item " + Integer.toString(keyItem) + "! Current price is " + Integer.toString(money) + "\n";
							nameServer.get(curBidder).stateChange(notifyCurBidder);
						}
					}

					// current bidder lose money
					//List<String> nextBankInfo = bankServer.get(usrName);
					//nextBankInfo.set(1, Integer.toString(Integer.parseInt(nextBankInfo.get(1)) - money));
					//bankServer.put(usrName, nextBankInfo);

					// update itemServer
					List<String> curItemInfo = itemServer.get(keyItem);
					curItemInfo.set(2, Integer.toString(money));
					curItemInfo.set(3, usrName);
					itemServer.put(keyItem, curItemInfo);

					// Notify seller
					if (nameServer.containsKey(curItemInfo.get(0))){
						String notifySeller = "Another higher bid to your Item " + Integer.toString(keyItem) + " given by " + usrName + "! Current price is " + Integer.toString(money) + "\n";
						nameServer.get(curItemInfo.get(0)).stateChange(notifySeller);
					}

					String res = curBidder + ";" + Integer.toString(curPrice);

					return res;
				}
				else{
					return "Your bid needs to be higher than current price \n";
				}
			}
			else{
				return "Auction is unavailable now \n";
			}
		}
		else{
			return "Invalid Key \n";
		}
	}

	public String closeAccount(String usrName){
		// <key = itemID, value = [seller, description, price, bidder, status]>
		//long startTime = System.nanoTime();
		for (int itemID : itemServer.keySet()){
			if ( itemServer.get(itemID).get(0).equals(usrName) || itemServer.get(itemID).get(3).equals(usrName) ){
				//long endTime = System.nanoTime();
				//System.out.println(endTime-startTime);
				return "Failed";
			}
		}

		bankServer.remove(usrName);
		nameServer.remove(usrName);
		//long endTime = System.nanoTime();
		//System.out.println(endTime-startTime);

		return "Already Sign Out \n";

	}
}

class BankImpl extends BankPOA{
	private ORB orb;
	
	public void setORB(ORB orb_val) {
		orb = orb_val;
	}
	// <key = usrName, value = balance>
	private Map<String, Integer> bankServer = new HashMap<>();

	public void regBank(String usrName, int balance){
		bankServer.put(usrName, balance);
	}

	public void deposit(String usrName, int balance){
		int curBalance = bankServer.get(usrName);
		bankServer.put(usrName, curBalance + balance);
	}

	public void withdraw(String usrName, int balance){
		int curBalance = bankServer.get(usrName);
		bankServer.put(usrName, curBalance - balance);
	}

	public boolean compareBank(String usrName, int money){
		if (bankServer.get(usrName) >= money){
			return true;
		}
		else{
			return false;
		}
	}

	public String checkBank(String usrName){
		return "Your current balance: " + Integer.toString(bankServer.get(usrName)) + "\n";
	}

	public String depositBank(String usrName, int money){
		// <key = usrName, value = [password, money, status]>
		int curBalance = bankServer.get(usrName);
		bankServer.put(usrName, curBalance + money);
		return "Deposit Successfully! Your current balance is " + Integer.toString(bankServer.get(usrName)) + "\n";
	}

	public String withdrawBank(String usrName, int money){
		// <key = usrName, value = [password, money, status]>
		int curBalance = bankServer.get(usrName);

		if (money > curBalance){
			return "No enough money in your account! \n";
		}
		else{
			bankServer.put(usrName, curBalance - money);
			return "Withdraw " + Integer.toString(money) + " from your account. Your current balance is " + Integer.toString(bankServer.get(usrName)) + "\n";
		}
	}

	public String closeBank(String usrName){
		// <key = itemID, value = [seller, description, price, bidder, status]>

		int endBalance = bankServer.get(usrName);

		bankServer.remove(usrName);
		
		return "Get remaining " + Integer.toString(endBalance) + " from your bank account \n";

	}

}

public class P2Server {
	public static void main(String args[]){

		String selection = args[0];
		//System.out.println(selection);

		String[] argServer = Arrays.copyOfRange(args, 1, args.length);

		if (selection.equals("Auction")){

			try{
				// create and initialize the ORB
				ORB orb = ORB.init(argServer, null);

				// get reference to rootpoa & activate the POAManager
				POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
				rootpoa.the_POAManager().activate();

				// create servant and register it with the ORB
				P2Impl p2Impl = new P2Impl();
				p2Impl.setORB(orb);

				// get object reference from the servant
				org.omg.CORBA.Object ref = rootpoa.servant_to_reference(p2Impl);
				P2 href = P2Helper.narrow(ref);
			
				// get the root naming context. NameService invokes the name service
				org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			
				// Use NamingContextExt which is part of the Interoperable
				// Naming Service (INS) specification.
				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

				// bind the Object Reference in Naming
				String name = "P2";
				NameComponent path[] = ncRef.to_name( name );
				ncRef.rebind(path, href);

				System.out.println("Auction Server ready and waiting ...");

				// wait for invocations from clients
				orb.run();
			}
			catch (Exception e) {
				System.err.println("ERROR: " + e);
				e.printStackTrace(System.out);
			}
			System.out.println("AuctionServer Closing");
		}
		else if (selection.equals("Bank")){

			try{
				// create and initialize the ORB
				ORB orb = ORB.init(argServer, null);

				// get reference to rootpoa & activate the POAManager
				POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
				rootpoa.the_POAManager().activate();

				// create servant and register it with the ORB
				BankImpl bankImpl = new BankImpl();
				bankImpl.setORB(orb);

				// get object reference from the servant
				org.omg.CORBA.Object ref = rootpoa.servant_to_reference(bankImpl);
				Bank href = BankHelper.narrow(ref);
			
				// get the root naming context. NameService invokes the name service
				org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			
				// Use NamingContextExt which is part of the Interoperable
				// Naming Service (INS) specification.
				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

				// bind the Object Reference in Naming
				String name = "Bank";
				NameComponent path[] = ncRef.to_name( name );
				ncRef.rebind(path, href);

				System.out.println("Bank Server ready and waiting ...");

				// wait for invocations from clients
				orb.run();
			}
			catch (Exception e) {
				System.err.println("ERROR: " + e);
				e.printStackTrace(System.out);
			}
			System.out.println("Bank Server Closing");
		}
		else{
			System.out.println("Input Invalid");
		}
	}
}