package br.com.utfpr.porta.util;

public class Algorithm {
	
	private static double TOLERANCIA = 0.25;
	private static int NUM_AMOSTRAS = 22000; //1.5*16000
	private static int DELAY_MAX = 1100;	
	
	private static float max(int nElem, float[] buffer) {
		
		float maior = 0;

		for (int i=0; i<nElem; i++) {
			if (buffer[i] > maior) {
				maior = buffer[i];
			}
		}

		return maior;
	}
	
	private static float autoCorr(int[] buffer) {
		
		float rAuto = 0;

		for (int n = 0; n < (NUM_AMOSTRAS + 2*DELAY_MAX) - 1; n++) {
			rAuto += (buffer[n]*buffer[n])/255;
		}
		
		return rAuto;
	}

	private static float crossCorr(int[] bufferDatabase, int[] bufferRecebido) {
		
		float[] rCross = new float[2*DELAY_MAX + 1];
		
		for (int k = -DELAY_MAX; k <= DELAY_MAX; k++) {
			for (int n = 0; n < (NUM_AMOSTRAS + 2*DELAY_MAX) - 1; n++) {
				rCross[DELAY_MAX + k] += (bufferDatabase[n]*bufferRecebido[n-k])/255;
			}
		}

		return max(2*DELAY_MAX + 1, rCross);
	}
		
	public static boolean validate(int[] bufferDatabase, int[] bufferRecebido) {
		float auto1 = autoCorr(bufferDatabase);
		float auto2 = autoCorr(bufferRecebido);
		float cross12 = crossCorr(bufferDatabase, bufferRecebido);

		double coef = cross12 / Math.sqrt(auto1*auto2);
		
		return coef >= (1 - TOLERANCIA);
	}

}

