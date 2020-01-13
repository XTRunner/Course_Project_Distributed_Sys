import P2App.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.io.*;
import java.util.*;

class P2CallBackImpl extends P2CallBackPOA{
    private ORB orb;

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    public void stateChange(String changeInfo)
    {
        //
    	System.out.println(changeInfo);
        //
    }

    public String stillAlive()
    {
        //
    	return "ack";
        //
    }
}

public class P2Client{
	static P2 p2Impl;
	static Bank bankImpl;
	
	static int clntFlag;
	
	public static void main(String args[]){
		try{
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();
			
			// get the root naming context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			
			// Use NamingContextExt instead of NamingContext. This is
			// part of the Interoperable naming Service.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			
			// resolve the Object Reference in Naming
			String name = "P2";
			p2Impl = P2Helper.narrow(ncRef.resolve_str(name));

			// resolve the Object Reference in Naming
			String bankName = "Bank";
			bankImpl = BankHelper.narrow(ncRef.resolve_str(bankName));

			P2CallBackImpl p2CallBackImpl = new P2CallBackImpl();
			p2CallBackImpl.setORB(orb);

			org.omg.CORBA.Object scRef = rootpoa.servant_to_reference(p2CallBackImpl);
			P2CallBack ncscRef = P2CallBackHelper.narrow(scRef);

			System.out.println("Obtained a handle on auction server object: " + p2Impl);
			phase3(ncscRef, p2Impl, bankImpl);
		}
		catch (Exception e){
			System.out.println("ERROR : " + e) ;
			e.printStackTrace(System.out);
		}
	}
	

	public static void phase3(P2CallBack clnref, P2 p2Impl, Bank bankImpl)throws IOException{
		// Status for user
		/*
		99. Login Selection
		1. Login with name and password
		2. Register 
		3. Return items list and client chooses the actor
		4. Post a Selling item
		5. Seller functions
		6. Bidder functions
		7. Bank Mangement
		
		*/
		clntFlag = 99;
		
		// Link to account in auction server
		String aucAccount = "";
		
		// For keyboard input
		int myInt = -1;
		String inputStr = "";
		String inputPass = "";
		int itemNum = -1;

		//long startTime;
		//long endTime;
		
		while(clntFlag != 0){
			switch(clntFlag){
				
				case 99:
					System.out.println("Welcome! 1. Login Existed Account 2. Register New Account 3. Exit \n");
					
					boolean rightType = true;
					while (rightType){
						try{
							Scanner scan = new Scanner(System.in);
							myInt = scan.nextInt();
							if ((myInt == 1) || (myInt == 2)){
								rightType = false;
								clntFlag = myInt;
							}
							else if (myInt == 3){
								System.exit(0);
							}
							else{
								System.out.println("Please enter 1, 2, or 3 \n");
							}
						}
						catch(Exception e){
							e.printStackTrace(System.out);
						}
					}
								
					//clntFlag = myInt;
					break;
					
				case 1:
					System.out.println("Please enter your account name: ");
					try{
						Scanner scan= new Scanner(System.in);
						inputStr= scan.nextLine();
					}
					catch(Exception e){
						e.printStackTrace(System.out);
						break;
					}
					System.out.println("\n");


					System.out.println("Please enter password: ");
					try{
						Scanner scan= new Scanner(System.in);
						inputPass= scan.nextLine();
					}
					catch(Exception e){
						e.printStackTrace(System.out);
						break;
					}

					//startTime = System.nanoTime();
					if(p2Impl.login(inputStr, inputPass, clnref)){
						//endTime = System.nanoTime();
						//System.out.println(endTime-startTime);
						aucAccount = inputStr;
						clntFlag = 3;
					}
					else{
						System.out.println("Login Failed!");
						clntFlag = 99;
					}
					break;
					
				case 2:
					System.out.println("Please enter your account name: ");
					try{
						Scanner scan= new Scanner(System.in);
						inputStr= scan.nextLine();
					}
					catch(Exception e){
						e.printStackTrace(System.out);
						break;
					}
					if(inputStr.length() < 3){
						System.out.println("The length of account name must be no less than 3 chars! \n");
						break;
					}
					
					//System.out.println("\n");
					
					boolean uniqueUsr = p2Impl.regAccount(inputStr);
					if(uniqueUsr){
						aucAccount = inputStr;

						System.out.println("Please enter your password: ");
						try{
							Scanner scan= new Scanner(System.in);
							inputPass= scan.nextLine();
						}
						catch(Exception e){
							e.printStackTrace(System.out);
							break;
						}
						if(inputPass.length() < 6){
							System.out.println("The length of password must be no less than 6 chars! \n");
							break;
						}

						System.out.println("Please enter your intial deposit: ");
						try{
							Scanner scan= new Scanner(System.in);
							myInt = scan.nextInt();
						}
						catch(Exception e){
							e.printStackTrace(System.out);
							break;
						}

						if (myInt < 0) {
							System.out.println("Your deposit balance cannot be negative! \n");
							break;
						}

						p2Impl.regNew(aucAccount, inputPass);
						bankImpl.regBank(aucAccount, myInt);
						clntFlag = 99;
					}
					else{
						System.out.println("Please try another name! \n");
					}
					break;

				case 3:
					//startTime = System.nanoTime();
					String itemsList = p2Impl.getItems(aucAccount);
					//endTime = System.nanoTime();
					//System.out.println(endTime - startTime);

					if (itemsList.equals(";")){
						System.out.println("There is no active auction now \n");
					}
					else{
						String[] showLists = itemsList.split(";");
						if (!showLists[0].isEmpty()) {
							System.out.println("Items you are selling: \n");
							System.out.println("Key \t Description \t Current Price \t Bidder \n");
							String[] sellList = showLists[0].split(":");
							for (String sellItem : sellList){
								System.out.println(sellItem + "\n");
							}
						}

						if (showLists.length == 2){
							if (!showLists[1].isEmpty()) {
								System.out.println("Items you can bid: \n");
								System.out.println("Key \t Description \t Current Price \t Seller \n");
								String[] buyList = showLists[1].split(":");
								for (String buyItem : buyList){
									System.out.println(buyItem + "\n");
								}
							}
						}
					}
					
					System.out.println("Which operation you wanna choose? 1. Post New Auction 2. Check Ur Posted Auction 3. Bid Active Auction 4. Bank Acount Mangement 5. Log Out\n");
					boolean right12 = true;
					while (right12){
						Scanner scan = new Scanner(System.in);
						try{
							myInt = scan.nextInt();
							if (myInt == 1){
								right12 = false;
								clntFlag = 4;
							}
							else if (myInt == 2){
								right12 = false;
								clntFlag = 5;
							}
							else if (myInt == 3){
								right12 = false;
								clntFlag = 6;
							}
							else if (myInt == 4){
								right12 = false;
								clntFlag = 7;
							}
							else if (myInt == 5){
								right12 = false;
								p2Impl.logout(aucAccount);
								clntFlag = 99;
							}
							else{
								System.out.println("Please enter 1, 2, 3, 4, or 5\n");
							}
						}
						catch(Exception e){
							e.printStackTrace(System.out);
						}
					}
					break;

				case 4:
					System.out.println("Please enter item description: ");
					try{
						Scanner scan= new Scanner(System.in);
						inputStr= scan.nextLine();
					}
					catch(Exception e){
						e.printStackTrace(System.out);
						break;
					}

					System.out.println("Please give an inital price: ");
					try{
						Scanner scan = new Scanner(System.in);
						myInt = scan.nextInt();
					}
					catch(Exception e){
						e.printStackTrace(System.out);
						break;
					}

					System.out.println("Here is the new auction with Description " + inputStr + " with the inital Price: " + Integer.toString(myInt) + ". Type Y for confirming, or N for cancelling \n");
					boolean rightYN4 = true;
					String inputYN4 = "";
					while (rightYN4){
						try{
							Scanner scan= new Scanner(System.in);
							inputYN4 = scan.nextLine();
							if ( inputYN4.equals("Y") || inputYN4.equals("N")){
								rightYN4 = false;
							}
							else{
								System.out.println("Please enter Y, or N \n");
							}
						}
						catch(Exception e){
							e.printStackTrace(System.out);
						}
					}

					if (inputYN4.equals("Y")){
						itemNum = p2Impl.postItem(aucAccount, inputStr, myInt);
						System.out.println("Your item is already posted! And your item key is " + Integer.toString(itemNum) + "\n");
					}
					
					clntFlag = 3;

					break;

				case 5:
					System.out.println("1. Check Status of Ur Posted Auction 2. Quit Current Stage \n");
					boolean right125 = true;
					while (right125){
						Scanner scan = new Scanner(System.in);
						try{
							myInt = scan.nextInt();
							if (myInt == 1){
								right125 = false;
							}
							else if (myInt == 2){
								right125 = false;
								clntFlag = 3;
							}
							else{
								System.out.println("Please enter 1, or 2 \n");
							}
						}
						catch(Exception e){
							e.printStackTrace(System.out);
						}
					}
					if (clntFlag == 3){
						break;
					}

					String postedList = p2Impl.getItems(aucAccount);
					if (postedList.equals(";")) {
						System.out.println("There is no active auction now \n");
						break;
					}
					String[] showLists = postedList.split(";");
					if (showLists[0].isEmpty()) {
						System.out.println("There is no active auction now \n");
						break;
					}
					else{
						System.out.println("Items you are selling: \n");
						System.out.println("Key \t Description \t Current Price \t Bidder \n");
						String[] sellList = showLists[0].split(":");
						for (String sellItem : sellList){
							System.out.println(sellItem + "\n");
						}
					}

					System.out.println("Do you wanna sell your item? Type Y for yes and N for no \n");
					boolean rightYN5 = true;
					while (rightYN5){
						try{
							Scanner scan= new Scanner(System.in);
							inputStr= scan.nextLine();
							if ( inputStr.equals("Y") || inputStr.equals("N") ){
								rightYN5 = false;
							}
							else{
								System.out.println("Please enter Y, or N \n");
							}
						}
						catch(Exception e){
							e.printStackTrace(System.out);
						}
					}
					if (inputStr.equals("Y")){
						System.out.println("Please enter the key (number) of the item you wanna sell \n");
						Scanner scan = new Scanner(System.in);
						try{
							myInt = scan.nextInt();
						}
						catch(Exception e){
							e.printStackTrace(System.out);
							break;
						}
						String curInfo = p2Impl.lockItems(aucAccount, myInt);
						if (curInfo.equals("Invalid Key \n") || curInfo.equals("No bidding on this now! Try again later \n") || curInfo.equals("Failed \n")){
							System.out.println(curInfo);
							break;
						}
						else{
							System.out.println(curInfo);
						}
					}
					else{
						break;
					}

					System.out.println("Type Y for selling it right now and N for cancelling \n");
					rightYN5 = true;
					while (rightYN5){
						try{
							Scanner scan= new Scanner(System.in);
							inputStr= scan.nextLine();
							if ( inputStr.equals("Y") || inputStr.equals("N") ){
								rightYN5 = false;
							}
							else{
								System.out.println("Please enter Y, or N \n");
							}
						}
						catch(Exception e){
							e.printStackTrace(System.out);
						}
					}

					if (inputStr.equals("Y")){

						String soldInfo = p2Impl.sell(aucAccount, myInt);
						if (soldInfo.equals("Failed")){
							System.out.println("Failed! \n");
						}
						else{
							bankImpl.deposit(aucAccount, Integer.parseInt(soldInfo));
							System.out.println("Sold Successfully! \n");
						}
					}
					else{
						p2Impl.unlockItem(aucAccount, myInt);
					}

					break;

				case 6:
					System.out.println("1. Checking Status of Active Auctions 2. Quit Current Stage \n");
					boolean right126 = true;
					while (right126){
						Scanner scan = new Scanner(System.in);
						try{
							myInt = scan.nextInt();
							if (myInt == 1){
								right126 = false;
							}
							else if (myInt == 2){
								right126 = false;
								clntFlag = 3;
							}
							else{
								System.out.println("Please enter 1, or 2 \n");
							}
						}
						catch(Exception e){
							e.printStackTrace(System.out);
						}
					}
					if (clntFlag == 3){
						break;
					}

					String aucList = p2Impl.getItems(aucAccount);
					if (aucList.equals(";")) {
						System.out.println("There is no active auction now \n");
						break;
					}
					String[] aucLists = aucList.split(";");
					if (aucLists.length == 1) {
						System.out.println("There is no active auction now \n");
						break;
					}
					else{
						System.out.println("Items you can bid: \n");
						System.out.println("Key \t Description \t Current Price \t Seller \n");
						String[] buyList = aucLists[1].split(":");
						for (String buyItem : buyList){
							System.out.println(buyItem + "\n");
						}
					}

					System.out.println("Do you wanna put any bid? Type Y for yes and N for no \n");
					boolean rightYN = true;
					while (rightYN){
						try{
							Scanner scan= new Scanner(System.in);
							inputStr= scan.nextLine();
							if ( inputStr.equals("Y") || inputStr.equals("N") ){
								rightYN= false;
							}
							else{
								System.out.println("Please enter Y, or N \n");
							}
						}
						catch(Exception e){
							e.printStackTrace(System.out);
						}
					}

					if (inputStr.equals("Y")){
						System.out.println("Please enter the key (number) of the item you wanna buy: \n");
						Scanner scan = new Scanner(System.in);
						try{
							myInt = scan.nextInt();
						}
						catch(Exception e){
							e.printStackTrace(System.out);
							break;
						}

						System.out.println("How much you wanna pay for it? \n");
						int payPrice = -1;
						scan = new Scanner(System.in);
						try{
							payPrice = scan.nextInt();
						}
						catch(Exception e){
							e.printStackTrace(System.out);
							break;
						}

						if (bankImpl.compareBank(aucAccount, payPrice)){
							String bidInfos = p2Impl.bid(aucAccount, myInt, payPrice);
							String[] bidInfo = bidInfos.split(";");

							if (bidInfo.length > 1){
								if (!bidInfo[0].equals("-")){
									bankImpl.deposit(bidInfo[0], Integer.parseInt(bidInfo[1]));
								}
								bankImpl.withdraw(aucAccount, payPrice);
								System.out.println("Bidding Successfully! \n");
							}
							else{
								System.out.println(bidInfos);
							}
							break;
						}
						else{
							System.out.println("No enough money in your account!\n");
						}
					}
					else{
						break;
					}

				case 7:
					System.out.println("1. Check Balance 2. Deposit 3. Withdraw 4. Close Account 5. Quit Current Stage \n");
					boolean right15 = true;
					String accountInfo = "";
					while (right15){
						try{
							Scanner scan = new Scanner(System.in);
							myInt = scan.nextInt();
							if (myInt == 5){
								clntFlag = 3;
								right15 = false;
							}
							else if(myInt == 1){
								//startTime = System.nanoTime();
								accountInfo = bankImpl.checkBank(aucAccount);
								//endTime = System.nanoTime();
								//System.out.println(endTime-startTime);
								System.out.println(accountInfo);
								right15 = false;
							}
							else if(myInt == 2){

								System.out.println("How much you want to deposit? \n");
								int payMoney = -1;

								scan = new Scanner(System.in);
								try{
									payMoney = scan.nextInt();
								}
								catch(Exception e){
									e.printStackTrace(System.out);
									break;
								}

								if (payMoney < 0){
									System.out.println("Input cannot be nonegative \n");
									break;
								}

								accountInfo = bankImpl.depositBank(aucAccount, payMoney);
								System.out.println(accountInfo);
								right15 = false;

							}
							else if(myInt == 3){
								
								System.out.println("How much you want to withdraw? \n");
								int payMoney = -1;

								scan = new Scanner(System.in);
								try{
									payMoney = scan.nextInt();
								}
								catch(Exception e){
									e.printStackTrace(System.out);
									break;
								}
								
								if (payMoney < 0){
									System.out.println("Input cannot be nonegative \n");
									break;
								}

								accountInfo = bankImpl.withdrawBank(aucAccount, payMoney);
								System.out.println(accountInfo);
								right15 = false;

							}
							else if(myInt == 4){
								//startTime = System.nanoTime();
								accountInfo = p2Impl.closeAccount(aucAccount);
								//endTime = System.nanoTime();
								//System.out.println(endTime-startTime);

								if (accountInfo.equals("Failed")){
									System.out.println("Failed! You are currently in an auction");
									right15 = false;
								}
								else{
									System.out.println(accountInfo);
									accountInfo = bankImpl.closeBank(aucAccount);
									System.out.println(accountInfo);
									clntFlag = 99;
									right15 = false;
								}
							}
							else{
								System.out.println("Please enter 1, 2, 3, 4 or 5\n");
							}
						}
						catch(Exception e){
							e.printStackTrace(System.out);
						}
					}

					break;
			}		
		}		
	}
}
