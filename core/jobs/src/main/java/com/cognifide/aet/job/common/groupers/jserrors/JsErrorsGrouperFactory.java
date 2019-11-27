package com.cognifide.aet.job.common.groupers.jserrors;

import com.cognifide.aet.job.api.grouper.GrouperFactory;
import com.cognifide.aet.job.api.grouper.GrouperJob;
import com.cognifide.aet.job.common.comparators.jserrors.JsErrorsComparator;
import com.cognifide.aet.vs.ArtifactsDAO;
import com.cognifide.aet.vs.DBKey;
import java.util.concurrent.atomic.AtomicLong;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class JsErrorsGrouperFactory implements GrouperFactory {

  @Reference private ArtifactsDAO artifactsDAO;

  @Override
  public String getName() {
    return JsErrorsComparator.COMPARATOR_NAME;
  }

  @Override
  public GrouperJob createInstance(DBKey dbKey, long expectedInputCount) {
    AtomicLong expectedMessagesCount = new AtomicLong(expectedInputCount);
    return new JsErrorsGrouper(artifactsDAO, dbKey, expectedMessagesCount);
  }
}
