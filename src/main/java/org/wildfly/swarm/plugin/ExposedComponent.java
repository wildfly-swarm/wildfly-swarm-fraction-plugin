/*
 * Copyright 2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.plugin;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExposedComponent {

    public static List<ExposedComponent> parseDescriptor(final URL content) {
        try {
            final ObjectMapper mapper = new ObjectMapper();

            return mapper.readValue(content, mapper.getTypeFactory()
                    .constructCollectionType(List.class, ExposedComponent.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse descriptor", e);
        }
    }

    public String name = null;
    public String doc = null;
    public boolean bom = true;
}
