// Ref: https://www.stellar.org/developers/guides/get-started/create-account.html#fnref1

import java.util.Scanner;
import org.stellar.sdk.Server;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.KeyPair;


public class query_account {

	public static void main(String[] args) {
		
		String seed;
		String key;
		Scanner scan;
		
		try{
			System.out.println("Please enter your seed: ");
			scan= new Scanner(System.in);
			seed = scan.nextLine();
			//System.out.println("\n");
			//System.out.println("Please enter your account: ");
			//scan = new Scanner(System.in);
			//key = scan.nextLine();
			
			Server server = new Server("https://horizon-testnet.stellar.org");
			KeyPair pair = KeyPair.fromSecretSeed(seed);
			
			AccountResponse account = server.accounts().account(pair);
			System.out.println("Balances for account " + pair.getAccountId());
			for (AccountResponse.Balance balance : account.getBalances()) {
			  System.out.println(String.format(
			    "Type: %s, Code: %s, Balance: %s",
			    balance.getAssetType(),
			    balance.getAssetCode(),
			    balance.getBalance()));
			}
		}
		catch(Exception e){
			e.printStackTrace(System.out);
			System.exit(0);
		}
		

	}

}
