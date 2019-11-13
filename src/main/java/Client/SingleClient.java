package Client;

import Data.Record;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class SingleClient extends Thread {

  private int successReq;
  private int unSuccessReq;
  private int skierIdStart;
  private int skierIdEnd;
  private int numLifts;
  private int startTime;
  private int endTime;
  private int runTimes;
  private boolean isPhase3;
  private CountDownLatch startCountDown;
  private CountDownLatch endCountDown;
  private String URL;
  private List<Record> records;


  public SingleClient(int skierIdStart, int skierIdEnd, int numLifts, int startTime, int endTime,
      int runTimes, boolean isPhase3, CountDownLatch startCountDown, CountDownLatch endCountDown, String URL) {
    this.successReq = 0;
    this.unSuccessReq = 0;
    this.skierIdStart = skierIdStart;
    this.skierIdEnd = skierIdEnd;
    this.numLifts = numLifts;
    this.startTime = startTime;
    this.endTime = endTime;
    this.runTimes = runTimes;
    this.isPhase3 = isPhase3;
    this.startCountDown = startCountDown;
    this.endCountDown = endCountDown;
    this.URL = URL;
    this.records = new ArrayList<>();
  }

  @Override
  public void run() {
    try {
      if (startCountDown != null) {
        startCountDown.await();
      }
      SkiersApi apiInstance = new SkiersApi();
      ApiClient client = apiInstance.getApiClient();
      client.setBasePath(URL);


      try {

        for (int i = 0; i < runTimes; i++) {
          LiftRide body = new LiftRide();
          body.setTime(ThreadLocalRandom.current().nextInt(endTime - startTime + 1) + startTime);
          body.setLiftID(ThreadLocalRandom.current().nextInt(numLifts - 1) + 1);

          int skierID = ThreadLocalRandom.current().nextInt(skierIdEnd - skierIdStart + 1) + skierIdStart;
          long start = System.currentTimeMillis();
          apiInstance.writeNewLiftRide(body, 3, "2019", "22", skierID);
          if (isPhase3) {
            apiInstance.getSkierDayVertical(3, "2019", "22", skierID);
//            System.out.println("Total vertical: " + vertical);
          }
          records.add(new Record(startTime, System.currentTimeMillis() - start, 201));

          successReq++;
        }
      } catch (ApiException e) {
//        records.add(new Record(startTime, System.currentTimeMillis() - start, e.getCode()));
//        System.out.println(e.getCode());
        unSuccessReq++;
      }

    } catch (InterruptedException e) {
      System.out.println(e.getMessage());
    } finally {
      if (endCountDown != null) {
        endCountDown.countDown();
      }
    }
  }

  public int getSuccessReq() {
    return successReq;
  }

  public int getUnSuccessReq() {
    return unSuccessReq;
  }

  public List<Record> getRecords() {
    return records;
  }
}
