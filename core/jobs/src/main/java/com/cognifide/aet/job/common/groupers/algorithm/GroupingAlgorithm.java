package com.cognifide.aet.job.common.groupers.algorithm;

import java.util.Collection;
import java.util.Set;

public interface GroupingAlgorithm<T> {

  Set<Set<T>> group(Collection<T> elementsToGroup) throws GroupingException;
}
