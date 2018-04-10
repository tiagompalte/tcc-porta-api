import static org.junit.Assert.*;

import org.junit.Test;

import br.com.utfpr.porta.util.Conversao;

public class CodigoRfidTest {

	@Test
	public void codigoHexadecimalInvertido() throws Exception {
		String rfid = "C9 42 6A 7B";
		String numero = Conversao.convertHexToDecimal(rfid, true);
		assertTrue("2070561481" == numero);
	}

}
