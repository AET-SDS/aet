package com.cognifide.aet.job.common.groupers.jserrors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.cognifide.aet.job.api.collector.JsErrorLog;
import org.junit.Test;

public class JsErrorsDistanceFunctionTest {

  private final JsErrorsDistanceFunction function = new JsErrorsDistanceFunction();
  private JsErrorLog error1;
  private JsErrorLog error2;

  @Test
  public void apply_whenErrorMessagesAreTheSame_expect0() {
    String message = "Test error message";
    error1 = new JsErrorLog(message, null, 0);
    error2 = new JsErrorLog(message, null, 1);

    Double result = function.apply(error1, error2);
    assertThat(result, is((double) 0));
  }

  @Test(expected = NullPointerException.class)
  public void apply_whenOneOfJsErrorsIsNull_expectException() {
    error1 = null;
    error2 = new JsErrorLog("", null, 0);

    function.apply(error1, error2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void apply_whenOneOfMessagesIsNull_expectException() {
    error1 = new JsErrorLog(null, null, 0);
    error2 = new JsErrorLog("test", null, 1);

    function.apply(error1, error2);
  }

  @Test
  public void apply_whenBothMessagesAreEmpty_expectNaN() {
    error1 = new JsErrorLog("", null, 0);
    error2 = new JsErrorLog("", null, 0);

    Double result = function.apply(error1, error2);
    assertThat(Double.isNaN(result), is(true));
  }

  @Test
  public void apply_whenMessagesAreCompletelyDifferent_expect1() {
    error1 = new JsErrorLog("qwerty", null, 0);
    error2 = new JsErrorLog("asdfgh", null, 1);

    Double result = function.apply(error1, error2);
    assertThat(result, is((double) 1));
  }

  @Test
  public void apply_whenMessagesAreSlightlyDifferent_expectAlmost0() {
    error1 = new JsErrorLog("Test error message", null, 0);
    error2 = new JsErrorLog("Test errors messages", null, 1);

    Double result = function.apply(error1, error2);
    assertThat(result > 0 && result <= 0.1, is(true));
  }

  @Test
  public void apply_whenMessagesAreDifferent_expectValue() {
    error1 = new JsErrorLog("First test error", null, 1);
    error2 = new JsErrorLog("Some other test error", null, 2);

    Double result = function.apply(error1, error2);
    assertThat(result, is(1 - 12/21.0));
  }

  @Test
  public void apply_whenSecondMessageIsHalfTheSame_expectZeroPointHalf() {
    error1 = new JsErrorLog("abcd", null, 1);
    error2 = new JsErrorLog("abcdabcd", null, 2);

    Double result = function.apply(error1, error2);
    assertThat(result, is(0.5));
  }
}
