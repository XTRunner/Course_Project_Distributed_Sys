import P3App.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class P3CallBackImpl extends P3CallBackPOA{
    private ORB orb;
    
    public void setORB(ORB orb_val) {
        orb = orb_val;
    }
    
    public void inform(String deadline){
        //
    	SimpleDateFormat formatter =new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    	Date due = null;
		try {
			due = formatter.parse(deadline);
		} catch (ParseException e) {
			System.out.println("date incorrect");
		}
    	System.out.println("A gambling is on! The deadline is: " + due);
    	System.out.println("Press Y to join Or just ignore that msg");
    }
    
    public void inform_end(){
        
    	System.out.println("Your gambling is end!");
    	System.out.println("Please enter your gambling number:");
    	//System.out.println("Press Y to join Or just ignore that msg");
    }
    
    public void inform_msg(String broad_cast){
        
    	System.out.println(broad_cast);
    	//System.out.println("Please enter your gambling number:");
    	//System.out.println("Press Y to join Or just ignore that msg");
    }

}

public class P3Participant{
	static P3 p3Impl;
	
	static int clntFlag;
	
	public static boolean gamble_on = false;
	
	public static void main(String args[]){
		
		String selection = args[0];

		String[] argServer = Arrays.copyOfRange(args, 1, args.length);
		
		if (selection.equals("participants")) {
			try{
				// create and initialize the ORB
				ORB orb = ORB.init(argServer, null);

				POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
				rootpoa.the_POAManager().activate();
			
				// get the root naming context
				org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			
				// Use NamingContextExt instead of NamingContext. This is
				// part of the Interoperable naming Service.
				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			
				// resolve the Object Reference in Naming
				String name = "P3";
				p3Impl = P3Helper.narrow(ncRef.resolve_str(name));

				P3CallBackImpl p3CallBackImpl = new P3CallBackImpl();
				p3CallBackImpl.setORB(orb);

				org.omg.CORBA.Object scRef = rootpoa.servant_to_reference(p3CallBackImpl);
				P3CallBack ncscRef = P3CallBackHelper.narrow(scRef);

				System.out.println("Obtained a handle on bank server object: " + p3Impl);
				phase3_play(ncscRef, p3Impl);
			}
			catch (Exception e){
				System.out.println("ERROR : " + e) ;
				e.printStackTrace(System.out);
			}
		}
		else if(selection.equals("banker")) {
			try{
				// create and initialize the ORB
				ORB orb = ORB.init(argServer, null);

				POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
				rootpoa.the_POAManager().activate();
			
				// get the root naming context
				org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			
				// Use NamingContextExt instead of NamingContext. This is
				// part of the Interoperable naming Service.
				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			
				// resolve the Object Reference in Naming
				String name = "P3";
				p3Impl = P3Helper.narrow(ncRef.resolve_str(name));

				P3CallBackImpl p3CallBackImpl = new P3CallBackImpl();
				p3CallBackImpl.setORB(orb);

				org.omg.CORBA.Object scRef = rootpoa.servant_to_reference(p3CallBackImpl);
				P3CallBack ncscRef = P3CallBackHelper.narrow(scRef);

				System.out.println("Obtained a handle on bank server object: " + p3Impl);
				phase3_bank(ncscRef, p3Impl);
			}
			catch (Exception e){
				System.out.println("ERROR : " + e) ;
				e.printStackTrace(System.out);
			}
		}
	}
	
	public static String get_double_SHA256() {
		try { 
			String input = Integer.toString(new Random().nextInt(10000000));
			
			// First SHA-256
			// Static getInstance method is called with hashing SHA 
			MessageDigest md = MessageDigest.getInstance("SHA-256"); 
			
			// digest() method called 
			// to calculate message digest of an input 
			// and return array of byte 
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8)); 
			
			// Convert byte array into signum representation 
			BigInteger no = new BigInteger(1, messageDigest); 
			
			// Convert message digest into hex value 
			String hashtext = no.toString(16); 
			
			while (hashtext.length() < 32) { 
				hashtext = "0" + hashtext; 
			} 
			
			// Second SHA-256
			// Static getInstance method is called with hashing SHA 
			MessageDigest md_2 = MessageDigest.getInstance("SHA-256"); 
						
			// digest() method called 
			// to calculate message digest of an input 
			// and return array of byte 
			byte[] messageDigest_2 = md_2.digest(hashtext.getBytes(StandardCharsets.UTF_8)); 
						
			// Convert byte array into signum representation 
			BigInteger no_2 = new BigInteger(1, messageDigest_2); 
						
			// Convert message digest into hex value 
			String hash_hashtext = no_2.toString(16); 
						
			while (hash_hashtext.length() < 32) { 
				hash_hashtext = "0" + hash_hashtext; 
			} 

			return input + "	" + hash_hashtext; 
		} 
		// For specifying wrong message digest algorithms 
		catch (NoSuchAlgorithmException e) { 
			System.out.println("Exception thrown" + " for incorrect algorithm: " + e); 
			return "fail"; 
		} 
	} 
	
	
	public static void phase3_play(P3CallBack clnref, P3 p3Impl)throws IOException{
		// Status for user
		/*
		99. Login Selection
		1. Register 
		2. Login
		3. Wait for Gambling
		*/
		clntFlag = 99;
		int myInt = 0;
		String myStr;
		String seed_num = "";
		String rand_num;
		// long start_time;
		// long stop_time;
		
		while(clntFlag != 0){
			switch(clntFlag) {
			case 99:
				System.out.println("Welcome! 1. Create New Account 2. Login Existed Account 3. Exit \n");
				
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
				break;
			
			case 1:
				String seed_key = p3Impl.reg_account();
				//System.out.println(seed_key);
				if (seed_key.equals("Failed")) {
					System.out.println("Account Created Failed! Please try again \n");
				}
				else {
					//System.out.println(seed_key);
					String[] seed_key_sep = seed_key.split("\\s+");
					System.out.println("Your seed is: " + seed_key_sep[0]);
					System.out.println("Your account is: " + seed_key_sep[1]);
				}
				clntFlag = 99;
				break;
				
			case 2:
				System.out.println("Please enter your secret seed: ");
				try{
					Scanner scan= new Scanner(System.in);
					seed_num = scan.nextLine();
				}
				catch(Exception e){
					e.printStackTrace(System.out);
					break;
				}

				//start_time = System.nanoTime();
				String account_info = p3Impl.login_account(seed_num, clnref);
				if(account_info.equals("false")){
					System.out.println("Login Failed! Please try another seed");
					clntFlag = 99;
				}
				else{
					String[] account_info_sep = account_info.split("\\s+");
					System.out.println("Balances for account: " + account_info_sep[0]);
					System.out.println("Account Info: Type:" + account_info_sep[1] +
							", Code:" + account_info_sep[2] + 
							", Balance:" + account_info_sep[3]);
					clntFlag = 3;
				}
				//stop_time = System.nanoTime();
				//System.out.println(stop_time-start_time);
				
				break;
				
			case 3:
				String if_active_gambleString = p3Impl.check_active_gamble();
				if (if_active_gambleString.equals("No")) {
					System.out.println("Wait for next gambling!");
				}
				else {
					SimpleDateFormat formatter =new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			    	Date due = null;
					try {
						due = formatter.parse(if_active_gambleString);
					} catch (ParseException e) {
						System.out.println("date incorrect");
					}
			    	System.out.println("A gambling is on! The deadline is: " + due);
			    	System.out.println("Press Y to join Or just ignore that msg");
				}
				Scanner scan= new Scanner(System.in);
				myStr= scan.nextLine();
				if (myStr.equals("Y")) {
					clntFlag = 4;
				}
				break;
				
			case 4:
				System.out.println("How much you want to bet: ");
				try{
					scan= new Scanner(System.in);
					myInt = scan.nextInt();
				}
				catch(Exception e){
					e.printStackTrace(System.out);
					break;
				}
				String hash_value = get_double_SHA256();
				if (hash_value.equals("fail")) {
					System.out.println("Sorry! Your gambling number is generated with some fault...");
					break;
				}
				String[] hash_value_sep = hash_value.split("\\s+");
				rand_num = hash_value_sep[0];
				
				//start_time = System.nanoTime();
				String bet_reString = p3Impl.put_bet(seed_num, myInt, hash_value_sep[1]);
				//stop_time = System.nanoTime();
				//System.out.println(stop_time-start_time);
				
				if (bet_reString.split("\\s+")[0].equals("success")) {
					System.out.println("Gambling successfully!");
					System.out.println("Please remember your gambling number: " + rand_num);
					System.out.println("Please remember your player ID: " + bet_reString.split("\\s+")[1]);
					System.out.println("Please wait for the msg from Server!");
					clntFlag = 5;
				}
				else {
					System.out.println(bet_reString);
					clntFlag = 3;
				}
				break;
				
			case 5:
				scan= new Scanner(System.in);
				myStr = scan.nextLine();
				
				if (!p3Impl.verify_hash(seed_num, myStr)) {
					System.out.println("Please enter your real gambling number: ");
					break;
				}
				
				System.out.println("Wait for all participants confirming");
				
				rightType = true;
				while (rightType){
					try{
						scan = new Scanner(System.in);
						myInt = scan.nextInt();
						if ((myInt == 1) || (myInt == 2)){
							rightType = false;
						}
						else{
							System.out.println("Please enter 1, or 2\n");
						}
					}
					catch(Exception e){
						e.printStackTrace(System.out);
					}
				}
				
				if (myInt == 2) {
					clntFlag = 3;
					break;
				}
				else {
					String player_query_res = p3Impl.player_query_transac();
					System.out.println(player_query_res);
					clntFlag = 3;
					break;
				}
			}		
		}		
	}
	
	public static void phase3_bank(P3CallBack clnref, P3 p3Impl)throws IOException{
		int log_flag = 99;
		String myStr;
		String bank_seed = "";
		boolean rightType;
		int myInt = 0;
		
		while (log_flag != 0) {
			switch(log_flag) {
			case 99:
				System.out.println("Enter the secret password/seed of Banker: ");
				
				try{
					Scanner scan = new Scanner(System.in);
					bank_seed = scan.nextLine();
					if (p3Impl.login_bank(bank_seed)) {
						log_flag = 1;
					}
					else {
						System.out.println("Authen Failed! Incorrect Password");
					}
				}
				catch(Exception e){
					e.printStackTrace(System.out);
				}
				break;
			
			case 1:
				System.out.println("Type Y to start the gambling");
				try{
					Scanner scan = new Scanner(System.in);
					myStr = scan.nextLine();
					if (myStr.equals("Y")) {
						if (p3Impl.check_before_gamble()) {
							log_flag = 2;
						}
						else {
							System.out.println("There is already a gambling running");
						}
					}
					else {
						System.out.println("Start the gambling by typing Y");
					}
				}
				catch(Exception e){
					e.printStackTrace(System.out);
				}
				break;
				
			case 2:
				System.out.println("Specify the deadline (format: dd-MM-yyyy HH:mm:ss, e.g., 31-01-1998 23:37:50): ");
				try{
					Scanner scan = new Scanner(System.in);
					myStr = scan.nextLine();
					SimpleDateFormat formatter =new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					Date due = formatter.parse(myStr);
					if (p3Impl.start_gamble(myStr)) {
						System.out.println("Gambling start successfully!");
						p3Impl.start_timer(myStr);
					}
					else {
						System.out.println("Please offer a valid Date!");
						break;
					}
				}
				catch(Exception e){
					e.printStackTrace(System.out);
				}
				
				System.out.println("Type 1. Query the transcation within one day 2. Go for next round!");
				rightType = true;
				while (rightType){
					try{
						Scanner scan = new Scanner(System.in);
						myInt = scan.nextInt();
						if ((myInt == 1) || (myInt == 2)){
							rightType = false;
						}
						else{
							System.out.println("Please enter 1, or 2\n");
						}
					}
					catch(Exception e){
						e.printStackTrace(System.out);
					}
				}
				
				if (myInt == 2) {
					log_flag = 1;
				}
				else {
					String banker_query_reString = p3Impl.banker_query_transac(bank_seed);
					System.out.println(banker_query_reString);
					log_flag = 1;
				}
				break;
				
			}
		}
	}
}
