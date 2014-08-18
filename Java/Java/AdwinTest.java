
public class AdwinTest {

	public static void main(String[] args) {

		ADWIN adwin=new ADWIN(.01); // Init Adwin with delta=.01
		for (int p=0;p<1000;p++){
			if(adwin.setInput(f(p))) //Input data into Adwin
				System.out.println("Change Detected: "+p);
		}
		//Get information from Adwin
		System.out.println("Mean:"+adwin.getEstimation());
		System.out.println("Variance:"+adwin.getVariance());
		System.out.println("Stand. dev:"+Math.sqrt(adwin.getVariance()));
		System.out.println("Width:"+adwin.getWidth());
	}
	private static double f(int p) { 
		//Gradual Change
		//return (double) 3*p;
		//Abrupt Change
		return (p<500 ? 1000:500);
		}

}
