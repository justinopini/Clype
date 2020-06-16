package main;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicInteger;

/** Utility for scheduling tasks that need to be done frequently. */
public class SchedulerUtils {
  // No instances.
  private SchedulerUtils() {}

  /**
   * Schedules tasks every second.
   *
   * <p>See
   * https://stackoverflow.com/questions/27853735/simple-example-for-scheduledservice-in-javafx
   */
  static class TimerService extends ScheduledService<Integer> {
    private final IntegerProperty count = new SimpleIntegerProperty();

    public final Integer getCount() {
      return count.get();
    }

    public final void setCount(Integer value) {
      count.set(value);
    }

    protected Task<Integer> createTask() {
      return new Task<>() {
        protected Integer call() {
          // Adds 1 to the count
          count.set(getCount() + 1);
          return getCount();
        }
      };
    }

    /** Creates a task that happens at the frequency of every epochs of the provided time.*/
    static TimerService getNewTimeService(int seconds){
        TimerService service = new TimerService();
        AtomicInteger count = new AtomicInteger(0);
        service.setCount(count.get());
        service.setPeriod(Duration.seconds(seconds));
        return service;
    }
  }
}
