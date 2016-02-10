import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LineProcessor {

	public static double[][] ProcessImage(List<List<Double>> input) {
		double[][] goals = new double[2][];
		
		
		return goals;
	}
	
	private Rectangle findNextClump(List<List<Double>> input) {
		List<List<Double>> inputCopy;
		Collections.copy(inputCopy, input);
		Collections.sort(inputCopy.get(0)); // List of x1
		Collections.sort(inputCopy.get(1)); // List of y1
		Collections.sort(inputCopy.get(2)); // List of x2
		Collections.sort(inputCopy.get(3)); // List of y2
		Rectangle clump = new Rectangle();
		clump.setBounds(indexOfLargestValue(input), y, width, height);
		return clump;
		
	}
	
	private int indexOfLargestValue(List<Double> input) {
		
		int largestValueIndex = 0;
		double largestValue = input.get(0);
		for (int i = 0; i < input.size(); i++) {
			if (input.get(i) > largestValue) {
				largestValueIndex = i;
				largestValue = input.get(i);				
			}
		}
		
		return largestValueIndex;
		
	}
	
	private int indexOfSmallestValue(List<Double> input) {
		
		int smallestValueIndex = 0;
		double smallestValue = input.get(0);
		for (int i = 0; i < input.size(); i++) {
			if (input.get(i) < smallestValue) {
				smallestValueIndex = i;
				smallestValue = input.get(i);
			}
		}
		
		return smallestValueIndex;
		
	}

}
