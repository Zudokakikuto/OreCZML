package org.example.CZMLObjects;

import cesiumlanguagewriter.*;

public class Show {

    private boolean toShow;
    private TimeInterval availability;

    public Show(boolean toShow, TimeInterval availability){
        this.toShow = toShow;
        this.availability = availability;
    }

    public TimeInterval getAvailability() {
        return availability;
    }

    public boolean getShow() {
        return toShow;
    }

    public void setAvailability(TimeInterval availability) {
        this.availability = availability;
    }

    public void setShow(boolean toShow) {
        this.toShow = toShow;
    }

    public void write(PolylineCesiumWriter polylineWriter, CesiumOutputStream output){
        try(BooleanCesiumWriter showWriter =  polylineWriter.getShowWriter()){
            showWriter.open(output);
            showWriter.writeInterval(availability);
            showWriter.writeBoolean(toShow);
        }
    }
}
