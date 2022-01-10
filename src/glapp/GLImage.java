package glapp;

import java.nio.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.imageio.*;
import java.net.URL;

import org.lwjgl.*;
import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;

public class GLImage 
{
    public int height = 0;
    public int width = 0;
	
    public ByteBuffer pixelBuffer = null;
    public int[] pixels = null;
	
	public static final int SIZE_DOUBLE = 8;
    public static final int SIZE_FLOAT = 4;
    public static final int SIZE_INT = 4;
    public static final int SIZE_BYTE = 1;

    public int textureW;
    public int textureH;
	public int textureHandle;

    public GLImage() {}

    public GLImage(String imgName)
    {
		BufferedImage img = loadJavaImage(imgName);
		
        makeGLImage(img,true,false);
    }

    public GLImage(ByteBuffer gl_pixels, int width, int height) 
	{
		if (gl_pixels != null) 
		{
       		this.pixelBuffer = gl_pixels;
        	this.pixels = null;
        	this.height = height;
        	this.width = width;
		}
    }

    public void makeGLImage(BufferedImage tmpi, boolean flipYaxis, boolean convertToPow2) 
	{
        if (tmpi != null) 
		{
            if (flipYaxis) 
	            tmpi = flipY(tmpi);
			
            if (convertToPow2) 
            	tmpi = convertToPowerOf2(tmpi);
			
            width = tmpi.getWidth(null);
            height = tmpi.getHeight(null);
            pixels = getImagePixels(tmpi);
            pixelBuffer = convertImagePixelsRGBA(pixels,width,height,false);
            textureW = getPowerOfTwoBiggerThan(width);
            textureH = getPowerOfTwoBiggerThan(height);
        }
        else 
		{
            pixels = null;
            pixelBuffer = null;
            height = 0;
			width = 0;
        }
    }

    public BufferedImage loadJavaImage(String imgName) 
	{
    	BufferedImage tmpi = null;
		
    	try 
		{
    		tmpi = ImageIO.read(GLApp.getInputStream(imgName));
    	}
    	catch (Exception e) 
		{
    		e.printStackTrace(System.out);
    	}
		
    	return tmpi;
	}

    public static int[] getImagePixels(Image image)
    {
    	int[] pixelsARGB = null;
		
        if (image != null) 
		{
        	int imgw = image.getWidth(null);
        	int imgh = image.getHeight(null);
        	pixelsARGB = new int[ imgw * imgh];
            PixelGrabber pg = new PixelGrabber(image, 0, 0, imgw, imgh, pixelsARGB, 0, imgw);
			
            try 
			{
                pg.grabPixels();
            }
            catch (Exception e) 
			{
            	e.printStackTrace(System.out);
                return null;
            }
        }
		
        return pixelsARGB;
    }

    public ByteBuffer getPixelBytes()
    {
        return pixelBuffer;
    }
	
	public static ByteBuffer allocBytes(byte[] bytearray) 
	{
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytearray.length).order(ByteOrder.nativeOrder());
		
        buffer.put(bytearray).flip();
		
        return buffer;
    }

    public static int[] flipPixels(int[] imgPixels, int imgw, int imgh)
    {
        int[] flippedPixels = null;
		
        if (imgPixels != null) 
		{
            flippedPixels = new int[imgw * imgh];
			
            for (int y = 0; y < imgh; y++) 
			{
                for (int x = 0; x < imgw; x++) 
				{
                    flippedPixels[ ( (imgh - y - 1) * imgw) + x] = imgPixels[ (y * imgw) + x];
                }
            }
        }
		
        return flippedPixels;
    }
	
	public static BufferedImage flipY(BufferedImage bsrc) 
	{
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -bsrc.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		
        return op.filter(bsrc, null);
    }

    public static ByteBuffer convertImagePixelsRGBA(int[] jpixels, int imgw, int imgh, boolean flipVertically) 
	{
        byte[] bytes;
		
        if (flipVertically) 
		{
            jpixels = flipPixels(jpixels, imgw, imgh);
        }
		
        bytes = convertARGBtoRGBA(jpixels);
		
        return allocBytes(bytes);
    }

    public static byte[] convertARGBtoRGBA(int[] jpixels)
    {
        byte[] bytes = new byte[jpixels.length*4];
        int p, r, g, b, a;
        int j=0;
		
        for (int i = 0; i < jpixels.length; i++) 
		{
            p = jpixels[i];
            a = (p >> 24) & 0xFF;
            r = (p >> 16) & 0xFF;
            g = (p >> 8) & 0xFF;
            b = (p >> 0) & 0xFF;
            bytes[j+0] = (byte)r;
            bytes[j+1] = (byte)g;
            bytes[j+2] = (byte)b;
            bytes[j+3] = (byte)a;
            j += 4;
        }
		
        return bytes;
    }

    public void convertToPowerOf2() 
	{
    	BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    	image.setRGB(0, 0, width, height, pixels, 0, width);

    	BufferedImage scaledImg = convertToPowerOf2(image);

    	width = scaledImg.getWidth(null);
        height = scaledImg.getHeight(null);
        pixels = getImagePixels(scaledImg);
        pixelBuffer = convertImagePixelsRGBA(pixels,width,height,false);
        textureW = getPowerOfTwoBiggerThan(width);
        textureH = getPowerOfTwoBiggerThan(height);
    }
	
	public static int getPowerOfTwoBiggerThan(int n) 
	{
        if(n < 0)
            return 0;
			
        --n;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
		
        return n+1;
    }

    public static BufferedImage convertToPowerOf2(BufferedImage bsrc) 
	{
        int newW = getPowerOfTwoBiggerThan(bsrc.getWidth());
        int newH = getPowerOfTwoBiggerThan(bsrc.getHeight());
		
        if (newW == bsrc.getWidth() && newH == bsrc.getHeight())
        	return bsrc;
        else 
		{
	        AffineTransform at = AffineTransform.getScaleInstance((double)newW / bsrc.getWidth(),(double)newH / bsrc.getHeight());
	        BufferedImage bdest = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
	        Graphics2D g = bdest.createGraphics();
			
	        g.drawRenderedImage(bsrc,at);
			
	        return bdest;
        }
    }
	
	public boolean isLoaded()
    {
        return (pixelBuffer != null);
    }
}