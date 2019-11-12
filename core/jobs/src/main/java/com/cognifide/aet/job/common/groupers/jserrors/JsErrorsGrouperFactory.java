package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.job.api.grouper.GrouperFactory;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.job.common.comparators.jserrors.JsErrorsComparator;
import org.osgi.service.component.annotations.Component;

@Component
public class JsErrorsGrouperFactory implements GrouperFactory {

  @Override
  public String getName() {
    return JsErrorsComparator.COMPARATOR_NAME;
  }

  @Override
  public GrouperJob createInstance() {
    return new JsErrorsGrouper();
  }
}
