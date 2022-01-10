package glapp;

import java.util.ArrayList;
import java.util.regex.*;
import java.nio.*;
import java.io.*;
import java.net.URL;

import org.lwjgl.*;
import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;

public class GLApp 
{
    public static String windowTitle = "";
	public static URL baseURL = null;
    public static Class rootClass = GLApp.class;
	public static boolean exit;

    public static int displayWidth = -1;
    public static int displayHeight = -1;
    public static int displayColorBits = 24;
    public static int displayFrequency = 60;
    public static int depthBufferBits = 24;
	public static float aspectRatio = 0;
	public static boolean vSync = false;
    public static boolean fullScreen = false;
	public static int viewport[] = {0, 0, 0, 0};
	public static DisplayMode display[] = {null, null, null};
	
	public static final int SIZE_DOUBLE = 8;
    public static final int SIZE_FLOAT = 4;
    public static final int SIZE_INT = 4;
    public static final int SIZE_BYTE = 1;

    public static int screenTextureSize = 1024;
    public static IntBuffer bufferViewport = allocInts(16);
    public static FloatBuffer bufferModelviewMatrix = allocFloats(16);
    public static FloatBuffer bufferProjectionMatrix = allocFloats(16);
    public static FloatBuffer tmpResult = allocFloats(16);
	public static IntBuffer tmpInts = allocInts(16);
	public static FloatBuffer tmpFloats = allocFloats(4);
    public static ByteBuffer tmpFloat = allocBytes(SIZE_FLOAT);
    public static ByteBuffer tmpByte = allocBytes(SIZE_BYTE);
    public static ByteBuffer tmpInt = allocBytes(SIZE_INT); 

	public void setup(){}
	public void render(){}
	
    public static void main(String args[]) 
	{
        GLApp app = new GLApp();
        app.run();
    }

    public void run() 
	{
    	rootClass = this.getClass();
		
        try 
		{
            init();
            
            while (!exit) 
			{
                if(!Display.isVisible()) 
                    Thread.sleep(200L);
                else if(Display.isCloseRequested()) 
                    exit = true;
                else 
                    Thread.sleep(1);
					     
                handleExit();     
                render();             
                Display.update();
            }
        }
        catch (Exception e) 
		{
            e.printStackTrace(System.out);
        }
        
        cleanup();
        System.exit(0);
    }

    public void init()
    {
        initDisplay();
        initInput();
        initGL();
        setup();        
    }

    public boolean initDisplay() 
	{
        display[1] = Display.getDisplayMode();  
    	
    	if(displayHeight == -1) 
			displayHeight = display[1].getHeight();
			
    	if(displayWidth == -1) 
			displayWidth = display[1].getWidth();
			
		displayColorBits = display[1].getBitsPerPixel();
		displayFrequency = display[1].getFrequency();
		display[0] = getDisplayMode(displayWidth, displayHeight, displayColorBits, displayFrequency);
		display[2] = display[0];
		displayWidth = display[0].getWidth();
		displayHeight = display[0].getHeight();
		displayColorBits = display[0].getBitsPerPixel();
		displayFrequency = display[0].getFrequency();
		
		try
		{
			Display.setDisplayMode(display[0]);
		}
		catch(LWJGLException lwjgle) 
		{
			lwjgle.printStackTrace(System.out);
		}

        try 
		{
            Display.create(new PixelFormat(0, depthBufferBits, 8));  
            Display.setTitle(windowTitle);
            Display.setFullscreen(fullScreen);
            Display.setVSyncEnabled(vSync);
        }
        catch (Exception e) 
		{
            e.printStackTrace(System.out);
            System.exit(1);
        }
        
        if(aspectRatio == 0f) 
            aspectRatio = (float)display[0].getWidth() / (float)display[0].getHeight(); 
        
        viewport[3] = display[0].getHeight();                        
        viewport[2] = (int) (display[0].getHeight() * aspectRatio);  
		
        if(viewport[2] > display[0].getWidth()) 
		{
            viewport[2] = display[0].getWidth();
            viewport[3] = (int) (display[0].getWidth() * (1 / aspectRatio));
        }
        
        viewport[0] = (int) ((display[0].getWidth() - viewport[2]) / 2);
        viewport[1] = (int) ((display[0].getHeight() - viewport[3]) / 2);
		
        return true;
    }

    public static DisplayMode getDisplayMode(int w, int h, int colorBits, int freq) 
	{
		 try 
		 {
			 DisplayMode modes[] = Display.getAvailableDisplayModes();
			 DisplayMode mode = null;
			 
			 for(int i = 0; i < modes.length; i++) 
			 {
				 mode = modes[i];
				 
				 if(mode.getWidth() == w && mode.getHeight() == h && mode.getBitsPerPixel() == colorBits && mode.getFrequency() == freq) 
					 return mode;
			 }
		}
		catch(LWJGLException lwjgle) 
		{
			lwjgle.printStackTrace(System.out);
		}

		 
        return null;
    }

    public void initInput() 
	{
        try 
		{
            Keyboard.create();
        }
        catch (Exception e) 
		{
            e.printStackTrace(System.out);
        }
    }

    public void initGL() 
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST); 
		GL11.glDepthFunc(GL11.GL_LEQUAL);  
		GL11.glClearColor(0f, 0f, 0f, 1f); 
		GL11.glEnable(GL11.GL_NORMALIZE);  
		GL11.glEnable(GL11.GL_CULL_FACE);  
		GL11.glEnable(GL11.GL_TEXTURE_2D); 
		GL11.glEnable(GL11.GL_BLEND);      
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0f);
		GL11.glLightModeli(GL12.GL_LIGHT_MODEL_COLOR_CONTROL, GL12.GL_SEPARATE_SPECULAR_COLOR );
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
		setPerspective();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
    }

    public static IntBuffer getViewport()
    {
        bufferViewport.clear();
        GL11.glGetInteger(GL11.GL_VIEWPORT, bufferViewport);
		
        return bufferViewport;
    }

    public static int allocateTexture()
    {
        IntBuffer textureHandle = allocInts(1);
        GL11.glGenTextures(textureHandle);
        return textureHandle.get(0);
    }

    public static void activateTexture(int textureHandle)
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureHandle);
    }

    public static int makeTexture(String textureImagePath)
    {
		int textureHandle = 0;
		GLImage textureImg = loadImage(textureImagePath);
		
		if(textureImg != null) 
		{
			textureHandle = makeTexture(textureImg);
			makeTextureMipMap(textureHandle,textureImg);
		}
		
		return textureHandle;
    }

    public static int makeTexture(String textureImagePath, boolean mipmap, boolean anisotropic)
    {
        int textureHandle = 0;
        GLImage textureImg = loadImage(textureImagePath);
		
        if(textureImg != null) 
		{
            textureHandle = makeTexture(textureImg.pixelBuffer, textureImg.width, textureImg.height, anisotropic);
			
            if(mipmap)
                makeTextureMipMap(textureHandle,textureImg);
        }
		
        return textureHandle;
    }

    public static int makeTexture(GLImage textureImg)
    {
        if(textureImg != null) 
		{
            if(isPowerOf2(textureImg.width) && isPowerOf2(textureImg.height)) 
                return makeTexture(textureImg.pixelBuffer, textureImg.width, textureImg.height, false);
            else 
			{
                textureImg.convertToPowerOf2();
                return makeTexture(textureImg.pixelBuffer, textureImg.width, textureImg.height, false);
            }
        }
		
        return 0;
    }

    public static void deleteTexture(int textureHandle)
    {
        IntBuffer bufferTxtr = allocInts(1).put(textureHandle);;
        GL11.glDeleteTextures(bufferTxtr);
    }

    public static boolean isPowerOf2(int n) 
	{
    	if(n == 0)
			return false;
		
        return (n & (n - 1)) == 0;
    }

    public static int makeTexture(int w)
    {
        ByteBuffer pixels = allocBytes(w*w*SIZE_INT);  
		
        return makeTexture(pixels, w, w, false);
	}

    public static int makeTexture(int[] pixelsARGB, int w, int h, boolean anisotropic)
    {
    	if(pixelsARGB != null) 
		{
    		ByteBuffer pixelsRGBA = GLImage.convertImagePixelsRGBA(pixelsARGB,w,h,true);
    		return makeTexture(pixelsRGBA, w, h, anisotropic);
    	}
		
    	return 0;
    }

    public static int makeTexture(ByteBuffer pixels, int w, int h, boolean anisotropic)
    {
        int textureHandle = allocateTexture();
		
        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureHandle);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); 
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 

    	if(anisotropic) 
		{
    		FloatBuffer max_a = allocFloats(16);
    		GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max_a);
    		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, max_a.get(0));
    	}

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);				
        GL11.glPopAttrib();

        return textureHandle;
    }

    public static int makeTextureARGB(ByteBuffer pixels, int w, int h)
    {
		int pixel_byte_order = (pixels.order() == ByteOrder.BIG_ENDIAN) ? GL12.GL_UNSIGNED_INT_8_8_8_8 : GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
        int textureHandle = allocateTexture();
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureHandle);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); 
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL12.GL_BGRA, pixel_byte_order, pixels);
		
        return textureHandle;
    }

    public static int makeTextureMipMap(int textureHandle, GLImage textureImg)
    {
    	int ret = 0;
		
    	if(textureImg != null && textureImg.isLoaded()) 
		{
    		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
    		ret = GLU.gluBuild2DMipmaps(GL11.GL_TEXTURE_2D, GL11.GL_RGBA8, textureImg.width, textureImg.height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, textureImg.getPixelBytes());
       		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
    		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
    	}
		
        return ret;
    }

    public static int makeTextureForScreen(int screenSize)
    {
        screenTextureSize = getPowerOfTwoBiggerThan(screenSize);
        int textureHandle = allocateTexture();
        ByteBuffer pixels = allocBytes(screenTextureSize*screenTextureSize*SIZE_INT);
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,textureHandle);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, screenTextureSize, screenTextureSize, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
		
        return textureHandle;
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

	public static void copyPixelsToTexture(ByteBuffer bb, int w, int h, int textureHandle) 
	{
		int pixel_byte_order = (bb.order() == ByteOrder.BIG_ENDIAN) ? GL12.GL_UNSIGNED_INT_8_8_8_8 : GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

    	GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);
    	GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, w, h, GL12.GL_BGRA, pixel_byte_order, bb);
	}

	public static void copyImageToTexture(GLImage img, int textureHandle) 
	{
		copyPixelsToTexture(img.pixelBuffer, img.width, img.height, textureHandle);
	}

    public static void setPerspective()
    {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(40f, aspectRatio, 1f, 1000f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public static void setViewport(int x, int y, int width, int height)
    {
    	viewport[0] = x;
    	viewport[1] = y;
    	viewport[2] = width;
    	viewport[3] = height;
     	aspectRatio = (float)width / (float)height;
       	GL11.glViewport(x,y,width,height);
   }

    public static void resetViewport()
    {
    	setViewport(0,0,displayWidth,displayHeight);
    }

    public static void lookAt(float lookatX, float lookatY, float lookatZ, float distance)
    {
        GLU.gluLookAt(lookatX, lookatY, lookatZ+distance, lookatX, lookatY, lookatZ, 0, 1, 0);                            
    }

	public static void pushAttrib()
	{
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
	}

	public static void pushAttrib(int attribute_bits)
	{
        GL11.glPushAttrib(attribute_bits);
	}

    public static void setLight(int GLLightHandle, float[] diffuseLightColor, float[] ambientLightColor, float[] specularLightColor, float[] position)
    {
        FloatBuffer ltDiffuse = allocFloats(diffuseLightColor);
        FloatBuffer ltAmbient = allocFloats(ambientLightColor);
        FloatBuffer ltSpecular = allocFloats(specularLightColor);
        FloatBuffer ltPosition = allocFloats(position);
        GL11.glLight(GLLightHandle, GL11.GL_DIFFUSE, ltDiffuse);   
        GL11.glLight(GLLightHandle, GL11.GL_SPECULAR, ltSpecular); 
        GL11.glLight(GLLightHandle, GL11.GL_AMBIENT, ltAmbient);   
        GL11.glLight(GLLightHandle, GL11.GL_POSITION, ltPosition);
        GL11.glEnable(GLLightHandle);
    }


    public static void setSpotLight(int GLLightHandle, float[] diffuseLightColor, float[] ambientLightColor, float[] position, float[] direction, float cutoffAngle)
    {
        FloatBuffer ltDirection = allocFloats(direction);
        setLight(GLLightHandle, diffuseLightColor, ambientLightColor, diffuseLightColor, position);
        GL11.glLightf(GLLightHandle, GL11.GL_SPOT_CUTOFF, cutoffAngle);   
        GL11.glLight(GLLightHandle, GL11.GL_SPOT_DIRECTION, ltDirection);    
        GL11.glLightf(GLLightHandle, GL11.GL_CONSTANT_ATTENUATION, 2F);
    }

    public static void setAmbientLight(float[] ambientLightColor)
    {
        put(tmpFloats,ambientLightColor);
        GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, tmpFloats);
    }

    public static void setLightPosition(int GLLightHandle, float[] position)
    {
        put(tmpFloats,position);
        GL11.glLight(GLLightHandle, GL11.GL_POSITION, tmpFloats);
    }

    public static GLImage makeImage(int w, int h) 
	{
        ByteBuffer pixels = allocBytes(w*h*SIZE_INT);
		
        return new GLImage(pixels,w,h);
    }

    public static GLImage loadImage(String imgFilename) 
	{
        GLImage img = new GLImage(imgFilename);
		
        if(img.isLoaded())
            return img;
			
        return null;
    }

    public static ByteBuffer loadImagePixels(String imgFilename) 
	{
        GLImage img = new GLImage(imgFilename);
		
        return img.pixelBuffer;
    }

    public static void drawImage(GLImage img, int x, int y, float w, float h) 
	{
    	if(img.textureHandle <= 0) 
    		img.textureHandle = makeTexture(img);
    	
    	GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_LIGHTING_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D,img.textureHandle);
        GL11.glNormal3f(0.0f, 0.0f, 1.0f); 
        GL11.glBegin(GL11.GL_QUADS);
        {
	        GL11.glTexCoord2f(0f, 0f);
	        GL11.glVertex3f( (float)x, (float)y, (float)0);
	        GL11.glTexCoord2f(1f, 0f);
	        GL11.glVertex3f( (float)x+w, (float)y, (float)0);
	        GL11.glTexCoord2f(1f, 1f);
	        GL11.glVertex3f( (float)x+w, (float)y+h, (float)0);
	        GL11.glTexCoord2f(0f, 1f);
	        GL11.glVertex3f( (float)x, (float)y+h, (float)0);
        }
        GL11.glEnd();
        GL11.glPopAttrib();
    }

    public static InputStream getInputStream(String filename) 
	{
    	InputStream file = null;
    	
    	try 
		{
    		file = new FileInputStream(filename);
    	}
    	catch (IOException ioe) 
		{
    		ioe.printStackTrace(System.out);
    		if(file != null) 
			{
    			try 
				{
    				file.close();
    			}
    			catch (Exception e) {}
    			file = null;
    		}
    	}
    	catch (Exception e) 
		{
    		e.printStackTrace(System.out);
    	}
    	
    	if(file == null && rootClass != null) 
		{
    		URL loc = null;
    		if(filename.startsWith(".")) {   
    			filename = filename.substring(1);
    		}
    		try 
			{
				loc = rootClass.getResource(filename);
			}
    		catch (Exception ue) 
			{
				ue.printStackTrace(System.out);
			}
    		
    		if(loc != null) 
			{
    			try 
				{
    				file = loc.openStream();
    			}
    			catch (Exception e) 
				{
    				e.printStackTrace(System.out);
    			}
    		}
    		
    		
    		if(file == null && baseURL != null) 
			{
    			try 
				{
					loc = new URL(baseURL, filename);
				}
    			catch (Exception ue) 
				{
					ue.printStackTrace(System.out);
				}
    			
    			try 
				{
    				file = loc.openStream();
    			}
    			catch (Exception e) 
				{
    				e.printStackTrace(System.out);
    			}
    		}
    	}
		
    	return file;
    }

    public static byte[] getBytesFromStream(InputStream file) 
	{
    	int chunkSize = 1024;
		int num = 0;
    	int read = 0;
		int parsed = 0;
		
    	byte[] bytes = new byte[chunkSize];
    	ArrayList byteChunks = new ArrayList();

    	try 
		{
    		while (file.read(bytes) >= 0) 
			{
				num = file.read(bytes);
    			byteChunks.add(bytes);
    			bytes = new byte[chunkSize];
    			read += num;
    		}
    	}
    	catch (IOException ioe) 
		{
    		ioe.printStackTrace(System.out);
    	}

    	bytes = new byte[read];

    	while (byteChunks.size() > 0) {
    		byte[] byteChunk = (byte[]) byteChunks.get(0);
    		int copy = (read - parsed > chunkSize)? chunkSize : (read - parsed);
    		System.arraycopy(byteChunk, 0, bytes, parsed, copy);
    		byteChunks.remove(0);
    		parsed += copy;
    	}

    	return bytes;
    }

    public static byte[] getBytesFromFile(String filename) 
	{
    	InputStream file = getInputStream(filename);
    	byte[] bytes = getBytesFromStream(file);
		
    	try 
		{
    		file.close();
    	}
    	catch (IOException ioe) 
		{
    		ioe.printStackTrace(System.out);
    	}
		
    	return bytes;
    }

    public static String[] getPathAndFile(String filename) 
	{
    	String[] pathAndFile = new String[2];
    	Matcher matcher = Pattern.compile("^.*/").matcher(filename);
		
    	if(matcher.find()) 
		{
    		pathAndFile[0] = matcher.group();
    		pathAndFile[1] = filename.substring(matcher.end());
    	}
    	else 
		{
    		pathAndFile[0] = "";
    		pathAndFile[1] = filename;
    	}
		
    	return pathAndFile;
    }
	
	public static ByteBuffer allocBytes(int num) 
	{
    	return ByteBuffer.allocateDirect(num * SIZE_BYTE).order(ByteOrder.nativeOrder());
    }

    public static IntBuffer allocInts(int num) 
	{
    	return ByteBuffer.allocateDirect(num * SIZE_INT).order(ByteOrder.nativeOrder()).asIntBuffer();
    }

    public static FloatBuffer allocFloats(int num) 
	{
    	return ByteBuffer.allocateDirect(num * SIZE_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public static ByteBuffer allocBytes(byte[] bytearray) 
	{
    	ByteBuffer buffer = ByteBuffer.allocateDirect(bytearray.length * SIZE_BYTE).order(ByteOrder.nativeOrder());
		
    	buffer.put(bytearray).flip();
		
    	return buffer;
    }

    public static IntBuffer allocInts(int[] intarray) 
	{
    	IntBuffer buffer = ByteBuffer.allocateDirect(intarray.length * SIZE_FLOAT).order(ByteOrder.nativeOrder()).asIntBuffer();
		
    	buffer.put(intarray).flip();
		
    	return buffer;
    }

    public static FloatBuffer allocFloats(float[] floatarray) 
	{
    	FloatBuffer buffer = ByteBuffer.allocateDirect(floatarray.length * SIZE_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		
    	buffer.put(floatarray).flip();
		
    	return buffer;
    }

    public static void put(ByteBuffer buffer, byte[] values) 
	{
    	buffer.clear();
    	buffer.put(values).flip();
    }

    public static void put(IntBuffer buffer, int[] values) 
	{
    	buffer.clear();
    	buffer.put(values).flip();
    }

    public static void put(FloatBuffer buffer, float[] values) 
	{
    	buffer.clear();
    	buffer.put(values).flip();
    }
	
	public void handleExit() 
	{
        while (Keyboard.next())
		{
            if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
                exit = true;
        }
    }

	public void cleanup() 
	{
        Keyboard.destroy();
        Display.destroy();  
    }
	
    public void exit() 
	{
        exit = true;
    }
}