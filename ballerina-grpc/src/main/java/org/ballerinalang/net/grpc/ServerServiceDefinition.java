/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.grpc;

import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.listener.ServerCallHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Definition of a service to be exposed via a Server.
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 *
 */
public final class ServerServiceDefinition {

    /**
     * Create service definition builder with service name.
     *
     * @param serviceName Service name.
     * @return a new builder instance.
     * @throws GrpcServerException if failed.
     */
    public static Builder builder(String serviceName) throws GrpcServerException {
        return new Builder(serviceName);
    }

    private final ServiceDescriptor serviceDescriptor;
    private final Map<String, ServerMethodDefinition<?, ?>> methods;

    private ServerServiceDefinition(
            ServiceDescriptor serviceDescriptor, Map<String, ServerMethodDefinition<?, ?>> methods) throws
            GrpcServerException {
        if (serviceDescriptor == null) {
            throw new GrpcServerException("Service Descriptor cannot be null");
        }
        this.serviceDescriptor = serviceDescriptor;
        this.methods = Collections.unmodifiableMap(new HashMap<>(methods));
    }

    /**
     * Returns the descriptor of the service.
     *
     * @return service descriptor instance.
     */
    public ServiceDescriptor getServiceDescriptor() {
        return serviceDescriptor;
    }

    /**
     * Gets all the methods of service.
     *
     * @return Collection of method definitions.
     */
    public Collection<ServerMethodDefinition<?, ?>> getMethods() {
        return methods.values();
    }

    /**
     * Look up a method by its fully qualified name.
     *
     * @param methodName the fully qualified name without leading slash. E.g., "com.foo.Foo/Bar"
     */
    public ServerMethodDefinition<?, ?> getMethod(String methodName) {
        return methods.get(methodName);
    }

    /**
     * Builder for constructing Service instances.
     */
    public static final class Builder {

        private final String serviceName;
        private final ServiceDescriptor serviceDescriptor;
        private final Map<String, ServerMethodDefinition<?, ?>> methods =
                new HashMap<>();

        private Builder(String serviceName) throws GrpcServerException {
            if (serviceName == null) {
                throw new GrpcServerException("Service Name cannot be null");
            }
            this.serviceName = serviceName;
            this.serviceDescriptor = null;
        }

        private Builder(ServiceDescriptor serviceDescriptor) throws GrpcServerException {
            if (serviceDescriptor == null) {
                throw new GrpcServerException("Service Descriptor cannot be null");
            }
            this.serviceDescriptor = serviceDescriptor;
            this.serviceName = serviceDescriptor.getName();
        }

        /**
         * Add a method to be supported by the service.
         *
         * @param method  the {@link MethodDescriptor} of this method.
         * @param handler handler for incoming calls
         */
        public <ReqT, RespT> void addMethod(
                MethodDescriptor<ReqT, RespT> method, ServerCallHandler<ReqT, RespT> handler) throws
                GrpcServerException {
            if (method == null) {
                throw new GrpcServerException("Service Descriptor cannot be null");
            }
            if (handler == null) {
                throw new GrpcServerException("Service Descriptor cannot be null");
            }
            addMethod(ServerMethodDefinition.create(method, handler));
        }

        /**
         * Add a method to be supported by the service.
         *
         * @param def Service method definition.
         * @param <ReqT> Request Message type.
         * @param <RespT> Reponse Message type.
         * @throws GrpcServerException if failed.
         */
        <ReqT, RespT> void addMethod(ServerMethodDefinition<ReqT, RespT> def) throws GrpcServerException {

            MethodDescriptor<ReqT, RespT> method = def.getMethodDescriptor();
            if (!serviceName.equals(MethodDescriptor.extractFullServiceName(method.getFullMethodName()))) {
                throw new GrpcServerException(String.format("Method name should be prefixed with service name and " +
                        "separated with '/'. Expected service name: '%s'. Actual fully qualifed method name: '%s'.",
                        serviceName, method.getFullMethodName()));
            }
            String name = method.getFullMethodName();
            if (methods.containsKey(name)) {
                throw new GrpcServerException(String.format("Method by same name already registered: %s", name));
            }

            methods.put(name, def);
        }

        /**
         * Construct new ServerServiceDefinition.
         *
         * @return a new instance.
         * @throws GrpcServerException if failed
         */
        public ServerServiceDefinition build() throws GrpcServerException {

            ServiceDescriptor serviceDescriptor = this.serviceDescriptor;
            if (serviceDescriptor == null) {
                List<MethodDescriptor<?, ?>> methodDescriptors
                        = new ArrayList<>(methods.size());
                for (ServerMethodDefinition<?, ?> serverMethod : methods.values()) {
                    methodDescriptors.add(serverMethod.getMethodDescriptor());
                }
                serviceDescriptor = ServiceDescriptor.newBuilder(serviceName).addAllMethods(methodDescriptors).build();
            }
            Map<String, ServerMethodDefinition<?, ?>> tmpMethods = new HashMap<>(methods);
            for (MethodDescriptor<?, ?> descriptorMethod : serviceDescriptor.getMethods()) {
                ServerMethodDefinition<?, ?> removed = tmpMethods.remove(
                        descriptorMethod.getFullMethodName());
                if (removed == null) {
                    throw new IllegalStateException(
                            "No method bound for descriptor entry " + descriptorMethod.getFullMethodName());
                }
                if (removed.getMethodDescriptor() != descriptorMethod) {
                    throw new IllegalStateException(
                            "Bound method for " + descriptorMethod.getFullMethodName()
                                    + " not same instance as method in service descriptor");
                }
            }
            if (tmpMethods.size() > 0) {
                throw new IllegalStateException(
                        "No entry in descriptor matching bound method "
                                + tmpMethods.values().iterator().next().getMethodDescriptor().getFullMethodName());
            }
            return new ServerServiceDefinition(serviceDescriptor, methods);
        }
    }
}
