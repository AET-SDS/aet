package com.cognifide.aet.job.api.grouper;

// todo javadoc
public class SimilarityValue<T> {

  private final T error1;
  private final T error2;
  private final int value;

  public SimilarityValue(T error1, T error2, int value) {
    this.error1 = error1;
    this.error2 = error2;
    this.value = value;
  }
}
