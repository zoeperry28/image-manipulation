import java.awt.image.BufferedImage;

public class ImageManipulation {

	// linear transformation to compute prei from i
    // O D and P are points on a line with i between D and P 
	static double linTrans (double O, double D, double i, double P) {
		return D - (((i - D) / (P - D)) * (D - O));
	}

	   // take a cartesian coordinate (I,J) assumed to be in the positive octant OGV
	   // and compute (preI, PreJ) returned as elements pre[0] pre[1] of pre 
	static int[] octlinTrans (int O, int D, int I, int J, int size) { 

		// we will compute preI and preJ and return them in pre 
		int [] pre = new int[2];

		// compute d from I and J 
		double d = Math.sqrt((I * I) + (J * J));

		// calculate P (from theta, itself from I and J) 
		double theta;
		double P;
		theta = Math.atan((double) J / (double) I);
		
		P = size / Math.cos(theta);
	
		// compute pred from O, D, d, p
		double pred = linTrans(O, size / 3, d, P);
		
		pre[0] = (int) (pred * Math.cos(theta));
		pre[1] = (int) (pred * Math.sin(theta));

		return pre;

	} 

	static public void linearBox(BufferedImage image, int n, int x, int y, int size) {

		BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		(temp.getGraphics()).drawImage(image, 0, 0, image.getWidth(), image.getHeight(),null);

		// check if A(x,y) lies within image
		if (x + size < image.getWidth() && x - size >= 0 && y + size < image.getHeight() && y - size >= 0) {
			// loop visiting each pixel p at coordinate (i,j) in A(x,y) 
			for (int i = x - size; i <= x + size; i++) {
				for(int j = y - size; j <= y + size; j++) {	
					
			int O, D, P;
			O = x - size;
			D = x + n;
			P = x + size;

			 // ensure that D lies between O and P (as n varies)
			 // if n too large restrict D to P
			if (D > P) D = P;
			if(n < 0) D = x;

			
			 // check IF i is between D and P
			 // if so compute prei
			 // and hence get the prep pixel RGB and set the p pixel RGB
			 // IF not make pixel (i,j) grey with 0xaaaaaa
					if(i >= D && i <= P){
						int prei = (int) linTrans(O, D, i, P);
						image.setRGB(i, j, temp.getRGB(prei, j));}
					else{
						image.setRGB(i, j, 0xaaaaaa);}

				}
			} 
		} 
	} 

	
static public void linearOct(BufferedImage image, int n, int x, int y, int size) {
		 
BufferedImage temp=new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
	(temp.getGraphics()).drawImage(image, 0, 0, image.getWidth(), image.getHeight(),null);

	// check if A(x,y) lies within image
if (x + size < image.getWidth() && x - size >= 0 && y + size < image.getHeight() && y - size >= 0) {

	// loop visiting each pixel in A(x,y) at image coordinate (i,j) 
	for (int i = x - size; i <= x + size; i++) {
		for (int j = y - size; j <= y + size; j++) {
			
			 // Apply IMGTRANS to each line ODP specified by an (i,j): 
			  // a list to store preI and preJ as element 0 and 1 
		    // pre is calculated below using octlinTrans
			int [] pre = new int[2];
			int I, J, radius, d;
			
		    // convert image coordinates (i,j) to cartesian coordinates 
		    //  .... for example mouse position (x,y) is converted to (x,-y)
       	    	    // then move the mouse position to the origin (0,0) and 
		    // ... translate all other positions relatively ...
		    // so that you now work with A(0,0) in cartesian coordinates 
		    // I and J below are relative cartesian coordinates 
		    // note: Cart Coord -j moves up (ie - ) by an amount -y  
			
			I = i - x;
			J = -j + y;
			
			// set d = distance of origin to (I,J)
			d = (int) Math.sqrt((I * I) + (J * J));

			// if (I,J) is outside the circle of radius size/3
			// then we compute (preI, preJ) from (I,J) using octlinTrans
			radius = size / 3;
			if (d > radius) { 
				// perform linear transformation in octant OGV
				if (0 < J && J < I){
					pre = octlinTrans(0, d, I, J, size); // use octlinTrans
				}
				// perform linear transformation in octant OVH 
				else if (0 < -J && -J < I){
					pre = octlinTrans(n, d, I, -J, size);
					J = -J;
					pre[1] = -pre[1];
				}
				// perform linear transformation in octant OKU 
				else if(0 < -J && -J < -I){
					pre = octlinTrans(n, d, -I, -J, size);
					J = -J;
					I = -I;
					pre[0] = -pre[0];
					pre[1] = -pre[1];	 
				}
				// perform linear transformation in octant OUF
				else if(0 < J && J < -I){
					pre = octlinTrans(n, d, -I, J, size);
					I = -I;
					pre[0] = -pre[0];
				}
				// identity transformation elsewhere (outside the circle) 
				else { 
					pre[0] = I;
					pre[1] = J;
				} 
				pre[0] += x;
				pre[1] = y - pre[1];
				image.setRGB(i, j, temp.getRGB(pre[0], pre[1]));
				// transform relative cartesian coordinate (preI,preJ) 
			    // back to image coordinate (prei,prej) 
			} 
			else { 
				image.setRGB(i, j, 0xaaaaaa);}
			// set RGB of pixel at (i,j) to RGB from (prei,prej)
			}
		}
	}
}

static public void phaseShift(BufferedImage image, int n, int x, int y, int size) {	
	// creates a copy of the image called temp   
	BufferedImage temp=new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        (temp.getGraphics()).drawImage(image,0,0,image.getWidth(), image.getHeight(),null );

    	if (x + size < image.getWidth() && x - size >= 0 && y + size < image.getHeight() && y - size >= 0) {
    		// loop through each pixel (i,j) in "A(x,y) intersect (image - boundary)"
    		for (int i = x - size; i <= Math.min(image.getWidth()-1-2*n,x+size); i++) {
    				for(int j = y - size; j <= Math.min(image.getHeight()-1-2*n,  y+size); j++) {

    				
    					int O, D, P;
    					O = x - size;
    					D = x + n;
    					P = x + size;
    					int [] pre = new int[2];
    					
    					
    					if (D > P) D = P;
    					if(n < 0) D = x;
    					
    							
    					if(i > 300){
    						image.setRGB(i, j, 0x0000ff);}
    					//if at a width more than 300, make any pixel beyond 300 blue
    					else{
    						int col = temp.getRGB(i, j - (2 * n));
    							col = col >> 8;
    							col = col - ((col >> 8) << 8);
    							col = (col << 16) + (col << 8) + col;
    							image.setRGB(i, j, col);}

    				}
    		}
    	}
}
}
