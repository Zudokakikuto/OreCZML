package org.orekit.czml.errors;

import org.hipparchus.exception.Localizable;
import org.hipparchus.exception.MathRuntimeException;
import org.orekit.errors.OrekitException;

import java.text.MessageFormat;
import java.util.Locale;

public class OreCzmlException extends OrekitException {

    public OreCzmlException(final Localizable specifier, final Object... parts) {
        super(specifier, parts);
    }

    public OreCzmlException(OrekitException exception) {
        super(exception);
    }

    public OreCzmlException(MathRuntimeException exception) {
        super(exception);
    }

    public OreCzmlException(Localizable message, Throwable cause) {
        super(message, cause);
    }

    public OreCzmlException(Throwable cause, Localizable specifier, Object... parts) {
        super(cause, specifier, parts);
    }

    private String buildMessage(Locale locale) {
        if (super.getSpecifier() == null) {
            return "";
        } else {
            try {
                String localizedString = super.getSpecifier().getLocalizedString(locale);
                return localizedString == null ? "" : (new MessageFormat(localizedString, locale)).format(super.getParts());
            } catch (Throwable var3) {
                this.addSuppressed(var3);
                return super.getSpecifier().getSourceString();
            }
        }
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
