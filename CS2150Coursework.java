/* CS2150Coursework.java
 * 
 * Jordan Lees
 * 
 * 
 * This is entirely my own work, except for textures, which I downloaded and then resized appropriately.
 * 	Sun texture from: http://en.pudn.com/Download/item/id/836090.html
 * 	Earth and moon textures by James Hastings-Trew, available at: http://planetpixelemporium.com/earth.html
 * 	Arboretum texture by Textures.com, available at: https://www.textures.com/download/windowsblocks0040/39818
 * 	Space texture by César Vonc, available at: https://www.textures.com/download/skies0258/17438?q=stars
 *
 * Scene Graph:
 *  Scene origin
 *  |
 *	+-- [ S(sceneScale, sceneScale, sceneScale) R(sceneOrientation, 0, 1, 0) ] Sun
 *		| 
 *		+-- [ S(11, 11, 11) ] Skybox
 *		|
 *		+-- [ R(45, -0.5, -0.5, -0.5) T(5, 0, 5) R(earthOrbitalPosition, 0, 1, 0) ] Earth
 *			|
 *			+-- [ R(45, -0.5, -0.5, -0.5) T(2, 0, 2) R(moonOrbitalPosition, 0, 1, 0) R(50, 0.5, 0.5, 0.5) ] Moon
 *				|
 *				+-- [ S(0.04, 0.05, 0.04) R(10, 1, 0, 0) T(0.5, 0, 0.7) R(stationObitalPosition, 0, 1, 0) R(-30, -0.5, -0.5, -0.5) ] Station
 */
package coursework.leesja;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.Sphere;

import org.newdawn.slick.opengl.Texture;
import GraphicsLab.*;

/**
 * Inspired by the Talos I station from the game Prey (2017), this scene features a space station orbiting the moon,
 * with the moon orbiting the Earth and the Earth orbiting the Sun. All stellar bodies rotate on their own axis,
 * and orientation and orbit are to scale (e.g. in the time it takes Earth to rotate once on its axis, the Moon will 
 * have completed 1/27.5 of its orbit around the Earth and 1/27 of its axial rotation).
 * 
 * Talos I features functioning lights on its exterior and a rotating ring.
 * 
 * Animations make use of the animation scale set by the main method.
 *
 * <p>Controls:
 * <ul>
 * <li>Press the escape key to exit the application.
 * <li>Hold the x, y and z keys to view the scene along the x, y and z axis, respectively
 * <li>While viewing the scene along the x, y or z axis, use the up and down cursor keys
 *      to increase or decrease the viewpoint's distance from the scene origin
 *      
 * <li>Press L to toggle the red lights on the station 
 * <li>Use LEFT and RIGHT arrow keys to rotate around the scene
 * <li>Use UP and DOWN arrow keys to zoom in and out
 * <li>Press SPACE to reset the animation
 * </ul>
 *
 */
public class CS2150Coursework extends GraphicsLab
{
	// boolean represents whether exterior lights are on or off
	private boolean lightsOn;
	
	// scene scale and orientation fields control zoom and rotation based on user input
	private float sceneScale;
	private float sceneOrientation;
	
	// properties required for rotating sun
	private float sunRadius;
	private Texture sunTexture;
	private float sunOrientation;
	private float sunRotationPeriod;
	
	// properties required for rotating and orbiting earth
	private float earthRadius;
	private Texture earthTexture;
	private float earthOrientation;
	private float earthRotationPeriod;
	private float earthOrbitalPeriod;
	private float earthOrbitalPosition;
	
	// properties required for rotating and orbiting moon
	private float moonRadius;
	private Texture moonTexture;
	private float moonOrientation;
	private float moonRotationPeriod;
	private float moonOrbitalPeriod;
	private float moonOrbitalPosition;
	
	// rings rotate around station
	private float ringsOrientation;
	
	// properties for rotating station
	private float stationOrientation;
	private float stationRotationPeriod;
	private float stationOrbitalPeriod;
	private float stationOrbitalPosition;
	
	// texture for station arboretum
	private Texture arboretumTexture;
	
	// texture for space skybox
	private Texture spaceTexture;
	
	// emission values (for light sources)
	private final float sunEmission[] = {0.98f, 0.83f, 0.25f, 1.0f};
	private final float moonEmission[] = {0.12f, 0.12f, 0.12f, 1.0f};
	private final float redEmission[] = {1.0f, 0f, 0f, 1.0f};
	private final float noEmission[] = {0f, 0f, 0f, 1.0f};
	
	private float lightsEmission[];
	
	// material values for station exterior lights
	private final float lightsAmbient[] = {0.1f, 0.1f, 0.1f, 1.0f};
	private final float lightsDiffuse[] = {0.3f, 0, 0, 1.0f};
	private final float lightsSpecular[] = {0.6f, 0.5f, 0.5f, 1.0f};
	private final float lightsShininess = 100f;
	
	// material values for earth
	private final float earthAmbient[] = {0.2f, 0.2f, 0.2f, 1.0f};
	private final float earthDiffuse[] = {0.5f, 0.6f, 0.6f, 1.0f};
	private final float earthSpecular[] = {0.1f, 0.1f, 0.1f, 1.0f};
	private final float earthShininess = 50f;
	
	// material values for rotating rings around station
	private final float ringsAmbient[] = {0.1f, 0.1f, 0.1f, 1.0f};
	private final float ringsDiffuse[] = {0.854f, 0.647f, 0.125f, 1.0f};
	private final float ringsSpecular[] = {0.98f, 0.98f, 0.82f, 1.0f};
	private final float ringsShininess = 75f;
	
	// material values for station hull
	private final float stationAmbient[] = {0.1f, 0.1f, 0.1f, 1.0f};
	private final float stationDiffuse[] = {0.30f, 0.25f, 0.25f, 1.0f};
	private final float stationSpecular[] = {0.6f, 0.6f, 0.6f, 1.0f};
	private final float stationShininess = 75f;
	
	// ids for display lists
	private final int hullList = 1;
	private final int endPieceList = 2;
	private final int arboretumList = 3;
	private final int skyboxList = 4;
	
	
   
    public static void main(String args[])
    {   new CS2150Coursework().run(WINDOWED,"Prey - Talos I",1f);
    }

    
    /**
     * Set up the scene, load textures and initialise variables
     */
    protected void initScene() throws Exception
    {
    	sceneScale = 0.7f;
    	sceneOrientation = -90.0f;
    	
    	lightsOn = false;
    	lightsEmission = noEmission;
    	
    	sunTexture = loadTexture("coursework/leesja/textures/sun.bmp");
    	earthTexture = loadTexture("coursework/leesja/textures/earth.bmp");
    	moonTexture = loadTexture("coursework/leesja/textures/moon.bmp");
    	arboretumTexture = loadTexture("coursework/leesja/textures/arboretum.bmp");
    	spaceTexture = loadTexture("coursework/leesja/textures/space.bmp");
    	
    	sunRadius = 2.0f;
    	sunOrientation = 0.0f;
    	sunRotationPeriod = 24;
    	
    	earthRadius = 1.0f;
    	earthOrientation = 0.0f;
    	earthRotationPeriod = 1;
    	earthOrbitalPeriod = 365;
    	earthOrbitalPosition = 0;
    	
    	moonRadius = 0.4f;
    	moonOrientation = 0.0f;
    	moonRotationPeriod = 27;
    	moonOrbitalPeriod = 27.5f;
    	moonOrbitalPosition = 0;
    	
    	ringsOrientation = 0;
    	
    	stationOrientation = 0.0f;
    	stationRotationPeriod = 20;
    	stationOrbitalPeriod = 7;
    	stationOrbitalPosition = 0;
    	
    	// create display lists for static objects
    	GL11.glNewList(hullList,GL11.GL_COMPILE); 
    	{   
    		drawUnitHull();
        }
        GL11.glEndList();
        
        GL11.glNewList(endPieceList, GL11.GL_COMPILE); 
        {	
        	drawUnitEndPiece();        
        }
        GL11.glEndList();
        
        GL11.glNewList(arboretumList, GL11.GL_COMPILE); 
        {
        	drawArboretum();
        }
        GL11.glEndList();
        
        GL11.glNewList(skyboxList,  GL11.GL_COMPILE); 
        {
        	drawSkybox();
        }
        GL11.glEndList();
    	
    	
    	// set up a dim global ambient light
    	float globalAmbient[] = {0.1f, 0.1f, 0.1f, 1.0f};
    	GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT,FloatBuffer.wrap(globalAmbient));
    	
    	// define and set colour and position values for the light source representing the Sun
    	float sunAmbient[] = {0.12f, 0.1f, 0.1f, 1.0f};
    	float sunDiffuse[] = {0.98f, 0.83f, 0.25f, 1.0f};
    	float sunPosition[] = {0.0f, 0.0f, 0.0f, 1.0f};
    	
    	GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, FloatBuffer.wrap(sunAmbient));
    	GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, FloatBuffer.wrap(sunDiffuse));
    	GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, FloatBuffer.wrap(sunDiffuse));
    	GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, FloatBuffer.wrap(sunPosition));
    	
    	// enable lighting
    	GL11.glEnable(GL11.GL_LIGHTING);
    	GL11.glEnable(GL11.GL_LIGHT0);
    	
    	// Phong shading interpolates normals to make lighting more realistic
    	GL11.glShadeModel(GL11.GL_SMOOTH);
    	
    	GL11.glEnable(GL11.GL_NORMALIZE);
    }
    
    /**
     * Reset changing variables to initial values, resetting all objects to start positions
     */
    protected void resetScene() {
    	sceneScale = 0.7f;
    	sceneOrientation = -90.0f;
    	
    	lightsOn = false;
    	lightsEmission = noEmission;
    	
    	sunOrientation = 0.0f;
    	earthOrientation = 0.0f;
    	moonOrientation = 0.0f;
    	ringsOrientation = 0.0f;
    	stationOrientation = 0.0f;
    	
    	earthOrbitalPosition = 0;
    	moonOrbitalPosition = 0;
    	stationOrbitalPosition = 0;
    }
    
    /**
     * Check for user input from keyboard
     */
    protected void checkSceneInput()
    {
    	// if L key is pressed, toggle station lights
    	if(Keyboard.isKeyDown(Keyboard.KEY_L)) {
    		toggleLights();
        }
    	
    	// check keys for zooming in and out of scene
    	if(Keyboard.isKeyDown(Keyboard.KEY_UP) && sceneScale < 2.0f) {
    		sceneScale += (0.01f);
        }
    	else if(Keyboard.isKeyDown(Keyboard.KEY_DOWN) && sceneScale > 0.01f) {
    		sceneScale -= (0.01f);
        }
    	
    	// check keys for rotating the scene right and left
    	if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
    		sceneOrientation += 0.1f;
        }
    	else if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
    		sceneOrientation -= 0.1f;
        }
    	
    	// check key to reset scene
    	if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
    		resetScene();
    	}
    }
    
    /**
     * Make necessary changes to animation variables between frames
     */
    protected void updateScene()
    {
        // if lights are turned on, set them to emit red
    	if (lightsOn) {
    		lightsEmission = redEmission;
    	}
    	else {
    		lightsEmission = noEmission;
    	}
    	
    	// update angular positions of orbiting bodies
    	earthOrbitalPosition = (earthOrbitalPosition + (1/earthOrbitalPeriod) * getAnimationScale()) % 360;
		moonOrbitalPosition = (moonOrbitalPosition + (1/moonOrbitalPeriod) * getAnimationScale()) % 360;
		stationOrbitalPosition = (stationOrbitalPosition + (1/stationOrbitalPeriod) * getAnimationScale()) % 360;
		
		// update axial orientation
		stationOrientation = (stationOrientation + (1/stationRotationPeriod) * getAnimationScale()) % 360;
    }
    
    /**
     * Draw the scene
     */
    protected void renderScene()
    {
    	
    	// transformations for whole scene: allow zooming and rotating of the scene, based on user input
    	GL11.glScalef(sceneScale, sceneScale, sceneScale);
    	GL11.glRotatef(sceneOrientation, 0, 1, 0);
    	
    	// draw the sun at the scene origin
    	GL11.glPushMatrix();
    	{
    		// emission as sun is light source
	    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, FloatBuffer.wrap(sunEmission));
	    	sunOrientation = drawRotatingTexturedBody(sunRadius, sunOrientation, sunRotationPeriod, sunTexture);
	    	GL11.glMaterial(GL11.GL_FRONT,  GL11.GL_EMISSION, FloatBuffer.wrap(noEmission));
	    	
	    	// draw the skybox surrounding the scene
	    	GL11.glPushMatrix();
	    	{
	    		GL11.glScalef(11.0f, 11.0f, 11.0f);
	    		GL11.glCallList(skyboxList);
	    	}
	    	GL11.glPopMatrix();
	    	
	    	// draw the earth orbiting the sun and rotating on its axis
	    	GL11.glPushMatrix();
	    	{
	    		// earth materials
		    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, FloatBuffer.wrap(earthAmbient));
		    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, FloatBuffer.wrap(earthDiffuse));
		    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, FloatBuffer.wrap(earthSpecular));
		    	GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, earthShininess);
	    		
	    		// move earth to current angular orbital position
	    		GL11.glRotatef((float)earthOrbitalPosition, 0f, 1f, 0f);
	    		// move earth away from sun
	    		GL11.glTranslatef(5.0f, 0f, 5.0f);
	    		// rotate earth so it spins on its axis
	    		GL11.glRotatef(45f, -0.5f, -0.5f, -0.5f);
		    	// update orientation of earth and draw it at current orientation
	    		earthOrientation = drawRotatingTexturedBody(earthRadius, earthOrientation, earthRotationPeriod, earthTexture);
	    		
		    	
		    	// draw the moon orbiting the earth and rotating on its axis
		    	GL11.glPushMatrix();
		    	{
		    		// the moon orbits the earth at a slight incline
			    	GL11.glRotatef(50f, 0.5f, 0.5f, 0.5f);
		    		// orbit around earth
			    	GL11.glRotatef((float)moonOrbitalPosition, 0f, 1f, 0f);
		    		// translate away from earth
		    		GL11.glTranslatef(2.0f, 0f, 2.0f);
		    		// rotate moon to spin on axis
		    		GL11.glRotatef(45f, -0.5f, -0.5f, -0.5f);
		    		
			    		
			    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, FloatBuffer.wrap(moonEmission));
				    moonOrientation = drawRotatingTexturedBody(moonRadius, moonOrientation, moonRotationPeriod, moonTexture);
				    GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, FloatBuffer.wrap(noEmission));
			    	
			    	// draw the space station orbiting the moon and rotating on its axis
		    		GL11.glPushMatrix();
		    		{
		    			// update angular position
		    			
		    			GL11.glRotatef(-30f, -0.5f, -0.5f, -0.5f);
		    			GL11.glRotatef((float)stationOrbitalPosition, 0f, 1f, 0f);
		    			GL11.glTranslatef(0.5f, 0f, 0.7f);
		    			GL11.glRotatef(10, 1, 0, 0);
		    			GL11.glScalef(0.04f, 0.05f, 0.04f);
		    			
		    			// draw station and update orientation
		    			drawStation();
		    		}
		    		GL11.glPopMatrix();
		    	}
		    	GL11.glPopMatrix();
	    	}
	    	GL11.glPopMatrix();
    	}
    	GL11.glPopMatrix();
    }
    
    protected void setSceneCamera()
    {
        // call the default behaviour defined in GraphicsLab. This will set a default perspective projection
        // and default camera settings ready for some custom camera positioning below...  
        super.setSceneCamera();

        GLU.gluLookAt(-7.0f, 1.0f, 7.0f, 0.0f,0.0f,0.0f, 0.0f,1.0f,0.0f);
   }

    protected void cleanupScene()
    {
    }
    
    /** 
     * Draws a textured body (e.g. a planet) rotating on a vertical axis.
     * 
     * @param radius radius of the body.
     * @param orientation current orientation of the body.
     * @param rotation angle to rotate by each frame (multiplied by animation scale)
     * @param texture
     * @return new orientation of the body
     */
    protected float drawRotatingTexturedBody(float radius, float orientation, float rotation, Texture texture) {
    	
    	GL11.glPushMatrix(); 
    	{
        	Sphere body = new Sphere();
        	body.setTextureFlag(true);
        	body.setNormals(GL11.GL_SMOOTH);         

        	GL11.glEnable(GL11.GL_TEXTURE_2D);
        	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);            
        	 
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture.getTextureID());
        	
        	// rotate the object about the y axis
        	GL11.glRotatef(((orientation += (1/rotation) * getAnimationScale())), 0f, 1f, 0f);
        	
        	body.draw(radius,30,30);
        	
        	GL11.glDisable(GL11.GL_TEXTURE_2D);
    	}
    	GL11.glPopMatrix();
    	
    	return orientation % 360;
    }
    
    /**
	 * Draw and set normals for each face of the main piece of the space station hull, a cuboid with bevelled edges.
	 * This is (mistakenly) size 2x2 (instead of 1x1), but this should make little difference.
	 * 
	 * All vertex and face numbers correspond to a vertex and face on the design sketch.
	 */
	protected void drawUnitHull() {
		Vertex v1 = new Vertex(-1f, 0.8f, 1f);
		Vertex v2 = new Vertex(-0.8f, 1f, 1f);
		Vertex v3 = new Vertex(0.8f, 1f, 1f);
		Vertex v4 = new Vertex(1f, 0.8f, 1f);
		Vertex v5 = new Vertex(1f, -0.8f, 1f);
		Vertex v6 = new Vertex(0.8f, -1f, 1f);
		Vertex v7 = new Vertex(-0.8f, -1f, 1f);
		Vertex v8 = new Vertex(-1f, -0.8f, 1f);
		Vertex v9 = new Vertex(1f, -0.8f, -1f);
		Vertex v10 = new Vertex(0.8f, -1f, -1f);
		Vertex v11 = new Vertex(-0.8f, -1f, -1f);
		Vertex v12 = new Vertex(-1f, -0.8f, -1f);
		Vertex v13 = new Vertex(-1f, 0.8f, -1f);
		Vertex v14 = new Vertex(-0.8f, 1f, -1f);
		Vertex v15 = new Vertex(0.8f, 1f, -1f);
		Vertex v16 = new Vertex(1f, 0.8f, -1f);
		
		// F1
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v3.toVector(), v2.toVector(), v6.toVector(), v7.toVector()).submit();
			
			v1.submit();
			v8.submit();
			v7.submit();
			v6.submit();
			v5.submit();
			v4.submit();
			v3.submit();
			v2.submit();
		}
		GL11.glEnd();
		
		// F2
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v4.toVector(), v5.toVector(), v9.toVector(), v16.toVector()).submit();
			
			v4.submit();
			v5.submit();
			v9.submit();
			v16.submit();
		}
		GL11.glEnd();
		
		// F3
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v3.toVector(), v4.toVector(), v16.toVector(), v15.toVector()).submit();
			
			v3.submit();
			v4.submit();
			v16.submit();
			v15.submit();
		}
		GL11.glEnd();
		
		// F4
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v2.toVector(), v3.toVector(), v15.toVector(), v14.toVector()).submit();
			
			v2.submit();
			v3.submit();
			v15.submit();
			v14.submit();
		}
		GL11.glEnd();
		
		// F5
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v13.toVector(), v12.toVector(), v8.toVector(), v1.toVector()).submit();
			
			v13.submit();
			v12.submit();
			v8.submit();
			v1.submit();			
		}
		GL11.glEnd();
		
		// F6
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v14.toVector(), v13.toVector(), v1.toVector(), v2.toVector()).submit();
			
			v14.submit();
			v13.submit();
			v1.submit();
			v2.submit();		
		}
		GL11.glEnd();
		
		// F7
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v12.toVector(), v11.toVector(), v7.toVector(), v8.toVector()).submit();
			
			v12.submit();
			v11.submit();
			v7.submit();
			v8.submit();
		}
		GL11.glEnd();
		
		// F8
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v7.toVector(), v11.toVector(), v10.toVector(), v6.toVector()).submit();
			
			v7.submit();
			v11.submit();
			v10.submit();
			v6.submit();
		}
		GL11.glEnd();
		
		// F9
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v5.toVector(), v6.toVector(), v10.toVector(), v9.toVector()).submit();
			
			v5.submit();
			v6.submit();
			v10.submit();
			v9.submit();
		}
		GL11.glEnd();
		
		// F10
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v14.toVector(), v15.toVector(), v10.toVector(), v11.toVector()).submit();
			
			v14.submit();
			v15.submit();
			v16.submit();
			v9.submit();
			v10.submit();
			v11.submit();
			v12.submit();
			v13.submit();
			
		}
		GL11.glEnd();
	}
	
	
	/** 
	 * Draw and set normals for the piece of station hull which attaches to the ends of the bevelled main piece.
	 * 2x2 in size, matching main piece.
	 * 
	 * All vertex and face numbers correspond to a vertex and face on the design sketch.
	 */
	protected void drawUnitEndPiece() {
		Vertex v1 = new Vertex(-0.8f, 0f, 1f);
		Vertex v2 = new Vertex(0.8f, 0f, 1f);
		Vertex v3 = new Vertex(1f, 0f, 0.8f);
		Vertex v4 = new Vertex(1f, 0f, -0.8f);
		Vertex v5 = new Vertex(0.8f, 0f, -1f);
		Vertex v6 = new Vertex(-0.8f, 0f, -1f);
		Vertex v7 = new Vertex(-1f, 0f, -0.8f);
		Vertex v8 = new Vertex(-1f, 0f, 0.8f);
		
		Vertex v9 = new Vertex(-0.64f, 2f, 0.8f);
		Vertex v10 = new Vertex(0.64f, 2f, 0.8f);
		Vertex v11 = new Vertex(0.8f, 2f, 0.64f);
		Vertex v12 = new Vertex(0.8f, 2f, -0.64f);
		Vertex v13 = new Vertex(0.64f, 2f, -0.8f);
		Vertex v14 = new Vertex(-0.64f, 2f, -0.8f);
		Vertex v15 = new Vertex(-0.8f, 2f, -0.64f);
		Vertex v16 = new Vertex(-0.8f, 2f, 0.64f);
		
		// F1
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v1.toVector(), v6.toVector(), v5.toVector(), v2.toVector()).submit();
			
			v1.submit();
			v8.submit();
			v7.submit();
			v6.submit();
			v5.submit();
			v4.submit();
			v3.submit();
			v2.submit();
		}
		GL11.glEnd();
		
		// F2
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v9.toVector(), v1.toVector(), v2.toVector(), v10.toVector()).submit();
			
			v9.submit();
			v1.submit();
			v2.submit();
			v10.submit();
		}
		GL11.glEnd();
		
		// F3
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v10.toVector(), v2.toVector(), v3.toVector(), v11.toVector()).submit();
			
			v10.submit();
			v2.submit();
			v3.submit();
			v11.submit();
		}
		GL11.glEnd();
		
		// F4
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v11.toVector(), v3.toVector(), v4.toVector(), v12.toVector()).submit();
			
			v11.submit();
			v3.submit();
			v4.submit();
			v12.submit();
		}
		GL11.glEnd();
		
		// F5
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v12.toVector(), v4.toVector(), v5.toVector(), v13.toVector()).submit();
			
			v12.submit();
			v4.submit();
			v5.submit();
			v13.submit();
		}
		GL11.glEnd();
		
		// F6
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v13.toVector(), v5.toVector(), v6.toVector(), v14.toVector()).submit();
			
			v13.submit();
			v5.submit();
			v6.submit();
			v14.submit();
		}
		GL11.glEnd();
		
		// F7
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v7.toVector(), v15.toVector(), v14.toVector(), v6.toVector()).submit();
			
			v7.submit();
			v15.submit();
			v14.submit();
			v6.submit();
		}
		GL11.glEnd();
		
		// F8
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v15.toVector(), v7.toVector(), v8.toVector(), v16.toVector()).submit();
			
			v15.submit();
			v7.submit();
			v8.submit();
			v16.submit();
		}
		GL11.glEnd();
		
		// F9
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v16.toVector(), v8.toVector(), v1.toVector(), v9.toVector()).submit();
			
			v16.submit();
			v8.submit();
			v1.submit();
			v9.submit();
		}
		GL11.glEnd();
		
		// F10
		GL11.glBegin(GL11.GL_POLYGON);
		{
			new Normal(v14.toVector(), v9.toVector(), v10.toVector(), v13.toVector()).submit();
			
			v14.submit();
			v15.submit();
			v16.submit();
			v9.submit();
			v10.submit();
			v11.submit();
			v12.submit();
			v13.submit();
		}
		GL11.glEnd();
		
	}
	
	/** 
	 * Draw the space station: set relevant materials, set orientation and rotate, draw components and put them together
	 */
    protected void drawStation()
    {
    	// set current orientation of station
    	GL11.glRotatef(stationOrientation, 0f, 1f, 0f);
    	
    	// set colours and draw the ring surrounding the station
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, FloatBuffer.wrap(ringsAmbient));
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, FloatBuffer.wrap(ringsDiffuse));
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, FloatBuffer.wrap(ringsSpecular));
    	GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, ringsShininess);
    	
    	GL11.glPushMatrix();
    	{
    		GL11.glRotatef(88, 1, 0, 0);
    		GL11.glTranslatef(0, 0, -1);
    		GL11.glScalef(1, 1, 0.5f);
    		ringsOrientation = drawRotatingRing(5.0f, 4.8f, ringsOrientation);
    	}
    	GL11.glPopMatrix();
    	
    	// set colours and draw the red lights on the station exterior
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, FloatBuffer.wrap(lightsAmbient));
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, FloatBuffer.wrap(lightsDiffuse));
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, FloatBuffer.wrap(lightsSpecular));
    	GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, lightsShininess);
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, FloatBuffer.wrap(lightsEmission));
    	
    	GL11.glPushMatrix();
    	{
    		GL11.glTranslatef(1.5f, 0f, 0f);
    		drawLight(0.3f);
    	}
    	GL11.glPopMatrix();
    	
    	GL11.glPushMatrix();
    	{
    		GL11.glTranslatef(-1.5f, 0f, 0f);
    		drawLight(0.3f);
    	}
    	GL11.glPopMatrix();
    	
    	GL11.glPushMatrix();
    	{
    		GL11.glTranslatef(1.3f, 2.3f, 0f);
    		drawLight(0.3f);
    	}
    	GL11.glPopMatrix();
    	
    	GL11.glPushMatrix();
    	{
    		GL11.glTranslatef(-1.3f, 2.3f, 0f);
    		drawLight(0.3f);
    	}
    	GL11.glPopMatrix();
    	
    	GL11.glPushMatrix();
    	{
    		GL11.glTranslatef(-1.1f, 4.6f, 0f);
    		drawLight(0.3f);
    	}
    	GL11.glPopMatrix();
    	
    	GL11.glPushMatrix();
    	{
    		GL11.glTranslatef(1.1f, 4.6f, 0f);
    		drawLight(0.3f);
    	}
    	GL11.glPopMatrix();
    	
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_EMISSION, FloatBuffer.wrap(noEmission));
    	
    	// set materials and draw main station body
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, FloatBuffer.wrap(stationAmbient));
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, FloatBuffer.wrap(stationDiffuse));
    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, FloatBuffer.wrap(stationSpecular));
    	GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, stationShininess);
    	
    	GL11.glPushMatrix();
    	{
	    	GL11.glRotatef(90f, 1f, 0f, 0f);
	    	GL11.glScalef(1.5f, 2f, 2f);
	    	GL11.glCallList(hullList);
	    	
    	}
    	GL11.glPopMatrix();
    	
    	GL11.glPushMatrix();
    	{
	    	GL11.glTranslatef(0f, -2f, 0f);
	    	GL11.glRotatef(-180f, 1f, 0f, 0f);
	    	GL11.glScalef(1.5f, 0.5f, 2f);
	    	GL11.glCallList(endPieceList);
    	}
    	GL11.glPopMatrix();
    	
    	GL11.glPushMatrix();
    	{
	    	GL11.glTranslatef(0f, 2f, 0f);
	    	GL11.glRotatef(0f, 1f, 0f, 0f);
	    	GL11.glScalef(1.5f, 0.5f, 2f);
	    	GL11.glCallList(endPieceList);
	    	
	    	GL11.glPushMatrix();
	    	{
	    		GL11.glTranslatef(0f, 6f, 0f);
		    	GL11.glRotatef(90f, 1f, 0f, 0f);
		    	GL11.glScalef(0.8f, 0.8f, 4f);
		    	GL11.glCallList(hullList);
		    	
		    	// draw the arboretum on the top of the station
		    	GL11.glPushMatrix();
		    	{
		    		GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT, FloatBuffer.wrap(ringsAmbient));
			    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_DIFFUSE, FloatBuffer.wrap(ringsDiffuse));
			    	GL11.glMaterial(GL11.GL_FRONT, GL11.GL_SPECULAR, FloatBuffer.wrap(ringsSpecular));
			    	GL11.glMaterialf(GL11.GL_FRONT, GL11.GL_SHININESS, ringsShininess);
		    		
		    		GL11.glScalef(1f, 0.66f, 0.71f);
		    		GL11.glTranslatef(0f, 0.5f, -1.3f);
		    		GL11.glRotatef(90, 1, 0, 0);
		    		
		    		GL11.glCallList(arboretumList);
		    	}
		    	GL11.glPopMatrix();
	    	}
		    GL11.glPopMatrix();
    	}
    	GL11.glPopMatrix();
    }
    
    /**
     *  Draw a 3x2 pill shape representing the arboretum
     */
    protected void drawArboretum() {
	    Cylinder component = new Cylinder();
	    
	    // auto generate texture normals
	    component.setTextureFlag(true);
	    component.setNormals(GL11.GL_SMOOTH);         
	
	    // draw center cylinder
	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	    {
	    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);               	 
	    	
	    	GL11.glBindTexture(GL11.GL_TEXTURE_2D,arboretumTexture.getTextureID());
	    	component.draw(1f, 1f, 1f, 30, 30);
	    }
    	GL11.glDisable(GL11.GL_TEXTURE_2D);
    	
    	// draw left sphere
    	new Sphere().draw(1.0f, 30, 30);
    	
    	// draw right sphere and move into place
    	GL11.glPushMatrix();
    	{
    		GL11.glTranslatef(0f, 0f, 1f);
    		new Sphere().draw(1.0f, 30, 30);
    	}
    	GL11.glPopMatrix();
    }
    
    /**
     * Draw a 3D rotating ring made up of quadrics
     * @param outer radius of ring
     * @param inner radius of hole in ring
     * @param orientation current orientation of ring in 3D space
     * @return new orientation of ring in 3D space
     */
    protected float drawRotatingRing(float outer, float inner, float orientation) {
    	// set current orientation
    	GL11.glRotatef(((orientation += (1) * getAnimationScale())), 0f, 0f, 1f);
    	
    	GL11.glPushMatrix();
    	{
    		// change face culling so INSIDE of cylinder and TOP of disk are drawn
	    	GL11.glCullFace(GL11.GL_FRONT);
	    	
	    	// draw top disk
	    	new Disk().draw(inner, outer, 30, 10);
	    	
	    	// draw inner cylinder
	    	Cylinder innerCylinder = new Cylinder();
	    	innerCylinder.draw(inner, inner, 1, 30, 30);
	    	
	    	GL11.glCullFace(GL11.GL_BACK);
	    	
	    	// draw outer cylinder
	    	new Cylinder().draw(outer, outer, 1, 30, 30);
	    	
	    	// draw bottom disk
	    	GL11.glPushMatrix();
	    	{
		    	Disk bottomDisk = new Disk();
		    	
		    	GL11.glTranslatef(0, -1, 0);
		    	bottomDisk.draw(inner, outer, 30, 10);
	    	}
	    	GL11.glPopMatrix();
    	}
	    GL11.glPopMatrix();
    	
	    return orientation;
    }
    
    /**
     * Draw a sphere quadric representing station exterior lights
     * @param radius of sphere
     */
    protected void drawLight(float radius) {
    	new Sphere().draw(radius, 30, 30);
    }
    
    
    /**
     *  Change the lightsOn boolean variable, to toggle lights.
     */
    protected void toggleLights() {
    	if (lightsOn) {
    		lightsOn = false;
    	}
    	else {
    		lightsOn = true;
    	}
    }
    
    /**
     * Draw an inverted textured sphere, making up the skybox
     */
    protected void drawSkybox() {
    	Sphere skybox = new Sphere();
    	
    	// auto generate normals
	    skybox.setTextureFlag(true);
	    skybox.setNormals(GL11.GL_SMOOTH); 
	    // texture on inside of sphere
	    skybox.setOrientation(GLU.GLU_INSIDE);
	
	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	    {
	    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
	    	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);         
	    	
	    	GL11.glBindTexture(GL11.GL_TEXTURE_2D,spaceTexture.getTextureID());
	    	skybox.draw(1f, 30, 30);
	    }
    	GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
}


