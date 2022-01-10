package globject;

import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;
import org.lwjgl.util.vector.*;
import org.lwjgl.Sys;
import glapp.*;
import glmodel.*;
import glsound.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Projectile extends GLApp 
{
	float[] position = {0,0,0};
	float[] velocity = {0,0,0};
	
	int type = 0;
	boolean visible = true;
	float scale = 1.0f;
	int fract = 0;
	float timer = 200;
	
	GLModel object;

	public Projectile(float x, float y, float z, float vx, float vy, float vz, float s, int f, int t, boolean v)
	{
		position[0] = x;
		position[1] = y;
		position[2] = z;
		
		velocity[0] = vx;
		velocity[1] = vy;
		velocity[2] = vz;
		
		type = t;
		visible = v;
		scale = s;
		fract = f;
		
		if(visible)
		{
			object = new GLModel("dat/models/object/Bomb.obj");
			object.regenerateNormals();
		}
	}
	
	public void update(List<Particle> particleList, List<Projectile> projectileList, int sound)
	{
		position[0] += velocity[0];
		position[1] += velocity[1];
		position[2] += velocity[2];
		
		if(visible)
			velocity[1] -= 0.001f;
		else
		{
			velocity[1] -= 0.002f;
			scale -= 0.020f;
		}
		
		timer--;
		
		if(timer % 4 == 0)
		{
			Particle obj = new Particle(position[0],
										position[1]+1f,
										position[2],
										0f,0f,0f,
										scale/4,getRandom(20,60,0.0001f),
										0f);
			particleList.add(obj);
			obj = null;
		}
		
		if(timer <= 0  || scale <= 0)
		{
			if(type == 0)
				projectileList.remove(this);
				
			if(type == 1)
			{
				explodeA(particleList,projectileList);
					SoundScape.setSoundPosition(sound,position[0],position[1],position[2]);
					SoundScape.play(sound);
			}
			
			if(type == 2)
			{
				explodeB(particleList,projectileList);
					SoundScape.setSoundPosition(sound,position[0],position[1],position[2]);
					SoundScape.play(sound);
			}
		}
	}
	
	public void draw()
	{
		if(visible)
		{
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(position[0], position[1], position[2]);
				GL11.glScalef(0.10f * scale,0.10f * scale,0.10f * scale);
				object.render();
			}
			GL11.glPopMatrix();
		}
	}
	
	public float getRandom(int min, int max, float scale)
	{
		return (min + (int)(Math.random() * ((max - min) + 1))) * scale;
	}
	
	public void explodeA(List<Particle> particleList, List<Projectile> projectileList)
	{
		int count = 20;
	
		for(int i = 0; i < count; i++)
		{
			Particle obj = new Particle(position[0] + 2 * (float)Math.cos(i * 360/count * Math.PI/180),
												    position[1]+1f,
													position[2] + 2 * (float)Math.sin(i * 360/count * Math.PI/180),
													0f,0f,0f,
													getRandom(90,110,0.01f),getRandom(20,60, 0.0001f),
													0f);
			particleList.add(obj);
			obj = null;
		}
		
		for(int i = 0; i < count; i++)
		{
			Particle obj = new Particle(position[0] + 3 * (float)Math.cos(i * 360/count * Math.PI/180),
													position[1],
													position[2] + 3 * (float)Math.sin(i * 360/count * Math.PI/180),
													0f,0f,0f,
													getRandom(90,110,0.01f),getRandom(20,60, 0.0001f),
													0f);
			particleList.add(obj);
			obj = null;
		}
		
		for(int i = 0; i < count; i++)
		{
			Particle obj = new Particle(position[0] + 2 * (float)Math.cos(i * 360/count * Math.PI/180),
													position[1]-1f,
													position[2] + 2 * (float)Math.sin(i * 360/count * Math.PI/180),
													0f,0f,0f,
													getRandom(90,110,0.01f),getRandom(20,60, 0.0001f),
													0f);
			particleList.add(obj);
			obj = null;
		}
		
		for(int i = 0; i < 3; i++)
		{			
			Projectile obj = new Projectile(position[0],position[1],position[2],
			getRandom(-100,100,0.001f),
			getRandom(200,400,0.001f),
			getRandom(-100,100,0.001f),
			6.0f,0,0,false);
			projectileList.add(obj);
			obj = null;
		}
		
		projectileList.remove(this);
	}
	
	public void explodeB(List<Particle> particleList, List<Projectile> projectileList)
	{
		int count = 20;
		
		for(int i = 0; i < count; i++)
		{
			Particle obj = new Particle(position[0] + 2 * (float)Math.cos(i * 360/count * Math.PI/180),
												    position[1]+1f,
													position[2] + 2 * (float)Math.sin(i * 360/count * Math.PI/180),
													0f,0f,0f,
													getRandom(90,110,0.01f),getRandom(20,60, 0.0001f),
													0f);
			particleList.add(obj);
			obj = null;
		}
		
		for(int i = 0; i < count; i++)
		{
			Particle obj = new Particle(position[0] + 3 * (float)Math.cos(i * 360/count * Math.PI/180),
													position[1],
													position[2] + 3 * (float)Math.sin(i * 360/count * Math.PI/180),
													0f,0f,0f,
													getRandom(90,110,0.01f),getRandom(20,60, 0.0001f),
													0f);
			particleList.add(obj);
			obj = null;
		}
		
		for(int i = 0; i < count; i++)
		{
			Particle obj = new Particle(position[0] + 2 * (float)Math.cos(i * 360/count * Math.PI/180),
													position[1]-1f,
													position[2] + 2 * (float)Math.sin(i * 360/count * Math.PI/180),
													0f,0f,0f,
													getRandom(90,110,0.01f),getRandom(20,60, 0.0001f),
													0f);
			particleList.add(obj);
			obj = null;
		}
		
		for(int i = 0; i < fract; i++)
		{			
			Projectile obj = new Projectile(position[0],position[1],position[2],
															getRandom(-100,100,0.001f),
															getRandom(100,200,0.001f),
															getRandom(-100,100,0.001f),
															1.0f,0,1,true);
			projectileList.add(obj);
			obj = null;
		}
		
		projectileList.remove(this);
	}
}