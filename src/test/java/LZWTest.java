import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import br.com.utfpr.porta.util.LZW;

public class LZWTest {

	@Test
	public void test() {
		
		String expected = "48494042434349565F6E7A8387827C6E50423B3D444B55555A5B5E5C5E62656462605E5B5D5F61656B72767A7D80817E7D7E7975685D5346403F41434A52565A61676C717C7777757579797978757577797B7D7C786A5D4D443B393C3E434646474D555D69727474716F727779817F7E7D7A78777A7477736865565146433D3B373D4348535A6065636A6B72758081807F7E776E6354493E393F454C5352524B4946474E5A65686D71777F7F80796F625342403F3F44474C535E6A757F827F766A604F453D3C3F44535E6972797D80867E7667564639373D4959616866645E636A6C77787B787372777C8387817868574638383D474F5555514B4F525C676E74706E6A656C6F717674746F6F73727374757877787878746F6A5D52443A383D45505B60615A575354575F677277797B7B7D7F7B706052483F3E435461686F727676787B7C7E8081786D5C4C3E36363C4350555756535459626A717B7E84847D77695D51464140444B5156595E656C70777B7D7E7C71654F4439363D424A4F51504F4E50565B5D5D5F65696C73767B7D7E7C776B5F4F453D3F43454C5055595F68737A7D7F7873716C6F75778080827D7A736D66605851433E3A3A41444C51504F4C48494A4F52535656575B606269696C656462615F5D58534E4A4F4E4E4C4A4C53575E63686C6D706B6867635F615F5D5A5652504C535A67727E857F796B5E4E";
		String compress = "4849404243OI565F6E7A8387827CW5L23B3D4lB5ooA5nE5C5E62T64z60xs5DU61|B7a6Y7D80817XD7X97S85D5P}03F41Qr2S5A61676C71b77777575797979_757577797B7Db_6rkkOi93C3EQ}}74DpD697a474716F7a779817F7XDY_77Y74777368TS514}3jh37jQ8535A60|36A6B7a58081807F7X7W635l93E393F454w3525NBI4}74uA|86D71777F7F80796V25PN03F3Fl474w3x6A757F`7F766A6LF45j3C3Fl53x697a97D80867X667S463937jR9616866}x636A6C77_7B_737a7b[^81_685746]]j474Fpo14B4F52v67W7470W6A|cF717674746VF737a37475_77___746VrD5NOA]j4o0s60615r7535457U677a7797B7B7D7F7B70605N[F3EO5461686F7a676_7Bb7E8081_6Dv4C3E36363COep7S535459z6A717B7EHH7D77695D514}1Kl4n1S59x|C70777B7D7XC71T4Fl3936jM4A4F51e4F4u0Ss5D5DU|96C73767B7D7XC776nF4F45j3FQ54w0p9U6^3Y7D7F_73716cF757_080`7DY736D66605851O3E3A3A41l4w1e4F4CGI4A4F5253SS57s60z69696cS4z61U5D585PE4A4F4E4E4C4A4w357x63686cD706B686763U61U5D5rT2e4w35A677aE857F796t4E";
		List<String> listCompress = Stream.of(compress.split("")).map(String::trim).collect(Collectors.toList());
		
		String result = LZW.decompress(listCompress);
		
		System.out.println("Tamanho do original: ".concat(String.valueOf(expected.length())));
		System.out.println("Tamanho do comprimido: ".concat(String.valueOf(compress.length())));

		System.out.println(expected);
		System.out.println(result);
		
		assertEquals(expected, result);
	}

}