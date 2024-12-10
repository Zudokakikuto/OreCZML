/* Copyright 2002-2024 CS GROUP
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.orekit.czml.errors;

import org.hipparchus.exception.Localizable;
import org.hipparchus.exception.MathRuntimeException;
import org.orekit.errors.OrekitException;

import java.util.Locale;

/**
 * OreCzmlException
 * <p>
 * This class aims at creating exceptions and display messages from OreCzml Messages.
 *
 * @author Julien LEBLOND
 * @since 1.0
 */
public class OreCzmlException extends OrekitException {

    /**
     * Instantiates a new Ore czml exception.
     *
     * @param specifier the specifier
     * @param parts     the parts
     */
    public OreCzmlException(final Localizable specifier, final Object... parts) {
        super(specifier, parts);
    }

    /**
     * Instantiates a new Ore czml exception.
     *
     * @param exception the exception
     */
    public OreCzmlException(final OrekitException exception) {
        super(exception);
    }

    /**
     * Instantiates a new Ore czml exception.
     *
     * @param exception the exception
     */
    public OreCzmlException(final MathRuntimeException exception) {
        super(exception);
    }

    /**
     * Instantiates a new Ore czml exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public OreCzmlException(final Localizable message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Ore czml exception.
     *
     * @param cause     the cause
     * @param specifier the specifier
     * @param parts     the parts
     */
    public OreCzmlException(final Throwable cause, final Localizable specifier, final Object... parts) {
        super(cause, specifier, parts);
    }

    @Override
    public Object[] getParts() {
        return super.getParts();
    }

    @Override
    public Localizable getSpecifier() {
        return super.getSpecifier();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage();
    }

    @Override
    public String getMessage(final Locale locale) {
        return super.getMessage(locale);
    }

    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return super.getStackTrace();
    }

}
