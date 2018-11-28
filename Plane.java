package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
    
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }

        
    @Override
    public void intersect( Ray ray, IntersectResult result ) {

    	// TODO: Objective 4: intersection of ray with plane
    	//ray=e+dirc*t;
    	//t=(Q-A)*n/direc*n, where Q is a point on the plane (choose 0,0,0 since y=0)

    	//v=Q-A
    	Vector3d v=new Vector3d();
    	v.sub(new Point3d(0, 0, 0), ray.eyePoint);
    	double t=v.dot(n)/ray.viewDirection.dot(n);

    	if(t<=0){
    		//does not intersect
    	}else{

    		result.n=this.n;
    		ray.getPoint(t, result.p);
    		if(material2==null){
    			result.material=this.material;
    		}else{
    			int x=(int) Math.floor(result.p.x);
    			int z=(int)Math.floor(result.p.z);

    			//when z mod 2=0, x%2=0->m1, x%2=1->m2
    			//when z%2=1: x%2=0->m2, x%2=1->m1
    			if(z%2==0&&x%2==0||Math.abs(z%2)==1&&Math.abs(x%2)==1){
    				result.material=this.material;
    			}else{
    				result.material=this.material2;

    			}
    			
    			result.t=result.p.distance(ray.eyePoint);
    		}
    	}

    }
    
}
