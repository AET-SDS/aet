package com.cognifide.aet.runner.processing.steps;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cognifide.aet.runner.processing.CountGroupingResults;
import javax.jms.JMSException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GroupingResultsRouterTest extends StepManagerTest {

  @Override
  protected StepManager createTested() throws JMSException {
    int messagesToReceive = CountGroupingResults.INSTANCE.apply(suite);
    return new GroupingResultsRouter(
        timeoutWatch, connection, runnerConfiguration, runIndexWrapper, messagesToReceive);
  }

  @Override
  public void getQueueInName() throws Exception {
    assertThat(tested.getQueueInName(), is("AET.grouperResults"));
  }

  @Override
  public void getQueueOutName() throws Exception {
    assertThat(tested.getQueueOutName(), is(nullValue()));
  }

  @Override
  public void getStepName() throws Exception {
    assertThat(tested.getStepName(), is("GROUPED"));
  }

  @Override
  @Test
  public void closeConnections() throws Exception {
    tested.closeConnections();
    verify(session, times(1)).close();
    verify(consumer, times(1)).close();
    verify(sender, times(0)).close();
  }
}
