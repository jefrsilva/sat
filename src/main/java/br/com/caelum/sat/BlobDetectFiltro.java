package br.com.caelum.sat;

import org.bytedeco.javacpp.opencv_core.KeyPointVector;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.SimpleBlobDetector;
import org.bytedeco.javacpp.opencv_features2d.SimpleBlobDetector.Params;

import br.com.caelum.sat.filtro.Filtro;

public class BlobDetectFiltro extends Filtro<Mat, KeyPointVector> {

	private SimpleBlobDetector detectorDeBlob;
	private KeyPointVector output;

	public BlobDetectFiltro() {
		Params params = new Params();
		params.minDistBetweenBlobs(200.0f);
		params.filterByInertia(false);
		params.filterByConvexity(false);
		params.filterByColor(false);
		params.filterByCircularity(false);
		params.filterByArea(true);
		params.minArea(5000.0f);
		params.maxArea(4000000.0f);

		detectorDeBlob = SimpleBlobDetector.create(params);
	}

	@Override
	public KeyPointVector getOutput() {
		Mat input = getInput();
		if (output == null) {
			output = new KeyPointVector();
		}
		if (!pronto) {
			detectorDeBlob.detect(input, output);
			pronto = true;
		}
		return output;
	}

}
