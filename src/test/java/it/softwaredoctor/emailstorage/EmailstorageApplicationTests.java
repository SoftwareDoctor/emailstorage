package it.softwaredoctor.emailstorage;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class EmailstorageApplicationTests {

    @Autowired
    private ScheduledTaskHolder scheduledTaskHolder;

    @Test
    void testCronTaskScheduled() {
        Set<ScheduledTask> scheduledTasks = scheduledTaskHolder.getScheduledTasks();
        scheduledTasks.forEach(scheduledTask -> scheduledTask.getTask().getRunnable());
        long count = scheduledTasks.stream()
                .filter(scheduledTask -> scheduledTask.getTask() instanceof CronTask)
                .count();
        assertThat(count).isEqualTo(1L);
    }
}
