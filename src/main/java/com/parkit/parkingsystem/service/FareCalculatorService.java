package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

        public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        // Convert duration from milliseconds to hours
        double duration = (outHour - inHour) / 1000. / 60. / 60.;

        if (duration >= 0.5) {
            double discountRatio = 1;
            if (discount) {
                discountRatio = 0.95;
            }
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * discountRatio);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * discountRatio);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        } else {
            ticket.setPrice(0);
        }
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
}