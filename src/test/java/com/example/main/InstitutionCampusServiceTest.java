package com.example.main;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.main.exceptions.CampusWithNoZeroOccupationException;
import com.example.main.exceptions.CampusWithoutNameException;
import com.example.main.exceptions.InstitutionWithoutNameException;
import com.example.main.exceptions.URLWithoutProtocolException;
import com.example.main.model.Institution;
import com.example.main.model.Institutioncampus;
import com.example.main.repositories.InstitutionCampusRepository;
import com.example.main.services.interfaces.InstitutionCampusService;
import com.example.main.services.interfaces.InstitutionService;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class InstitutionCampusServiceTest {
	
	@Mock
	private InstitutionCampusRepository campusRepository;
	
	@Autowired
	private InstitutionCampusService campusService;
	
	@Autowired
	private InstitutionService instService;
	
	@BeforeAll
	public void setupInstitutions() throws URLWithoutProtocolException, InstitutionWithoutNameException {
		Institution newInst1 = new Institution();
		newInst1.setInstId(1);
		newInst1.setInstName("Icesi");
		newInst1.setInstAcademicserverurl("https://icesi.com");
		instService.saveInstitution(newInst1);
		
		Institution newInst2 = new Institution();
		newInst2.setInstId(2);
		newInst2.setInstName("Javeriana");
		newInst2.setInstAcademicserverurl("https://javeriana.com");
		instService.saveInstitution(newInst2);
		
		Institution newInst3 = new Institution();
		newInst3.setInstId(3);
		newInst3.setInstName("Univalle");
		newInst3.setInstAcademicserverurl("https://univalle.com");
		instService.saveInstitution(newInst3);
	}
	
	@Nested
	@TestInstance(Lifecycle.PER_CLASS)
	class Creation {		
		
		/**
		 * Prueba 1: Guardar un campus con un nombre no vacío, una ocupación en cero y que esté asociado a una 
		 * institución que existe.
		 */
		@Test
		public void saveCampusCorrectly() {
			Institutioncampus newCampus = new Institutioncampus();
			newCampus.setInstcamId(1);
			newCampus.setInstcamName("Icesi pance");
			newCampus.setInstcamOccupation(new BigDecimal(0));
			newCampus.setInstitution(instService.findById(1).get());
			
			Assertions.assertDoesNotThrow(() -> campusService.saveInstitutionCampus(newCampus));
			Assertions.assertEquals(1, campusService.getNumberOfCampus());
		}
		
		/**
		 * Prueba 2: Guardar un campus con un nombre vacío, una ocupación en cero y que esté asociado a una 
		 * institución que existe.
		 */
		@Test
		public void saveCampusWithoutAName() {
			Institutioncampus newCampus = new Institutioncampus();
			newCampus.setInstcamId(2);
			newCampus.setInstcamName("");
			newCampus.setInstcamOccupation(new BigDecimal(0));
			newCampus.setInstitution(instService.findById(3).get());
			
			Assertions.assertThrows(CampusWithoutNameException.class, () -> campusService.saveInstitutionCampus(newCampus));
		}
		
		/**
		 * Prueba 3: Guardar un campus con un nombre no vacío, pero con una ocupación mayor a cero y que 
		 * esté asociado a una institución que existe.
		 */
		@Test
		public void saveCampusWithOccupationGreaterThanZero() {
			Institutioncampus newCampus = new Institutioncampus();
			newCampus.setInstcamId(2);
			newCampus.setInstcamName("Univalle norte");
			newCampus.setInstcamOccupation(new BigDecimal(2));
			newCampus.setInstitution(instService.findById(2).get());
			
			Assertions.assertThrows(CampusWithNoZeroOccupationException.class, () -> campusService.saveInstitutionCampus(newCampus));
		}
		
		/**
		 * Prueba 4: Guardar un campus con un nombre no vacío, una ocupación en cero pero que esté 
		 * asociado a una institución que no existe.
		 */
		@Test
		public void saveCampusWithANonExistingInstitution() {
			Institution nonExistingInst = new Institution();
			nonExistingInst.setInstId(5);
			nonExistingInst.setInstName("Javeriana");
			nonExistingInst.setInstAcademicserverurl("https://javeriana.com");
			
			Institutioncampus newCampus = new Institutioncampus();
			newCampus.setInstcamId(1);
			newCampus.setInstcamName("Javeriana Cali");
			newCampus.setInstcamOccupation(new BigDecimal(0));
			newCampus.setInstitution(nonExistingInst);
			
			Assertions.assertThrows(NoSuchElementException.class, () -> campusService.saveInstitutionCampus(newCampus));
		}
	}
	
	@Nested
	@TestInstance(Lifecycle.PER_CLASS)
	class Edition {
		
		@BeforeEach
		public void setupCampus() throws NoSuchElementException, CampusWithoutNameException, CampusWithNoZeroOccupationException {
			Institutioncampus newCampus = new Institutioncampus();
			newCampus.setInstcamId(1);
			newCampus.setInstcamName("Icesi pance");
			newCampus.setInstcamOccupation(new BigDecimal(0));
			newCampus.setInstitution(instService.findById(1).get());
			
			campusService.saveInstitutionCampus(newCampus);
		}
		
		/**
		 * Prueba 5: Editar la información de un campus llenando los campos necesarios correctamente.
		 */
		@Test
		public void editCampusCorrectly() {
			String newName = "Univalle Palmira";
			Institutioncampus existingCampus = campusService.findById(1).get();
			existingCampus.setInstcamName(newName);
			existingCampus.setInstcamOccupation(new BigDecimal(0));
			existingCampus.setInstitution(instService.findById(3).get());
			
			Assertions.assertDoesNotThrow(() -> campusService.editInstitutionCampus(existingCampus));
			Assertions.assertEquals(newName, campusService.findById(1).get().getInstcamName());
			Assertions.assertEquals("Univalle", campusService.findById(1).get().getInstitution().getInstName());
			
		}
		
		/**
		 * Prueba 6: Editar la información de un campus pero poniendo en la ocupación un número negativo.
		 */
		@Test
		public void editCampusWithANegativeOccupation() {
			String newName = "Javeriana Bogotá";
			Institutioncampus existingCampus = campusService.findById(1).get();
			existingCampus.setInstcamName(newName);
			existingCampus.setInstcamOccupation(new BigDecimal(-2));
			existingCampus.setInstitution(instService.findById(2).get());
			
			Assertions.assertThrows(CampusWithNoZeroOccupationException.class, () -> campusService.editInstitutionCampus(existingCampus));
		}
		
		/**
		 * Prueba 7: Editar la información de un campus pero asignándole una institución que no existe.
		 */
		@Test
		public void editCampusWithANonExistingInstitute() {
			Institution nonExistingInst = new Institution();
			nonExistingInst.setInstId(6);
			nonExistingInst.setInstName("Icesi");
			nonExistingInst.setInstAcademicserverurl("https://icesi.com");
			
			String newName = "Icesi Bogotá";
			Institutioncampus existingCampus = campusService.findById(1).get();
			existingCampus.setInstcamName(newName);
			existingCampus.setInstcamOccupation(new BigDecimal(0));
			existingCampus.setInstitution(nonExistingInst);
			
			Assertions.assertThrows(NoSuchElementException.class, () -> campusService.editInstitutionCampus(existingCampus));
		}
		
		/**
		 * Prueba 8: Editar la información de un campus pero dejando el nombre vacío y los demás campos correctos.
		 */
		@Test
		public void editCampusWithoutName() {
			Institutioncampus existingCampus = campusService.findById(1).get();
			existingCampus.setInstcamName("");
			existingCampus.setInstcamOccupation(new BigDecimal(0));
			existingCampus.setInstitution(instService.findById(3).get());
			
			Assertions.assertThrows(CampusWithoutNameException.class, () -> campusService.editInstitutionCampus(existingCampus));
		}
	}
}
