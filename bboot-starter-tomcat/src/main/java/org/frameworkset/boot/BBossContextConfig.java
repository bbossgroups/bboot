package org.frameworkset.boot;
/**
 * Copyright 2025 bboss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.ContextRuleSet;
import org.apache.catalina.startup.NamingRuleSet;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author biaoping.yin
 * @Date 2025/9/11
 */
public class BBossContextConfig extends ContextConfig {
    /**
     * Create (if necessary) and return a Digester configured to process the context configuration descriptor for an
     * application.
     *
     * @return the digester for context.xml files
     */
    @Override
    protected Digester createContextDigester() {
        Digester digester = super.createContextDigester();
        digester.setRulesValidation(false);
      
        return digester;
    }
}
