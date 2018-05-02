package br.com.utfpr.porta.util;

public class Algorithm {
	
	private static final int DELAY_MAX = 1600;	
	
	private Algorithm() {
		throw new IllegalStateException("Utility class");
	}
	
	private static float max(int nElem, float[] buffer) {
		
		float maior = 0;

		for (int i=0; i<nElem; i++) {
			if (buffer[i] > maior) {
				maior = buffer[i];
			}
		}

		return maior;
	}
	
	private static int[] zeroFill (int[] buffer) {
		int[] bufferZ = new int[buffer.length + 2*DELAY_MAX];
		
		for (int i=DELAY_MAX; i<buffer.length + DELAY_MAX; i++) {
			bufferZ[i] = buffer[i - DELAY_MAX];
		}
		
		return bufferZ;
	}
	
	private static float autoCorr(int[] buffer) {
		
		float rAuto = 0;

		for (int n = 0; n < (buffer.length + 2*DELAY_MAX) - 1; n++) {
			rAuto += (buffer[n]*buffer[n])/255;
		}
		
		return rAuto;
	}

	private static float crossCorr(int[] bufferDatabase, int[] bufferRecebido) {
		
		float[] rCross = new float[2*DELAY_MAX + 1];
		
		if (bufferDatabase.length <= bufferRecebido.length) {
			for (int k = -DELAY_MAX; k <= DELAY_MAX; k++) {
				if (k <= 0) {
					for (int n = 0; n < (bufferDatabase.length + 2*DELAY_MAX + k); n++) {
						rCross[DELAY_MAX + k] += (bufferDatabase[n]*bufferRecebido[n-k])/255;
					}
				} else {
					for (int n = 0; n < (bufferDatabase.length + 2*DELAY_MAX - k); n++) {
						rCross[DELAY_MAX + k] += (bufferDatabase[n+k]*bufferRecebido[n])/255;
					}
				}
			}
		} else {
			for (int k = -DELAY_MAX; k <= DELAY_MAX; k++) {
				if (k <= 0) {
					for (int n = 0; n < (bufferRecebido.length + 2*DELAY_MAX + k); n++) {
						rCross[DELAY_MAX + k] += (bufferDatabase[n]*bufferRecebido[n-k])/255;
					}
				} else {
					for (int n = 0; n < (bufferRecebido.length + 2*DELAY_MAX - k); n++) {
						rCross[DELAY_MAX + k] += (bufferDatabase[n+k]*bufferRecebido[n])/255;
					}
				}
			}
		}

		return max(2*DELAY_MAX + 1, rCross);
	}
		
	public static boolean validate(double tolerancia, int[] bufferDatabase, int[] bufferRecebido) {
		int[] bufferDatabaseZ = zeroFill(bufferDatabase);
		int[] bufferRecebidoZ = zeroFill(bufferRecebido);
		
		float auto1 = autoCorr(bufferDatabaseZ);
		float auto2 = autoCorr(bufferRecebidoZ);
		float cross12 = crossCorr(bufferDatabaseZ, bufferRecebidoZ);

		double coef = cross12 / Math.sqrt(auto1*auto2);
		
		return coef >= (1 - tolerancia);
	}

}

