/**
 * AET
 *
 * Copyright (C) 2013 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.cognifide.aet.runner.processing.data.wrappers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.cognifide.aet.communication.api.metadata.Suite;
import com.cognifide.aet.communication.api.metadata.Url;
import com.cognifide.aet.communication.api.wrappers.MetadataRunDecorator;
import com.cognifide.aet.communication.api.wrappers.Run;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UrlRunIndexWrapperTest {

  private UrlRunIndexWrapper urlRunIndexWrapper;

  @Mock
  private Run objectToRunWrapper;

  @Mock
  private Suite suite;

  private Optional<com.cognifide.aet.communication.api.metadata.Test> test;

  private Optional<Url> url;

  @Before
  public void setUp() throws Exception {
    test = Optional
        .of(new com.cognifide.aet.communication.api.metadata.Test("testName", "proxy", "chrome"));
    url = Optional.of(new Url("urlName", "urlUrl", "urlDomain"));
    when(objectToRunWrapper.getRealSuite()).thenReturn(suite);
    when(suite.getTest(any(String.class))).thenReturn(test);
    when(objectToRunWrapper.getObjectToRun()).thenReturn(url.get());
    urlRunIndexWrapper = new UrlRunIndexWrapper(objectToRunWrapper);
  }

  @Test
  public void getUrls_expectOne() {
    ArrayList<MetadataRunDecorator<Url>> urls = urlRunIndexWrapper
        .getUrls();
    assertThat(urls.size(), is(1));
  }

  @Test
  public void countUrls_expectOne() {
    assertThat(urlRunIndexWrapper.countUrls(), is(1));
  }
}