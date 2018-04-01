/*
 * Copyright © 2017 Copyright 2018 ETRI All Rights Reserved. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/*
 * Copyright © 2017 Copyright 2017 ETRI all rights reserved and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package kr.re.etri.hello.impl;

import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev170803.HelloService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.hello.rev170803.HelloWorld;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.NotificationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloProvider {

    private static final Logger LOG = LoggerFactory.getLogger(HelloProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private final NotificationPublishService notificationPublishService;

    private BindingAwareBroker.RpcRegistration<HelloService> serviceRegistration;
    private ListenerRegistration<NotificationListener> listenerRegistration;
    private ListenerRegistration<DataTreeChangeListener> dataTreeChangeListenerListenerRegistration;

    private final NotificationService notificationRegisterService;
    private HelloWorldImpl helloWorldImpl;


    public HelloProvider(final DataBroker dataBroker, final RpcProviderRegistry rpcProviderRegistry,
                         final NotificationPublishService notificationPublishService,
                         final NotificationService notificationRegisterService) {

        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.notificationPublishService = notificationPublishService;
        this.notificationRegisterService = notificationRegisterService;

    }

    public void setHelloWorldImpl(HelloWorldImpl helloWorldImpl) {

        this.helloWorldImpl = helloWorldImpl;
    }

    public HelloWorldImpl getHelloWorldImpl() {
        return this.helloWorldImpl;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("HelloProvider Session Initiated");
        HelloWorldImpl helloWorldImpl = new HelloWorldImpl(dataBroker, notificationPublishService);
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(HelloService.class, helloWorldImpl);

        listenerRegistration = notificationRegisterService.registerNotificationListener(helloWorldImpl);

        final InstanceIdentifier<HelloWorld> path = helloWorldImpl.HELLO_IID;

        final DataTreeIdentifier<HelloWorld> dataTreeIdentifier = new DataTreeIdentifier(LogicalDatastoreType.CONFIGURATION, path);
        dataTreeChangeListenerListenerRegistration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, helloWorldImpl);

    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        serviceRegistration.close();
        if (dataTreeChangeListenerListenerRegistration != null) {
            dataTreeChangeListenerListenerRegistration.close();
        }
        LOG.info("HelloProvider Closed");
    }
}