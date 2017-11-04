package com.piotrmajcher.piwind.mobileappserver.services.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.Assert;

import com.piotrmajcher.piwind.mobileappserver.domain.MeteoStation;
import com.piotrmajcher.piwind.mobileappserver.dto.MeteoStationTO;
import com.piotrmajcher.piwind.mobileappserver.repository.MeteoStationRepository;
import com.piotrmajcher.piwind.mobileappserver.services.MeteoStationService;
import com.piotrmajcher.piwind.mobileappserver.services.exceptions.MeteoStationServiceException;
import com.piotrmajcher.piwind.mobileappserver.util.EntityAndTOConverter;
import com.piotrmajcher.piwind.mobileappserver.util.impl.MeteoStationEntityConverter;

@Service
public class MeteoStationServiceImpl implements MeteoStationService{
	
	private static final Logger logger = Logger.getLogger(MeteoStationServiceImpl.class);
	
	private static final String REGISTER_STATION_NULL_ARG_ERROR = "Failed to register new station - passed argument is null.";
	private static final String REGISTRATION_EXCEPTION_OCCURED = "Exception occurred while trying to register new meteo station: ";
	private static final String DUPLICATE_STATION_NAME_ERROR = "A meteo station with this name already exists. Please choose another name.";
	
	private final MeteoStationRepository meteoStationRepository;
	private final EntityAndTOConverter<MeteoStation, MeteoStationTO> converter;
	
	@Autowired
	public MeteoStationServiceImpl(MeteoStationRepository meteoStationRepository) {
		this.meteoStationRepository = meteoStationRepository;
		this.converter = new MeteoStationEntityConverter();
	}
	
	@Override
	public UUID registerStation(MeteoStationTO stationTO) throws MeteoStationServiceException {
		try {
			Assert.notNull(stationTO, REGISTER_STATION_NULL_ARG_ERROR);
			return meteoStationRepository.save(converter.transferObjectToEntity(stationTO)).getId();
		} catch (Exception e) {
			String errorMessage = getPersistanceExceptionErrorMessage(e);
            throw new MeteoStationServiceException(errorMessage);
		}
	}

	private String getPersistanceExceptionErrorMessage(Exception e) {
		String errorMessage = null;
		if (e instanceof DataIntegrityViolationException) {
			errorMessage = DUPLICATE_STATION_NAME_ERROR;
		}
		if (e instanceof TransactionSystemException) {
		    Throwable cause = e.getCause();
		    while ( (cause != null) && !(cause instanceof ConstraintViolationException) ) {
		        cause = cause.getCause();
		    }

		    if (cause != null) {
		        StringBuilder message = new StringBuilder();
		        Set<ConstraintViolation<?>> violations = ((ConstraintViolationException)cause).getConstraintViolations();
		        for (ConstraintViolation<?> violation : violations) {
		            message.append(violation.getMessage());
		        }
		        
		        errorMessage = message.toString();
		    }
		}

		if (errorMessage == null) {
		    errorMessage = e.getMessage();
		}

		logger.error(REGISTRATION_EXCEPTION_OCCURED + errorMessage + "\n");
		e.printStackTrace();
		return errorMessage;
	}

	@Override
	public List<MeteoStationTO> getAllStations() {
		return converter.entityToTransferObject(meteoStationRepository.findAll());
	}
}
