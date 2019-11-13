package Client;

import Data.Record;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Client {

  private static int NUMTHREADS;
  private static final int NUMSKIERS = 20000;
  private static final int NUMLIFTS = 40;
  private static final int NUMRUNS = 20;
  private static final int DIVIDEND = 10;
  private static final int FACTOR = 4;

  // single server    http://3.88.62.185:8080/Server_war
  // load balancer    http://servers-1344196229.us-east-1.elb.amazonaws.com:80/Server_war
  // localhost        http://localhost:8080/Server_war_exploded
  private static String URL;

  private static CountDownLatch startUpCountDown;
  private static CountDownLatch peakPhaseCountDown;
  private static CountDownLatch coolDownCountDown;
  private static SingleClient[] threads;
  private static int idx = 0;

  private static void startUp() {

    int startTime = 0, endTime = 90;
    int skierIdRange = NUMSKIERS / (NUMTHREADS / FACTOR);

    for (int i = 0; i < NUMTHREADS / FACTOR; i++) {
      threads[idx] = new SingleClient(
          i * skierIdRange + 1, (i + 1) * skierIdRange,
          NUMLIFTS,
          startTime, endTime,
          NUMRUNS / DIVIDEND * skierIdRange,
          false,
          startUpCountDown, peakPhaseCountDown,
          URL);

      threads[idx++].start();
    }
  }

  private static void peakPhase() {
    int startTime = 91, endTime = 360;
    int skierIdRange = NUMSKIERS / NUMTHREADS;
    double factor = 0.8;

    for (int i = 0; i < NUMTHREADS; i++) {
      threads[idx] = new SingleClient(
          i * skierIdRange + 1, (i + 1) * skierIdRange,
          NUMLIFTS,
          startTime, endTime,
          (int) (factor * NUMRUNS) * skierIdRange,
          false,
          peakPhaseCountDown, coolDownCountDown,
          URL);
      threads[idx++].start();
    }
  }

  private static void coolDown() {

    int startTime = 361, endTime = 420;
    int skierIdRange = NUMSKIERS / (NUMTHREADS / FACTOR);

    for (int i = 0; i < NUMTHREADS / FACTOR; i++) {
      threads[idx] = new SingleClient(
          i * skierIdRange + 1, (i + 1) * skierIdRange,
          NUMLIFTS,
          startTime, endTime,
          NUMRUNS / DIVIDEND,
          true,
          coolDownCountDown, null,
          URL);

      threads[idx++].start();
    }
  }

  public static void main(String[] args) {
    NUMTHREADS = Integer.parseInt(args[0]);
    URL = args[1];
    startUpCountDown = new CountDownLatch(1);
    peakPhaseCountDown = new CountDownLatch(NUMTHREADS / FACTOR / DIVIDEND);
    coolDownCountDown = new CountDownLatch(NUMTHREADS / DIVIDEND);
    threads = new SingleClient[NUMTHREADS + 2 * NUMTHREADS / FACTOR];


    long wallStart = System.currentTimeMillis();

    startUp();
    peakPhase();
    coolDown();

    startUpCountDown.countDown();

    for (Thread thread: threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
    }

    long wallTime = System.currentTimeMillis() - wallStart;

    List<Record> records = new ArrayList<>();
    int successReq = 0, unSuccessReq = 0;

    for (SingleClient thread: threads) {
      successReq += thread.getSuccessReq();
      unSuccessReq += thread.getUnSuccessReq();
      records.addAll(thread.getRecords());
    }
//    Record.writeCSV(records, wallTime, NUMTHREADS);
    Record.outputResults(NUMTHREADS, records, successReq, unSuccessReq, wallTime);
  }
}
