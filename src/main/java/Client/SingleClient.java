package Client;

import Data.Record;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleClient implements Runnable {

  private AtomicInteger successReq;
  private AtomicInteger unSuccessReq;
  private int skierIdStart;
  private int skierIdEnd;
  private int numLifts;
  private int startTime;
  private int endTime;
  private int runTimes;
  private CountDownLatch startCountDown;
  private CountDownLatch endCountDown;
  private String URL;
  private BlockingQueue<Record> records;


  public SingleClient(AtomicInteger successReq, AtomicInteger unSuccessReq, int skierIdStart, int skierIdEnd, int numLifts, int startTime, int endTime,
      int runTimes, CountDownLatch startCountDown, CountDownLatch endCountDown, String URL, BlockingQueue<Record> records) {
    this.successReq = successReq;
    this.unSuccessReq = unSuccessReq;
    this.skierIdStart = skierIdStart;
    this.skierIdEnd = skierIdEnd;
    this.numLifts = numLifts;
    this.startTime = startTime;
    this.endTime = endTime;
    this.runTimes = runTimes;
    this.startCountDown = startCountDown;
    this.endCountDown = endCountDown;
    this.URL = URL;
    this.records = records;
  }

  @Override
  public void run() {

    try {
      startCountDown.await();
      SkiersApi apiInstance = new SkiersApi();
      ApiClient client = apiInstance.getApiClient();
      client.setBasePath(URL);

      try {

        for (int i = 0; i < runTimes; i++) {
          LiftRide body = new LiftRide();
          body.setTime(ThreadLocalRandom.current().nextInt(endTime - startTime + 1) + startTime);
          body.setLiftID(ThreadLocalRandom.current().nextInt(numLifts - 1) + 1);

          long startTime = System.currentTimeMillis();
          apiInstance.writeNewLiftRide(body, 3, "2019", "22", ThreadLocalRandom.current().nextInt(skierIdEnd - skierIdStart + 1) + skierIdStart);
          records.add(new Record(startTime, System.currentTimeMillis() - startTime, 201));

          successReq.getAndIncrement();
        }
      } catch (ApiException e) {
        records.add(new Record(startTime, System.currentTimeMillis() - startTime, e.getCode()));
        System.out.println(e.getCode());
        unSuccessReq.getAndIncrement();
      }

    } catch (InterruptedException e) {
      System.out.println(e.getMessage());
    } finally {
      if (endCountDown != null) {
        endCountDown.countDown();
      }
    }
  }

}
