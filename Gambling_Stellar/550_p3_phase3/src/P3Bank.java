import P3App.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Memo;
import org.stellar.sdk.MemoText;
import org.stellar.sdk.Network;
import org.stellar.sdk.PaymentOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.Transaction;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.requests.TooManyRequestsException;
import org.stellar.sdk.requests.TransactionsRequestBuilder;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.responses.TransactionResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.io.*;
import java.math.BigInteger;
import java.util.*;

import java.util.*;

class P3Impl extends P3POA{
	private ORB orb;
	
	public void setORB(ORB orb_val) {
		orb = orb_val;
	}
	
	// <key = seed, value = clntref>
	private Map<String, P3CallBack> nameServer = new HashMap<>();
	private boolean gamble_flag = false;
	
	// For memo
	private int gamble_round = 0;
	
	// <key = seed, value = hashvalue>
	private Map<String, String> seed_hash  = new HashMap<>();
	// <key = seed, value = ID>
	private Map<String, Integer> seed_id  = new HashMap<>();
	// <key = seed, value = ran_num>
	private Map<String, String> seed_ran  = new HashMap<>();
	// <key = seed, value = account>
	private Map<String, String> seed_account  = new HashMap<>();
	
	private String due;
	private int player_count = 0;
	private int total_fee = 0;
	private Date gambling_start_timeDate;
	private String bank_seed_str = "";
	private String bank_account_str = "";
	
	public static String get_double_SHA256(String ran_num) {
		try { 
			// First SHA-256
			// Static getInstance method is called with hashing SHA 
			MessageDigest md = MessageDigest.getInstance("SHA-256"); 
			
			// digest() method called 
			// to calculate message digest of an input 
			// and return array of byte 
			byte[] messageDigest = md.digest(ran_num.getBytes(StandardCharsets.UTF_8)); 
			
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

			return hash_hashtext; 
		} 
		// For specifying wrong message digest algorithms 
		catch (NoSuchAlgorithmException e) { 
			System.out.println("Exception thrown" + " for incorrect algorithm: " + e); 
			return "fail"; 
		} 
	} 
	
	public String reg_account(){
		
		String res = "";
		KeyPair pair = KeyPair.random();
		res = new String(pair.getSecretSeed());
		res = res + "	" + pair.getAccountId();
		
		// create a test account with pair above
		String friendbotUrl = String.format(
				"https://friendbot.stellar.org/?addr=%s", 
				pair.getAccountId());
		InputStream response;
		try {
			response = new URL(friendbotUrl).openStream();
			String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
			//System.out.println("SUCCESS! You have a new account :)\n" + body);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			res = "Failed";
			return res;
		}
		
		return res;	
	}
	
	public String login_account(String seed, P3CallBack objref) {
		//long start_time = System.nanoTime();
		String res = "";
		try {
			Server server = new Server("https://horizon-testnet.stellar.org");
			KeyPair pair = KeyPair.fromSecretSeed(seed);
			
			AccountResponse account = server.accounts().account(pair);
			/*
			System.out.println("Balances for account " + pair.getAccountId());
			for (AccountResponse.Balance balance : account.getBalances()) {
			  System.out.println(String.format(
			    "Type: %s, Code: %s, Balance: %s",
			    balance.getAssetType(),
			    balance.getAssetCode(),
			    balance.getBalance()));
			}
			*/
			seed_account.put(seed, pair.getAccountId());
			res = pair.getAccountId() + "	";
			for (AccountResponse.Balance balance : account.getBalances()) {
				/*
				  System.out.println(String.format(
				    "Type: %s, Code: %s, Balance: %s",
				    balance.getAssetType(),
				    balance.getAssetCode(),
				    balance.getBalance()));
				    */
				res += balance.getAssetType() + "	" 
						+ balance.getAssetCode() + "	"
						+ balance.getBalance();
			}
			nameServer.put(seed, objref);
		}
		catch(Exception e){
			e.printStackTrace(System.out);
			res = "false";
		}
		//long stop_time = System.nanoTime();
		//System.out.println(stop_time - start_time);
		return res;
	}
	
	
	public boolean login_bank(String seed) {
		//if (seed.equals("SBDVJSRNHWFTPRCJJSVGPIDRQQKIA3BWQRY5I34IQGQVAZ37472L6SQK")) {
			try {
				Server server = new Server("https://horizon-testnet.stellar.org");
				KeyPair pair = KeyPair.fromSecretSeed(seed);
				
				AccountResponse account = server.accounts().account(pair);
				
				bank_seed_str = seed;
				
				bank_account_str = pair.getAccountId();
				
				return true;
			}
			catch(Exception e){
				return false;
			}
		//}
		//else {
		//	return false;
		//}
	}
	
	public boolean check_before_gamble() {
		if (gamble_flag) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public boolean start_gamble(String deadline) {
		due = deadline;
		Date time_now = new Date();
		SimpleDateFormat formatter =new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date due_date = null;
		try {
			due_date = formatter.parse(deadline);
		} catch (ParseException e) {
			return false;
		}
		if (time_now.compareTo(due_date) < 0) {
			gamble_flag = true;
			for (String usr : nameServer.keySet()){
				try{
					nameServer.get(usr).inform(deadline);
					//System.out.println(exUsr + " " + ack);
				}
				catch(Exception e){
					continue;
				}
			}
			
			seed_hash = new HashMap<>();
			seed_id = new HashMap<>();
			seed_ran = new HashMap<>();
			player_count = 0;
			gamble_round += 1;
			total_fee = 0;
			gambling_start_timeDate = new Date();
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public void start_timer(String deadline){
		Date time_now = new Date();
		SimpleDateFormat formatter =new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date due_date = null;
		try{
			due_date = formatter.parse(deadline);
		} catch (ParseException e) {
		}

		long sleep_msec = due_date.getTime()-time_now.getTime();
		
		try {
			Thread.sleep(sleep_msec);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		gamble_flag = false;
		
		for (String playerString : seed_hash.keySet()) {
			try{
				nameServer.get(playerString).inform_end();
				//System.out.println(exUsr + " " + ack);
			}
			catch(Exception e){
				continue;
			}
		}
		
		while (!seed_hash.keySet().equals(seed_ran.keySet())) {
			
		}
		
		String broad_cast = "Player_ID	Account_ID	Gambling_Num\n";
		for (String user : seed_hash.keySet()) {
			broad_cast += seed_id.get(user);
			broad_cast += "	";
			broad_cast += seed_account.get(user);
			broad_cast += "	";
			broad_cast += seed_ran.get(user);
			broad_cast += "\n";
		}
		
		for (String playerString : seed_hash.keySet()) {
			try{
				nameServer.get(playerString).inform_msg(broad_cast);
				//System.out.println(exUsr + " " + ack);
			}
			catch(Exception e){
				continue;
			}
		}
		
		int winner_id = 0;
		
		for (String playerString : seed_ran.keySet()) {
			winner_id += Integer.parseInt(seed_ran.get(playerString));
		}
		
		winner_id = winner_id%(seed_ran.keySet().size());
		
		String winner_id_str = "Player " + Integer.toString(winner_id) + " is the winner!";
		
		String winner_account = null;
		for (String playerString : seed_id.keySet()) {
			if (seed_id.get(playerString) == winner_id) {
				winner_account = seed_account.get(playerString);
			}
			try{
				nameServer.get(playerString).inform_msg(winner_id_str);
				//System.out.println(exUsr + " " + ack);
			}
			catch(Exception e){
				continue;
			}
		}
		
		
		// transfer money to winner
		Network.useTestNetwork();
		Server server = new Server("https://horizon-testnet.stellar.org");
		
		KeyPair source = KeyPair.fromSecretSeed(bank_seed_str);
		KeyPair destination = KeyPair.fromAccountId(winner_account);

		AccountResponse sourceAccount = null;
		
		// First, check to make sure that the destination account exists.
		// You could skip this, but if the account does not exist, you will be charged
		// the transaction fee when the transaction fails.
		// It will throw HttpResponseException if account does not exist or there was another error.
		try {
			server.accounts().account(destination);
			sourceAccount = server.accounts().account(source);
		}catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		float winner_fee = (float) (total_fee * 0.95);
		
		try {
			Transaction transaction = new Transaction.Builder(sourceAccount)
			        .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), Float.toString(winner_fee)).build())
			        // A memo allows you to add your own metadata to a transaction. It's
			        // optional and does not affect how Stellar treats the transaction.
			        .addMemo(Memo.text("Gamble	" + "win	" + Float.toString(winner_fee) + "	" + Integer.toString(gamble_round)))
			        .setTimeout(1000)
			        .setOperationFee(100)
			        .build();
			// Sign the transaction to prove you are actually the person sending it.
			transaction.sign(source);
			SubmitTransactionResponse response = server.submitTransaction(transaction);
			//System.out.println("Success!");
			//System.out.println(response.isSuccess());

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for (String playerString : seed_hash.keySet()) {
			try{
				nameServer.get(playerString).inform_msg("Type 1. Query the transcation from last round 2. Go for next round!");
				//System.out.println(exUsr + " " + ack);
			}
			catch(Exception e){
				continue;
			}
		}
		
	}
	
	public String check_active_gamble() {
		if (gamble_flag) {
			return due;
		}
		else {
			return "No";
		}
	}
	
	public String put_bet(String s_seed, int money, String hash_value) {
		//long start_time = System.nanoTime();
		if (gamble_flag) {
			Network.useTestNetwork();
			
			String d_account = bank_account_str;
			
			Server server = new Server("https://horizon-testnet.stellar.org");
			
			KeyPair source = KeyPair.fromSecretSeed(s_seed);
			KeyPair destination = KeyPair.fromAccountId(d_account);
	
			AccountResponse sourceAccount = null;
			
			// First, check to make sure that the destination account exists.
			// You could skip this, but if the account does not exist, you will be charged
			// the transaction fee when the transaction fails.
			// It will throw HttpResponseException if account does not exist or there was another error.
			try {
				server.accounts().account(destination);
				sourceAccount = server.accounts().account(source);
	
				Transaction transaction = new Transaction.Builder(sourceAccount)
				        .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), Integer.toString(money)).build())
				        // A memo allows you to add your own metadata to a transaction. It's
				        // optional and does not affect how Stellar treats the transaction.
				        .addMemo(Memo.text("Gamble	" + player_count + "	" + Integer.toString(money) + "	" + Integer.toString(gamble_round)))
				        .setTimeout(1000)
				        .setOperationFee(100)
				        .build();
				// Sign the transaction to prove you are actually the person sending it.
				transaction.sign(source);
				SubmitTransactionResponse response = server.submitTransaction(transaction);
				if (response.isSuccess()) {
					
					seed_hash.put(s_seed, hash_value);
					seed_id.put(s_seed, player_count);
					player_count += 1;
					total_fee += money;
					//long stop_time = System.nanoTime();
					
					//System.out.println(stop_time - start_time);
					
					return "success	" + Integer.toString(player_count - 1);
				}
				else {
					return "No enough money";
				}
				//System.out.println("Success!");
				//System.out.println(response.isSuccess());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
				return "Internet connection fail";
			}
		}
		else {
			return "Gamble already ends! Pass the deadline";
		}
	}
	
	public boolean verify_hash(String seed, String ran_num) {
		String hash_ran_numString = get_double_SHA256(ran_num);
		if (seed_hash.get(seed).equals(hash_ran_numString)) {
			seed_ran.put(seed, ran_num);
			return true;
		}
		else {
			return false;
		}
	}
	
	public String player_query_transac() {
		
		String res = "";
		
		Server server = new Server("https://horizon-testnet.stellar.org");
		KeyPair account = KeyPair.fromSecretSeed(bank_seed_str);

		// Create an API call to query payments involving the account.
		TransactionsRequestBuilder paymentsRequest = server.transactions().forAccount(account);
		
		paymentsRequest = paymentsRequest.limit(200);
		Page<TransactionResponse> page;
		try {
			page = paymentsRequest.execute();
			for (TransactionResponse payment : page.getRecords()) {
				try {
					String memo_infoString = ((MemoText)payment.getMemo()).getText();
					String[] memo_infoString_sep = memo_infoString.split("\\s+");
					String time_stamp = payment.getCreatedAt();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			    	//cal2.setTime(date2);
			    	Date due = null;
			    	try {
						due = sdf.parse(time_stamp);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println(memo_infoString_sep);
					if ( (memo_infoString_sep[0].equals("Gamble")) && (Integer.parseInt(memo_infoString_sep[3]) == gamble_round) && (gambling_start_timeDate.compareTo(due) < 0) ) {
						if (memo_infoString_sep[1].equals("win")) {
							res += "Send in total " + memo_infoString_sep[2] + " to winner\n";
						}
						else{
							for (String seed_tmp : seed_id.keySet()) {
								if (seed_id.get(seed_tmp) == Integer.parseInt(memo_infoString_sep[1])) {
									res += seed_account.get(seed_tmp) + " bets " + memo_infoString_sep[2] + "\n";
									break;
								}	
							}
						}
					}
				}
				catch (Exception e) {
					//e.printStackTrace();
				}
			}
		} catch (TooManyRequestsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return res;
	}
	
	public String banker_query_transac(String bank_seed) {
		
		String query_res = "";
		Server server = new Server("https://horizon-testnet.stellar.org");
		KeyPair account = KeyPair.fromSecretSeed(bank_seed);
		// Create an API call to query payments involving the account.
		PaymentsRequestBuilder paymentsRequest = server.payments().forAccount(account);

		// If some payments have already been handled, start the results from the
		// last seen payment. (See below in `handlePayment` where it gets saved.)
		//System.out.println(paymentsRequest);
		paymentsRequest = paymentsRequest.limit(200);
		Page<OperationResponse> page;
		try {
			page = paymentsRequest.execute();
			for (OperationResponse payment : page.getRecords()) {
				try {
					String time_stamp = payment.getCreatedAt();
					
			    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			    	Date due = null;
			    	try {
						due = sdf.parse(time_stamp);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					Date time_now = new Date();
					
					Calendar cal1 = Calendar.getInstance();
					Calendar cal2 = Calendar.getInstance();
					cal1.setTime(due);
					cal2.setTime(time_now);
					boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
					                  cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

			    	String amount = ((PaymentOperationResponse) payment).getAmount();
			    	Asset asset = ((PaymentOperationResponse) payment).getAsset();
			    	String assetName;
			    	if (asset.equals(new AssetTypeNative())) {
			    		assetName = "lumens";
			    	} else {
			    		StringBuilder assetNameBuilder = new StringBuilder();
			    		assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getCode());
			    		assetNameBuilder.append(":");
			    		assetNameBuilder.append(((AssetTypeCreditAlphaNum) asset).getIssuer().getAccountId());
			    		assetName = assetNameBuilder.toString();
			    	}
			    	if ( sameDay ) {
			    		StringBuilder output = new StringBuilder();
			    		output.append(amount);
			    		output.append(" ");
			    		output.append(assetName);
			    		output.append(" from ");
			    		output.append(((PaymentOperationResponse) payment).getFrom().getAccountId());
			    		output.append(" to ");
			    		output.append(((PaymentOperationResponse) payment).getTo().getAccountId());
			    		output.append(" at ");
			    		output.append(due);
			    		output.append("\n");
			    		query_res += output;
			    		//System.out.println(output.toString());
			    	}
				}
				catch (Exception e) {
				}
			}
		} catch (TooManyRequestsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return query_res;
	}
	
}

public class P3Bank {
	public static void main(String args[]){

		//System.out.println(selection);
		//String[] argServer = Arrays.copyOfRange(args, 1, args.length);

		try{
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);
				// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			P3Impl p3Impl = new P3Impl();
			p3Impl.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(p3Impl);
			P3 href = P3Helper.narrow(ref);
			
			// get the root naming context. NameService invokes the name service
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		
			// Use NamingContextExt which is part of the Interoperable
			// Naming Service (INS) specification.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			String name = "P3";
			NameComponent path[] = ncRef.to_name( name );
			ncRef.rebind(path, href);

			System.out.println("Banker ready and waiting ...");

			// wait for invocations from clients
			orb.run();
		}
		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}
}