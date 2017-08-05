import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Driver extends SimpleApplication implements ActionListener
{
    private Spatial sceneModel;
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    //Temporary vectors used on each frame.
    //They here to avoid instanciating new vectors on each frame.
    private Vector3f camDir = new Vector3f();
    private Vector3f camLeft = new Vector3f();

    private float walkSpeed;
    private float runSpeed;

    public static void main(String[] args)
    {
        /*This is where the program starts and calls the start() method
          which runs the simpleInitApp() method.
        */
        Driver driver = new Driver();
        driver.start();
    }

    public void simpleInitApp()
    {
        /** The BulletAppState gives our application access to physics features,
         *  such as collision detection. It is integerated by the jME's jBullet integeration
         *  which is an external physics engine. This piece of code is required in every
         *  application that works with physics.*/
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);


        /** The debug feature shows us the wireframes of the mesh renderer.
         *  This should be enabled for debugging only.*/
        //bulletAppState.setDebugEnabled(true);

        /* These are the default run and walk speed in (w/s) which is
        *  world unit/second. We also set the move speed according to
        *  what mode are we on.*/
        runSpeed = 20f;
        walkSpeed = 0f;
        flyCam.setMoveSpeed(100f);

        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        setUpKeys();
        setUpLight();

        // We load the scene from the zip file and adjust its size.
        assetManager.registerLocator("town.zip", ZipLocator.class);
        sceneModel = assetManager.loadModel("main.scene");
        sceneModel.setLocalScale(2f);

        // We set up collision detection for the scene by creating a
        // compound collision shape and a static RigidBodyControl with mass zero.
        CollisionShape sceneShape =
                CollisionShapeFactory.createMeshShape(sceneModel);
        landscape = new RigidBodyControl(sceneShape, 0);
        sceneModel.addControl(landscape);

        // We set up collision detection for the player by creating
        // a capsule collision shape and a CharacterControl.
        // The CharacterControl offers extra settings for
        // size, stepheight, jumping, falling, and gravity.
        // We also put the player in its starting position.

        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));


        /*We attach the scene and the player to the rootnode and the physics space,
        * to make them appear in the game world. Note that you have to add all kinds of physics
        * control to the physics space for them to have physics.*/
        rootNode.attachChild(sceneModel);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(player);
        bulletAppState.setDebugEnabled(true);
    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }

    /** We over-write some navigational key mappings here, so we can
     * add physics-controlled walking and jumping: */
    private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Left", "Right", "Up", "Down", "Jump");
    }

    /** These are our custom actions triggered by key presses.
     * We do not walk yet, we just keep track of the direction the user pressed. */
    public void onAction(String binding, boolean isPressed, float tpf) {
        if (binding.equals("Left")) {
            left = isPressed;
        } else if (binding.equals("Right")) {
            right= isPressed;
        } else if (binding.equals("Up")) {
            up = isPressed;
        } else if (binding.equals("Down")) {
            down = isPressed;
        } else if (binding.equals("Jump")) {
            // Jump functionality not determined yet
            //if (isPressed) { player.jump(); }
        }
    }

    /**
     * This is the main event loop--walking happens here.
     * We check in which direction the player is walking by interpreting
     * the camera direction forward (camDir) and to the side (camLeft).
     * The setWalkDirection() command is what lets a physics-controlled player walk.
     * We also make sure here that the camera moves with player.
     */

    @Override
    public void simpleUpdate(float tpf) {
        /* The up and down movement has been allowed only in the x and z direction because
        * changing all direction would enable to player to jump awkwardly.*/
        camDir.setX(cam.getDirection().getX() * 0.4f);
        camDir.setZ(cam.getDirection().getZ() * 0.4f);
        camDir.setY(0f);

        camLeft.set(cam.getLeft()).multLocal(0.4f);

        /* The multiple if statements helps us determine which position the character wants to walk
         * in. The walk direction is calculated based on the boolean values determined by the onAction method.*/
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }

        // Sets the player walk direction and resets the cam location to where the player is now.
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
    }

}
