import java.util.Scanner;
import java.io.*;
import org.stellar.sdk.Server;
import org.stellar.sdk.responses.AccountResponse;
import org.stellar.sdk.responses.SubmitTransactionResponse;
import org.stellar.sdk.*;
import org.stellar.sdk.KeyPair;

public class transfer_money {

	public static void main(String[] args) {
		Network.useTestNetwork();
		
		String s_seed;
		String d_account;
		Scanner scan;
		
		System.out.println("Please enter your seed: ");
		scan = new Scanner(System.in);
		s_seed = scan.nextLine();
		
		System.out.println("Please enter the targeted account ID: ");
		scan= new Scanner(System.in);
		d_account = scan.nextLine();
		
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
		}catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Transaction transaction = new Transaction.Builder(sourceAccount)
			        .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), "10").build())
			        // A memo allows you to add your own metadata to a transaction. It's
			        // optional and does not affect how Stellar treats the transaction.
			        .addMemo(Memo.text("Test Transaction"))
			        .setTimeout(1000)
			        .setOperationFee(100)
			        .build();
			// Sign the transaction to prove you are actually the person sending it.
			transaction.sign(source);
			SubmitTransactionResponse response = server.submitTransaction(transaction);
			//System.out.println("Success!");
			System.out.println(response.isSuccess());

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
