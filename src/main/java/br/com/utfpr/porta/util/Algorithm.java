package br.com.utfpr.porta.util;

public class Algorithm {
	
	private static double TOLERANCIA = 0.1;
	private static int NUM_AMOSTRAS = 96000; //2*48000
	private static int DELAY_MAX = 480;	
	
	private static float maxFloat(int nElem, float[] buffer) {
		
		float maior = 0;

		for (int i=0; i<nElem; i++) {
			if (buffer[i] > maior) {
				maior = buffer[i];
			}
		}

		return maior;
	}
	
	private static void normalizar(float[] bufferDatabase, float[] bufferRecebido) {
		
		float maxValue;
		int i;

		maxValue = maxFloat(NUM_AMOSTRAS, bufferDatabase);

		for (i=0; i<NUM_AMOSTRAS + 2*DELAY_MAX; i++) {
			bufferDatabase[i] = (bufferDatabase[i])/(maxValue);
		}

		maxValue = maxFloat(NUM_AMOSTRAS, bufferRecebido);

		for (i=0; i<NUM_AMOSTRAS + 2*DELAY_MAX; i++) {
			bufferRecebido[i] = (bufferRecebido[i])/(maxValue);
		}
	}

	private static float autoCorr(float[] buffer) {
		
		float rAuto = 0;

		for (int n = 0; n < (NUM_AMOSTRAS + 2*DELAY_MAX) - 1; n++) {
			rAuto += buffer[n]*buffer[n];
		}
		
		return rAuto;
	}

	private static float crossCorr(float[] bufferDatabase, float[] bufferRecebido) {
		
		float[] rCross = new float[2*DELAY_MAX + 1];
		
		for (int k = -DELAY_MAX; k <= DELAY_MAX; k++) {
			for (int n = 0; n < (NUM_AMOSTRAS + 2*DELAY_MAX) - 1; n++) {
				rCross[DELAY_MAX + k] += bufferDatabase[n]*bufferRecebido[n-k];
			}
		}

		return maxFloat(2*DELAY_MAX + 1, rCross);
	}
		
	public static boolean validate(float[] bufferDatabase, float[] bufferRecebido) {
		
		normalizar(bufferDatabase, bufferRecebido);
		double coef = crossCorr(bufferDatabase, bufferRecebido) / Math.sqrt(autoCorr(bufferDatabase)*autoCorr(bufferRecebido));
		
		return coef >= (1 - TOLERANCIA);
	}

}

