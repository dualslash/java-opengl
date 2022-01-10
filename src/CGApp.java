import java.io.*;
import java.util.List;
import java.util.ArrayList;

import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;
import org.lwjgl.util.vector.*;
import org.lwjgl.Sys;

import glapp.*;
import glmodel.*;
import glsound.*;
import globject.*;

public class CGApp extends GLApp 
{
	static int width = displayWidth;
	static int height = displayHeight;
	public static int[] center = {0, 0};
	
	public static boolean full = true;
	public static boolean sync = false;
	public static boolean mute = false;

	public static float lightDirection[] = {-1f, 2f, -2f, 0f};
	public static float[] cameraPosition = {5.5f, 8.0f, 100.0f};
	public static float[] cameraTarget = {0.0f, 0.0f, 0.0f};
	public static float[] cameraOrientation = {0.0f, 180.0f};
	
	public static List<Projectile> projectileList = new ArrayList<Projectile>();
	public static List<Particle> particleList = new ArrayList<Particle>();

	public static GLModel object[] = {null, null, null, null};
	public static GLImage texture[] = {null, null};
	public static int textureHandle[] = {0, 0};
	public static int soundHandle[] = {0, 0, 0};
	public static float controller[] = {0, 0};
	
   public static void main(String args[]) 
	{
		CGApp app = new CGApp();
		app.windowTitle = "Java OpenGL";
		app.displayWidth = width;
		app.displayHeight = height;
		app.fullScreen = full;
		app.vSync = sync;
		
		try
		{
			app.run();
		}
		catch (Exception e) 
		{
            e.printStackTrace(System.out);
		}
    }

    public void setup()
    {
		bindMouse();
		initGLS();
		setPerspective();
		initLight();
		initGraphics();
		initAudio();
	}
	
	public static void bindMouse()
	{
		center[0] = displayWidth/2;
		center[1] = displayHeight/2;
		
		Mouse.setCursorPosition(center[0], center[1]);
		Mouse.setGrabbed(true);
	}
	
	public static void initGLS()
	{
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glClearColor(.3f,.7f,.9f,1f);
		GL11.glClearDepth(1.0f);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT,GL11.GL_NICEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public static void initLight()
	{
		setLight( GL11.GL_LIGHT1,
					  new float[] { 1f,  1f,  1f,  1f },
					  new float[] { .6f, .6f, .6f, .6f },
					  new float[] { 1f,  1f,  1f,  1f },
					  lightDirection );
		
        setAmbientLight(new float[] { .1f, .1f, .1f, .1f });
	}
	
	public static void initGraphics()
	{	
		object[0] = new GLModel("dat/models/scene/Island.obj");
		object[0].regenerateNormals();
		object[1] = new GLModel("dat/models/scene/Sea.obj");
		object[1].regenerateNormals();
		object[2] = new GLModel("dat/models/object/Pirate Ship.obj");
		object[2].regenerateNormals();
		object[3] = new GLModel("dat/models/object/makar.obj");
		object[3].regenerateNormals();
		
		texture[0] = loadImage("dat/images/smoke1.png");
		texture[1] = loadImage("dat/images/smoke2.png");
		textureHandle[0] = makeTexture(texture[0]);
		textureHandle[1] = makeTexture(texture[1]);
	}
	
	public static void initAudio()
	{
		SoundScape.create();
		SoundScape.setReferenceDistance(60.00f);
		
		int bgm = SoundScape.loadSoundData("dat/audio/bgm.wav");
		int can = SoundScape.loadSoundData("dat/audio/cannon.wav");
		int exp = SoundScape.loadSoundData("dat/audio/explosion.wav");
		
		soundHandle[0] = SoundScape.makeSoundSource( bgm );
		soundHandle[1] = SoundScape.makeSoundSource( can );
		soundHandle[2] = SoundScape.makeSoundSource( exp );
		
		SoundScape.setSoundPosition(soundHandle[0],cameraPosition[0],cameraPosition[1],cameraPosition[2]);
		SoundScape.setSoundPosition(soundHandle[1],12.8f,4.0f,84.0f);
		SoundScape.setLoop(soundHandle[0],true);
		SoundScape.setGain(soundHandle[0], 0.25f);
		
		if(!mute)
			SoundScape.play(soundHandle[0]);
	}

    public void render() 
	{
		updateLogic();
		updateAudio();
		updateCamera();
		renderScene();
		renderDynamic();
        setLightPosition(GL11.GL_LIGHT1, lightDirection);
    }
	
	public static void renderDynamic()
	{
		for (int i = 0; i < particleList.size(); i++)
		{
			if(particleList.get(i).getTex() == 1)
				particleList.get(i).draw(cameraPosition[0],cameraPosition[1],cameraPosition[2],textureHandle[0]);
			if(particleList.get(i).getTex() == 2)
				particleList.get(i).draw(cameraPosition[0],cameraPosition[1],cameraPosition[2],textureHandle[1]);
		}
		
		for (int i = 0; i < projectileList.size(); i++)
			projectileList.get(i).draw();
	}
	
	public static void renderScene()
	{
		controller[1] += 0.001f;
	
		GL11.glPushMatrix();
        {
			GL11.glScalef(0.01f,0.01f,0.01f);
            object[0].render();
        }
        GL11.glPopMatrix();
		
		GL11.glPushMatrix();
        {
			GL11.glTranslatef(0f,(float)Math.sin(controller[1]) * 0.10f + 0.10f,0f);
			GL11.glScalef(0.01f,0.01f,0.01f);
			object[1].render();
        }
        GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(20f,4f,80f);
			GL11.glScalef(0.004f,0.004f,0.004f);
			object[2].render();
		}
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(-16.35f, 1.68f, 122.5f);
			GL11.glRotatef(150f,0f,1f,0f);
			GL11.glScalef(1.60f,1.60f,1.60f);
			object[3].render();
		}
		GL11.glPopMatrix();
		GL11.glColor4f(1f,1f,1f,1f);
	}
	
	public static void updateCamera()
	{
		float dx = (float)Math.abs((center[0] - Mouse.getX())*0.05f);
		float dy = (float)Math.abs((center[1] - Mouse.getY())*0.05f);
		
		if(Mouse.getX() > center[0])
		{
			cameraOrientation[0] -= dx;
			if(cameraOrientation[0] < 0.0f) 
				cameraOrientation[0] += 360.0f;
		}
		
		if( Mouse.getX() < center[0])
		{
			cameraOrientation[0] += dx;
			if (cameraOrientation[0] > 360.0f)		
				cameraOrientation[0] -= 360.0f;
		}
		
		if(Mouse.getY() > center[1])
		{
			cameraOrientation[1] += dy;
			if ( cameraOrientation[1] > 240.0f )
			cameraOrientation[1] = 240.0f;
		}
		
		if(Mouse.getY() < center[1])
		{
			cameraOrientation[1] -= dy;
			if ( cameraOrientation[1] < 120.0f )
			cameraOrientation[1] = 120.0f;
		}
		
		Mouse.setCursorPosition(center[0], center[1]);
        handleCameraInput();
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
		
		cameraTarget[0] = cameraPosition[0] + (float)Math.sin(cameraOrientation[0] * Math.PI/180);
		cameraTarget[1] = cameraPosition[1] + (float)Math.tan(cameraOrientation[1] * Math.PI/180);
		cameraTarget[2] = cameraPosition[2] + (float)Math.cos(cameraOrientation[0] * Math.PI/180);
		
        GLU.gluLookAt(cameraPosition[0], cameraPosition[1], cameraPosition[2],
							   cameraTarget[0], cameraTarget[1], cameraTarget[2],
							   0.0f, 1.0f, 0.0f);	
	}
	
	public static void updateAudio()
	{
		SoundScape.setListenerPosition(cameraPosition[0],cameraPosition[1],cameraPosition[2]);
		SoundScape.setListenerOrientation(cameraTarget[0], cameraTarget[1], cameraTarget[2],0f, 1f, 0f);
		SoundScape.setSoundPosition(soundHandle[0], cameraPosition[0],cameraPosition[1],cameraPosition[2]);
	}
	
	public static void updateLogic()
	{
		handleInput();
		
		for (int i = 0; i < particleList.size(); i++)
			particleList.get(i).update(particleList);
			
		for (int i = 0; i < projectileList.size(); i++)
			projectileList.get(i).update(particleList,projectileList,soundHandle[2]);
	}

    public static void setPerspective()
    {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(40f, aspectRatio, 0.1f, 500f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    public static void handleCameraInput()
    {
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) 
		{
            cameraPosition[0] += (float) Math.sin((cameraOrientation[0] + 90) * Math.PI/180) * 0.3f;
            cameraPosition[2] += (float) Math.cos((cameraOrientation[0] + 90) * Math.PI/180) * 0.3f;
		}

        if (Keyboard.isKeyDown(Keyboard.KEY_D)) 
		{
            cameraPosition[0] += (float) Math.sin((cameraOrientation[0] - 90) * Math.PI/180) * 0.3f;
            cameraPosition[2] += (float) Math.cos((cameraOrientation[0] - 90) * Math.PI/180) * 0.3f;
		}

        if (Keyboard.isKeyDown(Keyboard.KEY_W)) 
		{
            cameraPosition[0] += (float) Math.sin(cameraOrientation[0] * Math.PI/180) * 0.3f;
            cameraPosition[2] += (float) Math.cos(cameraOrientation[0] * Math.PI/180) * 0.3f;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_S)) 
		{
            cameraPosition[0] -= (float) Math.sin(cameraOrientation[0] * Math.PI/180) * 0.3f;
            cameraPosition[2] -= (float) Math.cos(cameraOrientation[0] * Math.PI/180) * 0.3f;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
            cameraPosition[1] +=  .15f;

        if (Keyboard.isKeyDown(Keyboard.KEY_C))
            cameraPosition[1] -=  .15f;
    }
	
	public static void handleInput()
	{
		if(controller[0] > 0)
			controller[0]--;
	
		if(controller[0] == 0.0f)
		{
			if(Keyboard.isKeyDown(Keyboard.KEY_Q)) 
			{
				System.out.print("\n Direction: " + cameraPosition[0] + " " + cameraPosition[1] + " " + cameraPosition[2] + "\n" +
										  "Orientation: " + cameraOrientation[0] + " " + cameraOrientation[1] + "\n \n");
				controller[0] = 200.0f;
			}
			
			if(Keyboard.isKeyDown(Keyboard.KEY_M))
			{
				if(!mute)
				{
					SoundScape.stop(soundHandle[0]);
					mute = !mute;
				}
				else
				{
					SoundScape.play(soundHandle[0]);
					mute = !mute;
				}
				
				controller[0] = 200.0f;
			}
			
			if(Keyboard.isKeyDown(Keyboard.KEY_1))
			{
				SoundScape.play(soundHandle[1]);
				Projectile obj = new Projectile(12.8f,4.0f,84.0f,getRandom(-2,4,0.01f),getRandom(15,25,0.01f),getRandom(45,55,0.01f),1.0f,0,1,true);
				projectileList.add(obj);
				obj = null;
				
				for(int i = 0; i < 8; i++)
				{
					Particle par = new Particle(12.6f + 0.5f * (float)Math.cos(i * 45 * Math.PI/180),
															4.4f + 0.5f * (float)Math.sin(i * 45 * Math.PI/180),
															85.0f,
															0f,0f,0f,
															getRandom(50,60,0.01f),getRandom(20,60, 0.00005f),
															0f);
					particleList.add(par);
					par = null;
				}
				
				controller[0] = 200.0f;
			}
			
			if(Keyboard.isKeyDown(Keyboard.KEY_2))
			{
				SoundScape.play(soundHandle[1]);
				Projectile obj = new Projectile(12.8f,4.0f,84.0f,getRandom(-2,4,0.01f),getRandom(15,25,0.01f),getRandom(45,55,0.01f),1.0f,3,2,true);
				projectileList.add(obj);
				obj = null;
				controller[0] = 200.0f;
				
				for(int i = 0; i < 8; i++)
				{
					Particle par = new Particle(12.6f + 0.5f * (float)Math.cos(i * 45 * Math.PI/180),
															4.4f + 0.5f * (float)Math.sin(i * 45 * Math.PI/180),
															85.0f,
															0f,0f,0f,
															getRandom(50,60,0.01f),getRandom(20,60, 0.00005f),
															0f);
					particleList.add(par);
					par = null;
				}
				
				controller[0] = 200.0f;
			}
		}
	}
	
	public static float getRandom(int min, int max, float scale)
	{
		return (min + (int)(Math.random() * ((max - min) + 1))) * scale;
	}
	
	public void cleanup() 
	{
        super.cleanup();
        SoundScape.destroy();
    }
}