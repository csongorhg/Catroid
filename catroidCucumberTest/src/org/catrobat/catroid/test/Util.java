package org.catrobat.catroid.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import org.catrobat.catroid.common.Constants;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.io.StorageHandler;
import org.catrobat.catroid.utils.ImageEditing;
import org.catrobat.catroid.utils.Utils;

import java.io.*;

import static junit.framework.Assert.fail;

public final class Util {
	private Util() {
	}

	public static Sprite findSprite(Project project, String name) {
		for (Sprite sprite : project.getSpriteList()) {
			if (sprite.getName().equals(name)) {
				return sprite;
			}
		}
		fail(String.format("Sprite not found '%s'", name));
		return null;
	}

	public static Point libgdxToScreenCoordinates(Context context, float x, float y) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
//        Log.d(CucumberInstrumentation.TAG, String.format("center: [%d/%d]", size.x / 2, size.y / 2));
		Point point = new Point();
		point.x = Math.round((size.x / 2f) + x);
		point.y = Math.round((size.y / 2f) + y);
//        Log.d(CucumberInstrumentation.TAG, String.format("coords: [%d/%d]", point.x, point.y));
		return point;
	}

	public static Point getScreenDimensions(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point dimensions = new Point();
		display.getSize(dimensions);
		return dimensions;
	}

	public static LookData newLookData(String name, File file) {
		LookData look = new LookData();
		look.setLookName(name);
		look.setLookFilename(file.getName());
		return look;
	}

	public static File createObjectImage(Context context, String outputName, int id) {
		Project project = (Project) Cucumber.get(Cucumber.KEY_PROJECT);
		String directoryPath = Utils.buildPath(Utils.buildProjectPath(project.getName()), Constants.IMAGE_DIRECTORY);
		File directory = new File(directoryPath);
		directory.mkdirs();
		File result = null;

		try {
			File copiedFile = File.createTempFile(outputName, "", directory);
			InputStream in = context.getResources().openRawResource(id);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(copiedFile), Constants.BUFFER_8K);

			byte[] buffer = new byte[Constants.BUFFER_8K];
			int length;
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.flush();
			out.close();

			String finalImageFileString = Utils.buildPath(directoryPath, Utils.md5Checksum(copiedFile) + "_" + copiedFile.getName());
			result = new File(finalImageFileString);
			copiedFile.renameTo(result);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		return result;
	}

	public static File createBackgroundImage(Context context, String name) {
		Project project = (Project) Cucumber.get(Cucumber.KEY_PROJECT);
		String directoryName = Utils.buildPath(Utils.buildProjectPath(project.getName()), Constants.IMAGE_DIRECTORY);
		File directory = new File(directoryName);
		Point screen = Util.getScreenDimensions(context);
		Bitmap backgroundBitmap = ImageEditing.createSingleColorBitmap(screen.x, screen.y, Color.BLUE);
		try {
			directory.mkdirs();
			File backgroundTemp = File.createTempFile(name, ".png", directory);
			StorageHandler.saveBitmapToImageFile(backgroundTemp, backgroundBitmap);
			File backgroundFile = new File(directoryName, Utils.md5Checksum(backgroundTemp) + "_" + backgroundTemp.getName());
			backgroundTemp.renameTo(backgroundFile);
			return backgroundFile;
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

	public static Formula newUserVariableFormula(String name) {
		return new Formula(new FormulaElement(FormulaElement.ElementType.USER_VARIABLE, name, null));
	}
}
