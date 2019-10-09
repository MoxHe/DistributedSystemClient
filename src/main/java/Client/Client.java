package Client;

import Data.Record;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

  private static final int NUMTHREADS = 256;
  private static final int NUMSKIERS = 20000;
  private static final int NUMLIFTS = 40;
  private static final int NUMRUNS = 20;
  private static final int DIVIDEND = 10;
  private static final int FACTOR = 4;
  private static final int RECORDS_SISE = 400000;

  private static final String URL = "http://54.175.23.161:8080/Server_war";

  private static CountDownLatch startUpCountDown = new CountDownLatch(1);
  private static CountDownLatch peakPhaseCountDown = new CountDownLatch(NUMTHREADS / FACTOR / DIVIDEND);
  private static CountDownLatch coolDownCountDown = new CountDownLatch(NUMTHREADS / DIVIDEND);

  private static AtomicInteger successReq = new AtomicInteger(0);
  private static AtomicInteger unSuccessReq = new AtomicInteger(0);
  private static BlockingQueue<Record> records = new ArrayBlockingQueue<>(RECORDS_SISE);

  public static Thread[] startUp() {

    int startTime = 0, endTime = 90;
    int skierIdRange = NUMSKIERS / (NUMTHREADS / FACTOR);

    Thread[] threads = new Thread[NUMTHREADS / FACTOR];

    for (int i = 0; i < NUMTHREADS / FACTOR; i++) {
      threads[i] = new Thread(new SingleClient(
          successReq, unSuccessReq,
          i * skierIdRange + 1, (i + 1) * skierIdRange,
          NUMLIFTS,
          startTime, endTime,
          NUMRUNS / DIVIDEND * skierIdRange,
          startUpCountDown, peakPhaseCountDown,
          URL, records));
    }

    for (Thread thread: threads) {
      thread.start();
    }
    return threads;
  }

  public static Thread[] peakPhase() {
    int startTime = 91, endTime = 360;
    int skierIdRange = NUMSKIERS / NUMTHREADS;
    double factor = 0.8;

    Thread[] threads = new Thread[NUMTHREADS];

    for (int i = 0; i < NUMTHREADS; i++) {
      threads[i] = new Thread(new SingleClient(
          successReq, unSuccessReq,
          i * skierIdRange + 1, (i + 1) * skierIdRange,
          NUMLIFTS,
          startTime, endTime,
          (int) (factor * NUMRUNS) * skierIdRange,
          peakPhaseCountDown, coolDownCountDown,
          URL, records));
    }

    for (Thread thread: threads) {
      thread.start();
    }
    return threads;
  }

  public static Thread[] coolDown() {

    int startTime = 361, endTime = 420;
    int skierIdRange = NUMSKIERS / (NUMTHREADS / FACTOR);

    Thread[] threads = new Thread[NUMTHREADS / FACTOR];

    for (int i = 0; i < NUMTHREADS / FACTOR; i++) {
      threads[i] = new Thread(new SingleClient(
          successReq, unSuccessReq,
          i * skierIdRange + 1, (i + 1) * skierIdRange,
          NUMLIFTS,
          startTime, endTime,
          NUMRUNS / DIVIDEND,
          coolDownCountDown, null,
          URL, records));
    }

    for (Thread thread: threads) {
      thread.start();
    }
    return threads;
  }


  public static void main(String[] args) {

    long wallStart = System.currentTimeMillis();

    Thread[] startUpThreads = startUp();
    Thread[] peakPhaseThreads = peakPhase();
    Thread[] coolDownThreads = coolDown();

    startUpCountDown.countDown();

    for (Thread thread: startUpThreads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
    }

    for (Thread thread: peakPhaseThreads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
    }

    for (Thread thread: coolDownThreads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
    }

    long wallTime = System.currentTimeMillis() - wallStart;

    Record.writeCSV(records, wallTime, NUMTHREADS);

    Record.outputResults(NUMTHREADS, records, successReq.intValue(), unSuccessReq.intValue(), wallTime);
  }
}
