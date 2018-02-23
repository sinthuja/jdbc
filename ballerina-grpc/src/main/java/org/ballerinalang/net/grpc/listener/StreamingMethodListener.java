/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc.listener;

import com.google.protobuf.Descriptors;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.grpc.MessageConstants;

import java.util.Map;

/**
 * Abstract Streaming Method listener.
 * This provide method for all method listener child classes.
 */
public abstract class StreamingMethodListener extends MethodListener {

    public final Map<String, Resource> resourceMap;

    public StreamingMethodListener(Descriptors.MethodDescriptor methodDescriptor, Map<String, Resource> resourceMap) {
        super(methodDescriptor, resourceMap.get(MessageConstants.ON_MESSAGE_RESOURCE));
        this.resourceMap = resourceMap;
    }

}
