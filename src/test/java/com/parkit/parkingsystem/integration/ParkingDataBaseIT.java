package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    private static Ticket createTicketWithOneHourInTimeOffset(ParkingService parkingService) {
        Ticket ticket = new Ticket();
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        ticket.setInTime(inTime);
        ticket.setParkingSpot(parkingService.getNextParkingNumberIfAvailable());
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setPrice(0);
        return ticket;
    }

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Ticket registeredTicket = ticketDAO.getTicket("ABCDEF");
        assert(registeredTicket != null );
        assert(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR) > 1 ); // Next empty place should be 2
    }

    @Test
    public void testParkingLotExit(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);            
        Ticket ticket = createTicketWithOneHourInTimeOffset(parkingService);
        ticketDAO.saveTicket(ticket);
        parkingService.processExitingVehicle();
        Ticket registeredTicket = ticketDAO.getTicket("ABCDEF");
        assert(registeredTicket.getOutTime() != null);
        assert(registeredTicket.getPrice() != 0);
    }

    @Test
    public void testParkingLotExitRecurringUser(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticket = createTicketWithOneHourInTimeOffset(parkingService);
        ticketDAO.saveTicket(ticket);
        parkingService.processExitingVehicle();     
        ticketDAO.saveTicket(ticket);
        parkingService.processExitingVehicle();
        Ticket registeredTicket = ticketDAO.getTicket("ABCDEF");
        assert(Math.abs(registeredTicket.getPrice() - Fare.CAR_RATE_PER_HOUR * 0.95) < 0.001);
    }
}
