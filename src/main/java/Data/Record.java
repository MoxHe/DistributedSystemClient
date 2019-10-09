package Data;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Record {

  private long startTime, latency;
  private String requestType = "POST";
  private int responseCode;

  public Record(long startTime, long latency, int responseCode) {
    this.startTime = startTime;
    this.latency = latency;
    this.responseCode = responseCode;
  }


  public static void writeCSV(BlockingQueue<Record> records, long wallTime, int threadNum) {
    final String COMMA = ",";
    try {
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream("./src/main/java/OutputResults/" + threadNum + "thread_records.csv"), StandardCharsets.UTF_8));

      bw.write("StartTime,RequestType,Latency,ResponseCode");
      bw.newLine();

      for (Record record : records) {
        StringBuffer oneLine = new StringBuffer();
        oneLine.append(record.startTime);
        oneLine.append(COMMA);
        oneLine.append(record.requestType);
        oneLine.append(COMMA);
        oneLine.append(record.latency);
        oneLine.append(COMMA);
        oneLine.append(record.responseCode);
        bw.write(oneLine.toString());
        bw.newLine();
      }
      bw.flush();
      bw.close();
    } catch (UnsupportedEncodingException e) {
      System.out.println(e.getMessage());
    } catch (IOException e){
      System.out.println(e.getMessage());
    }
  }

  public static void outputResults(int threadNum, BlockingQueue<Record> records, int successReq, int unSuccessReq, long wallTime) {
    List<Long> latencies = new ArrayList<>();

    int latencySum = 0;
    for (Record record: records) {
      latencies.add(record.latency);
      latencySum += record.latency;
    }

    Collections.sort(latencies);

    System.out.println("Thread Number: " + threadNum);
    System.out.println("---------------------------------------------------------");
    System.out.println("The number of successful request: " + successReq);
    System.out.println("The number of successful unsuccessful request: " + unSuccessReq);
    System.out.println("Wall time: " + wallTime + " milliseconds");

    System.out.println("The mean of all latencies: " + latencySum / latencies.size() + " milliseconds.");
    System.out.println("The median of all latencies: " + latencies.get(latencies.size() / 2 - 1) + " milliseconds.");
    System.out.println("The throughput: " + latencies.size() / wallTime + " milliseconds.");
    System.out.println("The p99 of latencies: " + latencies.get(latencies.size() * 99 / 100 - 1) + " milliseconds.");
    System.out.println("The max response time: " + latencies.get(latencies.size() - 1) + " milliseconds.");
  }
}
