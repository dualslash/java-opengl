package globject;

import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;
import org.lwjgl.util.vector.*;
import org.lwjgl.Sys;
import glapp.*;
import glmodel.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Particle extends GLApp 
{
	float[] position = {0,0,0};
	float[] velocity = {0,0,0};
	
	float scale = 1;
	float scaleVelocity = 0;
	
	float rotationVelocity = 0;
	float rotation = 0;
	
	int texture = 0;

	public Particle(float x, float y, float z, float vx, float vy, float vz, float s, float sv, float r)
	{
		position[0] = x;
		position[1] = y;
		position[2] = z;
		
		velocity[0] = vx;
		velocity[1] = vy;
		velocity[2] = vz;
		
		scale = s;
		scaleVelocity = sv;
		
		rotationVelocity = r;
		texture = 1 + (int)(Math.random() * ((2 - 1) + 1));
	}
	
	public void update(List<Particle> particleList)
	{
		position[0] += velocity[0];
		position[1] += velocity[1];
		position[2] += velocity[2];
		rotation += rotationVelocity;
		
		scale -= scaleVelocity;
		
		if(scale < 0)
			particleList.remove(this);
			
		rotation += 1.00f;
	}
	
	public void draw(float cameraX, float cameraY, float cameraZ, int texture)
	{
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(position[0], position[1], position[2]);
			GL11.glRotatef((float)(Math.atan2((cameraX - position[0]),(cameraZ - position[2])) * 180 / Math.PI),0f,1f,0f);
			GL11.glRotatef(rotation,0f,0f,1f);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
			GL11.glScalef(scale,scale,0);
			renderCube();
		}
		GL11.glPopMatrix();
		GL11.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		GL11.glColor4f(1f,1f,1f,1f);
	}
	
	public static void renderCube()
    {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f( 0.0f, 0.0f, 1.0f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-1.0f, -1.0f,  1.0f);	
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( 1.0f, -1.0f,  1.0f);	
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( 1.0f,  1.0f,  1.0f);	
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-1.0f,  1.0f,  1.0f);	
        
        GL11.glNormal3f( 0.0f, 0.0f, -1.0f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-1.0f, -1.0f, -1.0f);	
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-1.0f,  1.0f, -1.0f);	
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( 1.0f,  1.0f, -1.0f);	
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( 1.0f, -1.0f, -1.0f);	
        
        GL11.glNormal3f( 0.0f, 1.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-1.0f,  1.0f, -1.0f);	
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-1.0f,  1.0f,  1.0f);	
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( 1.0f,  1.0f,  1.0f);	
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( 1.0f,  1.0f, -1.0f);	
        
        GL11.glNormal3f( 0.0f, -1.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-1.0f, -1.0f, -1.0f);	
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( 1.0f, -1.0f, -1.0f);	
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( 1.0f, -1.0f,  1.0f);	
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-1.0f, -1.0f,  1.0f);	
        
        GL11.glNormal3f( 1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( 1.0f, -1.0f, -1.0f);	
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( 1.0f,  1.0f, -1.0f);	
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( 1.0f,  1.0f,  1.0f);	
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( 1.0f, -1.0f,  1.0f);	
        
        GL11.glNormal3f( -1.0f, 0.0f, 0.0f);
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-1.0f, -1.0f, -1.0f);	
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-1.0f, -1.0f,  1.0f);	
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-1.0f,  1.0f,  1.0f);	
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-1.0f,  1.0f, -1.0f);	
        GL11.glEnd();
    }
	
	public int getTex()
	{
		return texture;
	}

}