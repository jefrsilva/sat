package br.com.caelum.sat.filtro;

import static org.bytedeco.javacpp.opencv_core.cvarrToMat;
import static org.bytedeco.javacpp.opencv_video.createBackgroundSubtractorMOG2;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_video.BackgroundSubtractor;

public class BackgroundSubFiltro extends Filtro<IplImage, Mat> {

	private BackgroundSubtractor extratorDeFundo;
	private int learningFrames;
	private Mat output;

	public BackgroundSubFiltro(int learningFrames, int history, int threshold,
			boolean shadows) {
		this.learningFrames = learningFrames;
		extratorDeFundo = createBackgroundSubtractorMOG2(history, threshold,
				shadows);
	}

	@Override
	public Mat getOutput() {
		IplImage input = getInput();
		if (output == null) {
			output = cvarrToMat(input.clone());
		}
		if (!pronto) {
			if (learningFrames > 0) {
				System.out.println("Learning " + learningFrames);
				learningFrames--;
				extratorDeFundo.apply(cvarrToMat(input), output, 0.001);
			} else {
				extratorDeFundo.apply(cvarrToMat(input), output, 0);
			}
			pronto = true;
		}
		return output;
	}

}
