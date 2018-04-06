import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class GerarBcrypt {

	@Test
	public void test() {
		String senha = BCrypt.hashpw("123456", BCrypt.gensalt());
		System.out.println(senha);
	}

}
