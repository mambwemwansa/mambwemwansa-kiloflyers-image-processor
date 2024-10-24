package com.kiloflyers.util;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageUtil {
  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }
  
  public static Mat removeBackground(String imagePath) {
    Mat image = Imgcodecs.imread(imagePath);
    if (image.empty()) {
      System.out.println("Could not load image: " + imagePath);
      return null;
    } 
    Mat grayImage = new Mat();
    Imgproc.cvtColor(image, grayImage, 6);
    return grayImage;
  }
  
  public static Mat reframeImage(Mat image, int width, int height) {
    Mat resizedImage = new Mat();
    Imgproc.resize(image, resizedImage, new Size(width, height));
    return resizedImage;
  }
  
  public static void saveImage(Mat image, String filename) {
    if (image != null) {
      Imgcodecs.imwrite(filename, image);
    } else {
      System.out.println("Image is null. Cannot save.");
    } 
  }
}