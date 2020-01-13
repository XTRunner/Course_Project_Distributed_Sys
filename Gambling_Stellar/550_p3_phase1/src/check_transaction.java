import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

import org.stellar.sdk.Asset;
import org.stellar.sdk.AssetTypeCreditAlphaNum;
import org.stellar.sdk.AssetTypeNative;
import org.stellar.sdk.KeyPair;
import org.stellar.sdk.Memo;
import org.stellar.sdk.MemoText;
import org.stellar.sdk.Server;
import org.stellar.sdk.requests.EventListener;
import org.stellar.sdk.requests.PaymentsRequestBuilder;
import org.stellar.sdk.requests.TooManyRequestsException;
import org.stellar.sdk.requests.TransactionsRequestBuilder;
import org.stellar.sdk.responses.Page;
import org.stellar.sdk.responses.TransactionResponse;
import org.stellar.sdk.responses.operations.OperationResponse;
import org.stellar.sdk.responses.operations.PaymentOperationResponse;

import com.sun.javafx.scene.EnteredExitedHandler;
import com.sun.xml.internal.ws.Closeable;

import sun.print.PSPrinterJob.PluginPrinter;

public class check_transaction {
	public static void main(String[] args) {
		
		//Date date = new Date();  
		/*
	    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
	    String cur_date = formatter.format(date);
	    int cur_day = Integer.parseInt(cur_date.split("/")[1]);
	    int cur_mon = Integer.parseInt(cur_date.split("/")[0]);
	    int cur_year = Integer.parseInt(cur_date.split("/")[2]);
	    //System.out.println("Date Format with MM/dd/yyyy : "+strDate.split("/")[0] +strDate.split("/")[1] + strDate.split("/")[2]);  
		String account_id;
		Scanner scan;
		*/
		System.out.println("Please enter your seed: ");
		Scanner scan = new Scanner(System.in);
		String account_id = scan.nextLine();
		Server server = new Server("https://horizon-testnet.stellar.org");
		KeyPair account = KeyPair.fromSecretSeed(account_id);
		String query_res = "";
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
		System.out.println(query_res);
		/*
		//paymentsRequest = paymentsRequest.limit(1);
		paymentsRequest.stream(new EventListener<OperationResponse>() {
			@Override
			public void onEvent(OperationResponse payment) {
				// Record the paging token so we can start from here next time.
				//savePagingToken(payment.getPagingToken());
			    // The payments stream includes both sent and received payments. We only
			    // want to process received payments here.
				//payment.getPagingToken();
			    if (payment instanceof PaymentOperationResponse) {
			    	String time_stamp = payment.getCreatedAt();
			    	//System.out.println(time_stamp);
			    	//DateTimeFormatter dateParser = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"));
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
					//System.out.println(due);
					
					Date time_now = new Date();
					
					Calendar cal1 = Calendar.getInstance();
					Calendar cal2 = Calendar.getInstance();
					cal1.setTime(due);
					cal2.setTime(time_now);
					boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
					                  cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
					
					
			    	//int time_stamp_day = Integer.parseInt(time_stamp.split("T")[0].split("-")[2]);
			    	//int time_stamp_mon = Integer.parseInt(time_stamp.split("T")[0].split("-")[1]);
			    	//int time_stamp_year = Integer.parseInt(time_stamp.split("T")[0].split("-")[0]);
			    	//System.out.println(time_stamp_mon);
			    	//System.out.println(time_stamp_year);
			    	//System.out.println(time_stamp_day);

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
			    		System.out.println(output.toString());
			    	}
			    }
			}
		});	*/
	}
}

