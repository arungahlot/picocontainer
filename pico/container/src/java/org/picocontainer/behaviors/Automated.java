/*****************************************************************************
 * Copyright (C) 2003-2010 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer.behaviors;

import org.picocontainer.Behavior;
import org.picocontainer.ComponentAdapter;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Automated<T> extends AbstractBehavior<T> implements Behavior<T>, Serializable {


	public Automated(ComponentAdapter<T> delegate) {
        super(delegate);
    }

    public boolean hasLifecycle(Class<?> type) {
        return true;
    }

    public String getDescriptor() {
        return "Automated";
    }
}