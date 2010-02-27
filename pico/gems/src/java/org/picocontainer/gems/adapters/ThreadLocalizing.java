/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by Joerg Schaible                                           *
 *****************************************************************************/

package org.picocontainer.gems.adapters;

import com.thoughtworks.proxy.ProxyFactory;
import com.thoughtworks.proxy.factory.StandardProxyFactory;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoCompositionException;
import org.picocontainer.ComponentMonitor;
import org.picocontainer.behaviors.Cached;
import org.picocontainer.behaviors.AbstractBehaviorFactory;
import org.picocontainer.ComponentFactory;
import org.picocontainer.LifecycleStrategy;
import org.picocontainer.references.ThreadLocalReference;

import java.util.Properties;


/**
 * A {@link ComponentFactory} for components kept in {@link ThreadLocal} instances.
 * <p>
 * This factory has two operating modes. By default it ensures, that every thread uses its own component at any time.
 * This mode ({@link #ENSURE_THREAD_LOCALITY}) makes internal usage of a {@link ThreadLocalized}. If the
 * application architecture ensures, that the thread that creates the component is always also the thread that is th
 * only user, you can set the mode {@link #THREAD_ENSURES_LOCALITY}. In this mode the factory uses a simple
 * {@link Cached} that uses a {@link ThreadLocalReference} to cache the component.
 * </p>
 * <p>
 * See the use cases for the subtile difference:
 * </p>
 * <p>
 * <code>THREAD_ENSURES_LOCALITY</code> is applicable, if the pico container is requested for a thread local addComponent
 * from the working thread e.g. in a web application for a request. In this environment it is ensured, that the request
 * is processed from the same thread and the thread local component is reused, if a previous request was handled in the
 * same thread. Note that thi scenario fails badly, if the thread local component is created because of another cached
 * component indirectly by a dependecy. In this case the cached component already have an instance of the thread local
 * component, that may have been created in another thread, since only the component adapter for the thread local
 * component can ensure a unique component for each thread.
 * </p>
 * <p>
 * <code>ENSURES_THREAD_LOCALITY</code> solves this problem. In this case the returned component is just a proxy for
 * the thread local component and this proxy ensures, that a new component is created for each thread. Even if another
 * cached component has an indirect dependency on the thread local component, the proxy ensures unique instances. This
 * is vital for a multithreaded application that uses EJBs.
 * </p>
 * @author J&ouml;rg Schaible
 */
@SuppressWarnings("serial")
public final class ThreadLocalizing extends AbstractBehaviorFactory {


	/**
     * <code>ENSURE_THREAD_LOCALITY</code> is the constant for created {@link ComponentAdapter} instances, that ensure
     * unique instances of the component by delivering a proxy for the component.
     */
    public static final boolean ENSURE_THREAD_LOCALITY = true;
    /**
     * <code>THREAD_ENSURES_LOCALITY</code> is the constant for created {@link ComponentAdapter} instances, that
     * create for the current thread a new component.
     */
    public static final boolean THREAD_ENSURES_LOCALITY = false;

    private final boolean ensureThreadLocal;
    private final ProxyFactory proxyFactory;

    /**
     * Constructs a wrapping ThreadLocalizing, that ensures the usage of the ThreadLocal. The Proxy
     * instances are generated by the JDK.
     */
    public ThreadLocalizing() {
        this(new StandardProxyFactory());
    }

    /**
     * Constructs a wrapping ThreadLocalizing, that ensures the usage of the ThreadLocal.
     * @param proxyFactory The {@link ProxyFactory} to use.
     */
    public ThreadLocalizing(final ProxyFactory proxyFactory) {
        this(ENSURE_THREAD_LOCALITY, proxyFactory);
    }

    /**
     * Constructs a wrapping ThreadLocalizing.
     * @param ensure {@link #ENSURE_THREAD_LOCALITY} or {@link #THREAD_ENSURES_LOCALITY}.
     */
    public ThreadLocalizing(final boolean ensure) {
        this(ensure, new StandardProxyFactory());
    }

    /**
     * Constructs a wrapping ThreadLocalizing.
     * @param ensure {@link #ENSURE_THREAD_LOCALITY} or {@link #THREAD_ENSURES_LOCALITY}.
     * @param factory The {@link ProxyFactory} to use.
     */
    protected ThreadLocalizing(
            final boolean ensure, final ProxyFactory factory) {
        ensureThreadLocal = ensure;
        proxyFactory = factory;
    }

    @Override
	public ComponentAdapter createComponentAdapter(
            final ComponentMonitor componentMonitor, final LifecycleStrategy lifecycleStrategy, final Properties componentProperties, final Object componentKey, final Class componentImplementation, final Parameter... parameters)
            throws PicoCompositionException
    {
        final ComponentAdapter componentAdapter;
        if (ensureThreadLocal) {
            componentAdapter = new ThreadLocalized(super.createComponentAdapter(
                    componentMonitor, lifecycleStrategy, componentProperties, componentKey, componentImplementation, parameters), proxyFactory);
        } else {
            componentAdapter = new Cached(super.createComponentAdapter(
                    componentMonitor, lifecycleStrategy, componentProperties, componentKey, componentImplementation, parameters), new ThreadLocalReference());
        }
        return componentAdapter;
    }


    @Override
	public ComponentAdapter addComponentAdapter(final ComponentMonitor componentMonitor,
                                                final LifecycleStrategy lifecycleStrategy,
                                                final Properties componentProperties,
                                                final ComponentAdapter adapter) {
        if (ensureThreadLocal) {
            return componentMonitor.newBehavior(new ThreadLocalized(super.addComponentAdapter(componentMonitor,
                                                                     lifecycleStrategy,
                                                                     componentProperties,
                                                                     adapter), proxyFactory));
        } else {
            return componentMonitor.newBehavior(new Cached(super.addComponentAdapter(componentMonitor,
                                                                 lifecycleStrategy,
                                                                 componentProperties,
                                                                 adapter), new ThreadLocalReference()));
        }

    }
}
