package custom;

import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.sun.org.apache.xpath.internal.axes.AxesWalker;
import org.lwjgl.Sys;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class FlyByCamera extends com.jme3.input.FlyByCamera
{
     /**
      * Calls constructor of FlyByCamera.
      *
      * @param cam
      */
     public FlyByCamera( Camera cam )
     {
          super(cam);
     }


     @Override
     protected void rotateCamera( float value, Vector3f axis )
     {
          if ( dragToRotate )
          {
               if ( canRotate )
               {
                    // value = -value;
               }
               else
               {
                    return;
               }
          }
          
          
          Matrix3f mat = new Matrix3f();
          mat.fromAngleNormalAxis(rotationSpeed * value, axis);

          //System.out.println(axis);
          
          Vector3f up = cam.getUp();
          Vector3f left = cam.getLeft();
          Vector3f dir = cam.getDirection();
          
          mat.mult(up, up);
          mat.mult(left, left);
          mat.mult(dir, dir);


          Quaternion q = new Quaternion();
          q.fromAxes(left, up, dir);
          q.normalizeLocal();

          // Gets the euler angles from the quaternion rotation (x, y , z)
          float[] eulerAngles = new float[3];
          q.toAngles(eulerAngles);

          // Sets the camera axis of view only if the x direction rotation is within the angle (in radians)
          if(eulerAngles[0] < rAngle && eulerAngles[0] > -rAngle)
          {
              cam.setAxes(q);
          }
     }

     private float rAngle = 1.5708f;

}
