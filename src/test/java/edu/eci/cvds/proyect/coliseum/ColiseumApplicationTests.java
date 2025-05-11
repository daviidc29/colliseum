package edu.eci.cvds.proyect.coliseum;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ColiseumApplicationTest {

	@Test
	void testMain() {
		// Ensure that main method runs without throwing any exceptions
		assertDoesNotThrow(() -> ColiseumApplication.main(new String[]{}));
	}
}