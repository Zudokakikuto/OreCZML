/** .*/

package org.example.CZMLObjects;

import cesiumlanguagewriter.*;

public class Show {

    /** .*/
    private boolean toShow;
    /** .*/
    private TimeInterval availability;

    public Show(final boolean toShow, final TimeInterval availability) {
        this.toShow = toShow;
        this.availability = availability;
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public boolean getShow() {
        return toShow;
    }

    public void setAvailability(final TimeInterval availability) {
        this.availability = availability;
    }

    public void setShow(final boolean toShow_temp) {
        this.toShow = toShow_temp;
    }

    public void write(final PolylineCesiumWriter polylineWriter, final CesiumOutputStream output) {

        try (BooleanCesiumWriter showWriter =  polylineWriter.getShowWriter()) {
            showWriter.open(output);
            showWriter.writeInterval(availability);
            showWriter.writeBoolean(toShow);
        }
    }
}
