import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * https://www.youneedabudget.com/support/article/csv-file-importing
 * Date,Payee,Category,Memo,Outflow,Inflow
 * 01/25/12,Sample Payee,,Sample Memo for an outflow,100.00,
 * 01/26/12,Sample Payee 2,,Sample memo for an inflow,,500.00
 *
 * Any field can be left blank except the date. Valid date formats:
 * DD/MM/YY
 * DD/MM/YYYY
 * DD/MM//YYYY
 * MM/DD/YY
 * MM/DD/YYYY
 * MM/DD//YYYY
 */
public class RabobankYnabConverter {
    public static void main(String[] args) throws Exception {
        convert("c:/transactions.txt", "c:/rabobank-ynab.csv", false);
    }

    public static void convert(String inputFileLocation, String outputFileLocation, boolean errorIfUnknownLine) throws Exception {
        SimpleDateFormat raboDateFormatter = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat ynabDateFormatter = new SimpleDateFormat("dd/MM/yyyy");

        int transactionCounter = 0;
        try (   BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFileLocation));
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileLocation))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split("\",\"");
                if (split.length == 19) {
                    String raboDateStr = split[2];
                    String raboFlow = split[3];
                    String raboAmount = split[4];
                    String raboPayee = split[6];
                    String raboMemo = split[11];
                    String raboCategory = "";

                    Date raboDate = raboDateFormatter.parse(raboDateStr);
                    String ynabDateStr = ynabDateFormatter.format(raboDate);

                    boolean isOutgoingPayment;
                    if (raboFlow.equals("D")) {
                        isOutgoingPayment = true;
                    } else if (raboFlow.equals("C")) {
                        isOutgoingPayment = false;
                    } else {
                        throw new RuntimeException("Onbekende Rabo flow waarde:" + raboFlow);
                    }

                    String ynabMemo = raboMemo.replace(',', '_');
                    String ynabOutflow = isOutgoingPayment ? raboAmount : "";
                    String ynabInflow = isOutgoingPayment ? "" : raboAmount;

                    String outputLine = String.format("%s,%s,%s,%s,%s,%s\n", ynabDateStr, raboPayee, raboCategory, ynabMemo, ynabOutflow, ynabInflow);
                    bufferedWriter.write(outputLine);
                    transactionCounter++;
                } else {
                    throw new RuntimeException("Unable to parse strange line: " + line);
                }
            }
        }
        System.out.printf("%d transaction converted\n", transactionCounter);
    }
}
