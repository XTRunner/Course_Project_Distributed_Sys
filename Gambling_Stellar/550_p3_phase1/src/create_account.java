// Ref: https://www.stellar.org/developers/guides/get-started/create-account.html#fnref1

import org.stellar.sdk.KeyPair;
import java.net.*;
import java.io.*;
import java.util.*;

public class create_account {

	public static void main(String[] args) {
		// create a completely new and unique pair of keys.
		KeyPair pair = KeyPair.random();
		System.out.println("Seed: " + new String(pair.getSecretSeed()));
		// SAV76USXIJOBMEQXPANUOQM6F5LIOTLPDIDVRJBFFE2MDJXG24TAPUU7
		System.out.println("Key: " + pair.getAccountId());
		// GCFXHS4GXL6BVUCXBWXGTITROWLVYXQKQLF4YH5O5JT3YZXCYPAFBJZB
		
		// create a test account with pair above
		String friendbotUrl = String.format(
				"https://friendbot.stellar.org/?addr=%s", 
				pair.getAccountId());
		InputStream response;
		try {
			response = new URL(friendbotUrl).openStream();
			String body = new Scanner(response, "UTF-8").useDelimiter("\\A").next();
			System.out.println("SUCCESS! You have a new account :)\n" + body);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		// check account's details and balance
		Server server = new Server("https://horizon-testnet.stellar.org");
		AccountResponse account;
		try {
			account = server.accounts().account(pair);
			System.out.println("Balances for account " + pair.getAccountId());
			for (AccountResponse.Balance balance : account.getBalances()) {
			  System.out.println(String.format(
			    "Type: %s, Code: %s, Balance: %s",
			    balance.getAssetType(),
			    balance.getAssetCode(),
			    balance.getBalance()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		

	}

}
