package Client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

  private static final int NUMTHREADS = 256;
  private static final int NUMSKIERS = 20000;
  private static final int NUMLIFTS = 40;
  private static final int NUMRUNS = 20;
  private static final int DIVIDEND = 10;
  private static final int FACTOR = 4;

  private static final String URL = "http://localhost:8080/Server_war_exploded/skiers/fsf";

  private static CountDownLatch startUpCountDown = new CountDownLatch(1);
  private static CountDownLatch peakPhaseCountDown = new CountDownLatch(NUMTHREADS / FACTOR / DIVIDEND);
  private static CountDownLatch coolDownCountDown = new CountDownLatch(NUMTHREADS / DIVIDEND);

  private static AtomicInteger successReq = new AtomicInteger(0);
  private static AtomicInteger unSuccessReq = new AtomicInteger(0);

  public static Thread[] startUp() {

    int startTime = 0, endTime = 90;
    int skierIdRange = NUMSKIERS / (NUMTHREADS / FACTOR);

    System.out.println(NUMRUNS / DIVIDEND * skierIdRange);

    Thread[] threads = new Thread[NUMTHREADS / FACTOR];

    for (int i = 0; i < NUMTHREADS / FACTOR; i++) {
      threads[i] = new Thread(new SingleClient(
          successReq, unSuccessReq,
          i * skierIdRange + 1, (i + 1) * skierIdRange,
          NUMLIFTS,
          startTime, endTime,
          NUMRUNS / DIVIDEND * skierIdRange,
          startUpCountDown, peakPhaseCountDown,
          URL));
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

    System.out.println((int) (factor * NUMRUNS) * skierIdRange);

    for (int i = 0; i < NUMTHREADS; i++) {
      threads[i] = new Thread(new SingleClient(
          successReq, unSuccessReq,
          i * skierIdRange + 1, (i + 1) * skierIdRange,
          NUMLIFTS,
          startTime, endTime,
          (int) (factor * NUMRUNS) * skierIdRange,
          peakPhaseCountDown, coolDownCountDown,
          URL));
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


    System.out.println(NUMRUNS / DIVIDEND);
    for (int i = 0; i < NUMTHREADS / FACTOR; i++) {
      threads[i] = new Thread(new SingleClient(
          successReq, unSuccessReq,
          i * skierIdRange + 1, (i + 1) * skierIdRange,
          NUMLIFTS,
          startTime, endTime,
          NUMRUNS / DIVIDEND,
          coolDownCountDown, null,
          URL));
    }

    for (Thread thread: threads) {
      thread.start();
    }
    return threads;
  }


  public static void main(String[] args) {

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

    System.out.println("finished!");
    System.out.println("successful: " + successReq);
    System.out.println("unsuccessful: " + unSuccessReq);

  }
}
