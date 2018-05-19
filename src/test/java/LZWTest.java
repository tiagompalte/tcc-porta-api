import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import br.com.utfpr.porta.util.LZW;

public class LZWTest {

	@Test
	public void test() {
						
		String expected = "10A2E5E9896967F587B939698999C9E010A2E5E9896967F587B939698999C9E010A2E5E9896967F587B939698999C9E010A2E5E9896967F587B939698999C9E";
		String compress = "10A2E5E9896P7F587B93PN9^C9E0GIKMOQ6SUWY[O_acHJL]ikVXZQp9`bdug[jTzn}^9`E";
		List<String> listCompress = Stream.of(compress.split("")).map(String::trim).collect(Collectors.toList());
		
		String result = LZW.decompress(listCompress);
		
		System.out.println(expected);
		System.out.println(result);
		
		assertEquals(expected, result);
	}

}
